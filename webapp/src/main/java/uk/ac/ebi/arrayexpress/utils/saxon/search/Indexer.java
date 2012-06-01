package uk.ac.ebi.arrayexpress.utils.saxon.search;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.store.Directory;
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
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.saxon.PrintUtils;

public class Indexer {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AbstractIndexEnvironment env;

	private Map<String, XPathExpression> fieldXpe = new HashMap<String, XPathExpression>();

	public Indexer(AbstractIndexEnvironment env) {
		this.env = env;
	}

	//keep information realted with attributes
	public  class AttsInfo {
		public String name;
		public String type;	
		
		public  AttsInfo(String name, String type){
			setName(name);
			setType(type);
		}
		
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}		
		
		
		
	}
	
	public void index(DocumentInfo document) {

		try {

			XPath xp = new XPathEvaluator(document.getConfiguration());
			XPathExpression xpe = xp.compile(this.env.indexDocumentPath);
			List documentNodes = (List) xpe.evaluate(document,
					XPathConstants.NODESET);

			for (FieldInfo field : this.env.fields.values()) {
				fieldXpe.put(field.name, xp.compile(field.path));
				logger.debug("Field Path->[{}]", field.path);
			}

			IndexWriter w = createIndex(this.env.indexDirectory,
					this.env.indexAnalyzer);

			int countNodes = 0;
			for (Object node : documentNodes) {
				countNodes++;
				Document d = new Document();

				// get all the fields taken care of
				for (FieldInfo field : this.env.fields.values()) {
					try {
						List values = (List) fieldXpe.get(field.name).evaluate(
								node, XPathConstants.NODESET);

						// logger.debug("Field->[{}] values-> [{}]", field.name,
						// values.toString());

						for (Object v : values) {

							if ("integer".equals(field.type)) {
								addIntIndexField(d, field.name, v,
										field.shouldStore, field.shouldSort);
							} else if ("date".equals(field.type)) {
								// todo: addDateIndexField(d, field.name, v);
								logger.error(
										"Date fields are not supported yet, field [{}] will not be created",
										field.name);
							} else if ("boolean".equals(field.type)) {
								addBooleanIndexField(d, field.name, v,
										field.shouldSort);
							}

							else {
								// TODO rpe: remove this
								// in this case I will put all the data
								// processed
								// in the index
								if (field.name.equalsIgnoreCase("xml")) {
									String acc = ((NodeInfo) v)
											.getStringValue();

									String connectionString = "xmldb:basex://localhost:1984/basexAE";
									Class<?> c = Class
											.forName("org.basex.api.xmldb.BXDatabase");

									// Class<?> c =
									// Class.forName("org.exist.xmldb.DatabaseImpl");
									Database db = (Database) c.newInstance();
									DatabaseManager.registerDatabase(db);
									Collection coll = null;
									try {
										System.out.println("ZZZZZZ->" + acc);
										coll = DatabaseManager
												.getCollection(connectionString);
										XPathQueryService service = (XPathQueryService) coll
												.getService(
														"XPathQueryService",
														"1.0");

										long time = System.nanoTime();
										ResourceSet set = service
												.query("for $x in ('"
														+ acc
														+ "')  let $y:= //folder[@accession=$x] return <all>{//experiment[accession=($x) and source/@visible!='false' and user/@id=1]} {$y}  </all>");
										double ms = (System.nanoTime() - time) / 1000000d;
										System.out
												.println("\n\n" + ms + " 2ms");

										ResourceIterator iter = set
												.getIterator();

										// Loop through all result items
										while (iter.hasMoreResources()) {

											v = iter.nextResource()
													.getContent();
											System.out.println("query result->"
													+ v);
										}
									} catch (final XMLDBException ex) {
										// Handle exceptions
										System.err
												.println("XML:DB Exception occured "
														+ ex.errorCode);
										ex.printStackTrace();
									} finally {
										if (coll != null) {
											try {
												coll.close();
											} catch (XMLDBException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
									}

								}

								// when i use "." i t means that i want to keep
								// all the xml text as text
								// if(field.path.equalsIgnoreCase("/.")){
								// String
								// xml=PrintUtils.printNodeInfo((NodeInfo)node,
								// document.getConfiguration());
								// //TODO RPE:
								// xml=xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
								// "");
								// xml=xml.replace("\n", "");
								// v=xml;
								//
								// }
								addIndexField(d, field.name, v,
										field.shouldAnalyze, field.shouldStore,
										field.shouldSort);
							}
						}
					} catch (XPathExpressionException x) {
						logger.error(
								"Caught an exception while indexing expression ["
										+ field.path
										+ "] for document ["
										+ ((NodeInfo) node).getStringValue()
												.substring(0, 20) + "...]", x);
					}
				}

				addIndexDocument(w, d);
				// append node to the list
			}
			// SET the number of nodes
			this.env.setCountDocuments(countNodes);

			// TODO calculate this value
			// this.env.setCountFiltered(this.env.calculateCountFiltered());
			// commitIndex(w);
			Map<String, String> map = new HashMap<String, String>();
			map.put("numberDocs", Integer.toString(countNodes));
			map.put("date", Long.toString(System.nanoTime()));
			map.put("keyValidator", "ZZZZZZZZ");
			commitIndex(w, map);

		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	
	
	
	// I will generate the Lucene Index based on a XmlDatabase
		public void indexFromXmlDB() throws Exception {
			int countNodes = 0;
			String driverXml = "";
			String connectionString = "";
			Database db;
			Collection coll;
			Class<?> c;
			try {

				HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
						.getInstance().getPreferences()
						.getConfSubset("ae.xmldatabase");

				if (null != connsConf) {
					driverXml = connsConf.getString("driver");
					connectionString = connsConf.getString("connectionstring");
				} else {
					logger.error("ae.xmldatabase Configuration is missing!!");
				}

				c = Class.forName(driverXml);

				db = (Database) c.newInstance();
				DatabaseManager.registerDatabase(db);
				coll = DatabaseManager.getCollection(connectionString);
				XPathQueryService service = (XPathQueryService) coll.getService(
						"XPathQueryService", "1.0");

				SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance()
						.getComponent("SaxonEngine");
				DocumentInfo source = null;
				// Loop through all result items

				// collect all the fields data
				Configuration config = ((SaxonEngine) Application
						.getAppComponent("SaxonEngine")).trFactory
						.getConfiguration();

				XPath xp = new XPathEvaluator(config);
				//XPathExpression xpe = xp.compile(this.env.indexDocumentPath);

				for (FieldInfo field : this.env.fields.values()) {
					fieldXpe.put(field.name, xp.compile(field.path));
					logger.debug("Field Path->[{}]", field.path);
				}

				// create the index
				IndexWriter w = createIndex(this.env.indexDirectory,
						this.env.indexAnalyzer);

				// the xmldatabase is not very correct and have memory problem for
				// queires with huge results, so its necessary to implement our own
				// iteration mechanism

				// I will collect all the results
				ResourceSet set = service.query(this.env.indexDocumentPath);
				//TODO rpe
				//ResourceSet set = service.query("//Sample");
				logger.debug("Number of results->" + set.getSize());
				long numberResults = set.getSize();
				if (coll != null) {
					try{
						coll.close();
					} catch (XMLDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				set = null;
				db=null;
				//c=null;
				long pageSizeDefault = 1000;
				long pageNumber = 1;

				String xml = "";
				Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
				Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
				Map<String, XPathExpression> cacheXpathAttValue=new HashMap<String, XPathExpression>();
				while ((pageNumber * pageSizeDefault) <= (numberResults
						+ pageSizeDefault - 1)) {

					// calculate the last hit
					long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
					long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
							: (numberResults - pageInit + 1);
					
					//c = Class.forName(driverXml);
					db = (Database) c.newInstance();
					DatabaseManager.registerDatabase(db);
					coll = DatabaseManager.getCollection(connectionString);
					service = (XPathQueryService) coll.getService(
							"XPathQueryService", "1.0");

					// xquery paging using subsequence function
					set = service.query("subsequence(" + this.env.indexDocumentPath
							+ "," + pageInit + "," + pageSize + ")");
					logger.debug("Number of results of page->" + set.getSize());
					ResourceIterator iter = set.getIterator();
					XPath xp2;
					XPathExpression xpe2;
					List documentNodes;
					StringReader reader;
					// cache of distinct attributes fora each sample group

					while (iter.hasMoreResources()) {

						xml = (String) iter.nextResource().getContent();
						reader = new StringReader(xml);
						source = config.buildDocument(new StreamSource(reader));

						// logger.debug("XML DB->[{}]",
						// PrintUtils.printNodeInfo((NodeInfo) source, config));
						Document d = new Document();

						xp2 = new XPathEvaluator(source.getConfiguration());
						// TODO rpe: remove this reference tp SmapleGroup (I should
						// test with /)

						// I need to

						int position = env.indexDocumentPath.lastIndexOf("/");
						;
						String pathRoot = "";
						if (position != -1) {
							pathRoot = env.indexDocumentPath.substring(position);
						} else {
							pathRoot = env.indexDocumentPath;
						}
						// logger.debug("PathRoot->[{}]",pathRoot);
						xpe2 = xp2.compile(pathRoot);
						//TODO rpe
//						xpe2 = xp2.compile("/Sample");
						documentNodes = (List) xpe2.evaluate(source,
								XPathConstants.NODESET);

						

						for (Object node : documentNodes) {
							// logger.debug("XML£££££££££ DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
							for (FieldInfo field : this.env.fields.values()) {
								try {

									// Configuration config=doc.getConfiguration();
									// I Just have to calculate the Xpath
									if (!field.process) {

										List values = (List) fieldXpe.get(
												field.name).evaluate(node,
												XPathConstants.NODESET);
										// logger.debug("Field->[{}] values-> [{}]",
										// field.name,
										// values.toString());
										for (Object v : values) {

											if ("integer".equals(field.type)) {
												addIntIndexField(d, field.name, v,
														field.shouldStore,
														field.shouldSort);
											} else if ("date".equals(field.type)) {
												// todo: addDateIndexField(d,
												// field.name,
												// v);
												logger.error(
														"Date fields are not supported yet, field [{}] will not be created",
														field.name);
											} else if ("boolean".equals(field.type)) {
												addBooleanIndexField(d, field.name,
														v, field.shouldSort);
											} else {
												addIndexField(d, field.name, v,
														field.shouldAnalyze,
														field.shouldStore,
														field.shouldSort);
											}
										}

									}
									else {
										if(field.name.equalsIgnoreCase("attributes")){
											//implement here the biosamples database sample attributes logic
											//TODO: rpe
											//logger.debug("There is A special treatment for this field->" + field.name);
											
											List values = (List) fieldXpe.get(
													field.name).evaluate(node,
													XPathConstants.NODESET);
											
//											XPathExpression classAtt=xp.compile("@class");
//											XPathExpression typeAtt=xp.compile("@dataType");
//											XPathExpression valueAtt=xp.compile("value");
											String groupId=(String)fieldXpe.get(
													"samplegroup").evaluate(node,
													XPathConstants.STRING);
											String id=(String)fieldXpe.get(
													"accession").evaluate(node,
													XPathConstants.STRING);
											
											//logger.debug(groupId+"$$$" + id);
											
											// logger.debug("Field->[{}] values-> [{}]",
											// field.name,
											// values.toString());

											AttsInfo[] attsInfo=null;
											if(cacheAtt.containsKey(groupId)){
												attsInfo=cacheAtt.get(groupId);	
											}
											else{
												logger.debug("No exists cache for samplegroup->" + groupId);
												//ResourceSet setAtt = service.query("distinct-values(/Biosamples/SampleGroup[@id='" + groupId + "']/Sample/attribute[@dataType!='INTEGER']/replace(@class,' ', '-'))");
												///ResourceSet setAtt = service.query("distinct-values(/Biosamples/SampleGroup[@id='" + groupId + "']/Sample/attribute/replace(@class,' ', '-'))");
												ResourceSet setAtt = service.query("distinct-values(/Biosamples/SampleGroup[@id='" + groupId + "']/Sample/attribute/@class)");
												ResourceIterator resAtt=setAtt.getIterator();
												int i=0;
												attsInfo= new AttsInfo[(int)setAtt.getSize()];
												while (resAtt.hasMoreResources()){
													String classValue=(String)resAtt.nextResource().getContent();
													//need to use this because of the use of quotes in the name of the classes
													String classValueWitoutQuotes=classValue.replaceAll("\"", "\"\"");
													//logger.debug("Class value->" + classValue);
													XPathExpression xpathAtt=null;
													XPathExpression xpathAttValue=null;
													if(cacheXpathAtt.containsKey(classValue)){
														 xpathAtt=cacheXpathAtt.get(classValue);
														 xpathAttValue=cacheXpathAttValue.get(classValue);
													}
													else{
														//String newXpathAttS=();
														//im using \" becuse there are some attributes thas has ' on the name!!!
														///xpathAtt=xp.compile("./attribute[@class=replace(\"" + classValueWitoutQuotes + "\",'-',' ')]/@dataType");
														xpathAtt=xp.compile("./attribute[@class=\"" + classValueWitoutQuotes + "\"]/@dataType");
														//I need to put[1] because some times I have more than one value (it generates problems when I need to piack up the integer value for sorting
														///xpathAttValue=xp.compile("./attribute[@class=replace(\"" + classValueWitoutQuotes + "\",'-',' ')]/value[1]/text()");
														xpathAttValue=xp.compile("./attribute[@class=\"" + classValueWitoutQuotes + "\"]/value[1]/text()");
														//logger.debug("./attribute[@class=\"" + classValueWitoutQuotes + "\"]/value[1]/text()");
														cacheXpathAtt.put(classValue, xpathAtt);
														cacheXpathAttValue.put(classValue,xpathAttValue);
													}
													//this doesnt work when the first sample of sample group doens have all the attributes
													//im using \" becuse there are some attributes thas has ' on the name!!!
													///ResourceSet setAttType = service.query("string((/Biosamples/SampleGroup[@id='" + groupId +"']/Sample/attribute[@class=replace(\"" + classValueWitoutQuotes + "\",'-',' ')]/@dataType)[1])");
													ResourceSet setAttType = service.query("string((/Biosamples/SampleGroup[@id='" + groupId +"']/Sample/attribute[@class=\"" + classValueWitoutQuotes + "\"]/@dataType)[1])");
													String dataValue=(String)setAttType.getIterator().nextResource().getContent();
													//logger.debug("Data Type of " + classValue + " ->" + dataValue);
													//String dataValue=(String)xpathAtt.evaluate(node, XPathConstants.STRING);
													AttsInfo attsI= new AttsInfo(classValue,dataValue);
													//logger.debug("Atttribute->class" + attsI.name + "->type->" + attsI.type + "->i" + i);
													attsInfo[i]=attsI;
//													logger.debug("distinct att->" + value);
													//cacheAtt.put(groupId, value);
													i++;
												}
												cacheAtt.put(groupId, attsInfo);
												//distinctAtt=cacheAtt.get(groupId);
												//logger.debug("Already exists->" + distinctAtt);
											}
											int len=attsInfo.length;
											for (int i=0;i<len;i++){
												//logger.debug("$$$$$$->" + attsInfo[i].name + "$$$$" + attsInfo[i].type);
												if(!attsInfo[i].type.equalsIgnoreCase("integer") && !attsInfo[i].type.equalsIgnoreCase("real")){
	
													//logger.debug("$$$$$$->" + "STRING");
													XPathExpression valPath=cacheXpathAttValue.get(attsInfo[i].name);
													String val=(String)valPath.evaluate(node, XPathConstants.STRING);
													if(attsInfo[i].name.equalsIgnoreCase("age")){
//														logger.debug("$$$$$$2->" + attsInfo[i].name + "$$$$" + attsInfo[i].type);
//														logger.debug("$$$$$$$$$$$$ AGE COMO STRING->" + val + "->groupId->" + groupId);
														//throw new Exception("ERRRRRRRRRRO!!!!!!!!");
													}
													addIndexField(d, (i+1)+"", val,false,false, true);
												}
												else{
													XPathExpression valPath=cacheXpathAttValue.get(attsInfo[i].name);
													String valS=(String)valPath.evaluate(node, XPathConstants.STRING);
													valS=valS.trim();
													//logger.debug("Integer->" + valS);
													int val=0;
													if(valS==null ||valS.equalsIgnoreCase("") || valS.equalsIgnoreCase("NaN")){
														valS="0";
													}
													//sort numbers as strings
													//logger.debug("class->" + attsInfo[i].name  +"value->##"+ valS + "##");
													BigDecimal num=new BigDecimal(valS);
													num=num.multiply(new BigDecimal(100));
													int taux= num.toBigInteger().intValue();
													valS=String.format("%07d", taux);
													//logger.debug("Integer->" + valS + "position->" +(i+1)+"integer");
													addIndexField(d, (i+1)+"", valS,false,false, true);
													//addIntIndexField(d, (i+1)+"integer", new BigInteger(valS),false, true);
//															
												}
											}
												
										}
										else{
											//logger.debug("There is NO special treatment for this field->" + field.name);
										}
									}
								} catch (XPathExpressionException x) {
									logger.error(
											"Caught an exception while indexing expression ["
													+ field.path
													+ "] for document ["
													+ ((NodeInfo) source)
															.getStringValue()
															.substring(0, 20)
													+ "...]", x);
									throw x;
								}
							}
						}

						documentNodes = null;
						source = null;
						reader = null;
						xml = null;
						countNodes++;
						// logger.debug("count->[{}]", countNodes);

						// add document to lucene index
						// TODO: rpe (an int just to make some tests);

						addIndexDocument(w, d);
//						//TODO: rpe - just for test
//						addIndexDocument(w, d);
//						addIndexDocument(w, d);
//						addIndexDocument(w, d);
//						addIndexDocument(w, d);
					}

					logger.debug("until now it were processed->[{}]",
							pageNumber * pageSizeDefault);
					pageNumber++;
					if (coll != null) {
						try{
							coll.close();
						} catch (XMLDBException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					set=null;
					db=null;
					// TODO: rpe (review this)
					// from 1000 to 1000 I will make a commit
					//w.commit();

				}

				this.env.setCountDocuments(countNodes);
				// add metadata to the lucene index
				Map<String, String> map = new HashMap<String, String>();
				map.put("numberDocs", Integer.toString(countNodes));
				map.put("date", Long.toString(System.nanoTime()));
				map.put("keyValidator", "ZZZZZZZZ");
				commitIndex(w, map);

			} catch (Exception x) {
				logger.error("Caught an exception:", x);
				throw x;
			}
		}
	
	
	

		
		
		// I will generate the Lucene Index based on a XmlDatabase
				public void indexFromXmlDB_29052012() {
					int countNodes = 0;
					String driverXml = "";
					String connectionString = "";
					Database db;
					Collection coll;
					Class<?> c;
					try {

						HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
								.getInstance().getPreferences()
								.getConfSubset("ae.xmldatabase");

						if (null != connsConf) {
							driverXml = connsConf.getString("driver");
							connectionString = connsConf.getString("connectionstring");
						} else {
							logger.error("ae.xmldatabase Configuration is missing!!");
						}

						c = Class.forName(driverXml);

						db = (Database) c.newInstance();
						DatabaseManager.registerDatabase(db);
						coll = DatabaseManager.getCollection(connectionString);
						XPathQueryService service = (XPathQueryService) coll.getService(
								"XPathQueryService", "1.0");

						SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance()
								.getComponent("SaxonEngine");
						DocumentInfo source = null;
						// Loop through all result items

						// collect all the fields data
						Configuration config = ((SaxonEngine) Application
								.getAppComponent("SaxonEngine")).trFactory
								.getConfiguration();

						XPath xp = new XPathEvaluator(config);
						//XPathExpression xpe = xp.compile(this.env.indexDocumentPath);

						for (FieldInfo field : this.env.fields.values()) {
							fieldXpe.put(field.name, xp.compile(field.path));
							logger.debug("Field Path->[{}]", field.path);
						}

						// create the index
						IndexWriter w = createIndex(this.env.indexDirectory,
								this.env.indexAnalyzer);

						// the xmldatabase is not very correct and have memory problem for
						// queires with huge results, so its necessary to implement our own
						// iteration mechanism

						// I will collect all the results
						ResourceSet set = service.query(this.env.indexDocumentPath);
						//TODO rpe
						//ResourceSet set = service.query("//Sample");
						logger.debug("Number of results->" + set.getSize());
						long numberResults = set.getSize();
						if (coll != null) {
							try{
								coll.close();
							} catch (XMLDBException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						set = null;
						db=null;
						//c=null;
						long pageSizeDefault = 1000;
						long pageNumber = 1;

						String xml = "";
						while ((pageNumber * pageSizeDefault) <= (numberResults
								+ pageSizeDefault - 1)) {

							// calculate the last hit
							long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
							long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
									: (numberResults - pageInit + 1);
							
							//c = Class.forName(driverXml);
							db = (Database) c.newInstance();
							DatabaseManager.registerDatabase(db);
							coll = DatabaseManager.getCollection(connectionString);
							service = (XPathQueryService) coll.getService(
									"XPathQueryService", "1.0");

							// xquery paging using subsequence function
							set = service.query("subsequence(" + this.env.indexDocumentPath
									+ "," + pageInit + "," + pageSize + ")");
							logger.debug("Number of results of page->" + set.getSize());
							ResourceIterator iter = set.getIterator();
							XPath xp2;
							XPathExpression xpe2;
							List documentNodes;
							StringReader reader;
							while (iter.hasMoreResources()) {

								xml = (String) iter.nextResource().getContent();
								reader = new StringReader(xml);
								source = config.buildDocument(new StreamSource(reader));

								// logger.debug("XML DB->[{}]",
								// PrintUtils.printNodeInfo((NodeInfo) source, config));
								Document d = new Document();

								xp2 = new XPathEvaluator(source.getConfiguration());
								// TODO rpe: remove this reference tp SmapleGroup (I should
								// test with /)

								// I need to

								int position = env.indexDocumentPath.lastIndexOf("/");
								;
								String pathRoot = "";
								if (position != -1) {
									pathRoot = env.indexDocumentPath.substring(position);
								} else {
									pathRoot = env.indexDocumentPath;
								}
								// logger.debug("PathRoot->[{}]",pathRoot);
								xpe2 = xp2.compile(pathRoot);
								//TODO rpe
//								xpe2 = xp2.compile("/Sample");
								documentNodes = (List) xpe2.evaluate(source,
										XPathConstants.NODESET);

								for (Object node : documentNodes) {
									// logger.debug("XML£££££££££ DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
									for (FieldInfo field : this.env.fields.values()) {
										try {

											// Configuration config=doc.getConfiguration();
											// TODO: remove this test

											if (!field.name.equalsIgnoreCase("order") && !field.name.equalsIgnoreCase("order3")) {

												List values = (List) fieldXpe.get(
														field.name).evaluate(node,
														XPathConstants.NODESET);
												// logger.debug("Field->[{}] values-> [{}]",
												// field.name,
												// values.toString());
												for (Object v : values) {

													if ("integer".equals(field.type)) {
														addIntIndexField(d, field.name, v,
																field.shouldStore,
																field.shouldSort);
													} else if ("date".equals(field.type)) {
														// todo: addDateIndexField(d,
														// field.name,
														// v);
														logger.error(
																"Date fields are not supported yet, field [{}] will not be created",
																field.name);
													} else if ("boolean".equals(field.type)) {
														addBooleanIndexField(d, field.name,
																v, field.shouldSort);
													} else {
														addIndexField(d, field.name, v,
																field.shouldAnalyze,
																field.shouldStore,
																field.shouldSort);
													}
												}

											}
											// TODO: remove this
											else {
												//new BigInteger(countNodes+""),
												if(field.name.equalsIgnoreCase("order")){
													addIntIndexField(d, field.name, new BigInteger(countNodes+""),
															field.shouldStore, field.shouldSort);											
												}
												else{
													addIndexField(d, field.name, countNodes, false,
															field.shouldStore, field.shouldSort);
												}
											}
										} catch (XPathExpressionException x) {
											logger.error(
													"Caught an exception while indexing expression ["
															+ field.path
															+ "] for document ["
															+ ((NodeInfo) source)
																	.getStringValue()
																	.substring(0, 20)
															+ "...]", x);
										}
									}
								}

								documentNodes = null;
								source = null;
								reader = null;
								xml = null;
								countNodes++;
								// logger.debug("count->[{}]", countNodes);

								// add document to lucene index
								// TODO: rpe (an int just to make some tests);

								addIndexDocument(w, d);
//								//TODO: rpe - just for test
//								addIndexDocument(w, d);
//								addIndexDocument(w, d);
//								addIndexDocument(w, d);
//								addIndexDocument(w, d);
							}

							logger.debug("until now it were processed->[{}]",
									pageNumber * pageSizeDefault);
							pageNumber++;
							if (coll != null) {
								try{
									coll.close();
								} catch (XMLDBException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							set=null;
							db=null;
							// TODO: rpe (review this)
							// from 1000 to 1000 I will make a commit
							//w.commit();

						}

						this.env.setCountDocuments(countNodes);
						// add metadata to the lucene index
						Map<String, String> map = new HashMap<String, String>();
						map.put("numberDocs", Integer.toString(countNodes));
						map.put("date", Long.toString(System.nanoTime()));
						map.put("keyValidator", "ZZZZZZZZ");
						commitIndex(w, map);

					} catch (Exception x) {
						logger.error("Caught an exception:", x);
					}
				}
					
		
		
	
	public void indexReader() {
		env.indexReader();
	}

	// //TODO RPE
	// public void indexReader()
	// {
	//
	// try {
	//
	// IndexReader ir = IndexReader.open(this.env.indexDirectory, true);
	//
	//
	//
	//
	// Map<String, String> map= ir.getCommitUserData();
	// System.out.println("numberDocs->" + map.get("numberDocs"));
	// System.out.println("date->" + map.get("date"));
	// System.out.println("keyValidator->" + map.get("keyValidator"));
	// this.env.setCountDocuments(Integer.parseInt(map.get("numberDocs")));
	//
	// } catch (Exception x) {
	// logger.error("Caught an exception:", x);
	// }
	// }
	//

	private IndexWriter createIndex(Directory indexDirectory, Analyzer analyzer) {
		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(indexDirectory, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
			//TODO: just to check if it solves the slowly indexing indexes with more 
			iwriter.setMaxBufferedDocs(100000);
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}

		return iwriter;
	}

	private void addIndexField(Document document, String name, Object value,
			boolean shouldAnalyze, boolean shouldStore, boolean sort) {
		String stringValue;
		if (value instanceof String) {
			stringValue = (String) value;
		} else if (value instanceof NodeInfo) {
			stringValue = ((NodeInfo) value).getStringValue();
		} else {
			stringValue = value.toString();
			logger.warn(
					"Not sure if I handle string value of [{}] for the field [{}] correctly, relying on Object.toString()",
					value.getClass().getName(), name);
		}
		// TODO
		// logger.debug("value->[{}]", stringValue);
		document.add(new Field(name, stringValue, shouldStore ? Field.Store.YES
				: Field.Store.NO, shouldAnalyze ? Field.Index.ANALYZED
				: Field.Index.NOT_ANALYZED));
		// ig Im indexing a String and the @sort=true I will always create a new
		// field (fieldname+sort)
		if (sort) {
			String newF = name + "sort";
			document.add(new Field(newF, stringValue, Field.Store.NO,
					Field.Index.NOT_ANALYZED));
		}

	}

	private void addBooleanIndexField(Document document, String name,
			Object value, boolean sort) {
		Boolean boolValue = null;
		if (value instanceof Boolean) {
			boolValue = (Boolean) value;
		} else if (null != value) {
			String stringValue = value.toString();
			boolValue = StringTools.stringToBoolean(stringValue);
			logger.warn(
					"Not sure if I handle string value [{}] for the field [{}] correctly, relying on Object.toString()",
					stringValue, name);
		}
		// TODO
		// logger.debug("value->[{}]", boolValue.toString());
		if (!sort) {
			document.add(new Field(name, null == boolValue ? "" : boolValue
					.toString(), Field.Store.NO, Field.Index.NOT_ANALYZED));
		} else {
			document.add(new Field(name, null == boolValue ? "" : boolValue
					.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		}

	}

	private void addIntIndexField(Document document, String name, Object value,
			boolean store, boolean sort) {
		Long longValue=null;
		if (value instanceof BigInteger) {
			longValue = ((BigInteger) value).longValue();
		} else if (value instanceof NodeInfo) {
			longValue = Long.parseLong(((NodeInfo) value).getStringValue());
		} else {

			logger.warn(
					"Not sure if I handle long value of [{}] for the field [{}] correctly, relying on Object.toString()",
					value.getClass().getName(), name);
		}
		// TODO
		// logger.debug("value->[{}]", longValue.toString());
		if (null != longValue) {
			// its more clear to divide the if statement in 3 parts
			if (sort) {
				document.add(new NumericField(name, Field.Store.YES, true)
						.setIntValue(longValue.intValue()));
			} else {
				if (!store) {
					document.add(new NumericField(name).setLongValue(longValue));
				} else {
					document.add(new NumericField(name, Field.Store.YES, true)
							.setIntValue(longValue.intValue()));
				}

			}
		} else {
			logger.warn("Long value of the field [{}] was null", name);
		}
	}

	private void addIndexDocument(IndexWriter iwriter, Document document) {
		try {
			iwriter.addDocument(document);
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	private void commitIndex(IndexWriter iwriter) {
		try {
			iwriter.optimize();
			iwriter.commit();
			iwriter.close();
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	private void commitIndex(IndexWriter iwriter, Map<String, String> map) {
		try {
			iwriter.optimize();
			iwriter.commit(map);
			iwriter.close();
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	
/*
	// I will generate the Lucene Index based on a XmlDatabase
	public void indexFromXmlDB_exp() {
		int countNodes = 0;
		String driverXml = "";
		String connectionString = "";
		Database db;
		Collection coll;
		Class<?> c;
		try {

			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
					.getInstance().getPreferences()
					.getConfSubset("ae.xmldatabase");

			if (null != connsConf) {
				driverXml = connsConf.getString("driver");
				connectionString = connsConf.getString("connectionstring");
			} else {
				logger.error("ae.xmldatabase Configuration is missing!!");
			}

			c = Class.forName(driverXml);

			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			coll = DatabaseManager.getCollection(connectionString);
			XPathQueryService service = (XPathQueryService) coll.getService(
					"XPathQueryService", "1.0");

			SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance()
					.getComponent("SaxonEngine");
			DocumentInfo source = null;
			// Loop through all result items

			// collect all the fields data
			Configuration config = ((SaxonEngine) Application
					.getAppComponent("SaxonEngine")).trFactory
					.getConfiguration();

			XPath xp = new XPathEvaluator(config);
			XPathExpression xpe = xp.compile(this.env.indexDocumentPath);

			for (FieldInfo field : this.env.fields.values()) {
				fieldXpe.put(field.name, xp.compile(field.path));
				logger.debug("Field Path->[{}]", field.path);
			}

			// create the index
			IndexWriter w = createIndex(this.env.indexDirectory,
					this.env.indexAnalyzer);

			// the xmldatabase is not very correct and have memory problem for
			// queires with huge results, so its necessary to implement our own
			// iteration mechanism

			// I will collect all the results
			ResourceSet set = service.query(this.env.indexDocumentPath);
			logger.debug("Number of results->" + set.getSize());
			long numberResults = set.getSize();

			set = null;
			long pageSizeDefault = 1000;
			long pageNumber = 1;

			String xml = "";
			while ((pageNumber * pageSizeDefault) <= (numberResults
					+ pageSizeDefault - 1)) {

				// calculate the last hit
				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
						: (numberResults - pageInit + 1);

				service = (XPathQueryService) coll.getService(
						"XPathQueryService", "1.0");

				// xquery paging using subsequence function
				
				XPath xp2;
				XPathExpression xpe2;
				List documentNodes;
				StringReader reader;
				int internali=0;
				while (internali<1000) {

					    Document d = new Document();
						for (FieldInfo field : this.env.fields.values()) {
						

								// Configuration config=doc.getConfiguration();
								// TODO: remove this test

							
								if (!field.name.equalsIgnoreCase("order")) {
										if ("integer".equals(field.type)) {
											addIntIndexField(d, field.name, 1,
													field.shouldStore,
													field.shouldSort);
										} else if ("date".equals(field.type)) {
											// todo: addDateIndexField(d,
											// field.name,
											// v);
											logger.error(
													"Date fields are not supported yet, field [{}] will not be created",
													field.name);
										} else if ("boolean".equals(field.type)) {
											addBooleanIndexField(d, field.name,
													true, field.shouldSort);
										} else {
											addIndexField(d, field.name, "teste",
													field.shouldAnalyze,
													field.shouldStore,
													field.shouldSort);
										}
	
								}
								// TODO: remove this
								else {
									addIntIndexField(d, field.name, new BigInteger(countNodes+""),
											field.shouldStore, field.shouldSort);
								}
							
						}
					
				

						documentNodes = null;
						source = null;
						reader = null;
						xml = null;
						countNodes++;
						internali++;
						// logger.debug("count->[{}]", countNodes);

						// add document to lucene index
						// TODO: rpe (an int just to make some tests);

						addIndexDocument(w, d);	
				
				}

				logger.debug("until now it were processed->[{}]",
						pageNumber * 1000);
				pageNumber++;
				// TODO: rpe (review this)
				// from 1000 to 1000 I will make a commit
				//w.commit();


	

			}

			
			this.env.setCountDocuments(countNodes);
			// add metadata to the lucene index
			Map<String, String> map = new HashMap<String, String>();
			map.put("numberDocs", Integer.toString(countNodes));
			map.put("date", Long.toString(System.nanoTime()));
			map.put("keyValidator", "ZZZZZZZZ");
			commitIndex(w, map);

		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

*/	
		
	
	
}
