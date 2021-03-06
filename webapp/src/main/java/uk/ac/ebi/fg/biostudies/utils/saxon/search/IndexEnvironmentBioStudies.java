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
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.components.XmlDbConnectionPool;
import uk.ac.ebi.fg.biostudies.utils.HttpServletRequestParameterMap;

import java.util.ArrayList;

/**
 * @author rpslpereira
 * 
 */
public class IndexEnvironmentBioStudies extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private XmlDbConnectionPool xmlDBConnectionPool;

	// private long numberOfExperiments;
	//
	// private long numberOfAssays;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentBioStudies(HierarchicalConfiguration indexConfig) {
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
		defaultPageSize = 50;

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
		// getInfoDB();
		// search
		if (!map.containsKey("accession")) {

			ArrayList<String> list = new ArrayList<String>();
			list.add("title");
			list.add("description");
			// list.add("aaaaa");
			// list.add("bbbbb");
			// list.add("ccccc");
			// list.add("ddddd");
			// list.add("eeeee");
			// list.add("fffff");
			// list.add("ggggg");
			// list.add("hhhhh");
			// list.add("iiiii");
			// list.add("aaa");
			// list.add("bbb");
			// list.add("ccc");
			// list.add("ddd");
			// list.add("eee");
			// list.add("fff");
			// list.add("ggg");
			// list.add("hhh");
			// list.add("iii");
			// list.add("aaa");
			// list.add("bbb");
			// list.add("ccc");
			// list.add("ddd");
			// list.add("eee");
			// list.add("fff");
			// list.add("ggg");
			// list.add("hhh");
			// list.add("iii");

			totalRes.append("<content><all>");
			totalRes.append("<attributes>");
			for (String att : list) {
				totalRes.append("<attribute>" + att + "</attribute>");
			}
			totalRes.append("</attributes>");

			//I will not limit the chars for now
			int maxChars = 200000;
			for (int i = initialExp; i < finalExp; i++) {

				int docId = hits[i].doc;
				Document doc = isearcher.doc(docId);
				totalRes.append("<entity>");
				totalRes.append("<id>" + doc.get("accession") + "</id>");
				totalRes.append("<linkscount>" + doc.get("linkscount")
						+ "</linkscount>");
				totalRes.append("<filescount>" + doc.get("filescount")
						+ "</filescount>");
				totalRes.append("<attributes>");
				for (String att : list) {
					String value=doc.get(att)!=null?doc.get(att):"";
					if (value.length() > maxChars) {
						totalRes.append("<attribute name='" + att + "'>"
								+ value.substring(0, maxChars)
								+ " ...</attribute>");
					} else {
						totalRes.append("<attribute name='" + att + "'>"
								+ value + "</attribute>");
					}
				}
				totalRes.append("</attributes>");
				totalRes.append("</entity>");

			}
			totalRes.append("</all></content>");
			// System.out.println("totalRes->" + totalRes.toString());
			ret = totalRes.toString();

		}
		// detail of a group sample
		else {

			try {

				// coll = DatabaseManager.getCollection(connectionString);
				Collection coll = xmlDBConnectionPool.getCollection();
				XPathQueryService service = (XPathQueryService) coll
						.getService("XPathQueryService", "1.0");
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

				set = service.query("<content><all>{for $x in "
						+ totalRes.toString()
						+ " let $group:=//section[@id=($x)]"
						+ " return $group " + " }</all></content>");
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

}