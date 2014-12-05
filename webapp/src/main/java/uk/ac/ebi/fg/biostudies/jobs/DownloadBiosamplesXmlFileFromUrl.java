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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;

public class DownloadBiosamplesXmlFileFromUrl implements
		IDownloadBiosamplesXmlFile {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public boolean downloadXml(String downloadDirectory) throws Exception {

		String url = Application.getInstance().getPreferences()
				.getString("bs.xmlupdate.url");

		URL website = new URL(url);
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		String fileLocation = "";
		boolean ok = false;
		try {

			in = new BufferedInputStream(website.openStream());
			fileLocation = downloadDirectory + "/biosamples.xml";
			fout = new FileOutputStream(fileLocation);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
			ok=true;
		} catch (Exception e) {
			log.error("ERROR on downloading file: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
			log.debug("File {} created successful", fileLocation);
		}

		return ok;
	}

}
