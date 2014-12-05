package uk.ac.ebi.fg.biostudies.servlets;

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

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.biostudies.app.ApplicationServlet;
import uk.ac.ebi.fg.biostudies.components.JobsController;
import uk.ac.ebi.fg.biostudies.utils.RegexHelper;
import uk.ac.ebi.fg.biostudies.utils.StringTools;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ControlServlet extends ApplicationServlet
{
    private static final long serialVersionUID = -4509580274404536983L;

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
    {
        return (requestType == RequestType.GET || requestType == RequestType.POST);
    }

    
    // Respond to HTTP requests from browsers.
    protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType ) throws ServletException, IOException
    {
        logRequest(logger, request, requestType);

        String command = "";
        String params = "";

        String[] requestArgs = new RegexHelper("/control/([^/]+)/?(.*)", "i")
                .match(request.getRequestURL().toString());
        if (null != requestArgs) {
            command = requestArgs[0];
            params = requestArgs[1];
        }

        try {
            if ("reload-atlas-info".equals(command)
                    || "reload-ae2-xml".equals(command)
                    || "reload-efo".equals(command)
                    || "update-efo".equals(command)
                    || "reload-all".equals(command)
                    || "reload-all-disk".equals(command)
                    || "update-global-setup-disk".equals(command)
                    | "incremental-reload".equals(command)
            		) {
                ((JobsController) getComponent("JobsController")).executeJob(command);
            } else if (command.equals("reload-ae1-xml")) {
                ((JobsController) getComponent("JobsController")).executeJobWithParam(command, "connections", params);
            } else if ("test-email".equals(command)) {
                getApplication().sendEmail(null,null,
                        "Test message"
                        , "This test message was sent from [${variable.appname}] running on [${variable.hostname}], please ignore."
                            + StringTools.EOL
                );
            }
        } catch (SchedulerException x) {
            logger.error("Jobs controller threw an exception", x);
        }
    }
}
