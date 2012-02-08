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

import net.sf.saxon.om.NodeInfo;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.search.BackwardsCompatibleQueryConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexEnvironment {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// source index configuration (will be eventually removed)
	public HierarchicalConfiguration indexConfig;

	// index configuration, parsed
	public String indexId;
	public Directory indexDirectory;
	public PerFieldAnalyzerWrapper indexAnalyzer;
	public String defaultField;

	// index document xpath
	public String indexDocumentPath;

	//number of documents indexed
	private int count;
	
	//number of documents that are available (for instance +visible:true +userid:1)
	private int countFiltered;
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCountFiltered() {
		return countFiltered;
	}

	public void setCountFiltered(int countFiltered) {
		this.countFiltered = countFiltered;
	}




	// field information, parsed
	public static class FieldInfo {
		public String name;
		public String title;
		public String type;
		public String path;
		public boolean shouldAnalyze;
		public String analyzer;
		public boolean shouldStore;
		public boolean shouldEscape;
		public boolean shouldSort;
		public boolean shouldAutoCompletion;
		public List<String> sortFields;

		public FieldInfo(HierarchicalConfiguration fieldConfig) {
			this.name = fieldConfig.getString("[@name]");
			this.title = fieldConfig.containsKey("[@title]") ? fieldConfig
					.getString("[@title]") : null;
			this.type = fieldConfig.getString("[@type]");
			this.path = fieldConfig.getString("[@path]");
			if ("string".equals(this.type)) {
				this.shouldAnalyze = fieldConfig.getBoolean("[@analyze]");
				this.analyzer = fieldConfig.getString("[@analyzer]");
				this.shouldEscape = fieldConfig.getBoolean("[@escape]");
			}
			this.shouldStore = fieldConfig.containsKey("[@store]") ? fieldConfig
					.getBoolean("[@store]") : false;
		
		
//			TODO: try to have debug information when i'm referring a field in the sortfieldsattribute thas does not is defined in the xml file	
			if(fieldConfig.containsKey("[@sortfields]")){
				this.shouldSort=true;
				String[] spl=fieldConfig.getString("[@sortfields]").split(",");
				this.sortFields=Arrays.asList(spl);
			}
			
			if(fieldConfig.containsKey("[@autocompletion]")){
				this.shouldAutoCompletion=false;
				if(fieldConfig.getBoolean("[@autocompletion]")){
					this.shouldAutoCompletion=true;
				}
				
			}
		}
	}

	public Map<String, FieldInfo> fields;

	// document info
	public int documentHashCode;
//	public List<NodeInfo> documentNodes;

	public IndexEnvironment(HierarchicalConfiguration indexConfig) {
		this.indexConfig = indexConfig;
		populateIndexConfiguration();
	}

//	public void putDocumentInfo(int documentHashCode,
//			List<NodeInfo> documentNodes) {
//		this.documentHashCode = documentHashCode;
//		this.documentNodes = documentNodes;
//	}

	private void populateIndexConfiguration() {
		try {
			this.indexId = this.indexConfig.getString("[@id]");

			String indexBaseLocation = this.indexConfig
					.getString("[@location]");
			this.indexDirectory = FSDirectory.open(new File(indexBaseLocation,
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
	
	
	
	
	public String queryPaged( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
        IndexReader ir = null;
        IndexSearcher isearcher = null;
        StringBuilder totalRes= new StringBuilder();
        totalRes.append("<content>");
        Query query=info.getQuery();
        try {
            ir = IndexReader.open(this.indexDirectory, true);
            
            
            // empty query returns everything
            if (query instanceof BooleanQuery && ((BooleanQuery)query).clauses().isEmpty()) {
                logger.info("Empty search, returned all [{}] documents", getCount());
//                TODO:see this RPE
                return "";
            }

            // to show _all_ available nodes
            isearcher = new IndexSearcher(ir);
            // +1 is a trick to prevent from having an exception thrown if documentNodes.size() value is 0
            boolean descending=true;
            
            
            String sortBy = StringTools.arrayToString(map.get("sortby"),
					" ");
            if (sortBy!=null && sortBy.equalsIgnoreCase("")){
            	sortBy="date";
            }
			String sortOrder = StringTools.arrayToString(
					map.get("sortorder"), " ");
            
            if(sortOrder!=null && sortOrder.equalsIgnoreCase("ascending")){
            	descending=false;
            }
            
            int sortFieldType=SortField.INT;
            //I have to test the sort field name. If it is a string i have to add "sort" to the name
            //I will only sort if I have a Field
            TopDocs hits;
            if(doesFieldExist(sortBy)){
            	FieldInfo sortField= fields.get(sortBy);
            	if(sortField==null){
              		logger.info("A sort field is trying to be used but that field is not defined! ->[{}]",sortBy);
            	}
            	
            	 int sortFieldsSize=sortField.sortFields!=null? sortField.sortFields.size()  :0 ;
            	 SortField[] sortFieldArray= new SortField[sortFieldsSize];
            	 //sortFieldArray[0]=new SortField(sortBy, sortFieldType, descending);
            	 if(sortFieldsSize>0){
            		 StringBuilder sb= new StringBuilder();
            		 for (int i = 0; i < sortField.sortFields.size(); i++) {
            			 FieldInfo otherSortField=fields.get(sortField.sortFields.get(i));
            			 
             			 if(otherSortField==null){
                      		logger.info("A sort field is trying to be used but that field is not defined! ->[{}]",sortField.sortFields.get(i));
                         }
             			 else{
             				String sortByName=otherSortField.name;
             				int descendingType=SortField.STRING_VAL;
             				sb.append("new sortField ->").append(otherSortField.name).append("; ");
                  			if(otherSortField.name.equalsIgnoreCase(sortBy) &&otherSortField.type.equalsIgnoreCase("string")){
                  				sortByName+="sort";
                        	}
                  			else{
                    			if(otherSortField.type.equalsIgnoreCase("integer")){
                      				descendingType=SortField.INT;			
                    			}
                  			}
                     				
             				sortFieldArray[i] = new SortField(sortByName, descendingType, descending);
        					
             				 
             			 }
    				}
         			logger.info("Query sorted by: ->[{}]",sb.toString());
            	 }
            	          	
            	  Sort sort = new Sort(sortFieldArray);
                    
                  //TODO the number of experiments
                   hits = isearcher.search(query, getCount() + 1, sort);
            }
            else{//TODO the number of experiments
            	 hits = isearcher.search(query, getCount() + 1);
            }
            	

            
            
          
            logger.info("Search of index [" + this.indexId + "] with query [{}] returned [{}] hits", query.toString(), hits.totalHits);

           
            //PAGING
            int pageSize = 25;
			if (map.containsKey("pagesize")) {
				pageSize = Integer.parseInt(StringTools.arrayToString(
						map.get("pagesize"), " "));
			}

			int page = 0;
			if (map.containsKey("page")) {
				page = Integer.parseInt(StringTools.arrayToString(
						map.get("page"), " ")) - 1;
			}
            
            
            int initialExp= page * pageSize;
            int finalExp= initialExp + pageSize;
            if (finalExp > hits.totalHits){
            	finalExp=hits.totalHits;
            }
            
      
            
            //bad solution: just to test 
            
            List<String> combinedTotal = new ArrayList<String>();
            combinedTotal.add(String.valueOf(hits.totalHits));
 
            
            map.put("total", combinedTotal.toArray(new String[combinedTotal.size()]));
            
            System.out.println("¢¢¢¢¢¢ InitialExp: " + initialExp + " FinalExp: " + finalExp);
            for (int i = initialExp; i < finalExp; i++) {
            	Document doc = isearcher.doc(hits.scoreDocs[i].doc);
            	System.out.println("xml->" + doc.get("xml"));
            	System.out.println("atlas->" + doc.get("atlas"));
            	totalRes.append(doc.get("xml")) ;	 
            }
            
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
        
        totalRes.append("</content>");
        return totalRes.toString();
    }
	
	
	
	
	public String queryPartialDocs(TopDocs hits, int initial, int end, HttpServletRequestParameterMap map) throws IOException
    {
        IndexReader ir = null;
        IndexSearcher isearcher = null;
        StringBuilder totalRes= new StringBuilder();
        totalRes.append("<content>");
        try {
            ir = IndexReader.open(this.indexDirectory, true);
            
             isearcher = new IndexSearcher(ir);
             for (int i = initial; i < end; i++) {
            	Document doc = isearcher.doc(hits.scoreDocs[i].doc);
            	totalRes.append(doc.get("xml")) ;	 
            }

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
        
        totalRes.append("</content>");
        return totalRes.toString();
    }
	
	
	public TopDocs queryAllDocs( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
        IndexReader ir = null;
        IndexSearcher isearcher = null;
        Query query=info.getQuery();
        TopDocs hits=null;
        try {
            ir = IndexReader.open(this.indexDirectory, true);
                   
            // empty query returns everything
            if (query instanceof BooleanQuery && ((BooleanQuery)query).clauses().isEmpty()) {
                logger.info("Empty search, returned all [{}] documents", getCount());
//                TODO:see this RPE
                return null;
            }

            // to show _all_ available nodes
            isearcher = new IndexSearcher(ir);
            // +1 is a trick to prevent from having an exception thrown if documentNodes.size() value is 0
            boolean descending=true;
            
            
            String sortBy = StringTools.arrayToString(map.get("sortby"),
					" ");
            if (sortBy!=null && sortBy.equalsIgnoreCase("")){
            	sortBy="date";
            }
			String sortOrder = StringTools.arrayToString(
					map.get("sortorder"), " ");
            
            if(sortOrder!=null && sortOrder.equalsIgnoreCase("ascending")){
            	descending=false;
            }
            
            int sortFieldType=SortField.INT;
            //I have to test the sort field name. If it is a string i have to add "sort" to the name
            //I will only sort if I have a Field
            
            if(doesFieldExist(sortBy)){
            	FieldInfo sortField= fields.get(sortBy);
            	if(sortField==null){
              		logger.info("A sort field is trying to be used but that field is not defined! ->[{}]",sortBy);
            	}
            	
            	 int sortFieldsSize=sortField.sortFields!=null? sortField.sortFields.size()  :0 ;
            	 SortField[] sortFieldArray= new SortField[sortFieldsSize];
            	 //sortFieldArray[0]=new SortField(sortBy, sortFieldType, descending);
            	 if(sortFieldsSize>0){
            		 StringBuilder sb= new StringBuilder();
            		 for (int i = 0; i < sortField.sortFields.size(); i++) {
            			 FieldInfo otherSortField=fields.get(sortField.sortFields.get(i));
            			 
             			 if(otherSortField==null){
                      		logger.info("A sort field is trying to be used but that field is not defined! ->[{}]",sortField.sortFields.get(i));
                         }
             			 else{
             				String sortByName=otherSortField.name;
             				int descendingType=SortField.STRING_VAL;
             				sb.append("new sortField ->").append(otherSortField.name).append("; ");
                  			if(otherSortField.name.equalsIgnoreCase(sortBy) &&otherSortField.type.equalsIgnoreCase("string")){
                  				sortByName+="sort";
                        	}
                  			else{
                    			if(otherSortField.type.equalsIgnoreCase("integer")){
                      				descendingType=SortField.INT;			
                    			}
                  			}
                     				
             				sortFieldArray[i] = new SortField(sortByName, descendingType, descending);
        					
             				 
             			 }
    				}
         			logger.info("Query sorted by: ->[{}]",sb.toString());
            	 }
            	          	
            	  Sort sort = new Sort(sortFieldArray);
                    
                  //TODO the number of experiments
                   hits = isearcher.search(query, getCount() + 1, sort);
            }
            else{//TODO the number of experiments
            	 hits = isearcher.search(query, getCount() + 1);
            }
            map.put("total", Integer.toString(hits.totalHits));
//            List<String> combinedTotal = new ArrayList<String>();
//            combinedTotal.add(String.valueOf(hits.totalHits));
// 
//            
//            map.put("total", combinedTotal.toArray(new String[combinedTotal.size()]));
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
	
	//Number of Filtered elements
	public int calculateCountFiltered() throws IOException, ParseException{
		
		return new Querier(this).getDocCount((new BackwardsCompatibleQueryConstructor()).construct(this, "visible:true userid:1"));
		
	}
	
}
