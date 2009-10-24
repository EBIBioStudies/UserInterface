package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public final class AccessionAnalyzer extends Analyzer
{
    private class AccessionTokenizer extends CharTokenizer
    {
        public AccessionTokenizer(Reader in)
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
        return new AccessionTokenizer(reader);
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
        if (tokenizer == null) {
            tokenizer = new AccessionTokenizer(reader);
            setPreviousTokenStream(tokenizer);
        } else
            tokenizer.reset(reader);
        return tokenizer;
    }
}
