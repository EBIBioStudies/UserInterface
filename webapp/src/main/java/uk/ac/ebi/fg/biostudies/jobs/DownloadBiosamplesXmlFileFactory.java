package uk.ac.ebi.fg.biostudies.jobs;

public class DownloadBiosamplesXmlFileFactory {

	
	public static  IDownloadBiosamplesXmlFile createDownloadBiosamplesXmlFile (String type) {
	       if (type.equalsIgnoreCase ("AGE")){
	              return new DownloadBiosamplesXmlFileFromAGE();
	       }
	       else if(type.equalsIgnoreCase ("FILE")){
	    	   return new DownloadBiosamplesXmlFileFromFile();
	       }else if(type.equalsIgnoreCase ("URL")){
	              return new DownloadBiosamplesXmlFileFromUrl();
	        }
	       throw new IllegalArgumentException("No such type of IDownloadBiosamplesXmlFile ");
	       }


}
