package uk.ac.ebi.arrayexpress.jobs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.app.ApplicationPreferences;
import uk.ac.ebi.arrayexpress.components.SearchEngine;

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

//reloads the biosamples based on a news directory that contain the Lucene indexes and the Xml database
public class ReloadBiosamplesJobFromDisk extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec) throws Exception {
		logger.info("Reloading all Biosamples form disk data into the Application Server");
		File setupDirectory = null;
		File backDir = null;
		File setupTempDirectory = null;
		try {
			// Thread.currentThread().sleep(30000);//sleep for 1000 ms
			ApplicationPreferences appPref=this.getPreferences();
			boolean updateActive = appPref
					.getBoolean("bs.xmlupdate.active");
			logger.debug("Is Reloading Active?->" + updateActive);

			if (!updateActive) {
				logger.error("ReloadBiosamplesJobFromDisk is trying to execute and the configuration does not allow that");
				this.getApplication()
						.sendEmail(null,null,
								"BIOSAMPLES: WARNING",
								"ReloadBiosamplesJobFromDisk is trying to execute and the configuration does not allow that!");
				// throw new
				// Exception("ReloadBiosamplesJob is trying to execute and the configuration does not allow that!");
				return;
			}

			// I will create a backup directory, where I will backup the Actual
			// Setup directory, where I will put the new biosamples.xml and
			// where I will creste a new SetupDirectory based on the new
			// biosamples.xml
	
			
			String setupDir = appPref
					.getString("bs.setupDirectory");
			logger.debug("setupDir->" + setupDir);
			setupDirectory = new File(setupDir);

			
			String backupDirectory = appPref
					.getString("bs.backupDirectory");
			logger.debug("backupDirectory->" + backupDirectory);
			
			//appPref=null;
			//backupDirectory=null;

			/*
			String globalSetupDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupDirectory");
			logger.debug("globalSetupDirectory->" + globalSetupDir);
			//File globalSetupDirectory = new File(globalSetupDir);
*/
			
/*
			String globalSetupDBDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupDBDirectory");
			logger.debug("globalSetupDBDirectory->" + globalSetupDBDir);
			File globalSetupDBDirectory = new File(globalSetupDir + File.separator + globalSetupDBDir);
			
			String globalSetupLuceneDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupLuceneDirectory");
			logger.debug("globalSetupLuceneDir->" + globalSetupLuceneDir);
			File globalSetupLuceneDirectory = new File(globalSetupDir + File.separator + globalSetupLuceneDir);
			
					
			String dbname = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.dbname");
			String dbPathDirectory = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.path");
			File dbDirectory = new File(dbPathDirectory + File.separator + dbname);
			logger.debug("dbPathDirectory->" + dbDirectory);

			// this variable will be used in the creation of the bakup
			// directory anda in the creation od the database backup
			Long tempDir = System.nanoTime();
			//I need the hostname because I have 2 different production servers
*/		
			String hostname="";
			
				// only after update the database I update the Lucenes Indexes
				logger.info("Deleting Setup Directory and renaming - from now on the application is not answering");

				SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));

				// I need to close the IndexReader otherwise it would not be
				// possible dor me to delete the Setup directory (this problem
				// only occurs on NFS);
				search.getController().getEnvironment("biosamplesgroup")
						.closeIndexReader();
				search.getController().getEnvironment("biosamplessample")
						.closeIndexReader();

				// remove the old setupdirectory /tmp/Setup is deleted
				deleteDirectory(setupDirectory);
				// Rename file (or directory) /tmp/newSetup->  /tmp/Setup
				logger.info("Before file renamed!!!");
				
				
				///boolean success2 = newSetupDir.renameTo(setupDirectory);
				boolean success2 =true;
				
				// FileUtilities.
				if (success2) {
					logger.info("newSetupDir was successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					
				} else {
					logger.error("newSetupDir was not successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					throw new Exception("newSetupDir was not successfully renamed to [{}]!!!" +
							setupDirectory.getAbsolutePath());
				}
				logger.info("Deleting Setup Directory and renaming - End");
				
				
//				logger.info("Before file DBrenamed!!!");
//				logger.info("newSetupDBDir is  in [{}]!!!",
//						newSetupDBDir.getAbsolutePath());
//				logger.info("dbDirectory is  in [{}]!!!",
//						dbDirectory.getAbsolutePath());

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}
		logger.info("End of Reloading all Biosamples data into the Application Server");

		// I want to start using the new version of the data
		// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
	}

	

	void deleteDirectory(File f) throws IOException {
		FileUtils.deleteDirectory(f);
	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		FileUtils.copyDirectory(sourceLocation, targetLocation);

	}

}