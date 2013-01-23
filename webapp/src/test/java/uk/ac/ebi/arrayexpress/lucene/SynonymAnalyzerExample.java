package uk.ac.ebi.arrayexpress.lucene;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.xqj.basex.bin.A;
import net.xqj.basex.bin.at;
import net.xqj.basex.bin.s;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

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
