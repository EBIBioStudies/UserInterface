<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ page import="javax.servlet.ServletException"%>
<%@ page import="javax.xml.bind.*"%>
<%@ page import="javax.xml.transform.stream.StreamSource"%>
<%@ page import="net.sf.saxon.Configuration"%>
<%@ page import="net.sf.saxon.om.DocumentInfo"%>
<%@ page import="org.apache.lucene.search.TopDocs"%>
<%@ page import="uk.ac.ebi.arrayexpress.app.Application"%>
<%@ page import="uk.ac.ebi.arrayexpress.components.SaxonEngine"%>
<%@ page import="uk.ac.ebi.arrayexpress.components.SearchEngine"%>
<%@ page
	import="uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap"%>
<%@ page import="uk.ac.ebi.xml.jaxb.Experiments"%>
<%@ page import="uk.ac.ebi.xml.jaxb.utils.*"%>
<%@ page import="uk.ac.ebi.arrayexpress.utils.RegexHelper"%>
<%@ page import="uk.ac.ebi.arrayexpress.utils.StringTools"%>
<%@ page import="java.io.StringReader"%>




<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="en-GB">
<meta http-equiv="Window-target" content="_top">
<meta name="no-email-collection"
	content="http://www.unspam.com/noemailcollection/">
<title>Experiments | ArrayExpress Archive | EBI</title>
<link rel="SHORTCUT ICON" href="/bookmark.ico">
<link rel="stylesheet"
	href="/ae-interface/assets/stylesheets/ae_common_20.css"
	type="text/css">
<link rel="stylesheet"
	href="/ae-interface/assets/stylesheets/ae_browse_printer_20.css"
	type="text/css">
<script src="/ae-interface/assets/scripts/jquery-1.4.2.min.js"
	type="text/javascript"></script>
<script src="/ae-interface/assets/scripts/jquery.query-2.1.7m-ebi.js"
	type="text/javascript"></script>
<script src="/ae-interface/assets/scripts/ae_common_20.js"
	type="text/javascript"></script>
<script src="/ae-interface/assets/scripts/ae_browse_printer_20.js"
	type="text/javascript"></script>
<script type="text/javascript">
	var _gaq = _gaq || [];
	_gaq.push([ '_setAccount', 'UA-21742948-1' ]);
	_gaq.push([ '_trackPageview' ]);

	(function() {
		var ga = document.createElement('script');
		ga.type = 'text/javascript';
		ga.async = true;
		ga.src = ('https:' == document.location.protocol ? 'https://ssl'
				: 'http://www')
				+ '.google-analytics.com/ga.js';
		var s = document.getElementsByTagName('script')[0];
		s.parentNode.insertBefore(ga, s);
	})();
</script>
</head>

<%

