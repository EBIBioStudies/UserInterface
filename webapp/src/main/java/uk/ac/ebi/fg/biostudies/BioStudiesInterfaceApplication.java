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

package uk.ac.ebi.fg.biostudies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.components.*;
import uk.ac.ebi.fg.biostudies.utils.StringTools;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.MalformedURLException;
import java.net.URL;

public class BioStudiesInterfaceApplication extends Application implements ServletContextListener
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ServletContext servletContext;

    public BioStudiesInterfaceApplication()
    {
        super("biostudies");

        //The first thing I should do is to check the Setup Directory
        addComponent(new SetupDirectoryCheck());

        addComponent(new SaxonEngine());
        addComponent(new SearchEngine());

        addComponent(new Autocompletion());

  
        addComponent(new XmlDbConnectionPool());
        addComponent(new BioStudies());
        
        addComponent(new Users());
        //addComponent(new BioSamplesGroup());
        //addComponent(new BioSamplesSample());
 
        addComponent(new JobsController());
        addComponent(new Ontologies());

    }

    public String getName()
    {
        return null != servletContext ? servletContext.getServletContextName() : null;
    }

    public URL getResource( String path ) throws MalformedURLException
    {
        return null != servletContext ? servletContext.getResource(path) : null;
    }


    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        servletContext = sce.getServletContext();

        logger.info("****************************************************************************************************************************");
        logger.info("*");
        logger.info("*  {}", servletContext.getServletContextName());
        logger.info("*");
        logger.info("****************************************************************************************************************************");

        // re-route all subsequent java.util.logging calls via slf4j
        SLF4JBridgeHandler.install();

        initialize();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        terminate();

        // restore java.util.logging calls to the original state
        SLF4JBridgeHandler.uninstall();

        servletContext = null;

        logger.info("****************************************************************************************************************************" + StringTools.EOL + StringTools.EOL);
    }
}
