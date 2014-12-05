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

import uk.ac.ebi.fg.biostudies.app.Application;
import uk.ac.ebi.fg.biostudies.components.JobsController;
import uk.ac.ebi.fg.biostudies.components.SaxonEngine;
import uk.ac.ebi.fg.biostudies.components.SearchEngine;

import java.net.MalformedURLException;
import java.net.URL;

public class AEInterfaceTestApplication extends Application
{
    public AEInterfaceTestApplication()
    {
        super("arrayexpress");

        // test-instance only code to emulate functionality missing from tomcat container
        // add a shutdown hook to to a proper termination
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        addComponent(new SaxonEngine());
        addComponent(new SearchEngine());
      //  addComponent(new Experiments());
       // addComponent(new Users());
        //addComponent(new Files());
        addComponent(new JobsController());

        initialize();
    }

    public String getName()
    {
        return "Arrayexpress Test Application";    
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return getClass().getResource(path.replaceFirst("/WEB-INF/classes", ""));
    }

    // this is to receive termination notification and shutdown system properly
    private class ShutdownHook extends Thread
    {
        public void run()
        {
            Application.getInstance().terminate();
        }
    }
}
