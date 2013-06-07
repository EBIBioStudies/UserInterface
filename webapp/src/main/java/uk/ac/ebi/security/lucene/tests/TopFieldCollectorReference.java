package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.util.PriorityQueue;




public  class TopFieldCollectorReference extends Collector {





	private TopFieldCollector topFieldCollector;
	private String[] reference;
	private long[] samples;
	
	
	 public TopFieldCollectorReference(TopFieldCollector tp) {
		 topFieldCollector=tp;
	    }
	 
	

		
		// TODO Auto-generated constructor stub

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		// TODO Auto-generated method stub
		topFieldCollector.setScorer(scorer);
		
	}

	@Override
	public void collect(int doc) throws IOException {
		// TODO Auto-generated method stub
		//topFieldCollector.collect(doc);
		long nsamp=samples[doc];

		if(nsamp>20){
			topFieldCollector.collect(doc);
			//System.out.println("$$$$$-> doc is->"+ doc +" samples->" + nsamp);
		}
		else{
			//System.out.println("$$$$$-> doc is not reference ->" + nsamp);
		}
		
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		// TODO Auto-generated method stub
		topFieldCollector.setNextReader(reader, docBase);
		try {
			reference = FieldCache.DEFAULT.getStrings(reader, "id");
			samples = FieldCache.DEFAULT.getLongs(reader, "samples");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		// TODO Auto-generated method stub
		return topFieldCollector.acceptsDocsOutOfOrder();
	}
	

	
	//@Override
	public TopDocs topDocs(){
		return topFieldCollector.topDocs();
	}

}
