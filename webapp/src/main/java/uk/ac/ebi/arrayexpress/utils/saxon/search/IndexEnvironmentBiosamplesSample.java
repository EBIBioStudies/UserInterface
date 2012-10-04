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
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.XmlDbConnectionPool;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

/**
 * @author rpslpereira
 * 
 */
public class IndexEnvironmentBiosamplesSample extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private XmlDbConnectionPool xmlDBConnectionPool;

	// private String driverXml;
	// private String connectionString;
	// private String dbname;
	// private Database db;
	// private Collection coll;

	// private long numberOfExperiments;
	//
	// private long numberOfAssays;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentBiosamplesSample(
			HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff nad to calculate the
	// number of assys ans the number os experiments (matter of performance)
	@Override
	public void setup() {

		defaultSortField = "";
		defaultSortDescending = false;
		defaultPageSize = 10;

		// I'm calling this to clean the reference to the IndexReader->
		// closeIndexReader();getIndexReader();
		super.setup();
		this.xmlDBConnectionPool = (XmlDbConnectionPool) Application
				.getAppComponent("XmlDbConnectionPool");

	}

	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		// Collection instance
		String ret = "";
		StringBuilder totalRes = new StringBuilder();

		// Collection coll=null;
		try {

			// coll = DatabaseManager.getCollection(connectionString);
			Collection coll = xmlDBConnectionPool.getCollection();
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

			ResourceSet set = null;
			// search

			// I'm showing the datail of a sample inside a sample Group browsing
			if (map.get("accession") != null) {

				set = service.query("<biosamples><all>{for $x in  "
						+ totalRes.toString() + "  let $y:=//Sample[@id=($x)]"
						+ " return ($y) " + " }</all></biosamples>");
			}
			// I'm browsing a sample group
			else {
				set = service
						.query("<biosamples><all>{for $x in  "
								+ totalRes.toString()
								+ "  let $y:=//Sample[@id=($x)]"
								+ "  return <Samples>{$y[../@id='"
								+ map.get("samplegroup")[0]
								+ "']} </Samples>} "
								+ " { let $att:= /Biosamples/SampleGroup[@id='"
								+ map.get("samplegroup")[0]
								+ "']"
								+ " return <SampleAttributes>{$att/SampleAttributes/*} </SampleAttributes>}"
								+ "</all></biosamples>");
			}

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

		} catch (final XMLDBException ex) {
			// Handle exceptions
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {

		}

		return ret;

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

		// Collection coll=null;
		try {

			// coll = DatabaseManager.getCollection(connectionString);
			Collection coll = xmlDBConnectionPool.getCollection();
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

			ResourceSet set = null;
			// search
			set = service.query("<biosamples><all>{for $x in  "
					+ totalRes.toString() + "  let $y:=//Sample[@id=($x)]"
					+ "  return <Samples>{$y[../@id='"
					+ map.get("samplegroup")[0] + "']}</Samples>} "
					+ " </all></biosamples>");

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
		// logger.debug("Xml->" + ret);
		// TODO rpe> remove this
		// ret=ret.replace("&", "ZZZZZ");
		return ret;

		// return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +ret;
	}

}
