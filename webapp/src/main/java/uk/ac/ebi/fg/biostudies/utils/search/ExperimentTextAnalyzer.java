package uk.ac.ebi.fg.biostudies.utils.search;

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

import org.apache.lucene.analysis.*;

import java.io.IOException;
import java.io.Reader;

public final class ExperimentTextAnalyzer extends Analyzer
{
    private static class ExperimentTextTokenizer extends CharTokenizer
    {
        public ExperimentTextTokenizer(Reader in)
        {
            super(in);
        }

        protected boolean isTokenChar(char c)
        {
            return Character.isLetter(c) | Character.isDigit(c) | ('-' == c);
        }

        protected char normalize(char c)
        {
            return Character.toLowerCase(c);
        }
    }
    
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new ASCIIFoldingFilter(new ExperimentTextTokenizer(reader));
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
        if (tokenizer == null) {
            tokenizer = new ExperimentTextTokenizer(reader);
            setPreviousTokenStream(tokenizer);
        } else
            tokenizer.reset(reader);
        return new ASCIIFoldingFilter(tokenizer);
    }
}
