package uk.ac.ebi.fg.biostudies.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.Arrays;
import java.util.List;

//field information, parsed
	public class FieldInfo {
		public String name;
		public String title;
		public String type;
		public String path;
		public boolean shouldAnalyze;
		public String analyzer;
		public boolean shouldStore;
		public boolean shouldEscape;
		public boolean shouldSort;
		public boolean shouldAutoCompletion;
		public List<String> sortFields;
		//this property means that thi s fiels will be not obtained by a direct Xpath expression ... the values will be obtained directly on the code
		public boolean process;

		public FieldInfo(HierarchicalConfiguration fieldConfig) {
			this.name = fieldConfig.getString("[@name]");
			this.title = fieldConfig.containsKey("[@title]") ? fieldConfig
					.getString("[@title]") : null;
			this.type = fieldConfig.getString("[@type]");
			this.path = fieldConfig.getString("[@path]");
			if ("string".equals(this.type)) {
				this.shouldAnalyze = fieldConfig.getBoolean("[@analyze]");
				this.analyzer = fieldConfig.getString("[@analyzer]");
				this.shouldEscape = fieldConfig.getBoolean("[@escape]");
			}
			this.shouldStore = fieldConfig.containsKey("[@store]") ? fieldConfig
					.getBoolean("[@store]") : false;

			// TODO: try to have debug information when i'm referring a field in
			// the sortfieldsattribute that does not is defined in the xml file
			if (fieldConfig.containsKey("[@sortfields]")) {
				this.shouldSort = true;
				String[] spl = fieldConfig.getString("[@sortfields]")
						.split(",");
				this.sortFields = Arrays.asList(spl);
			}

			if (fieldConfig.containsKey("[@autocompletion]")) {
				this.shouldAutoCompletion = false;
				if (fieldConfig.getBoolean("[@autocompletion]")) {
					this.shouldAutoCompletion = true;
				}

			}
			
			if (fieldConfig.containsKey("[@process]")) {
				this.process = false;
				if (fieldConfig.getBoolean("[@process]")) {
					this.process = true;
				}

			}
		}
	}
