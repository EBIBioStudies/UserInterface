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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;

public abstract class AbstractIndexEnvironment {

	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// source index configuration (will be eventually removed)
	public HierarchicalConfiguration indexConfig;

	// index configuration, parsed
	public String indexId;
	public Directory indexDirectory;
	//I need this to create an temporary directory during the relod job execution
	public String indexLocationDirectory;
	public PerFieldAnalyzerWrapper indexAnalyzer;
	public String defaultField;
	
	//I will not open the index in each request
	private IndexReader ir = null;

	// index document xpath
	public String indexDocumentPath;

	// number of documents indexed
	private int countDocuments;

	public int getCountDocuments() {
		return countDocuments;
	}

	public void setCountDocuments(int count) {
		this.countDocuments = count;
	}

	public String getDefaultField() {
		return defaultField;
	}

	//TODO: rpe (review this)
	private IndexReader getIndexReader(){
		if(ir==null){
			synchronized(this){
				try {
					//logger.debug("test");
					ir = IndexReader.open(this.indexDirectory, true);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		return ir;

	}

	
	
	public void closeIndexReader(){
		if(ir!=null){
			try {
					logger.debug("Close the closeIndexReader!!!");
					ir.close();
					ir=null;
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}

	}
	
	//TODO: rpe Just to test
//	private void closeIndexReader(){
//		ir=null;
//	}
	
	public void setDefaultField(String defaultField) {
		this.defaultField = defaultField;
	}

	public String getDefaultSortField() {
		return defaultSortField;
	}

	public void setDefaultSortField(String defaultSortField) {
		this.defaultSortField = defaultSortField;
	}

	public boolean getDefaultSortDescending() {
		return defaultSortDescending;
	}

	public void setDefaultSortDescending(boolean defaultSortDescending) {
		this.defaultSortDescending = defaultSortDescending;
	}

	public int getDefaultPageSize() {
		return defaultPageSize;
	}

	public void setDefaultPageSize(int defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	/**
	 * Default field used to sort if anyone is specified
	 */
	protected String defaultSortField = "releasedate";

	/**
	 * Default orientation (Ascending)
	 */
	protected boolean defaultSortDescending = false;

	/**
	 * Default page size
	 */
	protected int defaultPageSize = 25;

	public Map<String, FieldInfo> fields;

	// document info
	public int documentHashCode;

	public AbstractIndexEnvironment(HierarchicalConfiguration indexConfig) {
		this.indexConfig = indexConfig;
		populateIndexConfiguration();
		setup();
	}

	private void populateIndexConfiguration() {
		try {
			this.indexId = this.indexConfig.getString("[@id]");

			indexLocationDirectory = this.indexConfig
					.getString("[@location]");
			this.indexDirectory = FSDirectory.open(new File(indexLocationDirectory,
					this.indexId));
			String indexAnalyzer = this.indexConfig
					.getString("[@defaultAnalyzer]");
			Analyzer a = (Analyzer) Class.forName(indexAnalyzer).newInstance();
			this.indexAnalyzer = new PerFieldAnalyzerWrapper(a);

			this.indexDocumentPath = indexConfig.getString("document[@path]");

			this.defaultField = indexConfig
					.getString("document[@defaultField]");

			List fieldsConfig = indexConfig.configurationsAt("document.field");

			this.fields = new HashMap<String, FieldInfo>();
			for (Object fieldConfig : fieldsConfig) {
				FieldInfo fieldInfo = new FieldInfo(
						(HierarchicalConfiguration) fieldConfig);
				fields.put(fieldInfo.name, fieldInfo);
				if (null != fieldInfo.analyzer) {
					Analyzer fa = (Analyzer) Class.forName(fieldInfo.analyzer)
							.newInstance();
					this.indexAnalyzer.addAnalyzer(fieldInfo.name, fa);
				}
			}

		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	public boolean doesFieldExist(String fieldName) {
		return fields.containsKey(fieldName);
	}

	/*
	 * (non-Javadoc) This is the mains function of this classe and it will
	 * address the query, sort and paging issues
	 * 
	 * @see
	 * uk.ac.ebi.arrayexpress.utils.saxon.search.IIndexEnvironment#queryPaged
	 * (java.lang.Integer, uk.ac.ebi.arrayexpress.utils.saxon.search.QueryInfo,
	 * uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap)
	 */
	public String queryPaged(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException {
//		IndexReader ir = null;

		IndexSearcher isearcher = null;
		if (logger.isDebugEnabled()) {
			logger.debug("start of queryPaged");
		}
		StringBuilder totalRes = new StringBuilder();
		totalRes.append("<content>");
		Query query = info.getQuery();
		try {
			ir=getIndexReader();
			if (query instanceof BooleanQuery
					&& ((BooleanQuery) query).clauses().isEmpty()) {
				logger.info("Empty search, returned all [{}] documents",
						getCountDocuments());
				// this is much more faster
				query = new MatchAllDocsQuery();
			}

			isearcher = new IndexSearcher(ir);
			boolean descending = getDefaultSortDescending();
			;
			String sortBy = StringTools.arrayToString(map.get("sortby"), " ");
			if (sortBy == null || sortBy.equalsIgnoreCase("")) {
				sortBy = getDefaultSortField();
			}
			String sortOrder = StringTools.arrayToString(map.get("sortorder"),
					" ");

			if (sortOrder != null) {
				if (sortOrder.equalsIgnoreCase("ascending")) {
					descending = false;
				} else {
					descending = true;
				}
			}
		
			// I have to test the sort field name. If it is a string i have to
			// add "sort" to the name
			// I will only sort if I have a Field
			// TopDocs hits;
			ScoreDoc[] hits = null;
			Sort sort = null;
			//logger.debug("test");
			if (doesFieldExist(sortBy)) {
				FieldInfo sortField = fields.get(sortBy);
				if (sortField == null) {
					logger.info(
							"A sort field is trying to be used but that field is not defined! ->[{}]",
							sortBy);
				}

				int sortFieldsSize = sortField.sortFields != null ? sortField.sortFields
						.size() : 0;
				SortField[] sortFieldArray = new SortField[sortFieldsSize];
				if (sortFieldsSize > 0) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < sortField.sortFields.size(); i++) {
						FieldInfo otherSortField = fields
								.get(sortField.sortFields.get(i));

						if (otherSortField == null) {
							logger.info(
									"Other sort field is trying to be used but that field is not defined! ->[{}]",
									sortField.sortFields.get(i));
						} else {
							String sortByName = otherSortField.name;
							int descendingType = SortField.STRING_VAL;
							sb.append("new sortField ->")
									.append(otherSortField.name).append("; ");
							if (otherSortField.name.equalsIgnoreCase(sortBy)
									&& otherSortField.type
											.equalsIgnoreCase("string")) {
								sortByName += "sort";
							} else {
								if (otherSortField.type
										.equalsIgnoreCase("integer")) {
									descendingType = SortField.LONG;
								}
							}

							sortFieldArray[i] = new SortField(sortByName,
									descendingType, descending);

						}
					}
					logger.debug("Query sorted by: ->[{}] descending: ->[{}]",
							sb.toString(), descending);
				}

				sort = new Sort(sortFieldArray);
			} else {
				logger.info(
						"Sort query field [{}] doenst exist or the SortBy parameter was not specified",
						sortBy);
			}

			int pageSize = defaultPageSize;
			if (map.containsKey("pagesize")) {
				pageSize = Integer.parseInt(StringTools.arrayToString(
						map.get("pagesize"), " "));
			} else {
				pageSize = getDefaultPageSize();
				map.put("pagesize", Integer.toString(pageSize));
			}

			int page = 0;
			if (map.containsKey("page")) {
				page = Integer.parseInt(StringTools.arrayToString(
						map.get("page"), " ")) - 1;
			}

			int initialExp = page * pageSize;
			int finalExp = initialExp + pageSize;

			// I will execute the same query with or without Sortby parameter
			// (in the last case the sort will be null)
			///TopFieldCollector collectorAux = null;
			TopFieldCollector collector = null;
			int numHits = getCountDocuments() + 1;
collector = TopFieldCollector.create(sort == null ? new Sort()
					: sort,
		///	collectorAux = TopFieldCollector.create(sort == null ? new Sort()
			///		: sort,
			// TODO: rpe If im returning page 3 using pagesize of 50 i need to sort (3*50)
					(page == 0 ? 1 : page + 1) * pageSize, false, // fillFields
																	// - not
																	// needed,
																	// we want
																	// score and
																	// doc only
					false, // trackDocScores - need doc and score fields
					false, // trackMaxScore - related to trackDocScores
					sort == null); // should docs be in docId order?
			
			///TopFieldCollectorReference collector= new TopFieldCollectorReference(collectorAux);
			isearcher.search(query, collector);
			//I will use this Collector to know how much results do i have
			long timeHits=System.nanoTime();
			TotalHitCountCollector collector2 = new TotalHitCountCollector();		
			///TotalHitCountCollectorReference collector2 = new TotalHitCountCollectorReference();
			isearcher.search(query, collector2);
			double ms = (System.nanoTime() - timeHits) / 1000000d;
			logger.info("Number of Docs TotalHitCountCollector->" + collector2.getTotalHits() + "- TOTALHITS TOOK->" + ms );
			int totalHits= collector2.getTotalHits();

			TopDocs topDocs = collector.topDocs();
			// hits= topDocs.scoreDocs;
			hits = topDocs.scoreDocs;

			logger.info("Search of index [" + this.indexId
					+ "] with query [{}] returned [{}] hits", query.toString(),
					hits.length);

			logger.info("Beginning of paging logic");

			if (finalExp > hits.length) {
				finalExp = hits.length;
			}

			List<String> combinedTotal = new ArrayList<String>();
			combinedTotal.add(String.valueOf(totalHits));

			map.put("total",
					combinedTotal.toArray(new String[combinedTotal.size()]));

			logger.info(
					"End of paging logic, requesting data from [{}] to [{}]",
					initialExp, finalExp);
			long time = System.nanoTime();
			if (logger.isDebugEnabled()) {
				logger.debug("Requesting data from xml database");
			}
			// this QueryDB should be implemented by all subclasses and is
			// responsible for the data collecting
			totalRes.append(queryDB(hits, isearcher, initialExp, finalExp, map));
			if (logger.isDebugEnabled()) {
				logger.debug("End of requesting data from xml database");
			}

			isearcher.close();
			///ir.close();
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			if (null != isearcher)
				isearcher.close();
//			if (null != ir)
//				ir.close();
		}

		totalRes.append("</content>");
		if (logger.isDebugEnabled()) {
			logger.debug("End of QueryPaged");
		}
		return totalRes.toString();
	}

	/**
	 * @param hits this just represents a subset of the result
	 * @param TotalHits
	 * @param isearcher
	 * @param initialExp
	 * @param finalExp
	 * @param map
	 * @return
	 * @throws Exception
	 */
	abstract public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
			int initialExp, int finalExp, HttpServletRequestParameterMap map)
			throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.arrayexpress.utils.saxon.search.IIndexEnvironment#queryAllDocs
	 * (java.lang.Integer, uk.ac.ebi.arrayexpress.utils.saxon.search.QueryInfo,
	 * uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap)
	 */
	public ScoreDoc[] queryAllDocs(Integer queryId, QueryInfo info,
			HttpServletRequestParameterMap map) throws IOException {
		IndexReader ir = null;
		IndexSearcher isearcher = null;
		Query query = info.getQuery();
		ScoreDoc[] hits = null;
		try {
			ir = IndexReader.open(this.indexDirectory, true);

			// empty query returns everything
			if (query instanceof BooleanQuery
					&& ((BooleanQuery) query).clauses().isEmpty()) {
				logger.info(
						"queryAllDocs Empty search, returned all [{}] documents",
						getCountDocuments());
				// I need to continue because e i need to sort the data, so I
				// will create an empty query (this happens when I'm a curator
				// and I dont have any search criteria)
				// Term t = new Term(defaultField, "*");
				// ((BooleanQuery) query).add(new BooleanClause(new
				// WildcardQuery(
				// t), BooleanClause.Occur.SHOULD));

				// this is much more faster
				query = new MatchAllDocsQuery();
			}

			// to show _all_ available nodes
			isearcher = new IndexSearcher(ir);
			// +1 is a trick to prevent from having an exception thrown if
			// documentNodes.size() value is 0
			boolean descending = true;

			String sortBy = StringTools.arrayToString(map.get("sortby"), " ");
			if (sortBy != null && sortBy.equalsIgnoreCase("")) {
				sortBy = getDefaultSortField();
			}
			String sortOrder = StringTools.arrayToString(map.get("sortorder"),
					" ");

			if (sortOrder != null) {
				if (sortOrder.equalsIgnoreCase("ascending")) {
					descending = false;
				} else {
					descending = true;
				}
			}

			int sortFieldType = SortField.INT;
			// I have to test the sort field name. If it is a string i have to
			// add "sort" to the name
			// I will only sort if I have a Field
			Sort sort = null;
			if (!sortBy.equalsIgnoreCase("") && doesFieldExist(sortBy)) {
				FieldInfo sortField = fields.get(sortBy);
				if (sortField == null) {
					logger.info(
							"A sort field is trying to be used but that field is not defined! ->[{}]",
							sortBy);
				}

				int sortFieldsSize = sortField.sortFields != null ? sortField.sortFields
						.size() : 0;
				SortField[] sortFieldArray = new SortField[sortFieldsSize];
				// sortFieldArray[0]=new SortField(sortBy, sortFieldType,
				// descending);
				if (sortFieldsSize > 0) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < sortField.sortFields.size(); i++) {
						FieldInfo otherSortField = fields
								.get(sortField.sortFields.get(i));

						if (otherSortField == null) {
							logger.info(
									"Other sort field is trying to be used but that field is not defined! ->[{}]",
									sortField.sortFields.get(i));
						} else {
							String sortByName = otherSortField.name;
							int descendingType = SortField.STRING_VAL;
							sb.append("new sortField ->")
									.append(otherSortField.name).append("; ");
							if (otherSortField.name.equalsIgnoreCase(sortBy)
									&& otherSortField.type
											.equalsIgnoreCase("string")) {
								sortByName += "sort";
							} else {
								if (otherSortField.type
										.equalsIgnoreCase("integer")) {
									descendingType = SortField.INT;
								}
							}

							sortFieldArray[i] = new SortField(sortByName,
									descendingType, descending);

						}
					}
					logger.info("Query sorted by: ->[{}]", sb.toString());
				}

				sort = new Sort(sortFieldArray);

				// hits = isearcher.search(query, getCountDocuments() + 1,
				// sort);
			} else {
				// hits = isearcher.search(query, getCountDocuments() + 1);
				logger.info(
						"Sort query field [{}] doenst exist or the SortBy parameter was not specified",
						sortBy);
			}

			// I will execute the same query with or without Sortby parameter
			// (in the last case the sort will be null)
			int numHits = getCountDocuments() + 1;
			TopFieldCollector collector = TopFieldCollector.create(
					sort == null ? new Sort() : sort, numHits, false, // fillFields
																		// - not
																		// needed,
																		// we
																		// want
																		// score
																		// and
																		// doc
																		// only
					false, // trackDocScores - need doc and score fields
					false, // trackMaxScore - related to trackDocScores
					sort == null); // should docs be in docId order?
			isearcher.search(query, collector);
			TopDocs topDocs = collector.topDocs();
			// hits= topDocs.scoreDocs;
			hits = topDocs.scoreDocs;

			map.put("total", Integer.toString(hits.length));

			isearcher.close();
			ir.close();
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			if (null != isearcher)
				isearcher.close();
			if (null != ir)
				ir.close();
		}

		return hits;

	}

	// TODO RPE
	public void indexReader() {
		IndexReader ir = null;
		try {
			logger.info("Reload the Lucene Index for [{}]", indexId);
			ir = IndexReader.open(this.indexDirectory, true);

			Map<String, String> map = ir.getCommitUserData();
			logger.info("numberDocs->" + map.get("numberDocs"));
			logger.info("date->" + map.get("date"));
			logger.info("keyValidator->" + map.get("keyValidator"));
			this.setCountDocuments(Integer.parseInt(map.get("numberDocs")));
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			try {
				if (null != ir) {
					ir.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Caught an exception:", e);
			}
		}
	}

	public void setup() {
		// TODO Auto-generated method stub
		closeIndexReader();
		getIndexReader(); //I need to do this, because the setup method is called when a full reload occurs and we need to open it again
		logger.info("default setup for Index Environment");

	}
	
	public String getMetadataInformation(){
		
		String ret="<table>";
		Map<String, String> map = getIndexReader().getCommitUserData();
		for (String key : map.keySet()) {
			ret+="<tr><td valign='top'><u>" + key + "</u></td><td>" + map.get(key) + "</td></tr>";
		}
		ret+="</table>";
		return ret;
	}

}
