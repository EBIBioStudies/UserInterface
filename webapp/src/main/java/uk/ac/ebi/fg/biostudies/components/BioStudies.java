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

package uk.ac.ebi.fg.biostudies.components;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.app.ApplicationComponent;
import uk.ac.ebi.fg.biostudies.utils.saxon.IDocumentSource;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentBioStudies;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.Indexer;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class BioStudies extends ApplicationComponent implements
		IDocumentSource {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SaxonEngine saxon;
	private SearchEngine search;
	private Autocompletion autocompletion;
	
	private boolean buildIndexes;


	public final String INDEX_ID = "biostudies";

	


	public BioStudies() {
		
	}

	public void initialize() throws Exception {
		buildIndexes= Application
				.getInstance().getPreferences().getBoolean("bs.buildLuceneIndexes");
		
		this.saxon = (SaxonEngine) getComponent("SaxonEngine");
		this.search = (SearchEngine) getComponent("SearchEngine");
		this.autocompletion = (Autocompletion) getComponent("Autocompletion");

		//TODO: change this (I will never restart the server with an incremental update, even though I should change it)
		if(buildIndexes) {
			updateIndex(Indexer.RebuildCategories.REBUILD);
		} else {
			updateIndex(Indexer.RebuildCategories.NOTREBUILD);
		}
					
			
		
//		TODO rpe
		//updateAccelerators(docTemp);
		this.saxon.registerDocumentSource(this);
	
		
		
		//
		// setExperimentsNumber(this.document.)
	}

	public void terminate() throws Exception {
	}

	// implementation of IDocumentSource.getDocumentURI()
	//TODO rpe (this must be reformulated)
	public String getDocumentURI() {
		return "BioSamplesGroup.xml";
	}

	// implementation of IDocumentSource.getDocument()
	// this is not the best way to do it ... but i dont know if in a near future
	// we will create a staging area and all the xml files references will
	// disappear, so
	public synchronized DocumentInfo getDocument() throws Exception {
		return getXmlFromFile(new File(getPreferences().getString(
				"bs.experiments.persistence-location")));
	}

	// implementation of IDocumentSource.setDocument(DocumentInfo)
	public synchronized void setDocument(DocumentInfo doc) throws Exception {
		throw new UnsupportedOperationException("This is temporary situation, all Xml reference are being removed, and this methos wont be supported in the future!");
	}

	



	private void updateIndex(Indexer.RebuildCategories rebuild) {
		try {
			
			this.search.getController().indexFromXmlDB(INDEX_ID, rebuild);	
			//I need to do this because when I initialize the server with the interface.application.lucene.indexes.build=true I need to restart the indexreader;
			this.search.getController().getEnvironment(INDEX_ID).setup();
			
//			TODO review the autocompletion because of time it takes (maybe the problem is the xml field)
			this.autocompletion.rebuildBioStudies();
		} catch (Exception x) {
			this.logger.error("Caught an exception:", x);
		}
	}

	
	
	//used to reload the index after the synchronization process
	public void reloadIndex() {
		try {
			//String indexLocationDirectory= this.search.getController().getEnvironment(INDEX_ID).indexLocationDirectory + "_" + System.currentTimeMillis();
			this.search.getController().indexFromXmlDB(INDEX_ID, Indexer.RebuildCategories.NOTREBUILD);		
			this.autocompletion.rebuildBioStudies();
		} catch (Exception x) {
			this.logger.error("Caught an exception:", x);
		}
	}
	


	//TODO move this to anoher place (i will not do now because i dont know from where I will read the xml source... maybe in the future I will not use the file!!
	public DocumentInfo getXmlFromFile(File file) throws Exception {
		Configuration config = ((SaxonEngine) Application
				.getAppComponent("SaxonEngine")).trFactory.getConfiguration();
		DocumentInfo doc = null;
		doc = config.buildDocument(new StreamSource(file));

		return doc;
	}

	
	@Override
	public String getMetaDataInformation(){
		
		String info=((IndexEnvironmentBioStudies)this.search.getController().getEnvironment(INDEX_ID)).getMetadataInformation();
    	return info;
    }
	

}
