package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.IOException;
import java.io.Reader;

public class AttributeFieldAnalyzer extends Analyzer
{
       private class AttributeFieldTokenizer extends WhitespaceTokenizer
    {
        public AttributeFieldTokenizer( Reader in)
        {
            super(in);
        }

        protected boolean isTokenChar(char c)
        {
            return super.isTokenChar(c) && (',' != c) && (';' != c);
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
