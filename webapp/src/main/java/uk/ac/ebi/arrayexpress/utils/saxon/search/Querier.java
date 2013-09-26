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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;


public class Querier {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AbstractIndexEnvironment env;

	public Querier(AbstractIndexEnvironment env2) {
		this.env = env2;
	}

	public List<String> getTerms(String fieldName, int minFreq)
			throws IOException {
		List<String> termsList = new ArrayList<String>();
		IndexReader ir = null;
		TermEnum terms = null;

		try {
			ir = IndexReader.open(this.env.indexDirectory, true);
			terms = ir.terms(new Term(fieldName, ""));
			if (null != terms) {
				while (null != terms.term()
						&& fieldName.equals(terms.term().field())) {
					if (terms.docFreq() >= minFreq) {
						termsList.add(terms.term().text());
					}
					if (!terms.next())
						break;
				}
				terms.close();
			}
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			if (null != terms) {
				terms.close();
			}
			if (null != ir) {
				ir.close();
			}
		}
		return termsList;
	}

	
	//I need this because in the sample I have dynamic fields, do the are not build based on the biosamples.xml configuration file.
	public Collection<String> getFields()
			throws IOException {
		IndexReader ir = null;
		Collection<String> fieldsList=null;
		
		try {
			ir = IndexReader.open(this.env.indexDirectory, true);
			fieldsList=ir.getFieldNames(IndexReader.FieldOption.INDEXED);
			
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			if (null != ir) {
				ir.close();
			}
		}
		return fieldsList;
	}

	
	public void dumpTerms(String fieldName) {
		try {
			IndexReader ir = IndexReader.open(this.env.indexDirectory, true);
			TermEnum terms = ir.terms(new Term(fieldName, ""));
			File f = new File(System.getProperty("java.io.tmpdir"), fieldName
					+ "_terms.txt");
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			StringBuilder sb = new StringBuilder();
			while (fieldName.equals(terms.term().field())) {
				sb.append(terms.docFreq()).append('\t')
						.append(terms.term().text()).append(StringTools.EOL);
				if (!terms.next())
					break;
			}
			w.write(sb.toString());
			w.close();
			terms.close();
			ir.close();
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	public Integer getDocCount(Query query) throws IOException {
		Integer count = null;
		IndexReader ir = null;
		IndexSearcher isearcher = null;
		try {
			ir = IndexReader.open(this.env.indexDirectory, true);
			
			isearcher = new IndexSearcher(ir);
			// +1 is a trick to prevent from having an exception thrown if
			// documentNodes.size() value is 0
			
			TopDocs hits = isearcher.search(query,
					env.getCountDocuments() + 1);

			count = hits.totalHits;
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		} finally {
			if (null != isearcher)
				isearcher.close();
			if (null != ir)
				ir.close();
		}

		return count;
	}

/*
	// TODO: I want to remove this references from the XSL
	@Deprecated
	public List<NodeInfo> query( Query query ) throws IOException
    {
        List<NodeInfo> result = null;
        IndexReader ir = null;
        IndexSearcher isearcher = null;
        StringBuilder totalRes= new StringBuilder();
        totalRes.append("<content>");
        try {
            ir = IndexReader.open(this.env.indexDirectory, true);
            
            Experiments exp= (Experiments)Application.getInstance().getComponent("Experiments");
			int experimentsSize=exp.getExperimentsNumber().intValue();

            // empty query returns everything
            if (query instanceof BooleanQuery && ((BooleanQuery)query).clauses().isEmpty()) {
            	logger.info("Empty search, This shouldnt happen anymore!!");
//                logger.info("Empty search, returned all [{}] documents", this.env.documentNodes.size());
//                return this.env.documentNodes;
                return null;
            }

            // to show _all_ available nodes
            isearcher = new IndexSearcher(ir);
            // +1 is a trick to prevent from having an exception thrown if documentNodes.size() value is 0
            TopDocs hits = isearcher.search(query, experimentsSize + 1);
            logger.info("Search of index [" + this.env.indexId + "] with query [{}] returned [{}] hits - This was called by SearchExtension functions", query.toString(), hits.totalHits);

            result = new ArrayList<NodeInfo>(hits.totalHits);
            for (ScoreDoc d : hits.scoreDocs) {
            	Document doc=isearcher.doc(d.doc);
            	totalRes.append(doc.get("xml"));
            }
            totalRes.append("</content>");
//            build List<NodeInfo> based on a xml string
         
            DocumentInfo docInfo=((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument(totalRes.toString());
            
            
            XPath xp = new XPathEvaluator(docInfo.getConfiguration());
            XPathExpression xpe = xp.compile("//experiment");
            result = (List<NodeInfo>)xpe.evaluate(docInfo, XPathConstants.NODESET);
//            System.out.println("XML->" + totalRes.toString() + " Nodes number ->" + result.size() );
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

        return result;
    }
*/		
	
	
	
	
	public String queryPaged( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
           
        return env.queryPaged(queryId, info, map) ;
    }
	
	public ScoreDoc[] queryAllDocs( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
           
        return env.queryAllDocs(queryId, info, map) ;
    }

	
/*	
	public String queryPagedBAK( Integer queryId, QueryInfo info, HttpServletRequestParameterMap map) throws IOException
    {
        IndexReader ir = null;
        IndexSearcher isearcher = null;
        StringBuilder totalRes= new StringBuilder();
        totalRes.append("<content>");
        Query query=info.getQuery();
        try {
            ir = IndexReader.open(this.env.indexDirectory, true);
            
            
            Experiments exp= (Experiments)Application.getInstance().getComponent("Experiments");
			int experimentsSize=exp.getExperimentsNumber().intValue();
			// empty query returns everything
            
            // empty query returns everything
            if (query instanceof BooleanQuery && ((BooleanQuery)query).clauses().isEmpty()) {
                logger.info("Empty search, returned all [{}] documents", experimentsSize);
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
            if(env.doesFieldExist(sortBy)){
            	FieldInfo sortField= env.fields.get(sortBy);
            	if(sortField==null){
              		logger.info("A sort field is trying to be used but that field is not defined! ->[{}]",sortBy);
            	}
            	
            	 int sortFieldsSize=sortField.sortFields!=null? sortField.sortFields.size()  :0 ;
            	 SortField[] sortFieldArray= new SortField[sortFieldsSize];
            	 //sortFieldArray[0]=new SortField(sortBy, sortFieldType, descending);
            	 if(sortFieldsSize>0){
            		 StringBuilder sb= new StringBuilder();
            		 for (int i = 0; i < sortField.sortFields.size(); i++) {
            			 FieldInfo otherSortField=env.fields.get(sortField.sortFields.get(i));
            			 
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
                     				
                  			logger.info("Query sorted by: ->[{}]",sb.toString());
             				sortFieldArray[i] = new SortField(sortByName, descendingType, descending);
        					
             				 
             			 }
    				}	 
            	 }
            	 
            	
            	  Sort sort = new Sort(sortFieldArray);
       
                  
                  //TODO the number of experiments
                   hits = isearcher.search(query, experimentsSize + 1, sort);
            }
            else{//TODO the number of experiments
            	 hits = isearcher.search(query, experimentsSize + 1);
            }
            	

            
            
          
            logger.info("Search of index [" + this.env.indexId + "] with query [{}] returned [{}] hits", query.toString(), hits.totalHits);

           
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
            
      
            long samplesNumber=0;
            long assaysNumber=0;
            //join this with the for used to xml building
            long time=System.currentTimeMillis();
            for (int i = 0; i < hits.totalHits; i++) {
            	
//            	Document doc = isearcher.doc(hits.scoreDocs[i].doc);
//            	TODO isolate this from here, its only for experimets
//            	assaysNumber+=Integer.parseInt(doc.get("assays"));
//            	samplesNumber+=Integer.parseInt(doc.get("samplecount"));
            	
            }
            System.out.println("assays number->" + (System.currentTimeMillis()-time));
            
            
            //bad solution: just to test 
            
            List<String> combinedTotal = new ArrayList<String>();
            combinedTotal.add(String.valueOf(hits.totalHits));
            
            List<String> combinedSamples = new ArrayList<String>();
            combinedSamples.add(String.valueOf(samplesNumber));
            
            List<String> combinedAssays = new ArrayList<String>();
            combinedAssays.add(String.valueOf(assaysNumber));
            
            map.put("total", combinedTotal.toArray(new String[combinedTotal.size()]));
            map.put("totalsamples", combinedSamples.toArray(new String[combinedSamples.size()]));
            map.put("totalassays", combinedAssays.toArray(new String[combinedAssays.size()]));
            
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
    */
}
