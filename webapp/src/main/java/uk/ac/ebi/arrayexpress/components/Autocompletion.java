package uk.ac.ebi.arrayexpress.components;

import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteData;
import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteStore;
import uk.ac.ebi.arrayexpress.utils.efo.EFONode;
import uk.ac.ebi.arrayexpress.utils.efo.IEFO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class Autocompletion extends ApplicationComponent {
	private Map<String, AutocompleteStore> autocompleteStore;

//	private BioSamplesGroup bioSamplesGroup;
//	private BioSamplesSample bioSamplesSample;
	private BioStudies bioStudies;
	private SearchEngine search;
	private IEFO efo;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public Autocompletion() {
	}

	public void initialize() throws Exception {

		// TODO:The EFO is repeated in each index (I dont know if it will works
		// so, for now, I will do it in a easiest way)
		this.autocompleteStore = new HashMap<String, AutocompleteStore>();

//		this.bioSamplesGroup = (BioSamplesGroup) getComponent("BioSamplesGroup");
//		this.bioSamplesSample = (BioSamplesSample) getComponent("BioSamplesSample");
		this.bioStudies = (BioStudies) getComponent("BioStudies");
		this.search = (SearchEngine) getComponent("SearchEngine");

		this.autocompleteStore.put(bioStudies.INDEX_ID,
				new AutocompleteStore());
		

	}

	public void terminate() throws Exception {
	}

	private IEFO getEfo() {
		return this.efo;
	}

	public void setEfo(IEFO efo) {
		this.efo = efo;
	}

	public String getKeywords(String IndexId, String prefix, String field,
			Integer limit) {
		StringBuilder sb = new StringBuilder("");
		List<AutocompleteData> matches = getStore(IndexId).findCompletions(
				prefix, field, limit);
		for (AutocompleteData match : matches) {
			sb.append(match.getText()).append('|').append(match.getDataType())
					.append('|').append(match.getData()).append('\n');
		}
		return sb.toString();
	}

	public String getEfoChildren(String efoId) {
		StringBuilder sb = new StringBuilder();

		if (null != getEfo()) {
			EFONode node = getEfo().getMap().get(efoId);
			if (null != node) {
				Set<EFONode> children = node.getChildren();
				if (null != children) {
					for (EFONode child : children) {
						sb.append(child.getTerm()).append("|o|");
						if (child.hasChildren()) {
							sb.append(child.getId());
						}
						sb.append("\n");
					}
				}
			}
		}
		return sb.toString();
	}

	// TODO: this in being called associated to BioSampleGroup, but now is
	// relates also to BioSamplesSample - I need to change this
	public void rebuild() throws IOException {
		rebuildBioStudies();
		//rebuildBioSamplesSample();
	}

	public void rebuildBioStudies() throws IOException {
		if (getStore(bioStudies.INDEX_ID) != null) {
			getStore(bioStudies.INDEX_ID).clear();
		}

		Set<String> fields = search.getController().getFieldNames(
				bioStudies.INDEX_ID);
		for (String field : fields) {
			String fieldTitle = search.getController().getFieldTitle(
					bioStudies.INDEX_ID, field);
			if (!field.endsWith("sort") && null != fieldTitle && fieldTitle.length() > 0) {
				getStore(bioStudies.INDEX_ID).addData(
						new AutocompleteData(field,
								AutocompleteData.DATA_FIELD, fieldTitle));
			}
			String fieldType = search.getController().getFieldType(
					bioStudies.INDEX_ID, field);
			if (null != fieldType
					&& !"integer".equals(fieldType)
					&& search.getController().isFieldAutoCompletion(
							bioStudies.INDEX_ID, field)) {
				for (String term : search.getController().getTerms(
						bioStudies.INDEX_ID, field,
						"keywords".equals(field) ? 10 : 1)) {
					getStore(bioStudies.INDEX_ID).addData(
							new AutocompleteData(term,
									AutocompleteData.DATA_TEXT, field));
				}
			}
		}
		
		
		//I need to get all the different attributeNames

		for (String term : search.getController().getTerms(
				bioStudies.INDEX_ID, "attributeNames",1)){
			String newField=term;
			getStore(bioStudies.INDEX_ID).addData(
					new AutocompleteData(newField,
							AutocompleteData.DATA_FIELD, ""));
			System.out.println("nem att:->" + newField);
		
		}
		
		
		// System.out.println("autocompletion rebuild");
		Iterator<String> itr = search.getController()
				.getFields(bioStudies.INDEX_ID).iterator();
		while (itr.hasNext()) {
			String field = itr.next();
			//If exists on the configuration I Will igone it (it was added previously)
			if (!field.endsWith("sort") && !search.getController().doesFieldExist(bioStudies.INDEX_ID, field)) {
				getStore(bioStudies.INDEX_ID).addData(
						new AutocompleteData(field,
								AutocompleteData.DATA_FIELD, ""));
				
				for (String term : search.getController().getTerms(
						bioStudies.INDEX_ID, field,
						"keywords".equals(field) ? 10 : 1)) {
					getStore(bioStudies.INDEX_ID).addData(
						new AutocompleteData(term,
								AutocompleteData.DATA_TEXT, field));
				//System.out.println("add->" +field+"-->" + term );
				}
				//System.out.println("fieldName->" + field);
			}
		}


		
		// TODO:The EFO is repeated in each index
		if (null != getEfo()) {
			addEfoNodeWithDescendants(bioStudies.INDEX_ID, IEFO.ROOT_ID);
		}
	}



	
	
	
	private void addEfoNodeWithDescendants(String indexId, String nodeId) {
		EFONode node = getEfo().getMap().get(nodeId);
		// make node expandable only if it has children and not organizational
		// class
		if (null != node) {
			getStore(indexId).addData(
					new AutocompleteData(node.getTerm(),
							AutocompleteData.DATA_EFO_NODE, node.hasChildren()
									&& !node.isOrganizationalClass() ? node
									.getId() : ""));

			if (node.hasChildren()) {
				for (EFONode child : node.getChildren()) {
					addEfoNodeWithDescendants(indexId, child.getId());
				}
			}
		}
	}

	private AutocompleteStore getStore(String indexId) {
		return this.autocompleteStore.get(indexId);
	}
}
