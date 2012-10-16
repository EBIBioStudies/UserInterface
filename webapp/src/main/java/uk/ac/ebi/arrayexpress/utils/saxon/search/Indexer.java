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

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
import org.apache.lucene.store.FSDirectory;
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

	// keep information realted with attributes
	public class AttsInfo {
		public String name;
		public String type;

		public AttsInfo(String name, String type) {
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

	// no parameters menas that i will do all the work in the default database
	// (the one that is configured)
	public void indexFromXmlDB() throws Exception {

		String indexLocationDirectory = "";
		String dbHost = "";
		String dbPassword = "";
		String dbName = "";
		int dbPort = 0;

		// get the default location
		indexLocationDirectory = this.env.indexLocationDirectory;
		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");

		if (null != connsConf) {
			// connectionString = connsConf.getString("connectionstring");
			dbHost = connsConf.getString("host");
			dbPort = Integer.parseInt(connsConf.getString("port"));
			dbName = connsConf.getString("dbname");
			dbPassword = connsConf.getString("adminpassword");
		} else {
			logger.error("bs.xmldatabase Configuration is missing!!");
		}

		indexFromXmlDB(indexLocationDirectory, dbHost, dbPort, dbPassword,
				dbName);

	}

	public void indexFromXmlDB(String indexLocationDirectory,
			String dbHost, int dbPort, String dbPassword, String dbName)
			throws Exception {
		int countNodes = 0;
		String driverXml = "";
		String connectionString = "";
		Database db;
		Collection coll;
		Class<?> c;
		try {

			IndexWriter w = null;

			Directory indexTempDirectory = FSDirectory.open(new File(
					indexLocationDirectory, this.env.indexId));
			w = createIndex(indexTempDirectory, this.env.indexAnalyzer);

			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
					.getInstance().getPreferences()
					.getConfSubset("bs.xmldatabase");

			if (null != connsConf) {
				// TODO: rpe use the component XmlDatabasePooling
				driverXml = connsConf.getString("driver");
				// I will use the connectionString that was passed by parameter
				// (in several parameters)
				connectionString = connsConf.getString("base") + "://" + dbHost
						+ ":" + dbPort + "/" + dbName;
			} else {
				logger.error("bs.xmldatabase Configuration is missing!!");
			}

			//I cannot register this database again (this is already registered on XmlDbConnectionPool Component - java.nio.channels.OverlappingFileLockException 
			//c = Class.forName(driverXml);
			//db = (Database) c.newInstance();
			//DatabaseManager.registerDatabase(db);
			logger.debug("connectionString->" + connectionString);
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
			// XPathExpression xpe = xp.compile(this.env.indexDocumentPath);

			for (FieldInfo field : this.env.fields.values()) {
				fieldXpe.put(field.name, xp.compile(field.path));
				logger.debug("Field Path->[{}]", field.path);
			}

			// the xmldatabase is not very correct and have memory problem for
			// queires with huge results, so its necessary to implement our own
			// iteration mechanism
			//
			// // I will collect all the results
			// ResourceSet set = service.query(this.env.indexDocumentPath);
			// //TODO rpe
			// //ResourceSet set = service.query("//Sample");
			// logger.debug("Number of results->" + set.getSize());
			// long numberResults = set.getSize();
			long numberResults = 0;
			ResourceSet set = service.query("count("
					+ this.env.indexDocumentPath + ")");
			if (set.getIterator().hasMoreResources()) {
				numberResults = Integer.parseInt((String) set.getIterator()
						.nextResource().getContent());
			}
			logger.debug("Number of results->" + numberResults);
///			if (coll != null) {
///				try {
///					coll.close();
///				} catch (XMLDBException e) {
///					// TODO Auto-generated catch block
///					e.printStackTrace();
///				}
///			}
///			set = null;
///			db = null;
			// c=null;
			long pageSizeDefault = 50000;
			// the samplegroup cannot be big otherwise I will obtain a memory
			// error ... but the sample must b at least one million because the
			// paging queries are really slow - we need to balance it
			// (for samples 1million, for samplegroup 50000)
			if (numberResults > 1000000) {
				pageSizeDefault = 1000000;
			}

			long pageNumber = 1;
			int count = 0;
			Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
			Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
			Map<String, XPathExpression> cacheXpathAttValue = new HashMap<String, XPathExpression>();
			while ((pageNumber * pageSizeDefault) <= (numberResults
					+ pageSizeDefault - 1)) {
				// while ((pageNumber<=1)) {
				// calculate the last hit
				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
						: (numberResults - pageInit + 1);

				// c = Class.forName(driverXml);
///				db = (Database) c.newInstance();
///				DatabaseManager.registerDatabase(db);
///				coll = DatabaseManager.getCollection(connectionString);
				service = (XPathQueryService) coll.getService(
						"XPathQueryService", "1.0");

				// xquery paging using subsequence function
				long time = System.nanoTime();

				// /set =
				// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
				set = service.query("for $x in(subsequence("
						+ this.env.indexDocumentPath + "/@id," + pageInit + ","
						+ pageSize + ")) return string($x)");
				// logger.debug("Number of results of page->" + set.getSize());
				double ms = (System.nanoTime() - time) / 1000000d;
				logger.info("Query XMLDB took ->[{}]", ms);

				ResourceIterator iter = set.getIterator();
				XPath xp2;
				XPathExpression xpe2;
				List documentNodes;
				StringReader reader;
				// cache of distinct attributes fora each sample group

				while (iter.hasMoreResources()) {
					count++;
					logger.debug("its beeing processed the number ->" + count);
					String idSample = (String) iter.nextResource().getContent();
					logger.debug("idSample->" + idSample);
					// I need to get the sample
					ResourceSet setid = service
							.query(this.env.indexDocumentPath + "[@id='"
									+ idSample + "']");

					// System.out.println("/Biosamples/SampleGroup/Sample[@id='"
					// + idSample + "']");
					ResourceIterator iterid = setid.getIterator();
					while (iterid.hasMoreResources()) {
						// System.out.println("££££££££££££££££££££££££££££");
						// /xml=(String) iterid.nextResource().getContent();

						// /xml=(String) iter.nextResource().getContent();
						// logger.debug("xml->"+xml);
						// /reader = new StringReader(xml);
						StringBuilder xml = new StringBuilder();
						xml.append((String) iterid.nextResource().getContent());
						// logger.debug(xml.toString());
						reader = new StringReader(xml.toString());
						source = config.buildDocument(new StreamSource(reader));

						// logger.debug("XML DB->[{}]",
						// PrintUtils.printNodeInfo((NodeInfo) source, config));
						Document d = new Document();

						xp2 = new XPathEvaluator(source.getConfiguration());

						int position = env.indexDocumentPath.lastIndexOf("/");
						;
						String pathRoot = "";
						if (position != -1) {
							pathRoot = env.indexDocumentPath
									.substring(position);
						} else {
							pathRoot = env.indexDocumentPath;
						}
						// logger.debug("PathRoot->[{}]",pathRoot);
						xpe2 = xp2.compile(pathRoot);
						// TODO rpe
						// xpe2 = xp2.compile("/Sample");
						documentNodes = (List) xpe2.evaluate(source,
								XPathConstants.NODESET);

						for (Object node : documentNodes) {
							// logger.debug("XML£££££££££ DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
							for (FieldInfo field : this.env.fields.values()) {
								try {

									// Configuration
									// config=doc.getConfiguration();
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
												addIntIndexField(d, field.name,
														v, field.shouldStore,
														field.shouldSort);
											} else if ("date"
													.equals(field.type)) {
												// todo: addDateIndexField(d,
												// field.name,
												// v);
												logger.error(
														"Date fields are not supported yet, field [{}] will not be created",
														field.name);
											} else if ("boolean"
													.equals(field.type)) {
												addBooleanIndexField(d,
														field.name, v,
														field.shouldSort);
											} else {
												addIndexField(d, field.name, v,
														field.shouldAnalyze,
														field.shouldStore,
														field.shouldSort);
											}
										}

									} else {
										if (field.name
												.equalsIgnoreCase("attributes")) {
											// implement here the biosamples
											// database sample attributes logic
											// TODO: rpe
											// logger.debug("There is A special treatment for this field->"
											// + field.name);

											List values = (List) fieldXpe.get(
													field.name).evaluate(node,
													XPathConstants.NODESET);

											// XPathExpression
											// classAtt=xp.compile("@class");
											// XPathExpression
											// typeAtt=xp.compile("@dataType");
											// XPathExpression
											// valueAtt=xp.compile("value");
											String groupId = (String) fieldXpe
													.get("samplegroup")
													.evaluate(
															node,
															XPathConstants.STRING);
											String id = (String) fieldXpe.get(
													"accession").evaluate(node,
													XPathConstants.STRING);

											// logger.debug(groupId+"$$$" + id);

											// logger.debug("Field->[{}] values-> [{}]",
											// field.name,
											// values.toString());

											AttsInfo[] attsInfo = null;
											if (cacheAtt.containsKey(groupId)) {
												attsInfo = cacheAtt
														.get(groupId);
											} else {
												logger.debug("No exists cache for samplegroup->"
														+ groupId);
												// ResourceSet setAtt =
												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
												// + groupId +
												// "']/Sample/attribute[@dataType!='INTEGER']/replace(@class,' ', '-'))");
												// /ResourceSet setAtt =
												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
												// + groupId +
												// "']/Sample/attribute/replace(@class,' ', '-'))");
												// /ResourceSet setAtt =
												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
												// + groupId +
												// "']/Sample/attribute/@class)");
												ResourceSet setAtt = service
														.query("data(/Biosamples/SampleGroup[@id='"
																+ groupId
																+ "']/SampleAttributes/attribute/@class)");
												// logger.debug("££££££££££££££->"
												// +
												// "/Biosamples/SampleGroup[@id='"
												// + groupId +
												// "']/SampleAttributes/attribute/@class");

												ResourceIterator resAtt = setAtt
														.getIterator();
												int i = 0;
												attsInfo = new AttsInfo[(int) setAtt
														.getSize()];
												while (resAtt
														.hasMoreResources()) {
													String classValue = (String) resAtt
															.nextResource()
															.getContent();
													// logger.debug("££££££££££££££->"
													// + classValue);
													// need to use this because
													// of the use of quotes in
													// the name of the classes
													String classValueWitoutQuotes = classValue
															.replaceAll("\"",
																	"\"\"");
													// logger.debug("Class value->"
													// + classValue);
													XPathExpression xpathAtt = null;
													XPathExpression xpathAttValue = null;
													if (cacheXpathAtt
															.containsKey(classValue)) {
														xpathAtt = cacheXpathAtt
																.get(classValue);
														xpathAttValue = cacheXpathAttValue
																.get(classValue);
													} else {

														xpathAtt = xp
																.compile("./attribute[@class=\""
																		+ classValueWitoutQuotes
																		+ "\"]/@dataType");

														xpathAttValue = xp
																.compile("attribute[@class=\""
																		+ classValueWitoutQuotes
																		+ "\"]/value/text()[last()]");

														// logger.debug("attribute[@class=\""
														// +
														// classValueWitoutQuotes
														// +
														// "\"]//value/text()");
														// //xpathAttValue=xp.compile("./attribute[@class=\""
														// +
														// classValueWitoutQuotes
														// +
														// "\"]/value[1]/text()");
														// logger.debug("./attribute[@class=\""
														// +
														// classValueWitoutQuotes
														// +
														// "\"]/value[1]/text()");
														cacheXpathAtt.put(
																classValue,
																xpathAtt);
														cacheXpathAttValue.put(
																classValue,
																xpathAttValue);
													}
													// this doesnt work when the
													// first sample of sample
													// group doens have all the
													// attributes
													// im using \" becuse there
													// are some attributes thas
													// has ' on the name!!!
													// /ResourceSet setAttType =
													// service.query("string((/Biosamples/SampleGroup[@id='"
													// + groupId
													// +"']/Sample/attribute[@class=replace(\""
													// + classValueWitoutQuotes
													// +
													// "\",'-',' ')]/@dataType)[1])");
													// /ResourceSet setAttType =
													// service.query("string(/Biosamples/SampleGroup[@id='"
													// + groupId
													// +"']/Sample/attribute[@class=\""
													// + classValueWitoutQuotes
													// + "\"]/@dataType)");
													ResourceSet setAttType = service
															.query("data(/Biosamples/SampleGroup[@id='"
																	+ groupId
																	+ "']/SampleAttributes/attribute[@class=\""
																	+ classValueWitoutQuotes
																	+ "\"]/@dataType)");
													String dataValue = (String) setAttType
															.getIterator()
															.nextResource()
															.getContent();
													// logger.debug("Data Type of "
													// + classValue + " ->" +
													// dataValue);
													// String
													// dataValue=(String)xpathAtt.evaluate(node,
													// XPathConstants.STRING);
													AttsInfo attsI = new AttsInfo(
															classValue,
															dataValue);
													// logger.debug("Atttribute->class"
													// + attsI.name + "->type->"
													// + attsI.type + "->i" +
													// i);
													attsInfo[i] = attsI;
													// logger.debug("distinct att->"
													// + value);
													// cacheAtt.put(groupId,
													// value);
													i++;
												}
												cacheAtt.put(groupId, attsInfo);
												// distinctAtt=cacheAtt.get(groupId);
												// logger.debug("Already exists->"
												// + distinctAtt);
											}
											int len = attsInfo.length;
											for (int i = 0; i < len; i++) {
												// logger.debug("$$$$$$->" +
												// attsInfo[i].name + "$$$$" +
												// attsInfo[i].type);
												if (!attsInfo[i].type
														.equalsIgnoreCase("integer")
														&& !attsInfo[i].type
																.equalsIgnoreCase("real")) {

													XPathExpression valPath = cacheXpathAttValue
															.get(attsInfo[i].name);
													String val = (String) valPath
															.evaluate(
																	node,
																	XPathConstants.STRING);
													// logger.debug("$$$$$$->" +
													// "STRING->" + val + "££");
													addIndexField(d, (i + 1)
															+ "", val, true,
															false, true);
												} else {
													XPathExpression valPath = cacheXpathAttValue
															.get(attsInfo[i].name);
													String valS = (String) valPath
															.evaluate(
																	node,
																	XPathConstants.STRING);
													valS = valS.trim();
													// logger.debug("Integer->"
													// + valS);
													int val = 0;
													if (valS == null
															|| valS.equalsIgnoreCase("")
															|| valS.equalsIgnoreCase("NaN")) {
														valS = "0";
													}
													// sort numbers as strings
													// logger.debug("class->" +
													// attsInfo[i].name
													// +"value->##"+ valS +
													// "##");
													BigDecimal num = new BigDecimal(
															valS);
													num = num
															.multiply(new BigDecimal(
																	100));
													int taux = num
															.toBigInteger()
															.intValue();
													valS = String.format(
															"%07d", taux);
													// logger.debug("Integer->"
													// + valS + "position->"
													// +(i+1)+"integer");
													addIndexField(d, (i + 1)
															+ "", valS, true,
															false, true);
													// addIntIndexField(d,
													// (i+1)+"integer", new
													// BigInteger(valS),false,
													// true);
													//
												}
											}

										} else {
											// logger.debug("There is NO special treatment for this field->"
											// + field.name);
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

						addIndexDocument(w, d);

					}
				}
				logger.debug("until now it were processed->[{}]", pageNumber
						* pageSizeDefault);
				pageNumber++;
				if (coll != null) {
					try {
						//coll.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				set = null;
				db = null;

			}

			this.env.setCountDocuments(countNodes);
			// add metadata to the lucene index
			Map<String, String> map = new HashMap<String, String>();
			map.put("numberDocs", Integer.toString(countNodes));
			map.put("date", Long.toString(System.nanoTime()));
			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
			// I cannot call directly
			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
			// because I can be working in a did
			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
					dbPort, dbPassword, dbName);

			map.put("DBInfo", dbInfo);
			commitIndex(w, map);

		} catch (Exception x) {
			logger.error("Caught an exception:", x);
			throw x;
		}
	}

	public void indexReader() {
		env.indexReader();
	}

	private IndexWriter createIndex(Directory indexDirectory, Analyzer analyzer) {
		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(indexDirectory, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
			// TODO: just to check if it solves the slowly indexing indexes with
			// more
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
		Long longValue = null;
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
//		logger.debug( "field [{}] value->[{}]", name, longValue.toString());
//		logger.debug( "field [{}] store->[{}]", name, store);
//		logger.debug( "field [{}] sort->[{}]", name, sort);
		if (null != longValue) {
			// its more clear to divide the if statement in 3 parts
			if (sort) {
				//It has to be int because of sorting (otherwise the error: Invalid shift value in prefixCoded string (is encoded value really an INT?)) 
				document.add(new NumericField(name, Field.Store.YES, true)
						.setLongValue(longValue));
			} else {
				if (!store) {
					document.add(new NumericField(name).setLongValue(longValue));
				} else {
					document.add(new NumericField(name, Field.Store.YES, true)
							.setLongValue(longValue));
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
	 * // I will generate the Lucene Index based on a XmlDatabase public void
	 * indexFromXmlDB_exp() { int countNodes = 0; String driverXml = ""; String
	 * connectionString = ""; Database db; Collection coll; Class<?> c; try {
	 * 
	 * HierarchicalConfiguration connsConf = (HierarchicalConfiguration)
	 * Application .getInstance().getPreferences()
	 * .getConfSubset("ae.xmldatabase");
	 * 
	 * if (null != connsConf) { driverXml = connsConf.getString("driver");
	 * connectionString = connsConf.getString("connectionstring"); } else {
	 * logger.error("ae.xmldatabase Configuration is missing!!"); }
	 * 
	 * c = Class.forName(driverXml);
	 * 
	 * db = (Database) c.newInstance(); DatabaseManager.registerDatabase(db);
	 * coll = DatabaseManager.getCollection(connectionString); XPathQueryService
	 * service = (XPathQueryService) coll.getService( "XPathQueryService",
	 * "1.0");
	 * 
	 * SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance()
	 * .getComponent("SaxonEngine"); DocumentInfo source = null; // Loop through
	 * all result items
	 * 
	 * // collect all the fields data Configuration config = ((SaxonEngine)
	 * Application .getAppComponent("SaxonEngine")).trFactory
	 * .getConfiguration();
	 * 
	 * XPath xp = new XPathEvaluator(config); XPathExpression xpe =
	 * xp.compile(this.env.indexDocumentPath);
	 * 
	 * for (FieldInfo field : this.env.fields.values()) {
	 * fieldXpe.put(field.name, xp.compile(field.path));
	 * logger.debug("Field Path->[{}]", field.path); }
	 * 
	 * // create the index IndexWriter w = createIndex(this.env.indexDirectory,
	 * this.env.indexAnalyzer);
	 * 
	 * // the xmldatabase is not very correct and have memory problem for //
	 * queires with huge results, so its necessary to implement our own //
	 * iteration mechanism
	 * 
	 * // I will collect all the results ResourceSet set =
	 * service.query(this.env.indexDocumentPath);
	 * logger.debug("Number of results->" + set.getSize()); long numberResults =
	 * set.getSize();
	 * 
	 * set = null; long pageSizeDefault = 1000; long pageNumber = 1;
	 * 
	 * String xml = ""; while ((pageNumber * pageSizeDefault) <= (numberResults
	 * + pageSizeDefault - 1)) {
	 * 
	 * // calculate the last hit long pageInit = (pageNumber - 1) *
	 * pageSizeDefault + 1; long pageSize = (pageNumber * pageSizeDefault <
	 * numberResults) ? pageSizeDefault : (numberResults - pageInit + 1);
	 * 
	 * service = (XPathQueryService) coll.getService( "XPathQueryService",
	 * "1.0");
	 * 
	 * // xquery paging using subsequence function
	 * 
	 * XPath xp2; XPathExpression xpe2; List documentNodes; StringReader reader;
	 * int internali=0; while (internali<1000) {
	 * 
	 * Document d = new Document(); for (FieldInfo field :
	 * this.env.fields.values()) {
	 * 
	 * 
	 * // Configuration config=doc.getConfiguration(); // TODO: remove this test
	 * 
	 * 
	 * if (!field.name.equalsIgnoreCase("order")) { if
	 * ("integer".equals(field.type)) { addIntIndexField(d, field.name, 1,
	 * field.shouldStore, field.shouldSort); } else if
	 * ("date".equals(field.type)) { // todo: addDateIndexField(d, //
	 * field.name, // v); logger.error(
	 * "Date fields are not supported yet, field [{}] will not be created",
	 * field.name); } else if ("boolean".equals(field.type)) {
	 * addBooleanIndexField(d, field.name, true, field.shouldSort); } else {
	 * addIndexField(d, field.name, "teste", field.shouldAnalyze,
	 * field.shouldStore, field.shouldSort); }
	 * 
	 * } // TODO: remove this else { addIntIndexField(d, field.name, new
	 * BigInteger(countNodes+""), field.shouldStore, field.shouldSort); }
	 * 
	 * }
	 * 
	 * 
	 * 
	 * documentNodes = null; source = null; reader = null; xml = null;
	 * countNodes++; internali++; // logger.debug("count->[{}]", countNodes);
	 * 
	 * // add document to lucene index // TODO: rpe (an int just to make some
	 * tests);
	 * 
	 * addIndexDocument(w, d);
	 * 
	 * }
	 * 
	 * logger.debug("until now it were processed->[{}]", pageNumber * 1000);
	 * pageNumber++; // TODO: rpe (review this) // from 1000 to 1000 I will make
	 * a commit //w.commit();
	 * 
	 * 
	 * 
	 * 
	 * }
	 * 
	 * 
	 * this.env.setCountDocuments(countNodes); // add metadata to the lucene
	 * index Map<String, String> map = new HashMap<String, String>();
	 * map.put("numberDocs", Integer.toString(countNodes)); map.put("date",
	 * Long.toString(System.nanoTime())); map.put("keyValidator", "ZZZZZZZZ");
	 * commitIndex(w, map);
	 * 
	 * } catch (Exception x) { logger.error("Caught an exception:", x); } }
	 */

	// I will generate the Lucene Index based on a XmlDatabase
	// the indexLocationDirectory parameter tells me if I will create the index
	// in a different directory(we have a parametrized directory, but we may
	// need t define a new one because we dont want to avoid users access during
	// the generation of the new index - when o reload job is running)
	public void indexFromXmlDB_newapproach_18092102(
			String indexLocationDirectory, String dbHost, int dbPort,
			String dbPassword, String dbName) throws Exception {
		int countNodes = 0;
		String driverXml = "";
		String connectionString = "";
		Database db;
		Collection coll;
		Class<?> c;
		try {

			IndexWriter w = null;
			Directory indexTempDirectory = FSDirectory.open(new File(
					indexLocationDirectory, this.env.indexId));
			w = createIndex(indexTempDirectory, this.env.indexAnalyzer);

			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
					.getInstance().getPreferences()
					.getConfSubset("bs.xmldatabase");

			if (null != connsConf) {
				// TODO: rpe use the component XmlDatabasePooling
				driverXml = connsConf.getString("driver");
				// I will use the connectionString that was passed by parameter
				// (in several parameters)
				connectionString = connsConf.getString("base") + "://" + dbHost
						+ ":" + dbPort + "/" + dbName;
			} else {
				logger.error("bs.xmldatabase Configuration is missing!!");
			}

			c = Class.forName(driverXml);
			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			logger.debug("connectionString->" + connectionString);
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
			// XPathExpression xpe = xp.compile(this.env.indexDocumentPath);

			for (FieldInfo field : this.env.fields.values()) {
				fieldXpe.put(field.name, xp.compile(field.path));
				logger.debug("Field Path->[{}]", field.path);
			}

			// the xmldatabase is not very correct and have memory problem for
			// queires with huge results, so its necessary to implement our own
			// iteration mechanism
			//
			// // I will collect all the results
			// ResourceSet set = service.query(this.env.indexDocumentPath);
			// //TODO rpe
			// //ResourceSet set = service.query("//Sample");
			// logger.debug("Number of results->" + set.getSize());
			// long numberResults = set.getSize();

			ResourceSet setIds = service.query("//Sample/@id/string(.)");
			// /ArrayList<String> res=new ArrayList();
			String[] res = new String[(int) setIds.getSize() + 2];
			System.out.println("res->" + res.length);
			int ci = 0;
			while (setIds.getIterator().hasMoreResources()) {
				// /res.add((String)
				// set.getIterator().nextResource().getContent());

				// }

				// /long numberResults = res.size();
				long numberResults = setIds.getSize();

				logger.debug("Number of results->" + numberResults);
				// if (coll != null) {
				// try {
				// coll.close();
				// } catch (XMLDBException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

				long pageSizeDefault = 50000;
				// the samplegroup cannot be big otherwise I will obtain a
				// memory error ... but the sample must b at least one million
				// because the paging queries are really slow - we need to
				// balance it
				// (for samples 1million, for samplegroup 50000)
				if (numberResults > 1000000) {
					pageSizeDefault = 1000000;
				}

				long pageNumber = 1;
				int count = 0;
				Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
				Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
				Map<String, XPathExpression> cacheXpathAttValue = new HashMap<String, XPathExpression>();
				while ((pageNumber * pageSizeDefault) <= (numberResults
						+ pageSizeDefault - 1)) {
					long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
					long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
							: (numberResults - pageInit + 1);

					long time = System.nanoTime();
					// Strign
					StringBuilder totalRes = new StringBuilder();
					int finalExp = (int) ((pageInit - 1) + (pageSize));
					totalRes.append("(");
					for (int i = (int) (pageInit - 1); i < finalExp; i++) {

						// /totalRes.append("'" + res.get(i) + "'");
						// //totalRes.append("'" + res[i] + "'");
						totalRes.append("'"
								+ setIds.getIterator().nextResource()
										.getContent() + "'");

						ci++;
						// totalRes.append("'" + doc.get("id") + "'");
						if (i != (finalExp - 1)) {
							totalRes.append(",");
						}
					}
					totalRes.append(")");

					// /set =
					// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
					ResourceSet set = service.query("//Sample[@id="
							+ totalRes.toString() + "]");
					// logger.debug("Number of results of page->" +
					// set.getSize());
					double ms = (System.nanoTime() - time) / 1000000d;
					logger.info("Query XMLDB took ->[{}]", ms);

					ResourceIterator iter = set.getIterator();
					XPath xp2;
					XPathExpression xpe2;
					List documentNodes;
					StringReader reader;
					// cache of distinct attributes fora each sample group

					while (iter.hasMoreResources()) {
						count++;
						logger.debug("its beeing processed the number ->"
								+ count);
						StringBuilder xml = new StringBuilder();
						xml.append((String) iter.nextResource().getContent());
						// logger.debug(xml.toString());
						reader = new StringReader(xml.toString());
						source = config.buildDocument(new StreamSource(reader));

						// logger.debug("XML DB->[{}]",
						// PrintUtils.printNodeInfo((NodeInfo) source, config));
						Document d = new Document();

						xp2 = new XPathEvaluator(source.getConfiguration());

						int position = env.indexDocumentPath.lastIndexOf("/");
						;
						String pathRoot = "";
						if (position != -1) {
							pathRoot = env.indexDocumentPath
									.substring(position);
						} else {
							pathRoot = env.indexDocumentPath;
						}
						// logger.debug("PathRoot->[{}]",pathRoot);
						xpe2 = xp2.compile(pathRoot);
						// TODO rpe
						// xpe2 = xp2.compile("/Sample");
						documentNodes = (List) xpe2.evaluate(source,
								XPathConstants.NODESET);

						for (Object node : documentNodes) {
							// logger.debug("XML£££££££££ DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
							for (FieldInfo field : this.env.fields.values()) {
								try {

									// Configuration
									// config=doc.getConfiguration();
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
												addIntIndexField(d, field.name,
														v, field.shouldStore,
														field.shouldSort);
											} else if ("date"
													.equals(field.type)) {
												// todo: addDateIndexField(d,
												// field.name,
												// v);
												logger.error(
														"Date fields are not supported yet, field [{}] will not be created",
														field.name);
											} else if ("boolean"
													.equals(field.type)) {
												addBooleanIndexField(d,
														field.name, v,
														field.shouldSort);
											} else {
												addIndexField(d, field.name, v,
														field.shouldAnalyze,
														field.shouldStore,
														field.shouldSort);
											}
										}

									} else {
										if (field.name
												.equalsIgnoreCase("attributes")) {
											// implement here the biosamples
											// database sample attributes logic
											// TODO: rpe
											// logger.debug("There is A special treatment for this field->"
											// + field.name);

											List values = (List) fieldXpe.get(
													field.name).evaluate(node,
													XPathConstants.NODESET);

											// XPathExpression
											// classAtt=xp.compile("@class");
											// XPathExpression
											// typeAtt=xp.compile("@dataType");
											// XPathExpression
											// valueAtt=xp.compile("value");
											String groupId = (String) fieldXpe
													.get("samplegroup")
													.evaluate(
															node,
															XPathConstants.STRING);
											String id = (String) fieldXpe.get(
													"accession").evaluate(node,
													XPathConstants.STRING);

											// logger.debug(groupId+"$$$" + id);

											// logger.debug("Field->[{}] values-> [{}]",
											// field.name,
											// values.toString());

											AttsInfo[] attsInfo = null;
											if (cacheAtt.containsKey(groupId)) {
												attsInfo = cacheAtt
														.get(groupId);
											} else {
												logger.debug("No exists cache for samplegroup->"
														+ groupId);
												ResourceSet setAtt = service
														.query("data(/Biosamples/SampleGroup[@id='"
																+ groupId
																+ "']/SampleAttributes/attribute/@class)");
												// logger.debug("££££££££££££££->"
												// +
												// "/Biosamples/SampleGroup[@id='"
												// + groupId +
												// "']/SampleAttributes/attribute/@class");

												ResourceIterator resAtt = setAtt
														.getIterator();
												int i = 0;
												attsInfo = new AttsInfo[(int) setAtt
														.getSize()];
												while (resAtt
														.hasMoreResources()) {
													String classValue = (String) resAtt
															.nextResource()
															.getContent();
													// logger.debug("££££££££££££££->"
													// + classValue);
													// need to use this because
													// of the use of quotes in
													// the name of the classes
													String classValueWitoutQuotes = classValue
															.replaceAll("\"",
																	"\"\"");
													// logger.debug("Class value->"
													// + classValue);
													XPathExpression xpathAtt = null;
													XPathExpression xpathAttValue = null;
													if (cacheXpathAtt
															.containsKey(classValue)) {
														xpathAtt = cacheXpathAtt
																.get(classValue);
														xpathAttValue = cacheXpathAttValue
																.get(classValue);
													} else {

														xpathAtt = xp
																.compile("./attribute[@class=\""
																		+ classValueWitoutQuotes
																		+ "\"]/@dataType");

														xpathAttValue = xp
																.compile("attribute[@class=\""
																		+ classValueWitoutQuotes
																		+ "\"]/value/text()[last()]");

														cacheXpathAtt.put(
																classValue,
																xpathAtt);
														cacheXpathAttValue.put(
																classValue,
																xpathAttValue);
													}
													ResourceSet setAttType = service
															.query("data(/Biosamples/SampleGroup[@id='"
																	+ groupId
																	+ "']/SampleAttributes/attribute[@class=\""
																	+ classValueWitoutQuotes
																	+ "\"]/@dataType)");
													String dataValue = (String) setAttType
															.getIterator()
															.nextResource()
															.getContent();
													// logger.debug("Data Type of "
													// + classValue + " ->" +
													// dataValue);
													// String
													// dataValue=(String)xpathAtt.evaluate(node,
													// XPathConstants.STRING);
													AttsInfo attsI = new AttsInfo(
															classValue,
															dataValue);
													// logger.debug("Atttribute->class"
													// + attsI.name + "->type->"
													// + attsI.type + "->i" +
													// i);
													attsInfo[i] = attsI;
													// logger.debug("distinct att->"
													// + value);
													// cacheAtt.put(groupId,
													// value);
													i++;
												}
												cacheAtt.put(groupId, attsInfo);
												// distinctAtt=cacheAtt.get(groupId);
												// logger.debug("Already exists->"
												// + distinctAtt);
											}
											int len = attsInfo.length;
											for (int i = 0; i < len; i++) {
												// logger.debug("$$$$$$->" +
												// attsInfo[i].name + "$$$$" +
												// attsInfo[i].type);
												if (!attsInfo[i].type
														.equalsIgnoreCase("integer")
														&& !attsInfo[i].type
																.equalsIgnoreCase("real")) {

													XPathExpression valPath = cacheXpathAttValue
															.get(attsInfo[i].name);
													String val = (String) valPath
															.evaluate(
																	node,
																	XPathConstants.STRING);
													// logger.debug("$$$$$$->" +
													// "STRING->" + val + "££");
													addIndexField(d, (i + 1)
															+ "", val, false,
															false, true);
												} else {
													XPathExpression valPath = cacheXpathAttValue
															.get(attsInfo[i].name);
													String valS = (String) valPath
															.evaluate(
																	node,
																	XPathConstants.STRING);
													valS = valS.trim();
													// logger.debug("Integer->"
													// + valS);
													int val = 0;
													if (valS == null
															|| valS.equalsIgnoreCase("")
															|| valS.equalsIgnoreCase("NaN")) {
														valS = "0";
													}
													// sort numbers as strings
													// logger.debug("class->" +
													// attsInfo[i].name
													// +"value->##"+ valS +
													// "##");
													BigDecimal num = new BigDecimal(
															valS);
													num = num
															.multiply(new BigDecimal(
																	100));
													int taux = num
															.toBigInteger()
															.intValue();
													valS = String.format(
															"%07d", taux);
													// logger.debug("Integer->"
													// + valS + "position->"
													// +(i+1)+"integer");
													addIndexField(d, (i + 1)
															+ "", valS, false,
															false, true);
													// addIntIndexField(d,
													// (i+1)+"integer", new
													// BigInteger(valS),false,
													// true);
													//
												}
											}

										} else {
											// logger.debug("There is NO special treatment for this field->"
											// + field.name);
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

						addIndexDocument(w, d);

					}
				}
				logger.debug("until now it were processed->[{}]", pageNumber
						* pageSizeDefault);
				pageNumber++;
				if (coll != null) {
					try {
						coll.close();
					} catch (XMLDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				setIds = null;
				db = null;
			}

			this.env.setCountDocuments(countNodes);
			// add metadata to the lucene index
			Map<String, String> map = new HashMap<String, String>();
			map.put("numberDocs", Integer.toString(countNodes));
			map.put("date", Long.toString(System.nanoTime()));
			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
			// I cannot call directly
			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
			// because I can be working in a did
			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
					dbPort, dbPassword, dbName);

			map.put("DBInfo", dbInfo);
			commitIndex(w, map);

		} catch (Exception x) {
			logger.error("Caught an exception:", x);
			throw x;
		}
	}

}
