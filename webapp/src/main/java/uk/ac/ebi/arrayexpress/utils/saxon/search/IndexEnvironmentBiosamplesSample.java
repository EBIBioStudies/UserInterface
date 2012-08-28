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
import org.mortbay.log.Log;
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
public class IndexEnvironmentBiosamplesSample extends AbstractIndexEnvironment {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String driverXml;
	private String connectionString;
	private String dbname;
	private Database db;
	private Collection coll;

	// private long numberOfExperiments;
	//
	// private long numberOfAssays;

	/**
	 * @param indexConfig
	 */
	public IndexEnvironmentBiosamplesSample(HierarchicalConfiguration indexConfig) {
		super(indexConfig);
	}

	// I need to initialize the database connection stuff nad to calculate the
	// number of assys ans the number os experiments (matter of performance)
	@Override
	public void setup() {

		defaultSortField = "";
		defaultSortDescending = false;
		defaultPageSize = 10;

		//I'm calling this to clean the reference to the IndexReader-> closeIndexReader();getIndexReader();
		super.setup();
		
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
			
			//TODO: rpe: review this (DB open files)
			if(coll!=null){
				coll.close();
				db=null;
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
			throws Exception{
	 
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
			
			ResourceSet set=null;
			//search
			
			
			//I'm showing the datail of a sample inside a sample Group browsing a sample group
			if( map.get("samplegroup")!=null &&  map.get("accession")!=null){

				set = service
						.query("<biosamples><all>{for $x in  "
								+ totalRes.toString() 
								+ "  let $y:=//Sample[@id=($x) and @groupId='" +map.get("samplegroup")[0]  +"']"
								+ " return ($y) "
								+ " }</all></biosamples>");	
			}
				//I'm browsing a sample group
				else{ if( map.get("samplegroup")!=null){

//					// I need to implement her the sirt logic
//					int page= Integer.parseInt(map.get("page")[0]);
//					int pageSize=Integer.parseInt(map.get("pagesize")[0]);
//					int sampleInit=(page == 1 ? 1 : (page) * pageSize);
//					
//					
//					Log.debug("<biosamples><all>{subsequence( "
//							+ "  let $att:= /Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']"
//							+ " for $x in $att/Sample "
//							+ " order  by $x/attribute/value[../@class=replace('"  + map.get("sortby")[0]  + "','-',' ')] " + map.get("sortorder")[0] + " " 
//							+ " return $x, " + sampleInit +"," + pageSize + ") } "
//							+ "<attributes>{distinct-values(/Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']/Sample/attribute/replace(@class, ' ' , '-'))}</attributes> "
//							+ " </all></biosamples>");
//		 
//					set = service
//								.query("<biosamples><all>{subsequence( "
//										+ "  let $att:= /Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']"
//										+ " for $x in $att/Sample "
//										+ " order  by $x/attribute/value[../@class=replace('"  + map.get("sortby")[0]  + "','-',' ')] " + map.get("sortorder")[0] + " " 
//										+ " return $x, " + sampleInit +"," + pageSize + ") } "
//										+ "<attributes>{distinct-values(/Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']/Sample/attribute/replace(@class, ' ' , '-'))}</attributes> "
//										+ " </all></biosamples>");	
			
					 
//			
//					logger.debug("£££££££££############" + "<biosamples><all>{for $x in  "
//										+ totalRes.toString() 
//										+ "  let $y:=//Sample[@id=($x)]"
//										+ "  return <Samples>{$y[../@id='" +   map.get("samplegroup")[0]  + "']} </Samples>} "
//										///+ " { let $att:= /Biosamples/SampleGroup[@id='" +   map.get("samplegroup")[0]  + "'] " 
//										//+ " return <attributes notnumeric='{distinct-values($att/Sample/attribute[@dataType!='INTEGER']/replace(@class, ' ' , '-'))}' numeric='{distinct-values($att/Sample/attribute[@dataType='INTEGER']/replace(@class, ' ' , '-'))}'></attributes> "
//										///+ " return <attributes>{distinct-values($att/Sample/attribute/replace(@class, ' ' , '-'))}</attributes> "
//										///+ " }"
//										+ " { let $att:= /Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']"
//										+ " return {$att/SampleAttributes} }"
//										+ "</all></biosamples>");
					set = service
								.query("<biosamples><all>{for $x in  "
										+ totalRes.toString() 
										+ "  let $y:=//Sample[@id=($x)]"
										+ "  return <Samples>{$y[../@id='" +   map.get("samplegroup")[0]  + "']} </Samples>} "
										///+ " { let $att:= /Biosamples/SampleGroup[@id='" +   map.get("samplegroup")[0]  + "'] " 
										//+ " return <attributes notnumeric='{distinct-values($att/Sample/attribute[@dataType!='INTEGER']/replace(@class, ' ' , '-'))}' numeric='{distinct-values($att/Sample/attribute[@dataType='INTEGER']/replace(@class, ' ' , '-'))}'></attributes> "
										///+ " return <attributes>{distinct-values($att/Sample/attribute/replace(@class, ' ' , '-'))}</attributes> "
										///+ " }"
										+ " { let $att:= /Biosamples/SampleGroup[@id='" + map.get("samplegroup")[0]  +"']"
										+ " return <SampleAttributes>{$att/SampleAttributes/*} </SampleAttributes>}"
										+ "</all></biosamples>");	
				}
				
			
				
				//return everything
				else{
					 set = service
								.query("<biosamples><all>{for $x in  "
										+ totalRes.toString() 
										+ "  let $y:=//Sample[@id=($x)]"
										+ "  return <Samples>{$y}</Samples>} "
										+ " </all></biosamples>");	
				}
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
//		logger.debug("Xml->" + ret);
		//TODO rpe> remove this
//		ret=ret.replace("&", "ZZZZZ");
		

//		ret=ret.replace("&#x96;", "&#150;");
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
			
			ResourceSet set=null;
			//search
				 set = service
						.query("<biosamples><all>{for $x in  "
								+ totalRes.toString() 
								+ "  let $y:=//Sample[@id=($x)]"
								+ "  return <Samples>{$y[../@id='" +   map.get("samplegroup")[0]  + "']}</Samples>} "
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
//		logger.debug("Xml->" + ret);
		//TODO rpe> remove this
//		ret=ret.replace("&", "ZZZZZ");
		return ret;
		
//		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +ret; 
	}


}
