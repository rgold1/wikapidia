package org.wikapidia.core.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageInfo;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a canonically capitalized Wikipedia title.
 * Contains utility methods for querying aspects of the title.
 */
public class Title implements Externalizable {

    private String canonicalTitle;
    private LanguageInfo language;

	private static final long serialVersionUID = 3L;
	private static final Logger LOG = Logger.getLogger(Title.class);

    public Title(String text, LanguageInfo language) {
        this(text, false, language);
    }

    public Title(String text, boolean isCanonical, LanguageInfo lang) {
        this.canonicalTitle = isCanonical ? text : canonicalize(text, lang);
        this.language = lang;
    }

    public String getCanonicalTitle() {
        return canonicalTitle;
    }

    public LanguageInfo getLanguageInfo() {
        return language;
    }
    public Language getLanguage() {
        return language.getLanguage();
    }

    /**
	 * Gets the "Category:" or equivalent
	 * @return
	 */
	public String getNamespaceString(){
		return getNamespaceString(this.canonicalTitle);
	}
	
	private static String getNamespaceString(String text){
		String[] parts = text.split(":");
		if (parts.length > 1){
			return parts[0];
		}else{
			return null;
		}
	}
	
	/**
	 * [0] is the namespace string (if it exists)
	 * [1] is the non-namespace string
	 * @return
	 */
	private static String[] getNamespaceAndNonnamespaceString(String text){
		String[] rVal = new String[2];
		rVal[0] = null;
		rVal[1] = null;
		String[] parts = text.split(":");

		if (parts.length == 1) {
			rVal[1] = parts[0];
			return rVal;
		} else {
			rVal[0] = parts[0].trim();
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < parts.length; i++){
				sb.append(parts[i]);
			}
			rVal[1] = StringUtils.join(Arrays.copyOfRange(parts, 1, parts.length), ":").trim();
			return rVal;
		}
	}
	
	/**
	 * Gets the part of the title after the first colon. If there is no
	 * colon, returns the whole title.
	 * @return
	 */
	public String getNonnamespaceString(){
		return getNamespaceAndNonnamespaceString(canonicalTitle)[1];
	}
	
	
	@Override
	public String toString(){
		return canonicalTitle;
	}
	
	@Override
	public int hashCode(){
		return canonicalTitle.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof Title){
			return o.toString().equals(this.toString());
		}else{
			return false;
		}
	}
	
	public String toRawDataTableVersion(){
		return canonicalTitle.replaceAll(" ", "_");
	}
	
	/**
	 * Needs langId because Title does not store it for memory reasons
	 * @return
	 */
	public Title toUpperCase(){
		String upTitle = this.toString().toUpperCase();
		return new Title(upTitle, true, language);
	}
	
	public String toUrlEncodedVersion() throws WikapidiaException{
		try {
			return URLEncoder.encode(toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new WikapidiaException(e);
		}
	}

    /**
     * Returns the PageType associated with the page.
     * A return value of ARTICLE may actually be a redirect or disambiguation
     * because those depend on the wikitext of the article - not just the title.
     * TODO: figure out how to look for special pages...
     * @return
     */
    public PageType guessType() {
        // test for category
        Matcher m = language.getCategoryPattern().matcher(canonicalTitle);
        if (m.find())
            return PageType.CATEGORY;

        return PageType.ARTICLE;
    }

    /**
     * Deconstructs a title such as "Mash_(film)" into "Mash" and "film"
     * TODO: remove this, and have getNameAndDisambiuator return an array of 2 strings.
     */
	public static class NameAndDisambiguator{
		
		public final String name;
		public final String disambiguator;
		
		public NameAndDisambiguator(String name, String disambiguator) {
			super();
			this.name = name;
			this.disambiguator = disambiguator;
		}
		
		public boolean hasDisambiguator(){
			return (this.disambiguator != null);
		}
		
		public String toString(){
			
			String rVal = this.name;
			if (this.hasDisambiguator()){
				rVal = rVal + " " + this.disambiguator;
			}
			return rVal;
		}
	}


    /**
     * Deconstructs a title such as "Mash_(film)" into "Mash" and "film"
     * TODO: remove this, and have getNameAndDisambiuator return an array of 2 strings.
     */
	private static Pattern nameAndDisambiguatorPattern = Pattern.compile("(.+?)\\s*\\((.+?)\\)");
	public NameAndDisambiguator getNameAndDisambiguator(){
		String s = this.toString();
		Matcher m = nameAndDisambiguatorPattern.matcher(s);
		String disam = null;
		String name = this.toString();
		if (m.find()){
			name = m.group(1);
			disam = m.group(2);
		}
		return new NameAndDisambiguator(name, disam);

	}

    private static final Pattern COLON_PATTERN = Pattern.compile("\\s+:\\s+");

    /**
     * Converts a title into its canonical Wikipedia representation.
     * This may be imperfect, but it's a good guess.
     * @param title
     * @param lang
     * @return
     */
    public static String canonicalize(String title, LanguageInfo lang) {

        // spaces and underscores are equivalent
        title = title.replaceAll("_", " ");

        // remove the fragment
        int i = title.indexOf("#");
        if (i >= 0) {
            title = title.substring(0, i);
        }

        //removes leading and trailing spaces
        title = title.trim();


        // This code enforces the "first letter always caps, everything else
        // is case-sensitive policy" of Wikipedia. I modified this code from JWPL 0.9.1
        // just to be safe, but I think it's identical to my old code.
        title = StringUtils.capitalize(title);

        // handle whitespace around colons (only a problem for categories in this context)
        Matcher m = COLON_PATTERN.matcher(title);
        if (m.find()){
            title = m.replaceFirst(":");
        }

        // normalize all the category aliases
        m = lang.getCategoryReplacePattern().matcher(title);
        if (m.find() && m.group(1).equals(lang.getDefaultCategoryNamespaceName())) {
            title =  lang.getDefaultCategoryNamespaceName() + ":" + m.group(2);
        }

        // ensure reasonable capitalization with namespaces
        if (getNamespaceString(title) != null){
            String[] splitText = getNamespaceAndNonnamespaceString(title);
            String nonNsString = StringUtils.capitalize(splitText[1]); // make sure that titles following colons conform to first-letter-caps policy
            title = splitText[0] + ":" + nonNsString;
        }

        // this is a weird f-ing bug that needed to be fixed!
        title = title.replaceAll("\u200E", "");

        return title;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.canonicalTitle = (String)in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(canonicalTitle);
    }
	


}
