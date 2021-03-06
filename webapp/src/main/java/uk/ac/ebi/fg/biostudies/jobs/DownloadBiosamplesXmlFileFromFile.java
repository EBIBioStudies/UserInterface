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

package uk.ac.ebi.fg.biostudies.jobs;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;

import java.io.File;

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
