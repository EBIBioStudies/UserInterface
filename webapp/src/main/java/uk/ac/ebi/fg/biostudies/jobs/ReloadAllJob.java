package uk.ac.ebi.fg.biostudies.jobs;

import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.app.ApplicationJob;
import uk.ac.ebi.fg.biostudies.components.SearchEngine;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentArrayDesigns;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentExperiments;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentFiles;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentProtocols;

import java.io.*;

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


@Deprecated
public class ReloadAllJob extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec)  {
		logger.info("Reload all the data into the Application Server");
		File setupDirectory=null;
		File backDir=null;
		File setupTempDirectory=null;
		try{
		String setupDir= Application
				.getInstance().getPreferences().getString("bs.setupDirectory");
		System.out.println("$$$$$$->" + setupDir);
		
		setupDirectory=new File(setupDir);
		
		String backupDirectory= Application
				.getInstance().getPreferences().getString("bs.backupDirectory");
		System.out.println("$$$$$$->" + backupDirectory);
		
		String newDir="teste" + System.nanoTime();
		backDir= new File(backupDirectory+"/" + newDir);
		if(backDir.mkdir()){
			logger.info("Backup directory was created in [{}]",backDir.getAbsolutePath());
			copyDirectory(setupDirectory, backDir);
		}
		else{
			//TODO: rpe stop the process
			logger.error("Backup directory was NOT created in [{}]",backDir.getAbsolutePath());
		}
		
		setupTempDirectory=new File(setupDir + "temp");
		if(setupTempDirectory.mkdir()){
			logger.info("Setup temporary directory was created in [{}]",setupTempDirectory.getAbsolutePath());
		}
		else{
			//TODO: rpe stop the process
			logger.error("Setup temporary directory was NOT created in [{}]",setupTempDirectory.getAbsolutePath());
		}
		File sourceLocation=new File("/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/1");
		
		
		logger.error("Coying directory from [{}] to [{}]",sourceLocation.getAbsolutePath(),setupTempDirectory.getAbsolutePath());
		copyDirectory(sourceLocation, setupTempDirectory);
		
//		File setupTemp2Directory=new File(setupDir + "temp2");
		
//		// Rename file (or directory)
//		logger.info("Before file renamed!!!");
//		boolean success = setupDirectory.renameTo(setupTemp2Directory);
//		if (success) {
//			logger.info("file was not successfully renamed [{}]!!!",setupTemp2Directory.getAbsolutePath());
//		}
//		if (!success) {
//			logger.error("file was not successfully renamed [{}]!!!",setupTemp2Directory.getAbsolutePath());
//		}
//		
	
		//update of the xmlDatabase
		logger.info("DatabaseXml Creation");		
		
		updateXMLDatabase(setupTempDirectory);
		logger.info("End of DatabaseXml Creation");
		//only after update the database I update the Lucenes Indexes
		logger.info("Deleting Setup Directory and renaming - from now on the application is not answering");
	 	deleteDirectory(setupDirectory);
		
		// Rename file (or directory)
				logger.info("Before file renamed!!!");
				boolean success2 = setupTempDirectory.renameTo(setupDirectory);
				if (success2) {
					logger.info("file was successfully renamed [{}]!!!",setupDirectory.getAbsolutePath());
				}
				if (!success2) {
					logger.error("file was  successfully renamed [{}]!!!",setupDirectory.getAbsolutePath());
				}
		
		logger.info("Deleting Setup Directory and renaming - End");
		
		
		
		SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
		search.getController().getEnvironment("experiments").indexReader();
		((IndexEnvironmentExperiments)search.getController().getEnvironment("experiments")).setup();	
		search.getController().getEnvironment("arrays").indexReader();
		((IndexEnvironmentArrayDesigns)search.getController().getEnvironment("arrays")).setup();
		search.getController().getEnvironment("protocols").indexReader();
		((IndexEnvironmentProtocols)search.getController().getEnvironment("protocols")).setup();
		search.getController().getEnvironment("files").indexReader();
		((IndexEnvironmentFiles)search.getController().getEnvironment("files")).setup();
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
		finally{
			
		}
		// I want to start using the new version of the data
		// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
	}

	
	
	
	
	public void updateXMLDatabase(File setupDirectory) throws Exception{
		
		 // Create a client session with host name, port, user name and password
	    System.out.println("\n* Create a client session.");
	 
	    Session session = new ClientSession("localhost", 1984, "admin", "admin");
	 
	    // ------------------------------------------------------------------------
	    // Create a database
	    System.out.println("\n* Create a database.");
	 
	    //session.execute(new CreateDB("input", "/Users/rpereira/Downloads/factbook.xml"));
	    String logs=session.execute(new CreateDB("basexAEtemp", setupDirectory.getAbsolutePath() + "/XmlFiles"));
	    logger.debug("CreateDB('basexAEtemp' ...->" + logs);
	    logger.info("DatabaseXml Rename - From now on the database is not answering anymore!");	
	    logs= session.execute("ALTER DATABASE basexAE basexAEBackup");
	    logger.debug("ALTER DATABASE basexAE basexAEBackup->" + logs);
	     
	    
	    logs=session.execute("ALTER DATABASE basexAEtemp basexAE");
	    logger.debug("ALTER DATABASE basexAEtemp basexAE->" + logs);
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