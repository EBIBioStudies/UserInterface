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
public class IndexEnvironmentExperimentsCacheOnIndex extends AbstractIndexEnvironment {

	/**
	 * @param indexConfig
	 */
		
	private String driverXml;
	private String connectionString;
	private Database db;
	private Collection coll;

	private long numberOfExperiments;

	private long numberOfAssays;

	public IndexEnvironmentExperimentsCacheOnIndex(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
		// setup();

	}

	// I need to initialize the database connection stuff nad to calculate the
	// number of assys ans the number os experiments (matter of performance)
	@Override
	public void setup() {

		defaultSortField = "releasedate";
		defaultSortDescending = false;
		defaultPageSize = 50;

	
	}

	public long getNumberOfExperiments() {
		return numberOfExperiments;
	}

	private void setNumberOfExperiments(long numberOfExperiments) {
		this.numberOfExperiments = numberOfExperiments;
	}

	public long getNumberOfAssays() {
		return numberOfAssays;
	}

	private void setNumberOfAssays(long numberOfAssays) {
		this.numberOfAssays = numberOfAssays;
	}

	//
	// @Override
	// public String getDefaultSortField() {
	// return "releasedate";
	//
	// }
	//
	// @Override
	// public boolean getDefaultSortDescending() {
	// return false;
	// }
	//
	// @Override
	// public int getDefaultPageSize() {
	// return 25;
	// }

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

		// Collection instance
//		String ret = "";
		StringBuilder totalRes = new StringBuilder();
		totalRes.append("<experiments>");
		// Collection coll=null;
		

		
			for (int i = initialExp; i < finalExp; i++) {
				Document doc = isearcher.doc(hits[i].doc);
//				System.out.println("doc->" + doc.get("xml"));
				totalRes.append(doc.get("xml"));			
			}
			totalRes.append("</experiments>");
//			System.out.println("xml completo->" + ret);
		return totalRes.toString();
	}

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
		double ms = (System.nanoTime() - time) / 1000000d;
		System.out.println("\n\n" + ms
				+ " 2ms Count number of assays using index");

		// List<String> combinedTotal = new ArrayList<String>();
		// combinedTotal.add(String.valueOf(hits.totalHits));

		map.put("totalassays", Long.toString(ret));
		// combinedTotal.toArray(new String[combinedTotal.size()]));

	}

	// I need to know the number of experiments and the number of assays

	public long calculateNumberOfExperiments() {
		long ret = 0;
	
		return 343434343;

	}

	public long calculateNumberOfAssays() {
		double ret = 0;
		
		return 999999999;

	}

}
