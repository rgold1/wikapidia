package org.wikapidia.parser.wiki;

import de.tudarmstadt.ukp.wikipedia.parser.*;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageInfo;
import org.wikapidia.core.model.PageType;
import org.wikapidia.core.model.Title;
import org.wikapidia.parser.xml.PageXml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTextParser {
    public static final Logger LOG = Logger.getLogger(WikiTextParser.class.getName());

    private final MediaWikiParser jwpl;
    private final SubarticleParser subarticleParser;
    private final LanguageInfo lang;
    private final List<ParserVisitor> visitors;

    public WikiTextParser(LanguageInfo lang, List<ParserVisitor> visitors) {
        this(lang, null, visitors);
    }

    public WikiTextParser(LanguageInfo lang, List<String> allowedIllLangs, List<ParserVisitor> visitors) {
        this.lang = lang;
        subarticleParser = new SubarticleParser(lang);
        this.visitors = visitors;

        MediaWikiParserFactory pf = new MediaWikiParserFactory();
        pf.setCalculateSrcSpans(true);
        pf.setCategoryIdentifers(lang.getCategoryNames());
        if (allowedIllLangs != null) {
            pf.setLanguageIdentifers(allowedIllLangs);
        }
        jwpl = pf.createParser();
    }

    /**
     * TODO: change exception to WpParseException
     * @param xml
     * @throws WikapidiaException
     */
    public void parse(PageXml xml) throws WikapidiaException {
        visitBeginPage(xml);
        ParsedPage pp = jwpl.parse(xml.getBody());
        if (pp == null) {
            LOG.warning("invalid page: " + xml.getBody());
        }

        if (xml.getType() == PageType.REDIRECT) {
            ParsedRedirect pr = new ParsedRedirect();
            pr.location = new ParsedLocation(xml, -1, -1, -1);
            // TODO: calculate redirect text?
            visitRedirect(pr);
        } else if (xml.getType() == PageType.CATEGORY) {
            parseCategory(xml, pp);
        } else if (xml.getType() == PageType.ARTICLE) {
            parseArticle(xml, pp);
        }
        visitEndPage(xml);
    }

    private void parseRedirect(PageXml xml, ParsedPage page) {
    }

    private void parseArticle(PageXml xml, ParsedPage pp) {   		// *** LINKS, ANCHOR TEXTS, SECTIONS
        int secNum = 0;

        // paragraph numbers before first paragraph are negative
        // paragraph 0 is the first paragraph
        int paraNum = -pp.getFirstParagraphNr();

        for (Section curSection: pp.getSections()){
            try{
                ParsedLink.SubarticleType secSubType = subarticleParser.isSeeAlsoHeader(lang, curSection.getTitle());
                for (Content curContent : curSection.getContentList()){
                    // EASY LINKS
                    for (Link curLink : curContent.getLinks()){
                        if (curLink.getTarget().length() == 0){
                            LOG.warning("Found link with empty target: \t" + xml + "\t text=" + curLink.getText());
                            continue;
                        }
                        Title destTitle = link2Title(curLink);
                        if (destTitle == null || destTitle.guessType() != PageType.ARTICLE){
                            continue;
                        }
                        try{
                            ParsedLink.SubarticleType linkSubType;
                            if (secSubType == null){ // don't look for inlines in "see also"
                                linkSubType = subarticleParser.isInlineSubarticle(curLink.getSrcSpan().getStart(), xml);
                            }else{
                                linkSubType = secSubType; // captures see also
                            }
                            ParsedLocation location = new ParsedLocation(xml, secNum, paraNum, curLink.getSrcSpan().getStart());
                            visitLink(location, destTitle, curLink.getText(), linkSubType);
                        } catch (WikapidiaException e) {
                            LOG.log(Level.WARNING, String.format("Could not process link\t%s\t%s", xml, curLink.toString()),e);
                        }
                    }

                    //TEMPLATES
                    for (Template t : curContent.getTemplates()){
                        boolean errorWithSrcLocation = t.getSrcSpan().getEnd() < 0; // this checks for what seems to be when parsing fails in JWPL
                        String templateTextOrig;
                        if (!errorWithSrcLocation){
                            templateTextOrig = xml.getBody().substring(t.getSrcSpan().getStart(), t.getSrcSpan().getEnd());
                        }else{ // this makes up for errors in JWPL (or bad script, but it mostly looks like erros)
                            int estimatedLength = t.getPos().getEnd() - t.getPos().getStart();
                            templateTextOrig = xml.getBody().substring(t.getSrcSpan().getStart(), t.getSrcSpan().getStart() + estimatedLength + 1);
                        }
                        String templateText;
                        if (templateTextOrig.length() >= 5){
                            templateText = templateTextOrig.substring(2, templateTextOrig.length()-2);
                        }else{
                            continue; // blank template
                        }
                        String templateName = t.getName(); // SUBARTICLE INFO STUFF
                        templateName = new Title(templateName, false, lang).toString(); // this appears to be necessary due to JWPL's handling of template names
                        ParsedLink.SubarticleType tempSubType;
                        tempSubType = subarticleParser.isTemplateSubarticle(templateName, templateText);
                        if (tempSubType == null){
                            try{
                                templateText = templateText.replaceAll("\\{\\{", ""); // <-- these are all special cases in which JWPL fails
                                templateText = templateText.replaceAll("\\}\\}", "");
                                templateText = templateText.replaceAll("<!--", "");
                                templateText = templateText.replaceAll("\\[\\[\\]\\]", "");
                                ParsedPage parsedTemplate = jwpl.parse(templateText);
                                for (Link templateLink : parsedTemplate.getLinks()){
                                    Title destTitle = link2Title(templateLink);
                                    if (destTitle == null) { continue; }
                                    PageType type = destTitle.guessType();
                                    if (type == PageType.ARTICLE){
                                        ParsedLocation location = new ParsedLocation(xml, secNum, paraNum, t.getSrcSpan().getStart());
                                        visitLink(location, destTitle, templateLink.getText(), tempSubType);
                                    } else if (type == PageType.CATEGORY){
                                        throw new RuntimeException("Found a category link in a template");
                                    }
                                }
                            }catch(IndexOutOfBoundsException e){
                                LOG.severe("Parsing error while doing templates -> ParsedPages:\t" + xml + "\t" + templateText);
                            }

                        }else{
                            List<String> dests = subarticleParser.getContentsOfTemplatePipe(templateText);
                            for (String dest : dests){
                                dest = subarticleParser.removeTemplateAnchor(dest);
                                Title destTitle = new Title(dest, lang);
                                try {
                                    ParsedLocation location = new ParsedLocation(xml, secNum, paraNum, t.getSrcSpan().getStart());
                                    visitLink(location, destTitle, dest, tempSubType);
                                } catch (WikapidiaException e) {
                                    LOG.log(Level.SEVERE,
                                            String.format("Could not process template-based subarticle link: \t%s\t%s", xml, t.toString()), e);
                                }
                            }
                        }

                    }
                    if (curContent instanceof Paragraph) {
                        paraNum++;
                    }
                }
            } catch(WikapidiaException e){
                LOG.log(Level.SEVERE, String.format("Could not store whole section in %s", xml), e);
            }
            secNum++;
        }


        // *** ILLS
        parseIlls(xml, pp);

        // *** CATEGORY MEMBERSHIPS
        for (Link cat : pp.getCategories()){
            String linkText = cat.getText();
            if (linkText.contains(Pattern.quote("|"))){
                continue;
            }
            Title destTitle = new Title(cat.getTarget(), false, lang);
            // TODO: ensure destTitle is a category
            ParsedCategory pc = new ParsedCategory();
            pc.location = new ParsedLocation(xml, -1, -1, cat.getSrcSpan().getStart());
            pc.category = destTitle;
            visitCategory(pc);
        }
    }

    private static Pattern illPattern = Pattern.compile("(.+?)\\:\\s*(.+)");
    private void parseIlls(PageXml xml, ParsedPage pp) {
        if (pp.getLanguagesElement() !=  null){
            for (Link ill : pp.getLanguages()){
                try{
                    Matcher m = illPattern.matcher(ill.getTarget());
                    if (m.find()){
                        String langCode = m.group(1);
                        String target = m.group(2);
                        Language l = Language.getByLangCode(langCode);
                        if (l == null) {
                            LOG.warning("unkonwn lang code:\t" + langCode);
                        } else if (l != lang.getLanguage()) {
                            ParsedIll pill = new ParsedIll();
                            pill.location = new ParsedLocation(xml, -1, -1, ill.getSrcSpan().getStart());;
                            pill.title = new Title(target, false, lang);
                            visitIll(pill);
                        }
                    }else{
                        LOG.warning("Invalid ILL:\t" + xml + "\t" + ill.getTarget());
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, String.format("Error while parsing/storing ILL\t%s\t%s\t%s",xml,ill.toString().replaceAll("\n", ","), e.getMessage()));
                }
            }
        }
//        else{
//            LOG.info("No ILLs found for\t" + xml);
//        }

    }

    private void parseCategory(PageXml xml, ParsedPage pp){
        // handle links
        for (Link catMem : pp.getCategories()){
            try {
                Title destTitle = new Title(catMem.getTarget(), lang);
                // TODO: ensure title is a category
                ParsedLocation location = new ParsedLocation(xml, -1, -1, catMem.getSrcSpan().getStart());
                visitLink(location, destTitle, catMem.getText(), null);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, String.format("Could not parse/store link\t%s\t%s", xml, catMem.toString()), e);
            }
        }

        // handle ILLs
        parseIlls(xml, pp);
    }


    private void visitBeginPage(PageXml xml) {
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.beginPage(xml);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }
    private void visitEndPage(PageXml xml) {
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.endPage(xml);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }
    private void visitRedirect(ParsedRedirect redirect) {
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.redirect(redirect);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }
    private void visitIll(ParsedIll ill) {
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.ill(ill);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }
    private void visitCategory(ParsedCategory cat) {
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.category(cat);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }
    private void visitLink(ParsedLocation location, Title dest, String linkText, ParsedLink.SubarticleType subType) throws WikapidiaException{

        // don't want to consider within-page links
        Title src = location.getXml().getTitle();
        if (src.toString().startsWith("#") || src.equals(dest)) {
            return;
        }
        ParsedLink pl = new ParsedLink();
        pl.location = location;
        pl.target = dest;
        pl.text = linkText;
        pl.subarticleType = subType;
        for (ParserVisitor visitor : visitors) {
            try {
                visitor.link(pl);
            } catch (WikapidiaException e) {
                LOG.log(Level.WARNING, "beginPage failed:", e);
            }
        }
    }

    private PageType getLinkType(Link link){
        Title t = link2Title(link);
        return t == null ? null : t.guessType();
    }

    private Title link2Title(Link link) {
        if (link.getType().equals(Link.type.INTERNAL) || link.getType().equals(Link.type.UNKNOWN)) {
            return new Title(link.getTarget(), lang);
        } else {
            return null;
        }
    }

    static public List<String> getLangCodes(List<LanguageInfo> langs) {
        List<String> langCodes = new ArrayList<String>();
        for (LanguageInfo l : langs) {
            langCodes.add(l.getLanguage().getLangCode());
        }
        return langCodes;
    }
}
