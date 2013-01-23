package uk.ac.ebi.arrayexpress.lucene;

import java.io.IOException;


	
	  public interface SynonymEngine {
		     String[] getSynonyms(String s) throws IOException;
		   }

