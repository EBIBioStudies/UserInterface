package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.NodeInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.search.EFOQueryExpander;
import uk.ac.ebi.arrayexpress.utils.search.IEFOExpansionLookup;
import uk.ac.ebi.arrayexpress.utils.search.LowercaseAnalyzer;
import uk.ac.ebi.microarray.ontology.efo.EFOOntologyHelper;

import java.io.File;
import java.util.*;


public class Ontologies extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Ontologies()
    {
        super("Ontologies");
    }


    private class EFOExpansionLookupIndex implements IEFOExpansionLookup
    {
        // logging machinery
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private String indexLocation;
        private Directory indexDirectory;

        public EFOExpansionLookupIndex( String indexLocation )
        {
            this.indexLocation = indexLocation;
        }

        public void addMaps( Map<String, Set<String>> synonymMap, Map<String, Set<String>> efoMap )
        {
            // 1. create a joint set of keys from both maps
            Set<String> allTerms = new HashSet<String>();
            allTerms.addAll(synonymMap.keySet());
            allTerms.addAll(efoMap.keySet());

            // 2. iterate over the set
            try {
                this.indexDirectory = FSDirectory.open(new File(this.indexLocation));
                IndexWriter w = createIndex(this.indexDirectory, new LowercaseAnalyzer());

                for (String term : allTerms) {
                    Document d = new Document();

                    boolean hasJustAddedSomething = false;

                    if (synonymMap.containsKey(term)) {
                        Set<String> syns = synonymMap.get(term);
                        for (String syn : syns) {
                            if (allTerms.contains(syn)) {
                                this.logger.warn("Synonym [{}] for term [{}] is present as a different term itelf, skipping", syn, term);
                            } else {
                                if (-1 != syn.indexOf(' ')) {
                                    addIndexField(d, "term_no_lookup", syn, false, true);
                                } else {
                                    addIndexField(d, "term", syn, false, true);
                                    hasJustAddedSomething = true;
                                }
                            }
                        }
                    }

                    if (efoMap.containsKey(term)) {
                        Set<String> efoTerms = efoMap.get(term);
                        for (String efoTerm : efoTerms) {
                            addIndexField(d, "efo", efoTerm, false, true);
                            hasJustAddedSomething = true;
                        }
                    }

                    if (-1 != term.indexOf(' ')) {
                        addIndexField(d, "term_no_lookup", term, false, true);
                    } else {
                        addIndexField(d, "term", term, false, true);
                    }

                    if (hasJustAddedSomething) {
                        addIndexDocument(w, d);
                    } else {
                        this.logger.warn("Data for term [{}] wasn't included as there were no one-word synonyms found", term);
                    }
                }
                commitIndex(w);

            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }
        }

        public List<Set<String>> getExpansionTerms( Query query, boolean shouldIncludeEfo )
        {
            List<Set<String>> expansion = new ArrayList<Set<String>>(2);

            try {
                IndexReader ir = IndexReader.open(this.indexDirectory, true);

                // to show _all_ available nodes
                IndexSearcher isearcher = new IndexSearcher(ir);
                logger.debug("Looking up synonyms for query [{}]", query.toString());

                TopDocs hits = isearcher.search(query, 128); // todo: WTF is this hardcode?
                logger.debug("Query returned [{}] hits", hits.totalHits);

                for (ScoreDoc d : hits.scoreDocs) {
                    Document doc = isearcher.doc(d.doc);
                    String[] terms = doc.getValues("term");
                    String[] efo = doc.getValues("efo");
                    logger.debug("Synonyms [{}], EFO Terms [{}]", StringUtils.join(terms, ", "), StringUtils.join(efo, ", "));
                }

                isearcher.close();
                ir.close();
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }


            return expansion;
        }



        private IndexWriter createIndex( Directory indexDirectory, Analyzer analyzer )
        {
            IndexWriter iwriter = null;
            try {
                iwriter = new IndexWriter(indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            } catch (Throwable x) {
                this.logger.error("Caught an exception:", x);
            }

            return iwriter;
        }

        private void addIndexField( Document document, String name, Object value, boolean shouldAnalyze, boolean shouldStore )
        {
            String stringValue;
            if (value instanceof String) {
                stringValue = (String)value;
            } else if (value instanceof NodeInfo) {
                stringValue = ((NodeInfo)value).getStringValue();
            } else {
                stringValue = value.toString();
                this.logger.warn("Not sure if I handle string value of [{}] for the field [{}] correctly, relying on Object.toString()", value.getClass().getName(), name);
            }

            document.add(new Field(name, stringValue, shouldStore ? Field.Store.YES : Field.Store.NO, shouldAnalyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED));
        }

        private void addIndexDocument( IndexWriter iwriter, Document document )
        {
            try {
                iwriter.addDocument(document);
            } catch (Throwable x) {
                this.logger.error("Caught an exception:", x);
            }
        }

        private void commitIndex( IndexWriter iwriter )
        {
            try {
                iwriter.optimize();
                iwriter.commit();
                iwriter.close();
            } catch (Throwable x) {
                this.logger.error("Caught an exception:", x);
            }
        }
    }

    public void initialize()
    {
        try {
            EFOOntologyHelper efoHelper = new EFOOntologyHelper(this.getApplication().getResource("/WEB-INF/classes/efo.owl").openStream());

            Map<String, Set<String>> efoFullExpansionMap = efoHelper.getFullOntologyExpansionMap();
            Map<String, Set<String>> efoSynonymMap = efoHelper.getSynonymMap();

            EFOExpansionLookupIndex ix = new EFOExpansionLookupIndex(getPreferences().getString("ae.efo.index.location"));
            ix.addMaps(efoSynonymMap, efoFullExpansionMap);

            ((SearchEngine)getComponent("SearchEngine")).getController().setQueryExpander(new EFOQueryExpander(ix));

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
    }
}
