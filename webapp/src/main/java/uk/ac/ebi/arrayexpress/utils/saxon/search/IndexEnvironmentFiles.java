/**
 * 
 */
package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
public class IndexEnvironmentFiles extends AbstractIndexEnvironment {


	@Override
	public String getDefaultSortField(){
		return "date";
		
	}

	@Override
	public boolean getDefaultSortDescending(){
		return false;
	}


	@Override
	public int getDefaultPageSize(){
		return 25;
	}
	
	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentFiles(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
		// TODO Auto-generated constructor stub
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
				totalRes.append("'" + doc.get("name") + "'");
				if (i != (finalExp - 1)) {
					totalRes.append(",");
				}
			}
			totalRes.append(")");
			System.out.println("QueryString->" + totalRes);
			long time = System.nanoTime();
			
			ResourceSet set=null;
			//I need to put much more detail
			if(!map.containsKey("accession")){
				set = service
						.query("<files>{for $x in "
								+ totalRes.toString()
								+ " let $y:= //folder[file/@name=($x)] " 
								+ " let $acc:= $y/@accession " 
								+ " return <all accession='{data($acc)}'>{$y/file[@name=($x)]} </all> " 
								+ " } "
								+ " </files> ");
			
			}
			else{
				set = service
						.query("<files>{for $x in ('"
								+ (String)map.get("accession")[0] + "')"
								+ " let $y:= //folder[@accession=($x)] " 
								+ " let $aux:= $y/@kind "
								+ " let $metadata := if ($aux = 'array') then //array_designs/array_design[accession=($x)] else //experiments/experiment[accession=($x)] "
								+ " return <all>{$y} <metadata>{$metadata}</metadata> "
								+ " <arrayschilds> "
								+ " {for $child in ($metadata)/arraydesign "
								+ " let $fold:= //folder[@accession=$child/accession] "
								+ " return <arraychild>{//array_designs/array_design[accession=($child)/accession]}{$fold}</arraychild>} "
								+ " </arrayschilds> " 
								+ " </all>} "
								+ " </files> ");
			}
			
			ResourceIterator iter = set.getIterator();
			double ms = (System.nanoTime() - time) / 1000000d;
			System.out.println("\n\n" + ms + " 2ms");
			
			// Loop through all result items
			while (iter.hasMoreResources()) {

				ret += iter.nextResource().getContent();
			}
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

	

	
	
	
	
}
