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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class provided as source for 'Lucene In Action, 2nd Edition'
 */
public class AnalyzerUtils {
        public static void displayTokens(Analyzer analyzer,
                        String text) throws IOException {
                displayTokens(analyzer.tokenStream("contents", new StringReader(text)));  //A
        }

        public static void displayTokens(TokenStream stream)
        throws IOException {

                TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class);
                while(stream.incrementToken()) {
                        System.out.print("[" + term.term() + "] ");    //B
                }
                System.out.println();
        }
        
        

        public static List<String> getAnalyzedTokens(Analyzer analyzer, String text, boolean sortedAlphabetically) throws IOException 
        {
                return getAnalyzedTokens(analyzer.tokenStream("contents", new StringReader(text)), sortedAlphabetically);       
        }

        public static List<String> getAnalyzedTokens(Analyzer analyzer, String text) throws IOException {
                return getAnalyzedTokens(analyzer.tokenStream("contents", new StringReader(text)), false);  //A
        }

        public static List<String> getAnalyzedTokens(TokenStream stream) throws IOException 
        {
                return getAnalyzedTokens(stream, false);
        }
        public static List<String> getAnalyzedTokens(TokenStream stream, boolean sortedAlphabetically) throws IOException {

                List<String> result = new ArrayList<String>();
                TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class);
                while(stream.incrementToken()) 
                {
                        result.add(term.term());
                }

                if(sortedAlphabetically)
                {
                        Collections.sort(result);
                }

                return result;
        }

        public static String getTerm(AttributeSource source) {
                TermAttribute attr = (TermAttribute) source.addAttribute(TermAttribute.class);
                return attr.term();
        }

        public static String getType(AttributeSource source) {
                TypeAttribute attr = (TypeAttribute) source.addAttribute(TypeAttribute.class);
                return attr.type();
        }
        public static void setTerm(AttributeSource source, String term) {
                TermAttribute attr = (TermAttribute) source.addAttribute(TermAttribute.class);
                attr.setTermBuffer(term);
        }

        public static void setType(AttributeSource source, String type) {
                TypeAttribute attr = (TypeAttribute) source.addAttribute(TypeAttribute.class);
                attr.setType(type);
        }

        public static void displayTokensWithPositions
        (Analyzer analyzer, String text) throws IOException {

                TokenStream stream = analyzer.tokenStream("contents",
                                new StringReader(text));
                TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class);

                int position = 0;
                while(stream.incrementToken()) {
                        System.out.print("[" + term.term() + "] ");
                }
                System.out.println();
        }

        public static void displayTokensWithFullDetails(Analyzer analyzer,
                        String text) throws IOException {

                TokenStream stream = analyzer.tokenStream("contents",                        // #A
                                new StringReader(text));

                TermAttribute term = (TermAttribute)                               // #B
                stream.addAttribute(TermAttribute.class);                        // #B
                OffsetAttribute offset = (OffsetAttribute)                         // #B
                stream.addAttribute(OffsetAttribute.class);                      // #B
                TypeAttribute type = (TypeAttribute)                               // #B
                stream.addAttribute(TypeAttribute.class);                        // #B

                int position = 0;
                while(stream.incrementToken()) {                                  // #C
                        System.out.print("[" +                                 // #E
                                        term.term() + ":" +                   // #E
                                        offset.startOffset() + "->" +         // #E
                                        offset.endOffset() + ":" +            // #E
                                        type.type() + "] ");                  // #E
                }
                System.out.println();
        }

        
        public static void setPositionIncrement(AttributeSource source, int posIncr) {
        	PositionIncrementAttribute attr = (PositionIncrementAttribute) source.addAttribute(PositionIncrementAttribute.class);
        	attr.setPositionIncrement(posIncr);
        	}

       
        public static void main(String[] args) throws IOException
        {
                System.out.println("SimpleAnalyzer");
                displayTokensWithFullDetails(new SimpleAnalyzer(),
                "The quick brown fox....");

                System.out.println("\n----");
                System.out.println("StandardAnalyzer");
                displayTokensWithFullDetails(new StandardAnalyzer(Version.LUCENE_CURRENT),
                "I'll e-mail you at xyz@example.com");
        }
}

