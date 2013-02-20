package uk.ac.ebi.arrayexpress.jobs;

public interface IDownloadBiosamplesXmlFile {

	
	//Interface to different type of Xml update	
	public boolean downloadXml(String downloadDirectory) throws Exception;
	
}
