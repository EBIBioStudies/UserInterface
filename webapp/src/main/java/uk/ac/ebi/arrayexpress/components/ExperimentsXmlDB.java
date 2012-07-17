package uk.ac.ebi.arrayexpress.components;

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

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.xpath.XPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.components.Events.IEventInformation;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.persistence.FilePersistence;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentUpdater;
import uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions;
import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
import uk.ac.ebi.arrayexpress.utils.saxon.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentExperiments;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.URL;
import java.util.*;

public class ExperimentsXmlDB extends ApplicationComponent implements
		IDocumentSource {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RegexHelper ARRAY_ACCESSION_REGEX = new RegexHelper(
			"^[aA]-\\w{4}-\\d+$", "");

	// private FilePersistence<PersistableDocumentContainer> document;
	private FilePersistence<PersistableStringList> experimentsInAtlas;
	private FilePersistence<PersistableString> species;
	private FilePersistence<PersistableString> arrays;
	private Map<String, String> assaysByMolecule;
	private Map<String, String> assaysByInstrument;

	private SaxonEngine saxon;
	private SearchEngine search;
	private Events events;
	private Autocompletion autocompletion;
	
	private boolean buildIndexes;


	public final String INDEX_ID = "experimentsxmldb";

	public enum ExperimentSource {
		AE1, AE2;

		public String getStylesheetName() {
			switch (this) {
			case AE1:
				return "preprocess-experiments-ae1-xml.xsl";
			case AE2:
				return "preprocess-experiments-ae2-xml.xsl";
			}
			return null;
		}

		public String toString() {
			switch (this) {
			case AE1:
				return "AE1";
			case AE2:
				return "AE2";
			}
			return null;

		}
	}

	

	public static class UpdateSourceInformation implements IEventInformation {
		private ExperimentSource source;
		private String location = null;
		private Long lastModified = null;
		private boolean outcome;

		public UpdateSourceInformation(ExperimentSource source, File sourceFile) {
			this.source = source;
			if (null != sourceFile && sourceFile.exists()) {
				this.location = sourceFile.getAbsolutePath();
				this.lastModified = sourceFile.lastModified();
			}
		}

		public UpdateSourceInformation(ExperimentSource source,
				String location, Long lastModified) {
			this.source = source;
			this.location = location;
			this.lastModified = lastModified;
		}

		public void setOutcome(boolean outcome) {
			this.outcome = outcome;
		}

		public ExperimentSource getSource() {
			return this.source;
		}

		public DocumentInfo getEventXML() throws Exception {
			String xml = "<?xml version=\"1.0\"?><event><category>experiments-update-"
					+ this.source.toString().toLowerCase()
					+ "</category><location>"
					+ this.location
					+ "</location><lastmodified>"
					+ StringTools.longDateTimeToXSDDateTime(lastModified)
					+ "</lastmodified><successful>"
					+ (this.outcome ? "true" : "false")
					+ "</successful></event>";

			return ((SaxonEngine) Application.getAppComponent("SaxonEngine"))
					.buildDocument(xml);
		}
	}

	public ExperimentsXmlDB() {
		
	}

	public void initialize() throws Exception {
		buildIndexes= Application
				.getInstance().getPreferences().getBoolean("ae.buildLuceneIndexes");
		
		this.saxon = (SaxonEngine) getComponent("SaxonEngine");
		this.search = (SearchEngine) getComponent("SearchEngine");
		this.events = (Events) getComponent("Events");
		this.autocompletion = (Autocompletion) getComponent("Autocompletion");

		// this.document = new FilePersistence<PersistableDocumentContainer>(
		// new PersistableDocumentContainer("experiments"), new File(
		// getPreferences().getString(
		// "ae.experiments.persistence-location")));

		
		this.experimentsInAtlas = new FilePersistence<PersistableStringList>(
				new PersistableStringList(), new File(getPreferences()
						.getString("bs.atlasexperiments.persistence-location")));

		this.species = new FilePersistence<PersistableString>(
				new PersistableString(), new File(getPreferences().getString(
						"bs.species.dropdown-html-location"))

		);

		this.arrays = new FilePersistence<PersistableString>(
				new PersistableString(), new File(getPreferences().getString(
						"bs.arrays.dropdown-html-location")));

		this.assaysByMolecule = new HashMap<String, String>();
		this.assaysByMolecule
				.put("",
						"<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;metabolomic profiling&quot;\">Metabolite assay</option><option value=\"&quot;protein assay&quot;\">Protein assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
		this.assaysByMolecule
				.put("array assay",
						"<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
		this.assaysByMolecule
				.put("high throughput sequencing assay",
						"<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
		this.assaysByMolecule
				.put("proteomic profiling by mass spectrometer",
						"<option value=\"&quot;protein assay&quot;\">Protein assay</option>");

		this.assaysByInstrument = new HashMap<String, String>();
		this.assaysByInstrument
				.put("",
						"<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option><option value=\"&quot;proteomic profiling by mass spectrometer&quot;\">Mass spectrometer</option>");
		this.assaysByInstrument
				.put("DNA assay",
						"<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option>");
		this.assaysByInstrument.put("metabolomic profiling",
				"<option value=\"\">All technologies</option>");
		this.assaysByInstrument
				.put("protein assay",
						"<option value=\"\">All technologies</option><option value=\"&quot;proteomic profiling by mass spectrometer&quot;\">Mass spectrometer</option>");
		this.assaysByInstrument
				.put("RNA assay",
						"<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option>");

		//int numberOfExperiments=calculateNumberOfExperiments(docTemp);
		if(buildIndexes){
		DocumentInfo docTemp = getXmlFromFile(new File(getPreferences()
				.getString("bs.experiments.persistence-location")));

		updateIndex(docTemp);
		docTemp = null;
		}
		else{
			// null parameter means that I will read the index
			updateIndex(null);
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
	public String getDocumentURI() {
		return "experiments.xml";
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

	public boolean isAccessible(String accession, List<String> userIds)
			throws Exception {
		for (String userId : userIds) {
			if ("0".equals(userId) // superuser
					|| ARRAY_ACCESSION_REGEX.test(accession) // todo: check
																// array
																// accessions
																// against
																// arrays
					|| Boolean.parseBoolean( // tests document for access
							saxon.evaluateXPathSingle(
									//
									getDocument() //
									,
									"exists(/experiments/experiment[accession = '"
											+ accession + "' and user/@id = '"
											+ userId + "'])"))) {
				return true;
			}
		}
		return false;
	}

	public String getSpecies() throws Exception {
		return this.species.getObject().get();
	}

	public String getArrays() throws Exception {
		return this.arrays.getObject().get();
	}

	public String getAssaysByMolecule(String key) {
		return this.assaysByMolecule.get(key);
	}

	public String getAssaysByInstrument(String key) {
		return this.assaysByInstrument.get(key);
	}

	public void update(String xmlString,
			UpdateSourceInformation sourceInformation) throws Exception {
		boolean success = false;
		try {
			DocumentInfo updateDoc = this.saxon.transform(xmlString,
					sourceInformation.getSource().getStylesheetName(), null);
			if (null != updateDoc) {
				new DocumentUpdater(this, updateDoc).update();
				success = true;
			}
		} finally {
			sourceInformation.setOutcome(success);
			events.addEvent(sourceInformation);
		}
	}

	//TODO see when this is called (its called from a job)
	public void reloadExperimentsInAtlas(String sourceLocation)
			throws Exception {
		URL source = new URL(sourceLocation);
		String result = this.saxon.transformToString(source,
				"preprocess-atlas-experiments-txt.xsl", null);
		if (null != result) {
			String[] exps = result.split("\n");
			if (exps.length > 0) {
				this.experimentsInAtlas.setObject(new PersistableStringList(
						Arrays.asList(exps)));
				updateAccelerators(this.getDocument());
				this.logger.info("Stored GXA info, [{}] experiments listed",
						exps.length);
			} else {
				this.logger
						.warn("Atlas returned [0] experiments listed, will NOT update our info");
			}
		}
	}

	private void updateIndex(DocumentInfo doc) {
		try {
			
			this.search.getController().indexFromXmlDB(INDEX_ID, buildIndexes);
			
			
			
//			TODO review the autocompletion because of time it takes (maybe the problem is the xml field)
			this.autocompletion.rebuild();
		} catch (Exception x) {
			this.logger.error("Caught an exception:", x);
		}
	}

	private void updateAccelerators(DocumentInfo docInfo) {
		this.logger.debug("Updating accelerators for experiments");

		ExtFunctions.clearAccelerator("is-in-atlas");
		ExtFunctions.clearAccelerator("visible-experiments");
		ExtFunctions.clearAccelerator("experiments-for-protocol");
		ExtFunctions.clearAccelerator("experiments-for-array");
		try {
			for (String accession : this.experimentsInAtlas.getObject()) {
				ExtFunctions.addAcceleratorValue("is-in-atlas", accession, "1");
			}

			XPath xp = new XPathEvaluator(docInfo.getConfiguration());
			XPathExpression xpe = xp
					.compile("/experiments/experiment[source/@visible = 'true']");
			List documentNodes = (List) xpe.evaluate(docInfo,
					XPathConstants.NODESET);

			XPathExpression accessionXpe = xp.compile("accession");
			XPathExpression protocolIdsXpe = xp.compile("protocol/id");
			XPathExpression arrayAccXpe = xp.compile("arraydesign/accession");
			for (Object node : documentNodes) {

				try {
					// get all the expressions taken care of
					String accession = accessionXpe.evaluate(node);
					ExtFunctions.addAcceleratorValue("visible-experiments",
							accession, node);
					List protocolIds = (List) protocolIdsXpe.evaluate(node,
							XPathConstants.NODESET);
					if (null != protocolIds) {
						for (Object protocolId : protocolIds) {
							String id = ((ValueRepresentation) protocolId)
									.getStringValue();
							Set<String> experimentsForProtocol = (Set<String>) ExtFunctions
									.getAcceleratorValue(
											"experiments-for-protocol", id);
							if (null == experimentsForProtocol) {
								experimentsForProtocol = new HashSet<String>();
								ExtFunctions.addAcceleratorValue(
										"experiments-for-protocol", id,
										experimentsForProtocol);
							}
							experimentsForProtocol.add(accession);
						}
					}
					List arrayAccessions = (List) arrayAccXpe.evaluate(node,
							XPathConstants.NODESET);
					if (null != arrayAccessions) {
						for (Object arrayAccession : arrayAccessions) {
							String arrayAcc = ((ValueRepresentation) arrayAccession)
									.getStringValue();
							Set<String> experimentsForArray = (Set<String>) ExtFunctions
									.getAcceleratorValue(
											"experiments-for-array", arrayAcc);
							if (null == experimentsForArray) {
								experimentsForArray = new HashSet<String>();
								ExtFunctions.addAcceleratorValue(
										"experiments-for-array", arrayAcc,
										experimentsForArray);
							}
							experimentsForArray.add(accession);
						}
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

	private void buildSpeciesArrays() throws Exception {
		// todo: move this to a separate component (autocompletion?)
		String speciesString = saxon.transformToString(this.getDocument(),
				"build-species-list-html.xsl", null);
		this.species.setObject(new PersistableString(speciesString));

		String arraysString = saxon.transformToString(this.getDocument(),
				"build-arrays-list-html.xsl", null);
		this.arrays.setObject(new PersistableString(arraysString));
	}

	//TODO move this to anoher place (i will not do now because i dont know from where I will read the xml source... maybe in the future I will not use the file!!
	public DocumentInfo getXmlFromFile(File file) throws Exception {

		Configuration config = ((SaxonEngine) Application
				.getAppComponent("SaxonEngine")).trFactory.getConfiguration();
		DocumentInfo doc = null;
		doc = config.buildDocument(new StreamSource(file));

		return doc;
	}

	
	@Deprecated
	private int calculateNumberOfExperiments(DocumentInfo doc) throws Exception {
		 Long totalExperiments = Long.parseLong(((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(doc, "count(//experiment[source/@visible = 'true' and user/@id = '1'])"));
		return totalExperiments.intValue();
		
	}

	
	
	
	
	/*
	//TODO: rPE
	public void updateExperimentsStats(DocumentInfo doc) throws Exception {
		 Long totalExperiments = Long.parseLong(((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(doc, "count(//experiment[source/@visible = 'true' and user/@id = '1'])"));
		 setExperimentsNumber(totalExperiments);
		 System.out.println("Number of experiments->" + totalExperiments);
		 //Long totalAssays = Long.parseLong(((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(doc, "sum((if(//experiment[source/@visible = 'true']/assays castable as xs:integer) then  //experiment[source/@visible]/assays else 0) cast as xs:integer)"));
//		 Long totalAssays = Long.parseLong(((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(doc, "sum((if(//experiment[source/@visible = 'true' and user/@id = '1']/assays castable as xs:integer) then  //experiment[source/@visible = 'true' and user/@id = '1']/assays  else 0) cast as xs:integer)"));
		 
//		 TODO rpe
		 //Long totalAssays = Long.parseLong(((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(doc, "sum(//experiment[source/@visible = 'true' and user/@id = '1']/assays)"));
		 Long totalAssays=new Long(999);
		 setAssaysNumber(totalAssays);
		 //		 setAssaysNumber(totalAssays);
//		 System.out.println("Number of assays->" + totalAssays);
	
		 IndexEnvironmentExperiments indexExp=(IndexEnvironmentExperiments)this.search.getController().getEnvironment("experiments");
		 indexExp.setCountDocuments(totalExperiments.intValue());

		
	}
	*/
}
