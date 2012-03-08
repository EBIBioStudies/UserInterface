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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.search.BackwardsCompatibleQueryConstructor;

public class IndexEnvironment extends AbstractIndexEnvironment {
	
	public IndexEnvironment(HierarchicalConfiguration indexConfig) {
		super(indexConfig);

	}
	
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

	//this is specific for each type of document
	//TODO rpe
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
