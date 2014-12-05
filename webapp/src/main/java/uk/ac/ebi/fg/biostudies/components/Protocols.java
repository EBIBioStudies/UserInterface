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

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.app.ApplicationComponent;
import uk.ac.ebi.fg.biostudies.utils.persistence.FilePersistence;
import uk.ac.ebi.fg.biostudies.utils.saxon.DocumentUpdater;
import uk.ac.ebi.fg.biostudies.utils.saxon.IDocumentSource;
import uk.ac.ebi.fg.biostudies.utils.saxon.PersistableDocumentContainer;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class Protocols extends ApplicationComponent implements IDocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FilePersistence<PersistableDocumentContainer> document;
    private SaxonEngine saxon;
    private SearchEngine search;

    public final String INDEX_ID = "protocols";
	
    private boolean buildIndexes;

    public enum ProtocolsSource
    {
        AE1, AE2;

        public String getStylesheetName()
        {
            switch (this) {
                case AE1:   return "preprocess-protocols-ae1-xml.xsl";
                case AE2:   return "preprocess-protocols-ae2-xml.xsl";
            }
            return null;
        }
    }

    public Protocols()
    {
    }

    public void initialize() throws Exception
    {
    	buildIndexes= Application
				.getInstance().getPreferences().getBoolean("bs.buildLuceneIndexes");
        this.saxon = (SaxonEngine) getComponent("SaxonEngine");
        this.search = (SearchEngine) getComponent("SearchEngine");

//        this.document = new FilePersistence<PersistableDocumentContainer>(
//                new PersistableDocumentContainer("protocols")
//                , new File(getPreferences().getString("ae.protocols.persistence-location"))
//        );
        
        if(buildIndexes){
    		DocumentInfo docTemp = getXmlFromFile(new File(getPreferences()
    				.getString("bs.protocols.persistence-location")));

    		updateIndex(docTemp);
    		docTemp = null;
    		}
    		else{
    			// null parameter means that I will read the index
    			updateIndex(null);
    		}
        this.saxon.registerDocumentSource(this);

    }

    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    public String getDocumentURI()
    {
        return "protocols.xml";
    }

 // implementation of IDocumentSource.getDocument()
 	// this is not the best way to do it ... but i dont know if in a near future
 	// we will create a staging area and all the xml files references will
 	// disappear, so
 	public synchronized DocumentInfo getDocument() throws Exception {
 		return getXmlFromFile(new File(getPreferences().getString(
 				"ae.protocols.persistence-location")));
 	}
 	
	// implementation of IDocumentSource.setDocument(DocumentInfo)
	public synchronized void setDocument(DocumentInfo doc) throws Exception {
		throw new UnsupportedOperationException("This is temporary situation, all Xml reference are being removed, and this methos wont be supported in the future!");
	}

    public void update( String xmlString, ProtocolsSource source ) throws Exception
    {
        DocumentInfo updateDoc = this.saxon.transform(xmlString, source.getStylesheetName(), null);
        if (null != updateDoc) {
            new DocumentUpdater(this, updateDoc).update();
        }
    }

    private void updateIndex(DocumentInfo doc)
    {
        try {
            this.search.getController().index(INDEX_ID, doc);
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
    }
    
    //TODO put this method in aUtils Class
    public DocumentInfo getXmlFromFile(File file) throws Exception {

		Configuration config = ((SaxonEngine) Application
				.getAppComponent("SaxonEngine")).trFactory.getConfiguration();
		DocumentInfo doc = null;
		doc = config.buildDocument(new StreamSource(file));

		return doc;
	}

}