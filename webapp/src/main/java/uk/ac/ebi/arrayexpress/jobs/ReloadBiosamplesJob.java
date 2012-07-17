package uk.ac.ebi.arrayexpress.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentArrayDesigns;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesSample;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentExperiments;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentFiles;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentProtocols;

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

public class ReloadBiosamplesJob extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec) throws Exception {
		logger.info("Reloading all Biosamples data into the Application Server");
		File setupDirectory = null;
		File backDir = null;
		File setupTempDirectory = null;
		try {

			// download the xml
			// TODO: rpe (define it externally)
			
			String downloadDirectory = Application.getInstance().getPreferences()
					.getString("bs.downloadDirectory");
			logger.debug("downloadDirectory->" + downloadDirectory);
			
			DownloadBiosamplesXmlFile dxml = new DownloadBiosamplesXmlFile();
			boolean downloadOk = dxml.downloadXml(downloadDirectory);

		
			if (downloadOk) {
				String setupDir = Application.getInstance().getPreferences()
						.getString("bs.setupDirectory");
				logger.debug("setupDir->" + setupDir);

				setupDirectory = new File(setupDir);

				String backupDirectory = Application.getInstance()
						.getPreferences().getString("bs.backupDirectory");
				logger.debug("backupDirectory->" + backupDirectory);

				//this variable will be used in the creation of the bakup directory anda in the creation od the database backup
				Long tempDir = System.nanoTime();

				String newDir = "backup_" + tempDir;
				backDir = new File(backupDirectory + "/" + newDir);
				if (backDir.mkdir()) {
					logger.info("Backup directory was created in [{}]",
							backDir.getAbsolutePath());
					copyDirectory(setupDirectory, backDir);
				} else {
					// TODO: rpe stop the process
					logger.error("Backup directory was NOT created in [{}]",
							backDir.getAbsolutePath());
				}

				setupTempDirectory = new File(setupDir + newDir);
				if (setupTempDirectory.mkdir()) {
					logger.info(
							"Setup temporary directory was created in [{}]",
							setupTempDirectory.getAbsolutePath());
				} else {
					// TODO: rpe stop the process
					logger.error(
							"Setup temporary directory was NOT created in [{}]",
							setupTempDirectory.getAbsolutePath());
				}

				// Download it from

				File sourceLocation = new File(downloadDirectory);

				logger.error("Coying directory from [{}] to [{}]",
						sourceLocation.getAbsolutePath(),
						setupTempDirectory.getAbsolutePath());
				copyDirectory(sourceLocation, setupTempDirectory);

				// update of the xmlDatabase
				logger.info("DatabaseXml Creation");

				updateXMLDatabase(setupTempDirectory, tempDir);
				logger.info("End of DatabaseXml Creation");
				// only after update the database I update the Lucenes Indexes
				logger.info("Deleting Setup Directory and renaming - from now on the application is not answering");

				deleteDirectory(setupDirectory);

				// Rename file (or directory)
				logger.info("Before file renamed!!!");
				boolean success2 = setupTempDirectory.renameTo(setupDirectory);
				if (success2) {
					logger.info("file was successfully renamed [{}]!!!",
							setupDirectory.getAbsolutePath());
				}
				if (!success2) {
					logger.error("file was  successfully renamed [{}]!!!",
							setupDirectory.getAbsolutePath());
				}

				logger.info("Deleting Setup Directory and renaming - End");

				SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
				search.getController().getEnvironment("biosamplesgroup")
						.indexReader();
				((IndexEnvironmentBiosamplesGroup) search.getController()
						.getEnvironment("biosamplesgroup")).setup();
				search.getController().getEnvironment("biosamplessample")
						.indexReader();
				((IndexEnvironmentBiosamplesSample) search.getController()
						.getEnvironment("biosamplessample")).setup();
				//TODO: RPE Update the EFO!!??
				

			} else {
				logger.debug("Something went wrong on Xml download");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}
		logger.info("End of Reloading all Biosamples data into the Application Server");
		// I want to start using the new version of the data
		// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
	}

	public void updateXMLDatabase(File setupDirectory, long tempDir)
			throws Exception {

		// Create a client session with host name, port, user name and password
		logger.debug("\n* Create a client session int the Xml Database.");

		String dbHost = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.host");
		String dbPort = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.port");
		String dbPassword = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.adminpassword");
		
		String originalDbName = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.dbname");
		//String originalDbName = "biosamplesAEGroup";
		
		Session session = new ClientSession(dbHost, Integer.parseInt(dbPort), "admin", dbPassword);

		// ------------------------------------------------------------------------
		// Create a database
		logger.debug("\n* Create a database.");

		// session.execute(new CreateDB("input",
		// "/Users/rpereira/Downloads/factbook.xml"));
		//
	
		String tempDbName = originalDbName + "_" + tempDir;
		String logs = session.execute(new CreateDB(tempDbName, setupDirectory
				.getAbsolutePath()));
				//.getAbsolutePath() + "/XmlFiles"));
		logger.debug("CreateDB('" + tempDbName + "' ...->" + logs);

		// I will create now the Lucene Indexes ...
		SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
		// TODO: rpe change all this static values
		search.getController().indexFromXmlDB("biosamplesgroup", true,
				setupDirectory.getAbsolutePath() + "/LuceneIndexes",
				"xmldb:basex://localhost:1984/" + tempDbName);
		((IndexEnvironmentBiosamplesGroup) search.getController()
				.getEnvironment("biosamplesgroup")).setup();
		search.getController().indexFromXmlDB("biosamplessample", true,
				setupDirectory.getAbsolutePath() + "/LuceneIndexes",
				"xmldb:basex://localhost:1984/" + tempDbName);
		((IndexEnvironmentBiosamplesSample) search.getController()
				.getEnvironment("biosamplessample")).setup();
		//

		// index
		logger.info("DatabaseXml Rename - From now on the database is not answering anymore!");
		logs = session.execute("ALTER DATABASE " + originalDbName + " "
				+ tempDbName + "_backup");
		logger.debug("ALTER DATABASE " + originalDbName + " " + tempDbName
				+ "_backup" + "->" + logs);

		logs = session.execute("ALTER DATABASE " + tempDbName + " "
				+ originalDbName);
		logger.debug("ALTER DATABASE " + tempDbName + " " + originalDbName
				+ "->" + logs);
		logger.info("DatabaseXml Rename - End!");
		System.out.println("\n* Close the client session.");

		session.close();

	}

	void deleteDirectory(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteDirectory(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

}