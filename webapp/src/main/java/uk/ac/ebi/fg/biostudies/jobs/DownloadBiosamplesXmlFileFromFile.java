/**
 * 
 */
package uk.ac.ebi.fg.biostudies.jobs;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;

import java.io.File;

/**
 * @author rpslpereira
 * 
 */
public class DownloadBiosamplesXmlFileFromFile implements IDownloadBiosamplesXmlFile {



	private final Logger logger = LoggerFactory.getLogger(getClass());


	public boolean downloadXml(String downloadDirectory) throws Exception {
		
		String url = Application.getInstance().getPreferences()
				.getString("bs.xmlupdate.url");
		logger.info("Download file from ->"
				+ url);
		boolean ok = false;
		try {
			File sourceFile=new File(url);
			File destinationDirectory=new File(downloadDirectory);
			FileUtils.copyFileToDirectory(sourceFile, destinationDirectory);
			ok = true;
		} catch (Exception e) {
			logger.error("ERROR on copying file: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
			// return false;
		} 
		return ok;
	}

}
