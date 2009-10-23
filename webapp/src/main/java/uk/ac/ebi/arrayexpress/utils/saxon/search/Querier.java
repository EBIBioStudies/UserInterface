package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.Term;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class Querier
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Configuration config;

    public Querier( Configuration config )
    {
        this.config = config;
    }

    public List<String> getTerms( String indexId, String fieldName )
    {
        List<String> termsList = null;
        
        try {
            HierarchicalConfiguration indexConfig = this.config.getIndexConfig(indexId);
            String indexBaseLocation = indexConfig.getString("[@location]");
            IndexReader ir = IndexReader.open(new File(indexBaseLocation, indexId));
            TermEnum terms = ir.terms(new Term(fieldName, ""));
            while (fieldName.equals(terms.term().field())) {
                if (null == termsList)
                    termsList = new ArrayList<String>();

                termsList.add(terms.term().text());
            if (!terms.next())
                break;
            }
            // TODO: this should go to 'finally' clause
            terms.close();
            ir.close();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return termsList;
    }
}