long time = System.nanoTime();
try {

	
	

	RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper("/([^/]+)/([^/]+)/([^/]+)$", "i");

    String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request.getRequestURL().toString());

    if (null == requestArgs || requestArgs.length != 3
            || "".equals(requestArgs[0]) || "".equals(requestArgs[1]) || "".equals(requestArgs[2])) {
        throw new ServletException("Bad arguments passed via request URL [" + request.getRequestURL().toString() + "]");
    }

    String index = "experiments";
    String stylesheet = requestArgs[1];
    String outputType = requestArgs[2];
    
    
    HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);

    // adding "host" request header so we can dynamically create FQDN URLs
    
    
    params.put("host", request.getHeader("host"));
    String host=request.getHeader("host");
    params.put("basepath", request.getContextPath());
    String basePath=request.getContextPath();

    if(request.getParameter("sortby")==null){
    	params.put("sortby", "releasedate");
    }
  
	
  // to make sure nobody sneaks in the other value w/o proper authentication
  //TODO: RPE SECURITY ISSUE 
  params.put("userid", "1");

    // setting "preferred" parameter to true allows only preferred experiments to be displayed, but if
    // any of source control parameters are present in the query, it will not be added
    String[] keywords = params.get("keywords");

    if (!(params.containsKey("migrated")
        || params.containsKey("source")
        || params.containsKey("visible")
        || ( null != keywords && keywords[0].matches(".*\\bmigrated:.*"))
        || ( null != keywords && keywords[0].matches(".*\\bsource:.*"))
        || ( null != keywords && keywords[0].matches(".*\\bvisible:.*"))
        )) {

        params.put("visible", "true");
    }
   
    String sortBy = StringTools.arrayToString(params.get("sortby"), " ");
	String sortOrder = StringTools.arrayToString(params.get("sortorder")," ");
	boolean descending=false;
	if (sortOrder != null){
		if( sortOrder.equalsIgnoreCase("ascending")) {
			descending = false;
		}
		else{
			descending = true;
		}
	}
	
    SearchEngine search = ((SearchEngine) Application.getInstance().getComponent("SearchEngine"));
    SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance().getComponent("SaxonEngine");
    DocumentInfo source = saxonEngine.getAppDocument();
    if (search.getController().hasIndexDefined(index)) { // only do query if index id is defined
//        source = saxonEngine.getRegisteredDocument(index + ".xml");
        Integer queryId = search.getController().addQuery(index, params, request.getQueryString());
        params.put("queryid", String.valueOf(queryId));
        
        TopDocs hits=search.getController().queryAllDocs(queryId,params);
        //all the queries are now executes in this Servlet and not in the XSLT


%>

<body>
	<div id="ae_contents" class="ae_assign_font">
		<div class="ae_left_container_100pc">
			<div id="ae_header">
				<img src="/ae-interface/assets/images/ae_header.gif"
					alt="ArrayExpress">
			</div>
			<div id="ae_results_header">
				There are <%=hits.totalHits%> experiments matching your search criteria found in
				ArrayExpress Archive.<span id="ae_print_controls" class="noprint">
					<a href="javascript:window.print()"><img
						src="/ae-interface/assets/images/silk_print.gif" width="16"
						height="16" alt="Print">Print this window</a>.
				</span>
			</div>


			<table id="ae_results_table" border="0" cellpadding="0"
				cellspacing="0">
				<thead>
					<tr>
						<th class="col_accession sortable">ID 
						<%
						if(sortBy.equalsIgnoreCase("accession"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_name sortable">Title 
						<%
						if(sortBy.equalsIgnoreCase("name"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_assays sortable">Assays 
						<%
						if(sortBy.equalsIgnoreCase("assays"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_species sortable">Species 
						<%
						if(sortBy.equalsIgnoreCase("species"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_releasedate sortable">Release Date 
						<%
						if(sortBy.equalsIgnoreCase("releasedate"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_fgem sortable">Processed 
						<%
						if(sortBy.equalsIgnoreCase("processed"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_raw sortable">Raw 
						<%
						if(sortBy.equalsIgnoreCase("raw"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
						<th class="col_atlas sortable">Atlas 
						<%
						if(sortBy.equalsIgnoreCase("atlas"))
							if(descending){
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_down.gif" width="12" height="16" alt="^"/>
								<%
							}
							else{
								%>
								<img src="<%=basePath%>/assets/images/mini_arrow_up.gif" width="12" height="16" alt="^"/>
								<%
							}
						%>
						</th>
					</tr>
				</thead>
				<tbody>

<%

        System.out.println("Total Hits->" + hits.totalHits);
        int pageSizeDefault=1000;
        int pageSize = 25;
 		int pagenumber=1;   
        	while((pagenumber* pageSizeDefault)<=(hits.totalHits + pageSizeDefault -1)){
        		System.out.println("%%%%%%%%Paginacao->" + pagenumber);
        		if(pagenumber==1){
        			params.put("initial", "true");
        		}
        		else{
        			params.put("initial", "false");                  			
        		}
        		
        		//calculate the last hit 
        		int pageEnd=Math.min((pagenumber*pageSizeDefault)-1, hits.totalHits-1);
        			
        		String xml = search.getController().queryDB(queryId, hits, (pagenumber-1)*pageSizeDefault, pageEnd,params);
        		
        		
        		Experiments exp;
        		try {
        			JAXBContext context = JAXBContext.newInstance(Experiments.class);
        			
        			Unmarshaller um = context.createUnmarshaller();
        			exp = (Experiments) um.unmarshal(new StringReader(xml));
        			for (int i = 0; i < exp.getAll().toArray().length; i++) {
        				System.out.println("Experiment " + (i + 1) + ": "
        						+ exp.getAll().get(i).getExperiment().getAccession());
        				%>
        				
        				<tr>
        				<td class="col_accession"><div><a href="<%=basePath%>/experiments/<%=exp.getAll().get(i).getExperiment().getAccession()%>"><%=exp.getAll().get(i).getExperiment().getAccession()%></a></div></td>
        				<td class="col_name"><div><%=exp.getAll().get(i).getExperiment().getName()%></div></td>
        				<td class="col_assays"><div><%=exp.getAll().get(i).getExperiment().getAssays()%></div></td><td class="col_species"><div><%=exp.getAll().get(i).getExperiment().getSpecies()%></div></td>
        				<td class="col_releasedate"><div><%=exp.getAll().get(i).getExperiment().getReleasedate()%></div></td>
        				<td class="col_fgem"><div>
						<%
        				if(ExperimentUtils.hasFiles(exp.getAll().get(i).getFolder(),"fgem")){
        					%>
        				<img src="<%=basePath%>/assets/images/basic_tick.gif" width="16" height="16" alt="*"/>
                        <%	
        				}
        				else{
        					%>
        					<img src="<%=basePath%>/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/>
        				<%	
        				}

        				%>
        				</div></td>
        				<td class="col_raw"><div>
        				<%
        				if(ExperimentUtils.hasFiles(exp.getAll().get(i).getFolder(),"raw")){
        				%>
        				<img src="<%=basePath%>/assets/images/basic_tick.gif" width="16" height="16" alt="*"/>
                        <%	
        				}
        				else{
        					%>
        					<img src="<%=basePath%>/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/>
        				<%	
        				}

        				%>
        				</div></td>
        				<td class="col_atlas"><div>
        				<%
        				if(exp.getAll().get(i).getExperiment().isLoadedinatlas()){
        					%>
        				<a href="LINK/"<%=exp.getAll().get(i).getExperiment().getAccession()%>"><img src="<%=basePath%>/assets/images/basic_tick.gif" width="16" height="16" alt="*"/></a>
                        <%	
        				}
        				else{
        					%>
        					<img src="<%=basePath%>/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/>
        				<%	
        				}

        				%>
        				</div></td>
        				<%
        			}
        		} catch (JAXBException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}  		
        		
        		
        		pagenumber++;
        	}
        	
        }
   
    
	}catch (Exception x) {
		x.printStackTrace();
   
	}



double ms = (System.nanoTime() - time) / 1000000d;
System.out.println("\n\n############################REQUEST TOOK->" + ms + " 2ms");
System.out.println("################### END Printer #########################\n");
%>






				</tbody>
			</table>
</body>
</html>