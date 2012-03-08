package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class IndexEnvironmentFactory {

	public static AbstractIndexEnvironment getIndexEnvironment(String name,
			HierarchicalConfiguration indexConfig) {
		if (name.equalsIgnoreCase("experiments")) {
			return new IndexEnvironmentExperiments(indexConfig);
		} else {

			if (name.equalsIgnoreCase("protocols")) {
				return new IndexEnvironmentProtocols(indexConfig);
			} else if (name.equalsIgnoreCase("arrays")) {
				return new IndexEnvironmentArrayDesigns(indexConfig);
			} else if (name.equalsIgnoreCase("files")) {
				return new IndexEnvironmentFiles(indexConfig);
			} else {
				return new IndexEnvironment(indexConfig);
			}

		}
	}

}
