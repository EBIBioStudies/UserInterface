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

package uk.ac.ebi.fg.biostudies.servlets;

import uk.ac.ebi.fg.biostudies.app.ApplicationServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MetaDataServlet extends ApplicationServlet {
	private static final long serialVersionUID = 8929729058610937695L;

	protected boolean canAcceptRequest(HttpServletRequest request,
			RequestType requestType) {
		return (requestType == RequestType.GET || requestType == RequestType.POST);
	}

	// Respond to HTTP requests from browsers.
	protected void doRequest(HttpServletRequest request,
			HttpServletResponse response, RequestType requestType)
			throws ServletException, IOException {
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Cache-Control", "must-revalidate");
		response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some
																		// date
																		// in
																		// the
																		// past

		PrintWriter out = null;
		try {

			String[] componentsArray = { "JobsController", "XmlDbConnectionPool",
					"BioStudies" };
			out = response.getWriter();
			out.println("<html><head>");
			out.println("<link rel=\"stylesheet\" href=\"assets/stylesheets/biosamples_homepage_10.css\" type=\"text/css\">");
//			out.println("<link rel=\"stylesheet\" href=\"assets/stylesheets/biosamples_common_10.css\" type=\"text/css\">");
			out.println("</head><body class='" + request.getParameter("class") +  "'>");
			for (int j = 0; j < componentsArray.length; j++) {
				String infoDB = (getComponent(componentsArray[j]))
						.getMetaDataInformation();
				out.println("<br/><br/><b>"+ componentsArray[j] + "</b>");
				out.println(infoDB.replaceAll("\n", "<br/>"));
			}
			// String infoDB=((BioSamplesGroup)
			// getComponent("BioSamplesGroup")).getMetaDataInformation();
			// // .getEnvironment("biosamplesgroup")).getMetaDataInformation();
			// out = response.getWriter();
			// out.println("<br/><br/>BioSamplesGroup");
			// out.println(infoDB);
			//
			// infoDB=((BioSamplesSample)
			// getComponent("BioSamplesSample")).getMetaDataInformation();
			// out.println("<br/><br/>BioSamplesSample");
			// out.println(infoDB);
			//
			//
			// infoDB=((XmlDbConnectionPool)
			// getComponent("XmlDbConnectionPool")).getMetaDataInformation();
			// out.println("<br/><br/>XmlDbConnectionPool");
			// out.println(infoDB);
			//
			//
			// infoDB=((JobsController)
			// getComponent("JobsController")).getMetaDataInformation();
			// out.println("<br/><br/>JobsController");
			// out.println(infoDB);

		} finally {
			if (null != out) {
				out.flush();
				out.close();
			}
		}
	}
}
