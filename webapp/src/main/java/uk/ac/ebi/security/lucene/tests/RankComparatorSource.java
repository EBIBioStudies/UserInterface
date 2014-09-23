package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

public class RankComparatorSource extends FieldComparatorSource {

	private int x;
	private int y;

	public RankComparatorSource(int x, int y) { // #2
		this.x = x;
		this.y = y;
	}

	public FieldComparator newComparator(java.lang.String fieldName,
			int numHits, int sortPos,

			boolean reversed) throws IOException { // #3
		return new RankScoreDocLookupComparator(fieldName, numHits);
	}

	private class RankScoreDocLookupComparator // #4
			extends FieldComparator {
		private int[] xDoc, yDoc;
		private float[] values;
		private float bottom;
		String fieldName;

		// #5
		// #6
		// #7
		public RankScoreDocLookupComparator(String fieldName, int numHits)
				throws IOException {
			values = new float[numHits];
			this.fieldName = fieldName;
		}

		public void setNextReader(IndexReader reader, int docBase)
				throws IOException {
			//final TermEnum enumerator = reader.terms(new Term(fieldName, ""));
			xDoc = FieldCache.DEFAULT.getInts(reader, "x"); // #8
			yDoc = FieldCache.DEFAULT.getInts(reader, "y"); // #8
		}

		private float getDistance(int doc) {
			int deltax = xDoc[doc] - x;
			int deltay = yDoc[doc] - y;
			return (float) Math.sqrt(deltax * deltax + deltay * deltay); // #9
		}

		public int compare(int slot1, int slot2) {
			if (values[slot1] < values[slot2])
				return -1;
			if (values[slot1] > values[slot2])
				return 1;
			return 0;
		}

		public void setBottom(int slot) {

			bottom = values[slot];
		}

		public int compareBottom(int doc) {
			float docDistance = getDistance(doc);
			if (bottom < docDistance)
				return -1;
			if (bottom > docDistance)
				return 1;
			return 0;
		}

		public void copy(int slot, int doc) {
			values[slot] = getDistance(doc);
		}

		public Comparable value(int slot) {
			return new Float(values[slot]);
		}

		public int sortType() {
			return SortField.CUSTOM;
		}
	}

	public String toString() {
		return "Distance from (" + x + "," + y + ")";
	}
}
/*
 * #1 Extend FieldComparatorSource #2 Give constructor base location #3 Create
 * comparator #4 FieldComparator implementation #5 Array of x, y per document #6
 * Distances for documents in the queue #7 Worst distance in the queue #8 Get x,
 * y values from field cache #9 Compute distance for one document #10 Compare
 * two docs in the top N #11 Record worst scoring doc in the top N #12 Compare
 * new doc to worst scoring doc #13 Insert new doc into top N #14 Extract value
 * from top N
 */
