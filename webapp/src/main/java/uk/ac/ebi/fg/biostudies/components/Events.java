package uk.ac.ebi.fg.biostudies.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.ApplicationComponent;
import uk.ac.ebi.fg.biostudies.utils.persistence.FilePersistence;
import uk.ac.ebi.fg.biostudies.utils.saxon.DocumentUpdater;
import uk.ac.ebi.fg.biostudies.utils.saxon.IDocumentSource;
import uk.ac.ebi.fg.biostudies.utils.saxon.PersistableDocumentContainer;

import java.io.File;

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

public class Events extends ApplicationComponent implements IDocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FilePersistence<PersistableDocumentContainer> document;
    private SaxonEngine saxon;
    private SearchEngine search;

    public final String INDEX_ID = "events";

    public static interface IEventInformation
    {
        public abstract DocumentInfo getEventXML() throws Exception;
    }

    public Events()
    {
    }

    public void initialize() throws Exception
    {
        this.saxon = (SaxonEngine) getComponent("SaxonEngine");
        this.search = (SearchEngine) getComponent("SearchEngine");

        this.document = new FilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer("events")
                , new File(getPreferences().getString("bs.events.persistence-location"))
        );

        updateIndex();
        this.saxon.registerDocumentSource(this);
    }

    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    public String getDocumentURI()
    {
        return "events.xml";
    }

    // implementation of IDocumentSource.getDocument()
    public synchronized DocumentInfo getDocument() throws Exception
    {
        return this.document.getObject().getDocument();
    }

    // implementation of IDocumentSource.setDocument(DocumentInfo)
    public synchronized void setDocument( DocumentInfo doc ) throws Exception
    {
        if (null != doc) {
            this.document.setObject(new PersistableDocumentContainer("events", doc));
            updateIndex();
        } else {
            this.logger.error("Events NOT updated, NULL document passed");
        }
    }

    public void addEvent( IEventInformation event ) throws Exception
    {
        DocumentInfo eventDoc = event.getEventXML();
        if (null != eventDoc) {
            new DocumentUpdater(this, eventDoc).update();
        }
    }
    
    private void updateIndex()
    {
        try {
            this.search.getController().index(INDEX_ID, this.getDocument());
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
    }
}