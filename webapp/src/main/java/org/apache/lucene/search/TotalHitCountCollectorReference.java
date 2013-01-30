/**
 * 
 */
package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

/**
 * @author rpslpereira
 *
 */
public class TotalHitCountCollectorReference extends TotalHitCountCollector {

	
	private String[] reference;
	private long[] samples;
	
	@Override
	public void setNextReader(IndexReader reader, int docBase) {
		// TODO Auto-generated method stub
		super.setNextReader(reader, docBase);
		try {
			reference = FieldCache.DEFAULT.getStrings(reader, "id");
			samples = FieldCache.DEFAULT.getLongs(reader, "samples");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void collect(int doc) {
		// TODO Auto-generated method stub
		//super.collect(doc);
		String ref = reference[doc];
		long nsamp=samples[doc];
		//if(ref.equalsIgnoreCase("true")){
		if(nsamp>20){
			super.collect(doc);
			//System.out.println("$$$$$-> doc is->"+ doc +" samples->" + nsamp);
		}
		else{
			//System.out.println("$$$$$-> doc is not reference ->" + nsamp);
		}
	}

}
