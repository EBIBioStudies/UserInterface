/**
 * 
 */
package uk.ac.ebi.fg.biostudies.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XPathQueryService;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.utils.HttpServletRequestParameterMap;

/**
 * @author rpslpereira
 * 
 */
public class IndexEnvironmentFiles extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String driverXml;
	private String connectionString;
	private Database db;
	Collection coll;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentFiles(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff
	@Override
	public void setup() {

		defaultSortField = "date";
		defaultSortDescending = false;
		defaultPageSize = 25;

		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");
		System.out.println(connsConf.isEmpty());

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
	 * AbstractIndexEnvironment#queryDB
	 * (org.apache.lucene.search.TopDocs,
	 * org.apache.lucene.search.IndexSearcher, int, int)
	 */
	@Override
	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		String ret = "";
		StringBuilder totalRes = new StringBuilder();
		try {

			// Receive the XPath query service
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			totalRes.append("(");
			for (int i = initialExp; i < finalExp; i++) {
				Document doc = isearcher.doc(hits[i].doc);
				totalRes.append("'" + doc.get("name") + "'");
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
			if (!map.containsKey("accession")) {
				set = service
						.query("<files>{for $x in "
								+ totalRes.toString()
								+ " let $y:= //folder[file/@name=($x)] "
								+ " let $acc:= $y/@accession "
								+ " return <all accession='{data($acc)}'>{$y/file[@name=($x)]} </all> "
								+ " } " + " </files> ");

			} else {
				set = service
						.query("<files>{for $x in ('"
								+ (String) map.get("accession")[0]
								+ "')"
								+ " let $y:= //folder[@accession=($x)] "
								+ " let $aux:= $y/@kind "
								+ " let $metadata := if ($aux = 'array') then //array_designs/array_design[accession=($x)] else //experiments/experiment[accession=($x)] "
								+ " return <all>{$y} <metadata>{$metadata}</metadata> "
								+ " <arrayschilds> "
								+ " {for $child in ($metadata)/arraydesign "
								+ " let $fold:= //folder[@accession=$child/accession] "
								+ " return <arraychild>{//array_designs/array_design[accession=($child)/accession]}{$fold}</arraychild>} "
								+ " </arrayschilds> " + " </all>} "
								+ " </files> ");
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
