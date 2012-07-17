/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.util.ArrayList;
import java.util.List;

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
public class IndexEnvironmentExperiments extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String driverXml;
	private String connectionString;
	private Database db;
	private Collection coll;

	// private long numberOfExperiments;
	//
	// private long numberOfAssays;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentExperiments(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff nad to calculate the
	// number of assys ans the number os experiments (matter of performance)
	@Override
	public void setup() {

		defaultSortField = "releasedate";
		defaultSortDescending = false;
		defaultPageSize = 50;

		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");

		if (null != connsConf) {
			driverXml = connsConf.getString("driver");
//			connectionString = connsConf.getString("connectionstring");
			connectionString = connsConf.getString("base") + "://" + connsConf.getString("host") + ":" + connsConf.getString("port") + "/" + connsConf.getString("dbname");
		} else {
			logger.error("bs.xmldatabase Configuration is missing!!");
		}

		Class<?> c;
		try {
			c = Class.forName(driverXml);

			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			coll = DatabaseManager.getCollection(connectionString);
			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			// setNumberOfExperiments(calculateNumberOfExperiments());
			// setNumberOfAssays(calculateNumberOfAssays());
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

	
	
	
	@Override 
	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
				int initialExp, int finalExp, HttpServletRequestParameterMap map)
				throws Exception{
			// Collection instance
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
			// Collection coll=null;
			try {

				// coll = DatabaseManager.getCollection(connectionString);
				XPathQueryService service = (XPathQueryService) coll.getService(
						"XPathQueryService", "1.0");
				totalRes.append("(");
				for (int i = initialExp; i < finalExp; i++) {

				      int docId = hits[i].doc;
				      Document doc = isearcher.doc(docId);
					totalRes.append("'" + doc.get("accession") + "'");
					// totalRes.append("'" + doc.get("id") + "'");
					if (i != (finalExp - 1)) {
						totalRes.append(",");
					}
				}
				totalRes.append(")");
				if (logger.isDebugEnabled()) {
					logger.debug("QueryString->" + totalRes);
				}
				long time = System.nanoTime();
				ResourceSet set = service
						.query("<experiments>{for $x in "
								+ totalRes.toString()
								+ "  let $y:= //folder[@accession=$x] return <all>{//experiment[accession=($x) "
								+ userIdRestriction
								+ " and source/@visible!='false' ]} {$y}  </all>}</experiments>");
				double ms = (System.nanoTime() - time) / 1000000d;
				if (logger.isDebugEnabled()) {
					logger.debug("Xml db query took: " + ms + " ms");
				}

				time = System.nanoTime();
				ResourceIterator iter = set.getIterator();

				// Loop through all result items
				while (iter.hasMoreResources()) {

					ret += iter.nextResource().getContent();
				}
				ms = (System.nanoTime() - time) / 1000000d;
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieve data from Xml db took: " + ms + " ms");
				}

				time = System.nanoTime();
				// in the Experiments I need to calculate the total number of
				// assays.
				CalculateNumberOfAssaysInSearch(hits, isearcher, initialExp,
						finalExp, map);
				// map.put("totalassays", Integer.toString(12121));

				ms = (System.nanoTime() - time) / 1000000d;
				if (logger.isDebugEnabled()) {
					logger.debug("CalculateNumberOfAssaysInSearch took: " + ms
							+ " ms");
				}
			} catch (final XMLDBException ex) {
				// Handle exceptions
				logger.error("Exception:->[{}]", ex.getMessage());
				ex.printStackTrace();
			} finally {
				// if (coll!=null){
				// try {
				// coll.close();
				// } catch (XMLDBException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

			}
			return ret;
		 
		 
	 }

	
	public void CalculateNumberOfAssaysInSearch(ScoreDoc[] hits,
			IndexSearcher isearcher, int initialExp, int finalExp,
			HttpServletRequestParameterMap map) throws Exception {

		long ret = 0;
		long time = System.nanoTime();
		for (int i = 0; i < hits.length; i++) {
			// Document doc = isearcher.doc(hits.scoreDocs[i].doc);

			ret += Long.parseLong(isearcher.doc(hits[i].doc).get(
					"assays"));
		}

		map.put("totalassays", Long.toString(ret));
		// combinedTotal.toArray(new String[combinedTotal.size()]));

	}

	// I need to know the number of experiments and the number of assays
	@Deprecated
	public long calculateNumberOfExperiments() {
		long ret = 0;
		// Collection coll = null;

		try {
			long time = System.nanoTime();
			// coll = DatabaseManager.getCollection(connectionString);
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			ResourceSet set = service
					.query("count(//experiment[ source/@visible='true' and user/@id=1])");
			double ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("calculateNumberOfExperiments took:" + ms + " ms");
			}

			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			if (iter.hasMoreResources()) {

				ret += Long
						.parseLong((String) iter.nextResource().getContent());
			}

		} catch (final XMLDBException ex) {
			// Handle exceptions
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {
			// if (coll!=null){
			// try {
			// coll.close();
			// } catch (XMLDBException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
		}
		return ret;

	}

	@Deprecated
	public long calculateNumberOfAssays() {
		double ret = 0;
		// Collection coll = null;
		try {

			long time = System.nanoTime();
			// coll = DatabaseManager.getCollection(connectionString);

			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			ResourceSet set = service
					.query("sum(//experiment/assays[ ../source/@visible='true' and ../user/@id=1])");
			double ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("calculateNumberOfAssays took:" + ms + " ms");
			}

			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			if (iter.hasMoreResources()) {

				ret += Double.parseDouble((String) iter.nextResource()
						.getContent());
			}

		} catch (final XMLDBException ex) {
			// Handle exceptions
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();

		} finally {
			// if (coll!=null){
			// try {
			// coll.close();
			// } catch (XMLDBException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
		}
		return (long) ret;

	}



	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.arrayexpress.utils.saxon.search.AbstractIndexEnvironment#queryDB
	 * (org.apache.lucene.search.TopDocs,
	 * org.apache.lucene.search.IndexSearcher, int, int)
	 */
	@Deprecated
	public String queryDB(TopDocs hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		// Collection instance
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
		// Collection coll=null;
		try {

			// coll = DatabaseManager.getCollection(connectionString);
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			totalRes.append("(");
			for (int i = initialExp; i < finalExp; i++) {
				Document doc = isearcher.doc(hits.scoreDocs[i].doc);
				totalRes.append("'" + doc.get("accession") + "'");
				// totalRes.append("'" + doc.get("id") + "'");
				if (i != (finalExp - 1)) {
					totalRes.append(",");
				}
			}
			totalRes.append(")");
			if (logger.isDebugEnabled()) {
				logger.debug("QueryString->" + totalRes);
			}
			long time = System.nanoTime();
			ResourceSet set = service
					.query("<experiments>{for $x in "
							+ totalRes.toString()
							+ "  let $y:= //folder[@accession=$x] return <all>{//experiment[accession=($x) "
							+ userIdRestriction
							+ " and source/@visible!='false' ]} {$y}  </all>}</experiments>");
			double ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("Xml db query took: " + ms + " ms");
			}

			time = System.nanoTime();
			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			while (iter.hasMoreResources()) {

				ret += iter.nextResource().getContent();
			}
			ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieve data from Xml db took: " + ms + " ms");
			}

			time = System.nanoTime();
			// in the Experiments I need to calculate the total number of
			// assays.
			CalculateNumberOfAssaysInSearch(hits, isearcher, initialExp,
					finalExp, map);
			// map.put("totalassays", Integer.toString(12121));

			ms = (System.nanoTime() - time) / 1000000d;
			if (logger.isDebugEnabled()) {
				logger.debug("CalculateNumberOfAssaysInSearch took: " + ms
						+ " ms");
			}
		} catch (final XMLDBException ex) {
			// Handle exceptions
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {
			// if (coll!=null){
			// try {
			// coll.close();
			// } catch (XMLDBException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }

		}
		return ret;
	}


	@Deprecated
	public void CalculateNumberOfAssaysInSearch(TopDocs hits,
			IndexSearcher isearcher, int initialExp, int finalExp,
			HttpServletRequestParameterMap map) throws Exception {

		long ret = 0;
		long time = System.nanoTime();
		for (int i = 0; i < hits.totalHits; i++) {
			// Document doc = isearcher.doc(hits.scoreDocs[i].doc);
			ret += Long.parseLong(isearcher.doc(hits.scoreDocs[i].doc).get(
					"assays"));
		}

		map.put("totalassays", Long.toString(ret));
		// combinedTotal.toArray(new String[combinedTotal.size()]));

	}

}
