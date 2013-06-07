package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class Teste extends Collector {

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void collect(int doc) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		// TODO Auto-generated method stub
		return false;
	}

}
