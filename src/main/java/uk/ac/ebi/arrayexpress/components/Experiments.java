package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableExperimentsContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableExperimentsContainer> experiments;
    Map<Integer, NodeInfo> experimentsMap = new HashMap<Integer, NodeInfo>();
    //private ExperimentList experiments;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        experiments = new TextFilePersistence<PersistableExperimentsContainer>(
                new PersistableExperimentsContainer()
                , new File(tmpDir, getPreferences().getString("ae.experiments.cache.filename"))
        );

        DocumentInfo rootNode = experiments.getObject().getDocument();
        NodeInfo experimentsNode = (NodeInfo)rootNode.iterateAxis(Axis.CHILD).next();
        if (null != experimentsNode) {
            AxisIterator children = experimentsNode.iterateAxis(Axis.CHILD);
            while (true) {
                NodeInfo c = (NodeInfo)children.next();
                if (null == c) {
                    break;
                }
                AxisIterator children2 = c.iterateAxis(Axis.CHILD);
                NodeInfo idNode = (NodeInfo)children2.next();
                if (null != idNode) {
                    Integer idValue = Integer.decode(idNode.getStringValue());
                    if (null != idValue) {
                        experimentsMap.put(idValue, c);
                    } else {
                        logger.error("Unable to convert value [{}] of node [{}] to integer", idNode.getStringValue(), Navigator.getPath(idNode));
                    }
                } else {
                    logger.error("Unable to find any children for node [{}]", Navigator.getPath(c));
                }
            }
        }
        indexExperiments();
        
//        experimentsXml = new TextFilePersistence<PersistableString>(
//            new PersistableString()
//            , new File(tmpDir, "ae-experiments-2.xml")
//        );
//        if (!experimentsXml.getObject().isEmpty()) {
//            experiments = new ExperimentParser(ExperimentParserMode.MULTIPLE_EXPERIMENTS).parseMultiple(
//                experimentsXml.getObject().get()
//            );
//        }
    }

    public void terminate()
    {
    }

    public synchronized DocumentInfo getExperiments()
    {
        return experiments.getObject().getDocument();
    }

    public boolean isAccessible( String accession, String userId )
    {
        return false;
    }

    public boolean isInWarehouse( String accession )
    {
        return false;
    }

    public String getSpecies()
    {
        return "";
    }

    public String getArrays()
    {
        return "";
    }

    public String getExperimentTypes()
    {
        return "";
    }

    public String getDataSource()
    {
        if (null == dataSource) {
            dataSource = getPreferences().getString("ae.experiments.datasources");
        }

        return dataSource;
    }

    public void setDataSource( String ds )
    {
        dataSource = ds;
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);
        }
