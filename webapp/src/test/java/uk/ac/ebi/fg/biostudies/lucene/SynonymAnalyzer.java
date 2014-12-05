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

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;


	 public class SynonymAnalyzer extends Analyzer {
	     private SynonymEngine engine;
	     public SynonymAnalyzer(SynonymEngine engine) {
	       this.engine = engine;
	}
	     public TokenStream tokenStream(String fieldName, Reader reader) {
	       TokenStream result = new SynonymFilter(
	                             new StopFilter(true,
	                               new LowerCaseFilter(
	                                 new StandardFilter(
	                                   new StandardTokenizer(Version.LUCENE_CURRENT,reader))),
	                               StopAnalyzer.ENGLISH_STOP_WORDS_SET),
	                             engine);
	return result;
	}
}
