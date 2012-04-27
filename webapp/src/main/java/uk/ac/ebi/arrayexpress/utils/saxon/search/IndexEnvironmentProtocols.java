/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

/**
 * @author rpslpereira
 * 
 */

public class IndexEnvironmentProtocols extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String driverXml;
	private String connectionString;
	private Database db;
	Collection coll;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentProtocols(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff
	@Override
	public void setup() {

		defaultSortField = "accession";
		defaultSortDescending = false;
		defaultPageSize = 25;

		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("ae.xmldatabase");
		System.out.println(connsConf.isEmpty());
		// System.out.println(connsConf.);
		if (null != connsConf) {
			driverXml = connsConf.getString("driver");
			connectionString = connsConf.getString("connectionstring");
		} else {
			logger.error("ae.xmldatabase Configuration is missing!!");
		}

		Class<?> c;
		try {
			c = Class.forName(driverXml);

			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			coll = DatabaseManager.getCollection(connectionString);

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		}
		// Receive the database
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.arrayexpress.utils.saxon.search.AbstractIndexEnvironment#queryDB
	 * (org.apache.lucene.search.TopDocs,
	 * org.apache.lucene.search.IndexSearcher, int, int)
	 */
	@Override
	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		String ret = "";
		StringBuilder totalRes = new StringBuilder();
		// just to replace the OR by or (xquery only recognizes or)
		String userIds = map.get("userid")[0].toLowerCase();
		String userIdRestriction = "";
		if (!userIds.equalsIgnoreCase("")) {
			userIdRestriction = " and user/@id=(" + userIds + ")";
		}
		if (logger.isDebugEnabled()) {
			logger.debug("userid->" + map.get("userid")[0]);
		}
		try {

			// Receive the XPath query service
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			totalRes.append("(");
			for (int i = initialExp; i < finalExp; i++) {
				Document doc = isearcher.doc(hits[i].doc);
				totalRes.append("'" + doc.get("id") + "'");
				if (i != (finalExp - 1)) {
					totalRes.append(",");
				}
			}
			totalRes.append(")");
			if (logger.isDebugEnabled()) {
				logger.debug("QueryString->" + totalRes);
			}
			long time = System.nanoTime();

			ResourceSet set = null;
			// I need to put much more detail
			if (!map.containsKey("id")) {
				set = service.query("<protocols>{for $x in "
						+ totalRes.toString()
						+ " let $z:= //protocols/protocol[id=$x "
						+ userIdRestriction + " ] "
						+ " return <all>{$z}</all>}</protocols>");

			} else {
				set = service.query("<protocols>{for $x in "
						+ totalRes.toString()
						+ " let $z:= //protocols/protocol[id=$x"
						+ userIdRestriction + " ] "
						+ " let $y:= //experiments/experiment[protocol/id=$x"
						+ userIdRestriction
						+ "  and source/@visible!='false'] "
						// +
						// " let $y:= //experiments/experiment[protocol/accession=$ac and user/@id=1 and source/@visible='true'][position()<=11] "
						+ " return <all>{$z} {$y}</all>}</protocols>");
			}

			ResourceIterator iter = set.getIterator();
			double ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("Xml db query took: " + ms + " ms");
			}

			time = System.nanoTime();
			// Loop through all result items
			while (iter.hasMoreResources()) {

				ret += iter.nextResource().getContent();
			}
			ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieve data from Xml db took: " + ms + " ms");
			}
		} catch (final XMLDBException ex) {
			// Handle exceptions
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {

		}
		return ret;
	}

}
