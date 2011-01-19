package uk.ac.ebi.arrayexpress.utils.search;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.IOException;
import java.io.Reader;

public class AttributeFieldAnalyzer extends Analyzer
{
       private static class AttributeFieldTokenizer extends WhitespaceTokenizer
    {
        public AttributeFieldTokenizer( Reader in)
        {
            super(in);
        }

        protected boolean isTokenChar(char c)
        {
            return super.isTokenChar(c) && !(',' == c ||  ';' == c || '(' == c || ')' == c);
        }

        protected char normalize(char c)
        {
            return Character.toLowerCase(c);
        }
    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new AttributeFieldTokenizer(reader);
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
        if (tokenizer == null) {
            tokenizer = new AttributeFieldTokenizer(reader);
            setPreviousTokenStream(tokenizer);
        } else
            tokenizer.reset(reader);
        return tokenizer;
    }
}
