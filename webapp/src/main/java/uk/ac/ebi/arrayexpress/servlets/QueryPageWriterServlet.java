package uk.ac.ebi.arrayexpress.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.xml.jaxb.Experiments;
import uk.ac.ebi.xml.jaxb.utils.ExperimentUtils;

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

public class QueryPageWriterServlet extends AuthAwareApplicationServlet {
	private static final long serialVersionUID = 6806580383145704364L;

	private transient final Logger logger = LoggerFactory.getLogger(getClass());

	protected boolean canAcceptRequest(HttpServletRequest request,
			RequestType requestType) {
		return (requestType == RequestType.GET || requestType == RequestType.POST);
	}

	protected void doAuthenticatedRequest(HttpServletRequest request,
			HttpServletResponse response, RequestType requestType,
			List<String> authUserIDs) throws ServletException, IOException {

		logger.debug("\n################### BEGIN QueryPageWriterServlet #########################");

		RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper(
				"/([^/]+)/([^/]+)/([^/]+)$", "i");

		logRequest(logger, request, requestType);

		String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request
				.getRequestURL().toString());

		if (null == requestArgs || requestArgs.length != 3
				|| "".equals(requestArgs[0]) || "".equals(requestArgs[1])
				|| "".equals(requestArgs[2])) {
			throw new ServletException("Bad arguments passed via request URL ["
					+ request.getRequestURL().toString() + "]");
		}

		String index = requestArgs[0];
		String stylesheet = requestArgs[1];
		String outputType = requestArgs[2];

		if (outputType.equals("xls")) {
			// special case for Excel docs
			// we actually send tab-delimited file but mimick it as XLS doc
			String timestamp = new SimpleDateFormat("yyMMdd-HHmmss")
					.format(new Date());
			response.setContentType("application/vnd.ms-excel; charset=ISO-8859-1");
			response.setHeader("Content-disposition",
					"attachment; filename=\"ArrayExpress-Experiments-"
							+ timestamp + ".xls\"");
			outputType = "tab";
		} else if (outputType.equals("tab")) {
			// special case for tab-delimited files
			// we send tab-delimited file as an attachment
			String timestamp = new SimpleDateFormat("yyMMdd-HHmmss")
					.format(new Date());
			response.setContentType("text/plain; charset=ISO-8859-1");
			response.setHeader("Content-disposition",
					"attachment; filename=\"ArrayExpress-Experiments-"
							+ timestamp + ".txt\"");
			outputType = "tab";
		} else if (outputType.equals("json")) {
			response.setContentType("application/json; charset=UTF-8");
		} else if (outputType.equals("html")) {
			response.setContentType("text/html; charset=UTF-8");
		} else {
			response.setContentType("text/" + outputType + "; charset=UTF-8");
		}

		// tell client to not cache the page unless we want to
		if (!"true".equalsIgnoreCase(request.getParameter("cache"))) {
			response.addHeader("Pragma", "no-cache");
			response.addHeader("Cache-Control", "no-cache");
			response.addHeader("Cache-Control", "must-revalidate");
			response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some
																			// date
																			// in
																			// the
																			// past
		}

		// flushing buffer to output headers; should only be used for looooong
		// operations to mitigate proxy timeouts
		if (null != request.getParameter("flusheaders")) {
			response.flushBuffer();
		}

