<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%><%@ page import="javax.servlet.ServletException"
%><%@ page import="javax.xml.bind.*"
%><%@ page import="javax.xml.transform.stream.StreamSource"
%><%@ page import="net.sf.saxon.Configuration"
%><%@ page import="net.sf.saxon.om.DocumentInfo"
%><%@ page import="org.apache.lucene.search.TopDocs"
%><%@ page import="uk.ac.ebi.arrayexpress.app.Application"
%><%@ page import="uk.ac.ebi.arrayexpress.components.SaxonEngine"
%><%@ page import="uk.ac.ebi.arrayexpress.components.SearchEngine"
%><%@ page import="uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap"
%><%@ page import="uk.ac.ebi.xml.jaxb.Experiments"
%><%@ page import="uk.ac.ebi.xml.jaxb.utils.*"
%><%@ page import="uk.ac.ebi.arrayexpress.utils.RegexHelper"
%><%@ page import="uk.ac.ebi.arrayexpress.utils.StringTools"
%><%@ page import="java.io.StringReader"
%><%@ page import="java.text.SimpleDateFormat"
%><%@ page import="java.util.Date"
%><%
// the imports formation must be this away, otherwise the Jsp will contain some blank lines at the beiginning (http://www.fiendish.demon.co.uk/java/jsp_spaces.html) 
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
    basePath="http://" + host + basePath;

    if(request.getParameter("sortby")==null){
    	params.put("sortby", "releasedate");
    }

  // to make sure nobody sneaks in the other value w/o proper authentication
  //TODO: RPE SECURITY ISSUE 
  params.put("userid", "1");
    
    
  if (request.getParameter("type")!=null && request.getParameter("type").equalsIgnoreCase("xls")) {
      // special case for Excel docs
      // we actually send tab-delimited file but mimick it as XLS doc
      String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
      response.setContentType("application/vnd.ms-excel; charset=ISO-8859-1");
      response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".xls\"");
  }
  else{
	  String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
	  response.setContentType("text/plain; charset=ISO-8859-1");
	  response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".txt\"");
	  
  }

  	String tabChar="	";
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
   
    SearchEngine search = ((SearchEngine) Application.getInstance().getComponent("SearchEngine"));
    SaxonEngine saxonEngine = (SaxonEngine) Application.getInstance().getComponent("SaxonEngine");
    DocumentInfo source = saxonEngine.getAppDocument();
  
    if (search.getController().hasIndexDefined(index)) { 
        Integer queryId = search.getController().addQuery(index, params, request.getQueryString());
        params.put("queryid", String.valueOf(queryId));
        
        TopDocs hits=search.getController().queryAllDocs(queryId,params);
        //all the queries are now executes in this Servlet and not in the XSLT
		
        out.println("Accession	Title	Assays	Species	Release Date	Processed Data	Raw Data	Present in Atlas	ArrayExpress URL");


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
        				System.out.println("Experiment " + (i + 1) + ": " + exp.getAll().get(i).getExperiment().getAccession());
        				out.print(exp.getAll().get(i).getExperiment().getAccession() +tabChar);
        				out.print(exp.getAll().get(i).getExperiment().getName()+ tabChar);
        				out.print(exp.getAll().get(i).getExperiment().getAssays()+ tabChar);
        				out.print(exp.getAll().get(i).getExperiment().getSpecies()+ tabChar);
        				out.print(exp.getAll().get(i).getExperiment().getReleasedate()+ tabChar);

        			int count=ExperimentUtils.getNumberFiles(exp.getAll().get(i).getFolder(),"raw");
       				if(count>1){
       					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() +"?kind=raw" + tabChar);
        				}
        				else if(count==1){
         					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() + "/" + ExperimentUtils.getFile(exp.getAll().get(i).getFolder(),"raw").getName() + tabChar);
         			        				}
        				else{
        					out.print("Data is not available" + tabChar);
        				}
        		
       				
       				count=ExperimentUtils.getNumberFiles(exp.getAll().get(i).getFolder(),"fgem");
       				if(count>1){
        				out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() +"?kind=fgem" + tabChar);
        				}
        				else if(count==1){
        					out.print(basePath + "/files/" + exp.getAll().get(i).getExperiment().getAccession() + "/" + ExperimentUtils.getFile(exp.getAll().get(i).getFolder(),"fgem").getName() + tabChar);
        				}
        				else{
        					out.print("Data is not available" + tabChar);
        				}
        		
       				
    				if(exp.getAll().get(i).getExperiment().isLoadedinatlas()){
    					out.print("Yes" + "	");					
        			}
    				
    				out.print(basePath + "/experiments/" + exp.getAll().get(i).getExperiment().getAccession() + tabChar);
    				out.println();
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