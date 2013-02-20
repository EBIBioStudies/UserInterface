<%@ page import="java.net.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-Language" content="en-GB" />
<meta http-equiv="Window-target" content="_top" />
<meta name="no-email-collection"
	content="http://www.unspam.com/noemailcollection/" />

<title>BioSamples &lt; EMBL-EBI</title>

<link rel="stylesheet"
	href="${interface.application.link.www_domain.inc}/css/contents.css"
	type="text/css" />
<link rel="stylesheet"
	href="${interface.application.link.www_domain.inc}/css/userstyles.css"
	type="text/css" />
<script
	src="${interface.application.link.www_domain.inc}/js/contents.js"
	type="text/javascript"></script>
<link rel="SHORTCUT ICON"
	href="${interface.application.link.www_domain}/bookmark.ico" />

<!-- <link rel="stylesheet" href="assets/stylesheets/biosamples_common_10.css"
	type="text/css" /> -->


<%@ include file="WEB-INF/server-assets/html/mitigation_ebi.html"%>


<link rel="stylesheet"
	href="assets/stylesheets/biosamples_browse_10.css" type="text/css" />
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_common_10.css" type="text/css" />
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_metadata_10.css" type="text/css">

<script src="assets/scripts/jquery-1.4.2.min.js" type="text/javascript"></script>
<script src="assets/scripts/jsdeferred.jquery-0.3.1.js"
	type="text/javascript"></script>
<script src="assets/scripts/jquery.cookie-1.0.js" type="text/javascript"></script>
<script src="assets/scripts/jquery.query-2.1.7m-ebi.js"
	type="text/javascript"></script>
<script src="assets/scripts/jquery.caret-range-1.0.js"
	type="text/javascript"></script>
<script src="assets/scripts/jquery.autocomplete-1.1.0m-ebi.js"
	type="text/javascript"></script>

<script src="assets/scripts/biosamples_common_10.js"
	type="text/javascript"></script>
<script src="assets/scripts/biosamples_browse_10.js"
	type="text/javascript"></script>



<!-- Ellipsis -->
<!-- <script src="assets/scripts/ThreeDots.js" type="text/javascript"></script>
<script src="assets/scripts/jquery.tipsy.js" type="text/javascript"></script> -->

${interface.application.google.analytics}
</head>






<body class="${interface.application.body.class}">

	<%@ include file="WEB-INF/server-assets/html/header_ebi.html"%>
	<!-- <div id="browse_page_bgcolor"> -->

	<div id="bs_contents_box_100pc">
		<div id="bs_navi">
			<a href="http://www.ebi.ac.uk/">EBI</a> &gt; <a
				href="${interface.application.base.path}">BioSamples</a>
		</div>
		<!-- </div> -->
		<div id="bs_keywords_filters_area">


			<div id="bs_keywords_filters_box">



				<div class="form_outer">
					<div class="form_top">
						<div class="form_bottom">
							<div class="form_left">
								<div class="form_right">
									<div class="form_bottom_left">
										<div class="form_bottom_right">
											<div class="form_top_left">
												<div class="form_top_right">
													<div id="bs_query_box">
														<form id="bs_query_form" method="get" action="browse.html">
															<table border="0">
																<tr>
																	<td colspan="4" style="font-size: 11px">Search BioSample
																			Database at EBI [<a
																			href="javascript:aeClearField('#bs_keywords_field')">clear</a>]
																	</td>
																</tr>
																<tr>
																	<td valign="bottom">
																		<fieldset id="bs_keywords_fset"> 
																			<input id="bs_keywords_field" type="text"
																				name="keywords" value="" maxlength="200"
																				class="bs_assign_font" autocomplete="off" />
																		</fieldset> 
																		</td>
																		<td valign="bottom">
																		<div id="bs_submit_box">
																			<input id="bs_query_submit" type="submit"
																				value="Search" />
																		</div>
																	</td>
																	<td valign="middle" style="font-size: 11px">Sort
																		by: <select id="sortby" name="sortby" disabled>
																			<option value="">Relevance</option>
																			<option value="id">Accession</option>
																			<option value="title">Title</option>
																			<option value="samples">Samples</option>
																	</select>
																	</td>
																	<td valign="bottom" align="right"><span
																		class="${interface.application.metadata.class}"><a
																			href="metadata?class=${interface.application.body.class}"
																			target="ext">Metadata</a></span></td>
																</tr>
																<tr>
																	<td colspan="3" style="font-size: 11px">Examples:
																		<a href="browse.html?keywords=leukemia">leukemia</a>, <a href="browse.html?keywords=<%=URLEncoder.encode("ArrayExpress AND \"Mus musculus\"","UTF-8")%>">ArrayExpress AND "Mus musculus"</a>
																	</td>
															</table>


															<div id="bs_results_stats">
																<div id="bs_results_stats_fromto"></div>
																<div id="bs_results_pager"></div>
															</div>
														</form>
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>



		<div id="bs_results_area">
			<table id="bs_results_table" border="0" cellpadding="0"
				cellspacing="0">
				<thead>
					<tr>
						<th class="bs_results_accession sortable bs_results_id"
							id="bs_results_header_id"><a href="javascript:aeSort('id')"
							title="Click to sort by accession"><div
									class="table_header_inner">Accession</div></a> <!-- <img
								src="/arrayexpress/assets/images/mini_arrow_up.gif" width="12"
								height="16" alt="^"> --></th>
						<th class="bs_results_title sortable"
							id="bs_results_header_title"><a
							href="javascript:aeSort('title')"
							title="Click to sort by title"><div
									class="table_header_inner">Title</div></a></th>
						<th class="bs_results_samples sortable bs_results_samples"
							id="bs_results_header_samples"><a
							href="javascript:aeSort('samples')"
							title="Click to sort by accession"><div
									class="table_header_inner">Samples</div></a></th>
					</tr>
				</thead>
				<tbody id="bs_results_tbody">

				</tbody>
			</table>

		</div>
	</div>

	<!-- <div id="browse_page_footer_bgcolor"> -->
	<%@ include file="WEB-INF/server-assets/html/footer_ebi.html"%>
	<!--  </div>
 </div> -->
</body>
</html>