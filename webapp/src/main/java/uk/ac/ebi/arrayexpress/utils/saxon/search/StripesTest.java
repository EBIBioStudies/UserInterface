package uk.ac.ebi.arrayexpress.utils.saxon.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.om.DocumentInfo;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.TopDocs;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;

public class StripesTest implements ActionBean {
    private ActionBeanContext context;
    TopDocs hitsRet=null;
    String ret;

	@Override
	public ActionBeanContext getContext() { return context; }
	
	@Override
    public void setContext(ActionBeanContext context) { this.context = context; }


	
	
	public Resolution test(){
		System.out.println("$$$$$$$$$$$$$$$$$ entrou no test");
		hitsRet=null;
		ret="teste";
	    return new ForwardResolution("/printer.jsp");
		
	}


public TopDocs getHitsRet() {
		return hitsRet;
	}

	public void setHitsRet(TopDocs hitsRet) {
		this.hitsRet = hitsRet;
	}

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	@DefaultHandler
	public Resolution print() throws ParseException, IOException, ServletException {
    ret="print";		
	System.out.println("$$$$$$$$$$$$$$$$$ entrou no print");	
		
	HttpServletRequest request= getContext().getRequest();

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
    params.put("basepath", request.getContextPath());

  // to make sure nobody sneaks in the other value w/o proper authentication
  //TODO: RPE SECURITY ISSUE 
  //params.put("userid", StringTools.listToString(authUserIDs, " OR "));

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
    if (search.getController().hasIndexDefined(index)) { // only do query if index id is defined
//        source = saxonEngine.getRegisteredDocument(index + ".xml");
        Integer queryId = search.getController().addQuery(index, params, request.getQueryString());
        params.put("queryid", String.valueOf(queryId));
        
        TopDocs hits=search.getController().queryAllDocs(queryId,params);
        hitsRet = hits;
    }
	
    return new ForwardResolution("/printer.jsp");
}
}
