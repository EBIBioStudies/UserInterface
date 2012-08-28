package uk.ac.ebi.arrayexpress.servlets;

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

import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.JobsController;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MetaDataServlet extends ApplicationServlet
{
    private static final long serialVersionUID = 8929729058610937695L;

    protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
    {
        return (requestType == RequestType.GET || requestType == RequestType.POST);
    }

    // Respond to HTTP requests from browsers.
    protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
            throws ServletException, IOException
    {
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

        PrintWriter out = null;
        try {
        	SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
    		// TODO: rpe change all this static values
    		
    		String infoDB=((IndexEnvironmentBiosamplesGroup) search.getController()
    				.getEnvironment("biosamplesgroup")).getInfoDB();
            out = response.getWriter();
            out.println("METADATA Information");
            out.println(infoDB);
            JobsController jobs = ((JobsController) getComponent("JobsController"));
            out.println(jobs.getMetaDataInformation());
            
        } finally {
            if (null != out) {
                out.flush();
                out.close();
            }
        }
    }
}
