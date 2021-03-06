/**
 * This class is generated by jOOQ
 */
package org.wikapidia.core.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class LocalPage extends org.jooq.impl.TableImpl<org.wikapidia.core.jooq.tables.records.LocalPageRecord> {

	private static final long serialVersionUID = 2014919617;

	/**
	 * The singleton instance of <code>PUBLIC.LOCAL_PAGE</code>
	 */
	public static final org.wikapidia.core.jooq.tables.LocalPage LOCAL_PAGE = new org.wikapidia.core.jooq.tables.LocalPage();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.wikapidia.core.jooq.tables.records.LocalPageRecord> getRecordType() {
		return org.wikapidia.core.jooq.tables.records.LocalPageRecord.class;
	}

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.ID</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT, this);

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.LANG_ID</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Short> LANG_ID = createField("LANG_ID", org.jooq.impl.SQLDataType.SMALLINT, this);

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.PAGE_ID</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Integer> PAGE_ID = createField("PAGE_ID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.TITLE</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.String> TITLE = createField("TITLE", org.jooq.impl.SQLDataType.VARCHAR.length(256), this);

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.NS</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Short> NS = createField("NS", org.jooq.impl.SQLDataType.SMALLINT, this);

	/**
	 * The column <code>PUBLIC.LOCAL_PAGE.PAGE_TYPE</code>. 
	 */
	public final org.jooq.TableField<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Short> PAGE_TYPE = createField("PAGE_TYPE", org.jooq.impl.SQLDataType.SMALLINT, this);

	/**
	 * Create a <code>PUBLIC.LOCAL_PAGE</code> table reference
	 */
	public LocalPage() {
		super("LOCAL_PAGE", org.wikapidia.core.jooq.Public.PUBLIC);
	}

	/**
	 * Create an aliased <code>PUBLIC.LOCAL_PAGE</code> table reference
	 */
	public LocalPage(java.lang.String alias) {
		super(alias, org.wikapidia.core.jooq.Public.PUBLIC, org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.wikapidia.core.jooq.tables.records.LocalPageRecord, java.lang.Long> getIdentity() {
		return org.wikapidia.core.jooq.Keys.IDENTITY_LOCAL_PAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.wikapidia.core.jooq.tables.records.LocalPageRecord> getPrimaryKey() {
		return org.wikapidia.core.jooq.Keys.CONSTRAINT_6C;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.wikapidia.core.jooq.tables.records.LocalPageRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.wikapidia.core.jooq.tables.records.LocalPageRecord>>asList(org.wikapidia.core.jooq.Keys.CONSTRAINT_6C);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.wikapidia.core.jooq.tables.LocalPage as(java.lang.String alias) {
		return new org.wikapidia.core.jooq.tables.LocalPage(alias);
	}
}
