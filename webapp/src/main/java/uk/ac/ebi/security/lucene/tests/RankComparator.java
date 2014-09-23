package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;

public class RankComparator<T> extends FieldComparator<T> {

	@Override
	public int compare(int slot1, int slot2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBottom(int slot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareBottom(int doc) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void copy(int slot, int doc) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T value(int slot) {
		// TODO Auto-generated method stub
		return null;
	}

}
