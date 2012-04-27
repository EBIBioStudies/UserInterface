package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class IndexEnvironmentFactory {

	public static AbstractIndexEnvironment getIndexEnvironment(String name,
			HierarchicalConfiguration indexConfig) {
		if (name.equalsIgnoreCase("experiments")) {
			return new IndexEnvironmentExperiments(indexConfig);
			// return new IndexEnvironmentExperimentsCacheOnIndex(indexConfig);

		} else {

			if (name.equalsIgnoreCase("protocols")) {
				return new IndexEnvironmentProtocols(indexConfig);
			} else if (name.equalsIgnoreCase("arrays")) {
				return new IndexEnvironmentArrayDesigns(indexConfig);
			} else if (name.equalsIgnoreCase("files")) {
				return new IndexEnvironmentFiles(indexConfig);
			} else if (name.equalsIgnoreCase("experimentsxmldb")) {
				return new IndexEnvironmentExperiments(indexConfig);
			} else if (name.equalsIgnoreCase("biosamplesgroup")) {
				return new IndexEnvironmentBiosamplesGroup(indexConfig);
			} else if (name.equalsIgnoreCase("biosamplessample")) {
				return new IndexEnvironmentBiosamplesSample(indexConfig);
			} else {
				//TODO: rpe: consider the possibility of have a default implementation 
				//return new IndexEnvironment(indexConfig);
				return new IndexEnvironmentExperiments(indexConfig);
			}

		}
	}

}
