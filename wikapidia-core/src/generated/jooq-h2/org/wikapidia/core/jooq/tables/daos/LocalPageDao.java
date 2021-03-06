/**
 * This class is generated by jOOQ
 */
package org.wikapidia.core.jooq.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class LocalPageDao extends org.jooq.impl.DAOImpl<org.wikapidia.core.jooq.tables.records.LocalPageRecord, org.wikapidia.core.jooq.tables.pojos.LocalPage, java.lang.Long> {

	/**
	 * Create a new LocalPageDao without any configuration
	 */
	public LocalPageDao() {
		super(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE, org.wikapidia.core.jooq.tables.pojos.LocalPage.class);
	}

	/**
	 * Create a new LocalPageDao with an attached configuration
	 */
	public LocalPageDao(org.jooq.Configuration configuration) {
		super(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE, org.wikapidia.core.jooq.tables.pojos.LocalPage.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Long getId(org.wikapidia.core.jooq.tables.pojos.LocalPage object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>ID IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchById(java.lang.Long... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>ID = value</code>
	 */
	public org.wikapidia.core.jooq.tables.pojos.LocalPage fetchOneById(java.lang.Long value) {
		return fetchOne(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.ID, value);
	}

	/**
	 * Fetch records that have <code>LANG_ID IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchByLangId(java.lang.Short... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.LANG_ID, values);
	}

	/**
	 * Fetch records that have <code>PAGE_ID IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchByPageId(java.lang.Integer... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.PAGE_ID, values);
	}

	/**
	 * Fetch records that have <code>TITLE IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchByTitle(java.lang.String... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.TITLE, values);
	}

	/**
	 * Fetch records that have <code>NS IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchByNs(java.lang.Short... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.NS, values);
	}

	/**
	 * Fetch records that have <code>PAGE_TYPE IN (values)</code>
	 */
	public java.util.List<org.wikapidia.core.jooq.tables.pojos.LocalPage> fetchByPageType(java.lang.Short... values) {
		return fetch(org.wikapidia.core.jooq.tables.LocalPage.LOCAL_PAGE.PAGE_TYPE, values);
	}
}
