/**
 * 
 */
package uk.ac.ebi.security.lucene.tests;

import java.io.IOException;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

/**
 * @author rpslpereira
 *
 */
public class Rank2ComparatorSource extends FieldComparatorSource {

	/**
	 * 
	 */
	public Rank2ComparatorSource() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparatorSource#newComparator(java.lang.String, int, int, boolean)
	 */
	@Override
	public FieldComparator newComparator(String fieldname, int numHits,
			int sortPos, boolean reversed) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