//        experimentsXml.setObject(new PersistableString(xmlString));
//        experiments = new ExperimentParser(ExperimentParserMode.MULTIPLE_EXPERIMENTS).parseMultiple(xmlString);
//        if (null == experiments) {
//            logger.error("Null experiments received, expect problems down the road");
//        }
    }

    public void updateFiles()
    {
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
    }

    public Map<Integer,NodeInfo> getExperimentsMap()
    {
        return experimentsMap;
    }

    private class ExperimentsMapIterator implements LookaheadIterator, LastPositionFinder
    {

        private Map<Integer,NodeInfo> experimentsMap = ((Experiments) Application.getAppComponent("Experiments")).getExperimentsMap();
        private List<Integer> idsList = null;
        private NodeInfo currentNode = null;
        private int currentPosition = 0;

        public ExperimentsMapIterator(List<Integer> idsList)
        {
            this.idsList = idsList;
        }

        /**
        * Get the last position (that is, the number of items in the sequence). This method is
        * non-destructive: it does not change the state of the iterator.
        * The result is undefined if the next() method of the iterator has already returned null.
        * This method must not be called unless the result of getProperties() on the iterator
         * includes the bit setting {@link #LAST_POSITION_FINDER}
        */

        public int getLastPosition() throws XPathException
        {
            if (-1 == currentPosition || null == idsList) {
                return 0;
            }
            
            return idsList.size();
        }


        /**
         * Determine whether there are more items to come. Note that this operation
         * is stateless and it is not necessary (or usual) to call it before calling
         * next(). It is used only when there is an explicit need to tell if we
         * are at the last element.
         * <p/>
         * This method must not be called unless the result of getProperties() on the iterator
         * includes the bit setting {@link SequenceIterator#LOOKAHEAD}
         *
         * @return true if there are more items in the sequence
         */

        public boolean hasNext()
        {
            return currentPosition != -1 && currentPosition < idsList.size();
        }

        /**
         * Get the next item in the sequence. This method changes the state of the
         * iterator, in particular it affects the result of subsequent calls of
         * position() and current().
         * @throws XPathException if an error occurs retrieving the next item
         * @return the next item, or null if there are no more items. Once a call
         * on next() has returned null, no further calls should be made. The preferred
         * action for an iterator if subsequent calls on next() are made is to return
         * null again, and all implementations within Saxon follow this rule.
         * @since 8.4
         */

        public Item next() throws XPathException
        {
            if (hasNext()) {
                currentNode = experimentsMap.get(idsList.get(currentPosition));
                currentPosition++;
            } else {
                currentNode = null;
                currentPosition = -1;
            }
            return currentNode;
        }

        /**
         * Get the current value in the sequence (the one returned by the
         * most recent call on next()). This will be null before the first
         * call of next(). This method does not change the state of the iterator.
         *
         * @return the current item, the one most recently returned by a call on
         *     next(). Returns null if next() has not been called, or if the end
         *     of the sequence has been reached.
         * @since 8.4
         */

        public Item current()
        {
            return currentNode;
        }

        /**
         * Get the current position. This will usually be zero before the first call
         * on next(), otherwise it will be the number of times that next() has
         * been called. Once next() has returned null, the preferred action is
         * for subsequent calls on position() to return -1, but not all existing
         * implementations follow this practice. (In particular, the EmptyIterator
         * is stateless, and always returns 0 as the value of position(), whether
         * or not next() has been called.)
         * <p>
         * This method does not change the state of the iterator.
         *
         * @return the current position, the position of the item returned by the
         *     most recent call of next(). This is 1 after next() has been successfully
         *     called once, 2 after it has been called twice, and so on. If next() has
         *     never been called, the method returns zero. If the end of the sequence
         *     has been reached, the value returned will always be <= 0; the preferred
         *     value is -1.
         *
         * @since 8.4
         */

        public int position() {
            return currentPosition;
        }

        /**
         * Close the iterator. This indicates to the supplier of the data that the client
         * does not require any more items to be delivered by the iterator. This may enable the
         * supplier to release resources. After calling close(), no further calls on the
         * iterator should be made; if further calls are made, the effect of such calls is undefined.
         *
         * <p>(Currently, closing an iterator is important only when the data is being "pushed" in
         * another thread. Closing the iterator terminates that thread and means that it needs to do
         * no additional work. Indeed, failing to close the iterator may cause the push thread to hang
         * waiting for the buffer to be emptied.)</p>
         */

        public void close()
        {
            // notthing here
        }

        /**
         * Get another SequenceIterator that iterates over the same items as the original,
         * but which is repositioned at the start of the sequence.
         * <p>
         * This method allows access to all the items in the sequence without disturbing the
         * current position of the iterator. Internally, its main use is in evaluating the last()
         * function.
         * <p>
         * This method does not change the state of the iterator.
         *
         * @exception XPathException if any error occurs
         * @return a SequenceIterator that iterates over the same items,
         *     positioned before the first item
         * @since 8.4
         */

        public SequenceIterator getAnother() throws XPathException
        {
            return new ExperimentsMapIterator(idsList);
        }

        /**
         * Get properties of this iterator, as a bit-significant integer.
         * @return the properties of this iterator. This will be some combination of
         * properties such as {@link #GROUNDED}, {@link #LAST_POSITION_FINDER},
         * and {@link #LOOKAHEAD}. It is always
         * acceptable to return the value zero, indicating that there are no known special properties.
         * It is acceptable for the properties of the iterator to change depending on its state.
         * @since 8.6
         */

        public int getProperties()
        {
            return LOOKAHEAD | LAST_POSITION_FINDER;
        }

        /**
         * Property value: the iterator is "grounded". This means that (a) the
         * iterator must be an instance of {@link GroundedIterator}, and (b) the
         * implementation of the materialize() method must be efficient (in particular,
         * it should not involve the creation of new objects)
         */

        public static final int GROUNDED = 1;

        /**
         * Property value: the iterator knows the number of items that it will deliver.
         * This means that (a) the iterator must be an instance of {@link net.sf.saxon.expr.LastPositionFinder},
         * and (b) the implementation of the getLastPosition() method must be efficient (in particular,
         * it should take constant time, rather than time proportional to the length of the sequence)
         */

        public static final int LAST_POSITION_FINDER = 1<<1;

        /**
         * Property value: the iterator knows whether there are more items still to come. This means
         * that (a) the iterator must be an instance of {@link LookaheadIterator}, and (b) the
         * implementation of the hasNext() method must be efficient (more efficient than the client doing
         * it)
         */

        public static final int LOOKAHEAD = 1<<2;


    }

    public SequenceIterator getSequenceIterator()
    {
        return new ExperimentsMapIterator(new ArrayList<Integer>(experimentsMap.keySet()));
    }

    private synchronized void setExperiments( DocumentInfo doc )
    {
        if (null != doc ) {
            experiments.setObject(new PersistableExperimentsContainer(doc));
        } else {
            logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private DocumentInfo loadExperimentsFromString( String xmlString )
    {
        DocumentInfo doc = ((SaxonEngine) getComponent("SaxonEngine")).transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    private void indexExperiments()
    {
        ((SaxonEngine) getComponent("SaxonEngine")).transform(experiments.getObject().getDocument(), "index-experiments.xsl", null);    
    }
}
