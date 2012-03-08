<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>





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
<%@ page import="uk.ac.ebi.tests.basex.jaxb.Experiments"%>
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





<body>
	<div id="ae_contents" class="ae_assign_font">
		<div class="ae_left_container_100pc">
			<div id="ae_header">
				<img src="/ae-interface/assets/images/ae_header.gif"
					alt="ArrayExpress">
			</div>
			<div id="ae_results_header">
				There are $$$$$$$$$ experiments matching your search criteria found in
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
						<th class="col_accession sortable">ID</th>
						<th class="col_name sortable">Title</th>
						<th class="col_assays sortable">Assays</th>
						<th class="col_species sortable">Species</th>
						<th class="col_releasedate sortable">Release Date<img
							src="/ae-interface/assets/images/mini_arrow_down.gif" width="12"
							height="16" alt="v"></th>
						<th class="col_fgem sortable">Processed</th>
						<th class="col_raw sortable">Raw</th>
						<th class="col_atlas sortable">Atlas</th>
					</tr>
				</thead>
				<tbody>
				RESULTS
			<%-- 	<stripes
				<stripes:useActionBean id="test" beanclass="uk.ac.ebi.arrayexpress.utils.saxon.search.StripesTest"/>  --%>
		<%-- 	<c:set var="teste" >${actionBean.hitsRet}</c:set>
					${teste["totalHits"]} --%>
					
					<c:set var="teste" >${actionBean.hitsRet}</c:set>
					<c:out value="${teste}"></c:out>
	<%-- <c_rt:out value="<%=actionBean.hitsRet["totalHits"]%>" /> --%>
			<%-- ${actionBean.hitsRet["totalHits"]} --%>
	
				</tbody>
			</table>
</body>
</html>