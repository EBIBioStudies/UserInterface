package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.xpath.XPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.FilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentUpdater;
import uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions;
import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
import uk.ac.ebi.arrayexpress.utils.saxon.PersistableDocumentContainer;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.List;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
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

public class ArrayDesigns extends ApplicationComponent implements IDocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FilePersistence<PersistableDocumentContainer> document;
    private SaxonEngine saxon;
    private SearchEngine search;

    public final String INDEX_ID = "arrays";
    
    public enum ArrayDesignSource
    {
        AE1, AE2;

        public String getStylesheetName()
        {
            switch (this) {
                case AE1:   return "preprocess-arrays-ae1-xml.xsl";
                case AE2:   return "preprocess-arrays-ae2-xml.xsl";
            }
            return null;
        }
    }

    public ArrayDesigns()
    {
    }

    public void initialize() throws Exception
    {
        this.saxon = (SaxonEngine) getComponent("SaxonEngine");
        this.search = (SearchEngine) getComponent("SearchEngine");
        
//        this.document = new FilePersistence<PersistableDocumentContainer>(
//                new PersistableDocumentContainer("array_designs")
//                , new File(getPreferences().getString("ae.arrays.persistence-location"))
//        );

        
        DocumentInfo docTemp = getXmlFromFile(new File(getPreferences()
				.getString("ae.arrays.persistence-location")));

        updateIndex(docTemp);
//        updateAccelerators(docTemp);
        this.saxon.registerDocumentSource(this);
        docTemp=null;
    }

    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    public String getDocumentURI()
    {
        return "arrays.xml";
    }

    public synchronized DocumentInfo getDocument() throws Exception {
 		return getXmlFromFile(new File(getPreferences().getString(
 				"ae.arrays.persistence-location")));
 	}
 	
	// implementation of IDocumentSource.setDocument(DocumentInfo)
	public synchronized void setDocument(DocumentInfo doc) throws Exception {
		throw new UnsupportedOperationException("This is temporary situation, all Xml reference are being removed, and this methos wont be supported in the future!");
	}
	
	
    public void update( String xmlString, ArrayDesignSource source ) throws Exception
    {
        DocumentInfo updateDoc = this.saxon.transform(xmlString, source.getStylesheetName(), null);
        if (null != updateDoc) {
            new DocumentUpdater(this, updateDoc).update();
        }
    }

    private void updateIndex(DocumentInfo docInfo)
    {
        try {
            this.search.getController().index(INDEX_ID, docInfo);
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
    }

    private void updateAccelerators(DocumentInfo docInfo)
    {
        this.logger.debug("Updating accelerators for arrays");

        ExtFunctions.clearAccelerator("legacy-array-ids");
        try {
            XPath xp = new XPathEvaluator(docInfo.getConfiguration());
            XPathExpression xpe = xp.compile("/array_designs/array_design[@visible = 'true']");
            List documentNodes = (List) xpe.evaluate(docInfo, XPathConstants.NODESET);

            XPathExpression accessionXpe = xp.compile("accession");
            XPathExpression legacyIdsXpe = xp.compile("legacy_id");
            for (Object node : documentNodes) {

                try {
                    // get all the expressions taken care of
                    String accession = accessionXpe.evaluate(node);
                    List legacyIds = (List)legacyIdsXpe.evaluate(node, XPathConstants.NODESET);
                    if (null != legacyIds) {
                        ExtFunctions.addAcceleratorValue("legacy-array-ids", accession, legacyIds);
                    }
                } catch (XPathExpressionException x) {
                    this.logger.error("Caught an exception:", x);
                }
            }

            this.logger.debug("Accelerators updated");
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