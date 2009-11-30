package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public final class LowercaseAnalyzer extends Analyzer
{
   private class LowercaseTokenizer extends LetterTokenizer
    {
        public LowercaseTokenizer(Reader in)
        {
            super(in);
        }

        protected char normalize(char c)
        {
            return Character.toLowerCase(c);
        }

        protected boolean isTokenChar(char c)
        {
            return true;
        }
    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new LowercaseTokenizer(reader);
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
        if (tokenizer == null) {
            tokenizer = new LowercaseTokenizer(reader);
            setPreviousTokenStream(tokenizer);
        } else
            tokenizer.reset(reader);
        return tokenizer;
    }
}
