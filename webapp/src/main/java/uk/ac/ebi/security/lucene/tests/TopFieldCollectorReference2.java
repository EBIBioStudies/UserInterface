package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;

///public class TopFieldCollectorReference2 extends TopDocsCollector<ScoreDoc> {

public class TopFieldCollectorReference2 extends Collector {

	private TopFieldCollector topFieldCollector;
	private String[] reference;
	private long[] samples;

	// public TopFieldCollectorReference2(Sort sort, int numHits,
	// boolean fillFields, boolean trackDocScores, boolean trackMaxScore,
	// boolean docsScoredInOrder) {
	// FieldValueHitQueue queue = FieldValueHitQueue.create(sort., numHits);
	// super();
	// topFieldCollector=tp;
	//
	// }
	//

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		// TODO Auto-generated method stub
		topFieldCollector.setScorer(scorer);

	}

	@Override
	public void collect(int doc) throws IOException {
		// TODO Auto-generated method stub
		// topFieldCollector.collect(doc);
		long nsamp = samples[doc];

		if (nsamp > 20) {
			topFieldCollector.collect(doc);
			// System.out.println("$$$$$-> doc is->"+ doc +" samples->" +
			// nsamp);
		} else {
			// System.out.println("$$$$$-> doc is not reference ->" + nsamp);
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

	// @Override
	public TopDocs topDocs() {
		return topFieldCollector.topDocs();
	}

}
