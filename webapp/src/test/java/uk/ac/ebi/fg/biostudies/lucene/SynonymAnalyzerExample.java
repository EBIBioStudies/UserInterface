/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
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

package uk.ac.ebi.fg.biostudies.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

//import net.xqj.basex.bin.A;
//import net.xqj.basex.bin.at;
//import net.xqj.basex.bin.s;

//TODO: need to complete this testCase
public class SynonymAnalyzerExample{ //extends TestCase {
	private IndexSearcher searcher;
	private static SynonymAnalyzer synonymAnalyzer = new SynonymAnalyzer(
			new SynonymEngineExample());

	public void setUp() throws Exception {
		//RAMDirectory directory = new RAMDirectory();
//		IndexWriter writer = new IndexWriter(directory, synonymAnalyzer, // #1
//				IndexWriter.MaxFieldLength.UNLIMITED);
		
		IndexWriter writer = new IndexWriter(FSDirectory.open(new File("/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/index-syn")), synonymAnalyzer, IndexWriter.MaxFieldLength.UNLIMITED);
		Document doc = new Document();
		doc.add(new Field("content",
				"The quick brown fox jumps over the lazy dogs",
				Field.Store.YES, Field.Index.ANALYZED)); // #2
		writer.addDocument(doc);
		writer.close();
		Directory indexDirectory = FSDirectory.open(new File("/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/",
				"index-syn"));
		//new IndexSearcher(ir);
		IndexReader ir = IndexReader.open(indexDirectory, true);
		//IndexReader ir=writer();
		searcher = new IndexSearcher(ir);
	}

	public void tearDown() throws Exception {
		searcher.close();
	}

	public void testSearchByAPI() throws Exception {
		TermQuery tq = new TermQuery(new Term("content", "hops")); // #1
		// assertEquals(1, TestUtil.hitCount(searcher, tq));
		PhraseQuery pq = new PhraseQuery(); // #2
		pq.add(new Term("content", "fox")); // #2
		pq.add(new Term("content", "hops")); // #2
		search(pq);
		// assertEquals(1, TestUtil.hitCount(searcher, pq));
	}

	public void testWithQueryParser() throws Exception {
		Query query = new QueryParser(Version.LUCENE_CURRENT, "content", // A
				synonymAnalyzer).parse("\"fox jumps\""); // A
		// assertEquals(1, TestUtil.hitCount(searcher, query)); // A
		search(query);
		System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to "
				+ query.toString("content"));
		query = new QueryParser(Version.LUCENE_CURRENT, "content",
				new StandardAnalyzer(Version.LUCENE_CURRENT))
				.parse("\"fox jumps\""); // B
		// assertEquals(1, TestUtil.hitCount(searcher, query));
		search(query);
		 System.out.println("With StandardAnalyzer, \"fox jumps\" parses to "
		 +
		 query.toString("content"));
	}
	
	
	public void search(Query q){
		try {
			TopDocs tp=searcher.search(q, 10);
			for (int i = 0; i < tp.totalHits; i++) {
				System.out.println("document->" + searcher.doc(tp.scoreDocs[i].doc).get("content"));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
