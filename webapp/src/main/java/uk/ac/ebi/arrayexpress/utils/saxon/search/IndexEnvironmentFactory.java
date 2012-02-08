package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class IndexEnvironmentFactory {

	
	
	public static IndexEnvironment getIndexEnvironment(String name,
			HierarchicalConfiguration indexConfig) {
		if (name.equalsIgnoreCase("experiments")) {
			return new ExperimentsIndexEnvironment(indexConfig);
		} else {
			return new IndexEnvironment(indexConfig);
		}
	}

}
