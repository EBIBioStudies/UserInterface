package uk.ac.ebi.fg.biostudies.components;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.ApplicationComponent;
import uk.ac.ebi.fg.biostudies.utils.StringTools;
import uk.ac.ebi.fg.biostudies.utils.SynonymsFileReader;
import uk.ac.ebi.fg.biostudies.utils.efo.EFOLoader;
import uk.ac.ebi.fg.biostudies.utils.efo.EFONode;
import uk.ac.ebi.fg.biostudies.utils.efo.IEFO;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.Controller;
import uk.ac.ebi.fg.biostudies.utils.search.EFOExpandedHighlighter;
import uk.ac.ebi.fg.biostudies.utils.search.EFOExpansionLookupIndex;
import uk.ac.ebi.fg.biostudies.utils.search.EFOQueryExpander;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Ontologies extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IEFO efo;
    private EFOExpansionLookupIndex lookupIndex;

    private SearchEngine search;
    private Autocompletion autocompletion;

    public Ontologies()
    {
    }

    public void initialize() throws Exception
    {
        this.search = (SearchEngine) getComponent("SearchEngine");
        this.autocompletion = (Autocompletion) getComponent("Autocompletion");
        initLookupIndex();
        ((JobsController) getComponent("JobsController")).scheduleJobAtStart("reload-efo");
    }

    public void terminate() throws Exception
    {
    }

    public void update( InputStream ontologyStream ) throws IOException, InterruptedException
    {
        // load custom synonyms to lookup index
        loadCustomSynonyms();

        this.efo = removeIgnoredClasses(new EFOLoader().load(ontologyStream));

        this.lookupIndex.setEfo(getEfo());
        this.lookupIndex.buildIndex();

        if (null != this.autocompletion) {
            this.autocompletion.setEfo(getEfo());
            this.autocompletion.rebuild();
        }
    }

    public IEFO getEfo()
    {
        return this.efo;
    }

    private void loadCustomSynonyms() throws IOException
    {
        String synFileLocation = getPreferences().getString("bs.efo.synonyms");
        if (null != synFileLocation) {
            InputStream is = null;
            try {
                is = getApplication().getResource(synFileLocation).openStream();
                Map<String, Set<String>> synonyms = new SynonymsFileReader(new InputStreamReader(is)).readSynonyms();
                this.lookupIndex.setCustomSynonyms(synonyms);
                logger.debug("Loaded custom synonyms from [{}]", synFileLocation);
            } finally {
                if (null != is) {
                    is.close();
                }
            }
        }
    }

    private IEFO removeIgnoredClasses( IEFO efo ) throws IOException
    {
        String ignoreListFileLocation = getPreferences().getString("bs.efo.ignoreList");
        if (null != ignoreListFileLocation) {
            InputStream is = null;
            try {
                is = getApplication().getResource(ignoreListFileLocation).openStream();
                Set<String> ignoreList = StringTools.streamToStringSet(is, "UTF-8");

                logger.debug("Loaded EFO ignored classes from [{}]", ignoreListFileLocation);
                for (String id : ignoreList) {
                    if (null != id && !"".equals(id) && !id.startsWith("#") && efo.getMap().containsKey(id)) {
                        removeEFONode(efo, id);
                    }
                }
            } finally {
                if (null != is) {
                    is.close();
                }
            }
        }
        return efo;
    }

    private void removeEFONode( IEFO efo, String nodeId )
    {
        EFONode node = efo.getMap().get(nodeId);
        // step 1: for all parents remove node as a child
        for (EFONode parent : node.getParents()) {
            parent.getChildren().remove(node);
        }
        // step 2: for all children remove node as a parent; is child node has no other parents, remove it completely
        for (EFONode child : node.getChildren()) {
            child.getParents().remove(node);
            if (0 == child.getParents().size()) {
                removeEFONode(efo, child.getId());
            }
        }

        // step 3: remove node from efo map
        efo.getMap().remove(nodeId);
        logger.debug("Removed [{}] -> [{}]", node.getId(), node.getTerm());
    }

    private void initLookupIndex() throws IOException
    {
        Set<String> stopWords = new HashSet<String>();
        String[] words = getPreferences().getString("bs.efo.stopWords").split("\\s*,\\s*");
        if (null != words && words.length > 0) {
            stopWords.addAll(Arrays.asList(words));
        }
        this.lookupIndex = new EFOExpansionLookupIndex(
                getPreferences().getString("bs.efo.index.location")
                , stopWords
        );

        Controller c = search.getController();
        c.setQueryExpander(new EFOQueryExpander(this.lookupIndex));
        c.setQueryHighlighter(new EFOExpandedHighlighter());
    }
}
