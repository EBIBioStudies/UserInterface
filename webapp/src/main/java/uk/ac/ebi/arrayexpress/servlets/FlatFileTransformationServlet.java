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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.Files;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;
import uk.ac.ebi.arrayexpress.utils.saxon.FlatFileXMLReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class FlatFileTransformationServlet extends ApplicationServlet
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
    {
        return (requestType == RequestType.GET || requestType == RequestType.POST);
    }

    // Respond to HTTP requests from browsers.
    protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
            throws ServletException, IOException
    {
        RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper("/([^/]+)/([^/]+)/([^/]+)$", "i");

        logRequest(logger, request, requestType);

        String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request.getRequestURL().toString());

        if (null == requestArgs || requestArgs.length != 3
                || "".equals(requestArgs[0]) || "".equals(requestArgs[1]) || "".equals(requestArgs[2])) {
            throw new ServletException("Bad arguments passed via request URL [" + request.getRequestURL().toString() + "]");
        }

        String flatFileName = requestArgs[0];
        String stylesheet = requestArgs[1];
        String outputType = requestArgs[2];

        HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);

        // adding "host" request header so we can dynamically create FQDN URLs
        params.put("host", request.getHeader("host"));
        params.put("basepath", request.getContextPath());

        PrintWriter out = null;
        try {
            SaxonEngine saxonEngine = (SaxonEngine) getComponent("SaxonEngine");
            Files files = (Files) getComponent("Files");

            String stylesheetName = new StringBuilder(stylesheet)
                    .append('-').append(outputType).append(".xsl").toString();

            String flatFileLocation = files.getLocation(null, flatFileName);
            SAXSource source = new SAXSource();
            source.setInputSource(new InputSource(new FileReader(new File(files.getRootFolder(), flatFileLocation))));
            source.setXMLReader(new FlatFileXMLReader());

            if (outputType.equals("html")) {
                response.setContentType("text/html; charset=ISO-8859-1");
            } else {
                response.setContentType("text/" + outputType + "; charset=UTF-8");
            }
            // Disable cache no matter what (or we're fucked on IE side)
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "must-revalidate");
            response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

            // Output goes to the response PrintWriter.
            out = response.getWriter();

            if (!saxonEngine.transformToWriter(
                    source
                    , stylesheetName
                    , params
                    , out
            )) {                     // where to dump resulting text
                    throw new Exception("Transformation returned an error");
                }

        } catch (Exception x) {
            throw new RuntimeException(x);
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }
}
