/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
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
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;

/**
 * @author rpslpereira
 * 
 */
public class IndexEnvironmentBiosamplesGroup extends AbstractIndexEnvironment {

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
	public IndexEnvironmentBiosamplesGroup(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff nad to calculate the
	// number of assys ans the number os experiments (matter of performance)
	@Override
	public void setup() {

		// defaultSortField = "id";
		// defaultSortDescending = false;
		defaultSortField = "";
		defaultSortDescending = true;
		defaultPageSize = 25;

		// I'm calling this to clean the reference to the IndexReader->
		// closeIndexReader();getIndexReader();
		super.setup();

		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");

		if (null != connsConf) {
			driverXml = connsConf.getString("driver");
			connectionString = connsConf.getString("base") + "://"
					+ connsConf.getString("host") + ":"
					+ connsConf.getString("port") + "/"
					+ connsConf.getString("dbname");
		} else {
			logger.error("ae.xmldatabase Configuration is missing!!");
		}

		Class<?> c;
		try {

			// TODO: rpe: review this (DB open files - because the setup method
			// is being called from the reloadBioSamplesJob and I ned to close
			// the database files)
			if (coll != null) {
				coll.close();
				db = null;
			}

			c = Class.forName(driverXml);

			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
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

	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		// Collection instance
		String ret = "";
		StringBuilder totalRes = new StringBuilder();

		// Collection coll=null;
		// getInfoDB();
		// search
		if (!map.containsKey("id")) {
			totalRes.append("<biosamples><all>");
			for (int i = initialExp; i < finalExp; i++) {

				int docId = hits[i].doc;
				Document doc = isearcher.doc(docId);
				totalRes.append("<SampleGroup>");
				totalRes.append("<id>" + doc.get("id") + "</id>");
				totalRes.append("<description>"
						+ StringEscapeUtils.escapeXml(doc.get("description"))
						+ "</description>");
				totalRes.append("<samples>" + doc.get("samples") + "</samples>");
				totalRes.append("</SampleGroup>");

			}
			totalRes.append("</all></biosamples>");
			// System.out.println("totalRes->" + totalRes.toString());
			ret = totalRes.toString();

		}
		// detail of a group sample
		else {

			try {

				// coll = DatabaseManager.getCollection(connectionString);
				XPathQueryService service = (XPathQueryService) coll
						.getService("XPathQueryService", "1.0");
				totalRes.append("(");

				for (int i = initialExp; i < finalExp; i++) {

					int docId = hits[i].doc;
					Document doc = isearcher.doc(docId);
					totalRes.append("'" + doc.get("id") + "'");
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

				set = service
						.query("<biosamples><all>{for $x in "
								+ totalRes.toString()
								+ " let $group:=/Biosamples/SampleGroup[@id=($x)]"
								// +
								// "  return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} </SampleGroup> "
								// /+
								// " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} <attributes>{distinct-values($group/Sample/attribute/replace(@class, ' ' , '-'))} </attributes></SampleGroup> "
								// /+
								// " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} <attributes>Organism FamilyRelationship DiseaseType Name ExpansionLLot FamilyMember Ethnicity TransformationType Sample-Accession BiopsySite Age SampleType CellType TimeUnit Family Sex OrganismPart ClinicallyAffectedStatus ClinicalHistory GeographicOrigin GeneticStatus</attributes></SampleGroup> "
								+ " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} </SampleGroup> "
								+ " }</all></biosamples>");
				// +
				// " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} <attributes>{distinct-values($group/Sample/attribute[@dataType!='INTEGER']/replace(@class, ' ' , '-'))} </attributes> <attributesinteger>{distinct-values($group/Sample/attribute[@dataType='INTEGER']/replace(@class, ' ' , '-'))} </attributesinteger></SampleGroup> "

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
					logger.debug("Retrieve data from Xml db took: " + ms
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
		}

		return ret;
	}

	public String getInfoDB() {

		String ret = "";
		try {

			logger.debug("* Create a client session int the Xml Database to get information about it.");

			String dbHost = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.host");
			String dbPort = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.port");
			String dbPassword = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.adminpassword");

			String originalDbName = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.dbname");

			Session session = new ClientSession(dbHost,
					Integer.parseInt(dbPort), "admin", dbPassword);

			// ------------------------------------------------------------------------
			// Create a database
			logger.debug("* open database.");
			String logs = "";

			logs = session.execute("open " + originalDbName);
			logger.debug("open ...->" + logs);
			ret = session.execute("info db ");
			// logger.debug("info db ...->" + logs);
			logs = session.execute("close");
			logger.debug("close ...->" + logs);

			logger.debug("* Close the client session.");
			session.close();
			return ret;
		}

		catch (Exception ex) {
			// Handle exceptions
			ret = "ERROR";
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {

		}
		return ret;
	}

	public String queryDB_20062012(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		// Collection instance
		String ret = "";
		StringBuilder totalRes = new StringBuilder();

		// Collection coll=null;
		try {

			// coll = DatabaseManager.getCollection(connectionString);
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			totalRes.append("(");

			for (int i = initialExp; i < finalExp; i++) {

				int docId = hits[i].doc;
				Document doc = isearcher.doc(docId);
				totalRes.append("'" + doc.get("id") + "'");
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
			if (!map.containsKey("id")) {
				logger.debug("<biosamples><all>{for $x in  "
						+ totalRes.toString()
						+ " let $y:=//Biosamples/SampleGroup[@id=($x)]"
						+ "  return <SampleGroup><id>{$x}</id> "
						+ " <description>{($y)/attribute/value/text()[../../@class='Submission Description']}</description>"
						// + " <description>TESTE</description>"
						+ " <samples>{count(($y)/Sample)}</samples>"
						+ " </SampleGroup>}" + " </all></biosamples>");

				set = service
						.query("<biosamples><all>{for $x in  "
								+ totalRes.toString()
								+ " let $y:=//Biosamples/SampleGroup[@id=($x)]"
								+ "  return <SampleGroup><id>{$x}</id> "
								+ " <description>{($y)/attribute/value/text()[../../@class='Submission Description']}</description>"
								// + " <description>TESTE</description>"
								+ " <samples>{count(($y)/Sample)}</samples>"
								+ " </SampleGroup>}" + " </all></biosamples>");

			}
			// detail of a group sample
			else {

				set = service
						.query("<biosamples><all>{for $x in "
								+ totalRes.toString()
								+ " let $group:=/Biosamples/SampleGroup[@id=($x)]"
								// +
								// "  return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} </SampleGroup> "
								+ " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} <attributes>{distinct-values($group/Sample/attribute/replace(@class, ' ' , '-'))} </attributes></SampleGroup> "
								+ " }</all></biosamples>");
				// +
				// " return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} <attributes>{distinct-values($group/Sample/attribute[@dataType!='INTEGER']/replace(@class, ' ' , '-'))} </attributes> <attributesinteger>{distinct-values($group/Sample/attribute[@dataType='INTEGER']/replace(@class, ' ' , '-'))} </attributesinteger></SampleGroup> "
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
		// ret=ret.replace("&#x96;", "&ndash;");;

		// System.out.println("-->" +
		// StringTools.detectDecodeUTF8Sequences(ret));
		// ret=StringTools.replaceIllegalHTMLCharacters(StringTools.unescapeXMLHexaDecimalEntities(StringTools.unescapeXMLDecimalEntities(StringTools.detectDecodeUTF8Sequences(ret).replaceAll("&amp;#(\\d+);",
		// "&#$1;"))));
		//
		// System.out.println("-->" + ret);
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
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			totalRes.append("(");
			for (int i = initialExp; i < finalExp; i++) {
				Document doc = isearcher.doc(hits.scoreDocs[i].doc);
				totalRes.append("'" + doc.get("id") + "'");
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
			if (!map.containsKey("id")) {
				set = service
						.query("<biosamples><all>{for $x in  "
								+ totalRes.toString()
								+ " let $y:=//Biosamples/SampleGroup[@id=($x)]"
								+ "  return <SampleGroup><id>{$x}</id> "
								+ " <description>{($y)/attribute/value/text()[../../@class='Submission Description']}</description>"
								// + " <description>TESTE</description>"
								+ " <samples>{count(($y)/Sample)}</samples>"
								+ " </SampleGroup>}" + " </all></biosamples>");

			}
			// detail of a group sample
			else {

				set = service
						.query("<biosamples><all>{for $x in "
								+ totalRes.toString()
								+ " let $group:=/Biosamples/SampleGroup[@id=($x)]"
								+ "  return <SampleGroup samplecount=\"{count($group/Sample)}\"> {$group/(@*, * except Sample)} </SampleGroup> "
								+ " }</all></biosamples>");

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
		// TODO: rpe remove this
		// ret=ret.replace("&", "ZZZZZ");
		// ret=ret.replace("&#x96;", "AA");

		return ret;

		// return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +ret;
	}

}