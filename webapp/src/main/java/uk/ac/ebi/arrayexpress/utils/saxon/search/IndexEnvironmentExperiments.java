/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

/**
 * @author rpslpereira
 *
 */
public class IndexEnvironmentExperiments extends AbstractIndexEnvironment {

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentExperiments(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
		setNumberOfExperiments(calculateNumberOfExperiments());
		setNumberOfAssays(calculateNumberOfAssays());
	}

	
	public int getNumberOfExperiments() {
		return numberOfExperiments;
	}

	private void setNumberOfExperiments(int numberOfExperiments) {
		this.numberOfExperiments = numberOfExperiments;
	}

	public int getNumberOfAssays() {
		return numberOfAssays;
	}

	private void setNumberOfAssays(int numberOfAssays) {
		this.numberOfAssays = numberOfAssays;
	}


	private int numberOfExperiments;
	
	private int numberOfAssays;
	
	
	
	@Override
	public String getDefaultSortField(){
		return "releasedate";
		
	}

	@Override
	public boolean getDefaultSortDescending(){
		return false;
	}


	@Override
	public int getDefaultPageSize(){
		return 25;
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.ebi.arrayexpress.utils.saxon.search.AbstractIndexEnvironment#queryDB(org.apache.lucene.search.TopDocs, org.apache.lucene.search.IndexSearcher, int, int)
	 */
	@Override
	public String queryDB(TopDocs hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map) throws Exception {

		// Collection instance
		Collection coll = null;
		String ret = "";
		StringBuilder totalRes = new StringBuilder();
		try {

			// Register the database
			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			Class<?> c = Class.forName("org.basex.api.xmldb.BXDatabase");
			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			Database db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			// Receive the database

			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			coll = DatabaseManager
					.getCollection("xmldb:basex://localhost:1984/basexAE");
			// coll =
			// DatabaseManager.getCollection("xmldb:exist:////xmlrpc/existdbAE","admin","admin");
			// Receive the XPath query service
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
			System.out.println("QueryString->" + totalRes);
			long time = System.nanoTime();
			ResourceSet set = service
					.query("<experiments>{for $x in "
							+ totalRes.toString()
							+ "  let $y:= //folder[@accession=$x] return <all>{//experiment[accession=($x) and source/@visible!='false' and user/@id=1]} {$y}  </all>}</experiments>");
			double ms = (System.nanoTime() - time) / 1000000d;
			System.out.println("\n\n" + ms + " 2ms");

			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			while (iter.hasMoreResources()) {

				ret += iter.nextResource().getContent();
			}
			
			//in the Experiments I need to calculate the total number of assays.
			CalculateNumberOfAssaysInSearch(hits, isearcher, initialExp, finalExp, map);
//			map.put("totalassays", Integer.toString(12121));
			
			
			ms = (System.nanoTime() - time) / 1000000d;
			System.out.println("\n\n" + ms + " 2ms");
		} catch (final XMLDBException ex) {
			// Handle exceptions
			System.err.println("XML:DB Exception occured " + ex.errorCode);
			ex.printStackTrace();
		} finally {
			// Close the collection
			if (coll != null)
				coll.close();
		}
		return ret;
	}

	public void CalculateNumberOfAssaysInSearch(TopDocs hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception {

		int ret = 0;
		long time = System.nanoTime();
		for (int i = 0; i < hits.totalHits; i++) {
			// Document doc = isearcher.doc(hits.scoreDocs[i].doc);
			ret += +Integer.parseInt(isearcher.doc(hits.scoreDocs[i].doc).get(
					"assays"));
		}
		double ms = (System.nanoTime() - time) / 1000000d;
		System.out.println("\n\n" + ms
				+ " 2ms Count number of assays using index");
		
//		List<String> combinedTotal = new ArrayList<String>();
//		combinedTotal.add(String.valueOf(hits.totalHits));

		map.put("totalassays", Integer.toString(ret));
//				combinedTotal.toArray(new String[combinedTotal.size()]));
		
	}

	
	
	//I need to know the number of experiments and the number of assays
	
	
	public int calculateNumberOfExperiments(){
		int ret=0;
		Collection coll = null;
		try {
			// Register the database
			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)

			Class<?> c = Class.forName("org.basex.api.xmldb.BXDatabase");
			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			Database db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);

			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			coll = DatabaseManager
					.getCollection("xmldb:basex://localhost:1984/basexAE");
			// coll =
			// DatabaseManager.getCollection("xmldb:exist:////xmlrpc/existdbAE","admin","admin");
			// Receive the XPath query service
			long time=System.nanoTime();
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			ResourceSet set = service
					.query("count(//experiment[ source/@visible='true' and user/@id=1])");
			double ms = (System.nanoTime() - time) / 1000000d;
			System.out.println("\n\n" + ms + " 2ms");

			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			if (iter.hasMoreResources()) {

				ret +=Integer.parseInt((String)iter.nextResource().getContent());
			}
		
		} catch (final XMLDBException ex) {
			// Handle exceptions
			System.err.println("XML:DB Exception occured " + ex.errorCode);
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Close the collection
			if (coll != null)
				try {
					coll.close();
				} catch (XMLDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return ret;		
				
	}
	

	public int calculateNumberOfAssays() {
		int ret=0;
		Collection coll = null;
		try {
			// Register the database
			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)

			Class<?> c = Class.forName("org.basex.api.xmldb.BXDatabase");
			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			Database db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);

			// CHANGE BETWEEN DATABASES (EXISTDB AND BASEX)
			coll = DatabaseManager
					.getCollection("xmldb:basex://localhost:1984/basexAE");
			// coll =
			// DatabaseManager.getCollection("xmldb:exist:////xmlrpc/existdbAE","admin","admin");
			// Receive the XPath query service
			long time=System.nanoTime();
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");
			ResourceSet set = service
					.query("sum(//experiment/assays[ ../source/@visible='true' and ../user/@id=1])");
			double ms = (System.nanoTime() - time) / 1000000d;
			System.out.println("\n\n" + ms + " 2ms");

			ResourceIterator iter = set.getIterator();

			// Loop through all result items
			if (iter.hasMoreResources()) {

				ret +=Integer.parseInt((String)iter.nextResource().getContent());
			}
		
		} catch (final XMLDBException ex) {
			// Handle exceptions
			System.err.println("XML:DB Exception occured " + ex.errorCode);
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Close the collection
			if (coll != null)
				try {
					coll.close();
				} catch (XMLDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return ret;		
		
	}

	
	
	
	
}