		// Output goes to the response PrintWriter.
		PrintWriter out = response.getWriter();
		try {
			String stylesheetName = new StringBuilder(stylesheet).append('-')
					.append(outputType).append(".xsl").toString();

			HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(
					request);

			// adding "host" request header so we can dynamically create FQDN
			// URLs
			params.put("host", request.getHeader("host"));
			params.put("basepath", request.getContextPath());

			String basePath = request.getContextPath();

			// to make sure nobody sneaks in the other value w/o proper
			// authentication
			params.put("userid", StringTools.listToString(authUserIDs, " OR "));

			// setting "preferred" parameter to true allows only preferred
			// experiments to be displayed, but if
			// any of source control parameters are present in the query, it
			// will not be added
			String[] keywords = params.get("keywords");

			if (!(params.containsKey("migrated")
					|| params.containsKey("source")
					|| params.containsKey("visible")
					|| (null != keywords && keywords[0]
							.matches(".*\\bmigrated:.*"))
					|| (null != keywords && keywords[0]
							.matches(".*\\bsource:.*")) || (null != keywords && keywords[0]
					.matches(".*\\bvisible:.*")))) {

				params.put("visible", "true");
			}
			 
			if(request.getParameter("sortby")==null){
			    	params.put("sortby", "releasedate");
			    }
			String sortBy = StringTools
					.arrayToString(params.get("sortby"), " ");
			String sortOrder = StringTools.arrayToString(
					params.get("sortorder"), " ");
			boolean descending = false;
			if (sortOrder != null) {
				if (sortOrder.equalsIgnoreCase("ascending")) {
					descending = false;
				} else {
					descending = true;
				}
			}

			try {
				SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
				SaxonEngine saxonEngine = (SaxonEngine) getComponent("SaxonEngine");
				DocumentInfo source = saxonEngine.getAppDocument();
				if (search.getController().hasIndexDefined(index)) { // only do
																		// query
																		// if
																		// index
																		// id is
																		// defined
					// source = saxonEngine.getRegisteredDocument(index +
					// ".xml");
					Integer queryId = search.getController().addQuery(index,
							params, request.getQueryString());
					params.put("queryid", String.valueOf(queryId));

					ScoreDoc[] hits = search.getController().queryAllDocs(queryId,
							params);
					processPaging(out, hits, params, queryId, search,
							saxonEngine, source, index, stylesheetName,
							outputType, sortBy, descending, basePath);

				}

			} catch (ParseException x) {
				logger.error("Caught lucene parse exception:", x);
				reportQueryError(out, "query-syntax-error.txt",
						request.getParameter("keywords"));
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		logger.debug("################### END QueryPageWriterServlet #########################\n");
		out.close();
	}

	protected void processPaging(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, String sortBy,
			boolean descending, String basePath) {

		int pageSizeDefault = 1000;
		int pageSize = 25;
		int pagenumber = 1;

		processPagingHeader(out, hits, params, queryId, search, saxonEngine,
				source, index, stylesheet, outputType, sortBy, descending,
				basePath);
		while ((pagenumber * pageSizeDefault) <= (hits.length
				+ pageSizeDefault - 1)) {

			try {
				// calculate the last hit
				int pageEnd = Math.min((pagenumber * pageSizeDefault) - 1,
						hits.length - 1);

				int pageInit = (pagenumber - 1) * pageSizeDefault;

				processPagingNumber(out, hits, params, queryId, search,
						saxonEngine, source, index, stylesheet, outputType,
						pageInit, pageEnd, sortBy, descending, basePath);

			} catch (Exception e) {
				e.printStackTrace();
			}

			pagenumber++;

		}
		processPagingFooter(out, hits, params, queryId, search, saxonEngine,
				source, index, stylesheet, outputType, sortBy, descending,
				basePath);

	}

	protected void processPagingHeader(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, String sortBy,
			Boolean descending, String basePath) {

		out.print(includeSaticFile("header.html"));

		out.print("<div id=\"ae_contents\" class=\"ae_assign_font\">"
				+ "<div class=\"ae_left_container_100pc\">"
				+ "<div id=\"ae_header\">"
				+ "<img src=\"/ae-interface/assets/images/ae_header.gif\" alt=\"ArrayExpress\">"
				+ "</div>"
				+ "<div id=\"ae_results_header\">"
				+ "There are "
				+ hits.length
				+ " experiments matching your search criteria found in"
				+ "ArrayExpress Archive.<span id=\"ae_print_controls\" class=\"noprint\">"
				+ "<a href=\"javascript:window.print()\"><img src=\"/ae-interface/assets/images/silk_print.gif\" width=\"16\" height=\"16\" alt=\"Print\">Print this window</a>."
				+ "</span>" + "</div>");

		out.print("<table id=\"ae_results_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
				+ "<thead>"
				+ "<tr>"
				+ "<th class=\"col_accession sortable\">ID");

		if (sortBy.equalsIgnoreCase("accession")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}

		out.print("</th>");
		out.print("<th class=\"col_name sortable\">Title");

		if (sortBy.equalsIgnoreCase("name")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_assays sortable\">Assays");

		if (sortBy.equalsIgnoreCase("assays")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_species sortable\">Species");

		if (sortBy.equalsIgnoreCase("species")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_releasedate sortable\">Release Date");

		if (sortBy.equalsIgnoreCase("releasedate")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_fgem sortable\">Processed");

		if (sortBy.equalsIgnoreCase("processed")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_raw sortable\">Raw");

		if (sortBy.equalsIgnoreCase("raw")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");
		out.print("<th class=\"col_atlas sortable\">Atlas");

		if (sortBy.equalsIgnoreCase("atlas")) {
			if (descending) {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_down.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			} else {
				out.print("<img src=\""
						+ basePath
						+ "/assets/images/mini_arrow_up.gif\" width=\"12\" height=\"16\" alt=\"^\"/>");
			}
		}
		out.print("</th>");

		out.print("</tr>");
		out.print("</thead>");
		out.print("<tbody>");

	}

	protected void processPagingFooter(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, String sortBy,
			Boolean descending, String basePath) {

		out.print(includeSaticFile("footer.html"));

	}

	protected void processPagingNumber(PrintWriter out, ScoreDoc[] hits,
			HttpServletRequestParameterMap params, int queryId,
			SearchEngine search, SaxonEngine saxonEngine, DocumentInfo source,
			String index, String stylesheet, String outputType, int pageInit,
			int pageEnd, String sortBy, Boolean descending, String basePath) {

		String xml;
		try {
			xml = search.getController().queryDB(queryId, hits, pageInit,
					pageEnd, params);

			Experiments exp;
			JAXBContext context = JAXBContext.newInstance(Experiments.class);

			Unmarshaller um = context.createUnmarshaller();
			exp = (Experiments) um.unmarshal(new StringReader(xml));

			for (int i = 0; i < exp.getAll().toArray().length; i++) {
				System.out.println("Experiment " + (i + 1) + ": "
						+ exp.getAll().get(i).getExperiment().getAccession());

				out.print("<tr>");
				out.print("<td class=\"col_accession\"><div><a href=\""
						+ basePath + "/experiments/"
						+ exp.getAll().get(i).getExperiment().getAccession()
						+ "\">"
						+ exp.getAll().get(i).getExperiment().getAccession()
						+ "</a></div></td>");
				out.print("<td class=\"col_name\"><div>"
						+ exp.getAll().get(i).getExperiment().getName()
						+ "</div></td>");
				out.print("<td class=\"col_assays\"><div>"
						+ exp.getAll().get(i).getExperiment().getAssays()
						+ "</div></td><td class=\"col_species\"><div>"
						+ exp.getAll().get(i).getExperiment().getSpecies()
						+ "</div></td>");
				out.print("<td class=\"col_releasedate\"><div>"
						+ exp.getAll().get(i).getExperiment().getReleasedate()
						+ "</div></td>");
				out.print("<td class=\"col_fgem\"><div>");

				if (ExperimentUtils.hasFiles(exp.getAll().get(i).getFolder(),
						"fgem")) {

					out.print("<img src=\""
							+ basePath
							+ "/assets/images/basic_tick.gif\" width=\"16\" height=\"16\" alt=\"*\"/>");
				} else {

					out.print("<img src=\""
							+ basePath
							+ "/assets/images/silk_data_unavail.gif\" width=\"16\" height=\"16\" alt=\"-\"/>");

				}

				out.print("</div></td>");
				out.print("<td class=\"col_raw\"><div>");
				if (ExperimentUtils.hasFiles(exp.getAll().get(i).getFolder(),
						"raw")) {

					out.print("<img src=\""
							+ basePath
							+ "/assets/images/basic_tick.gif\" width=\"16\" height=\"16\" alt=\"*\"/>");
				} else {

					out.print("<img src=\""
							+ basePath
							+ "/assets/images/silk_data_unavail.gif\" width=\"16\" height=\"16\" alt=\"-\"/>");

				}

				out.print("</div></td>");
				out.print("<td class=\"col_atlas\"><div>");

				if (exp.getAll().get(i).getExperiment().isLoadedinatlas()) {
					// TODO LINK
					out.print("<a href=\"LINK/\""
							+ exp.getAll().get(i).getExperiment()
									.getAccession()
							+ "\"><img src=\""
							+ basePath
							+ "/assets/images/basic_tick.gif\" width=\"16\" height=\"16\" alt=\"*\"/></a>");

				} else {
					out.print("<img src=\""
							+ basePath
							+ "/assets/images/silk_data_unavail.gif\" width=\"16\" height=\"16\" alt=\"-\"/>");
				}

				out.print("</div></td>");

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void reportQueryError(PrintWriter out, String templateName,
			String query) {
		try {
			URL resource = Application.getInstance().getResource(
					"/WEB-INF/server-assets/templates/" + templateName);
			String template = StringTools.streamToString(resource.openStream(),
					"ISO-8859-1");
			Map<String, String> params = new HashMap<String, String>();
			params.put("variable.query", query);
			StrSubstitutor sub = new StrSubstitutor(params);
			out.print(sub.replace(template));
		} catch (Exception x) {
			logger.error("Caught an exception:", x);
		}
	}

	private String includeSaticFile(String filePath) {

		ServletContext context = getServletContext();

		String ret = "";
		String thisLine = "";
		InputStream inputStream = null;
		try {
			inputStream = context.getResourceAsStream(filePath);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			while ((thisLine = bufferedReader.readLine()) != null) {
				ret += thisLine;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}