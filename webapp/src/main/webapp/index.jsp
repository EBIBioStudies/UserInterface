<%@ page import="java.net.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 4.01 Transitional//EN">
<html lang="eng">
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



<%@ include file="WEB-INF/server-assets/html/mitigation_ebi.html"%>



<script src="assets/scripts/jquery-1.4.2.min.js" type="text/javascript"></script>
<script src="assets/scripts/jsdeferred.jquery-0.3.1.js"
	type="text/javascript"></script>
<script src="assets/scripts/jquery.cookie-1.0.js" type="text/javascript"></script>
<script src="assets/scripts/jquery.query-2.1.7m-ebi.js"
	type="text/javascript"></script>

<link rel="SHORTCUT ICON"
	href="${interface.application.link.www_domain.inc}/bookmark.ico" />

<meta name="keywords"
	content="Biosample, database, EBI, Europe, Sample, Bioinformatics" />
<meta name="description"
	content="BioSample database at the European Bioinformatics Institute" />
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_homepage_10.css" type="text/css">
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_metadata_10.css" type="text/css">

<style type="text/css">
body {
	font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 9pt;
}

.biosd_query_area {
	font-size: 9pt;
}

td {
	font-size: 9pt;
}

a:link#browseLink,a:visited#browseLink {
	color: #006666;
	text-decoration: none;
}

a:hover#browseLink {
	color: #006666;
	text-decoration: underline;
}

.mainFmtTable {
	width: 100%;
	margin: auto;
	border-spacing: 15px;
	border-collapse: separate
}

.mainFmtTable TDXXX {
	border: 1px solid #B1CCCC;
}

.introIF {
	height: 110px;
	border: 0px;
	overflow: hidden;
	width: 100%;
}

.newsIF {
	height: 220px;
	border: 0;
	overflow: hidden;
	width: 100%;
}

.sourcesIF {
	height: 220px;
	border: 0;
	overflow: hidden;
	width: 100%;
}

.requestBlock {
	background-color: rgb(238, 245, 245);
}

.title {
	font-size: 16pt;
	font-weight: regular;
}

.form_outer {
	background-color: #EEF5F5;
}

.form_top {
	background: url("assets/images/fft.gif") repeat-x scroll 0 0 transparent;
}

.form_top_left {
	background: url("assets/images/fftl.gif") no-repeat scroll 0 0
		transparent;
}

.form_top_right {
	background: url("assets/images/fftr.gif") no-repeat scroll 100% 0
		transparent;
}

.form_left {
	background: url("assets/images/ffl.gif") repeat-y scroll 0 0 transparent;
}

.form_right {
	background: url("assets/images/ffr.gif") repeat-y scroll 100% 0
		transparent;
}

.form_bottom {
	background: url("assets/images/ffb.gif") repeat-x scroll 0 100%
		transparent;
}

.form_bottom_left {
	background: url("assets/images/ffbl.gif") no-repeat scroll 0 100%
		transparent;
}

.form_bottom_right {
	background: url("assets/images/ffbr.gif") no-repeat scroll 100% 100%
		transparent;
}

.form_inner {
	padding: 1px;
}

.light_form_outer {
	background-color: #EEF5F5;
	background: url("assets/images/trbg.gif") repeat scroll 0 0;
}

.light_form_top {
	background: url("assets/images/lft.gif") repeat-x scroll 0 0 transparent;
}

.light_form_top_left {
	background: url("assets/images/lftl.gif") no-repeat scroll 0 0
		transparent;
}

.light_form_top_right {
	background: url("assets/images/lftr.gif") no-repeat scroll 100% 0
		transparent;
}

.light_form_left {
	background: url("assets/images/lfl.gif") repeat-y scroll 0 0 transparent;
}

.light_form_right {
	background: url("assets/images/lfr.gif") repeat-y scroll 100% 0
		transparent;
}

.light_form_bottom {
	background: url("assets/images/lfb.gif") repeat-x scroll 0 100%
		transparent;
}

.light_form_bottom_left {
	background: url("assets/images/lfbl.gif") no-repeat scroll 0 100%
		transparent;
}

.light_form_bottom_right {
	background: url("assets/images/lfbr.gif") no-repeat scroll 100% 100%
		transparent;
}

.light_form_inner {
	padding: 1px;
}
</style>

<script type="text/javascript">
	function updateBiosamplesStats() {
		// gets aer stats and updates the page
		$.get("biosamples-stats.xml").next(onBiosamplesStatsSuccess);
	}

	function onBiosamplesStatsError() {

	}

	function numberWithCommas(x) {
		return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
	}

	function onBiosamplesStatsSuccess(xml) {

		if (undefined != xml) {
			var bs_repxml = $($(xml).find("biosamples")[0]);
			var totalgroups = bs_repxml.attr("groups");
			var totalsamples = bs_repxml.attr("samples");

			document.getElementById("totalSamples").innerHTML = numberWithCommas(totalsamples);
			document.getElementById("totalGroups").innerHTML = numberWithCommas(totalgroups);
		}

	}
</script>
${interface.application.google.analytics}
</head>
<body class="${interface.application.body.class}"
	onload="updateBiosamplesStats();">

	<%@ include file="WEB-INF/server-assets/html/header_ebi.html"%>

<!-- 	<div class="contents" id="contents">
 -->
		<div id="contentspane" class="contentspane">

						<table class="mainFmtTable" cellspacing="15">
							<tr class="intro">
								<td colspan=2>
									<div class="light_form_outer">
										<div class="light_form_top">
											<div class="light_form_bottom">
												<div class="light_form_left">
													<div class="light_form_right">
														<div class="light_form_bottom_left">
															<div class="light_form_bottom_right">

																<div class="light_form_top_left">
																	<div class="light_form_top_right"></div>
																</div>
															</div>
														</div>
													</div>
												</div>
											</div>
										</div>
									</div>
								</td>
							</tr>

							<tr>
								<td colspan="2" class="requestBlock">
									<div class="biosd_query_area" style="display: block;">
										<div class="ae_left_box">
											<div class="form_outer">
												<div class="form_top">
													<div class="form_bottom">
														<div class="form_left">
															<div class="form_right">
																<div class="form_bottom_left">
																	<div class="form_bottom_right">
																		<div class="form_top_left">
																			<div class="form_top_right">
																				<div class="form_inner">
																					<div class="biosd_facts_box"
																						style="margin-left: 15px; margin-top: 15px">
																						<div class="facts">
																							Samples: <span id="totalSamples"></span> &nbsp;
																							Groups: <span id="totalGroups"></span> &nbsp; <span
																								class="${interface.application.metadata.class}"><a
																								href="metadata?class=${interface.application.body.class}"
																								target="ext">Metadata</a></span>
																							<!-- &nbsp; Sources: <span id="totalDataSources"></span>  -->
																							<br>
																						</div>
																					</div>



																					<form id="biosd_query_form" method="get"
																						action="browse.html">
																						<table
																							style="margin-left: 15px; margin-bottom: 15px">
																							<tr>

																								<td><input id="biosd_query_field"
																									name="keywords" size="80" maxlength="500"
																									style="width: 450px"><br></td>
																								<td>&nbsp;&nbsp;<input
																									id="biosd_query_submit" type="submit"
																									value="Search" style="width: 70px"><br>
																								</td>
																							</tr>
																							<tr>
																								<td colspan="2">Examples: <a
																									href="browse.html?keywords=leukemia">leukemia</a>,
																									<a
																									href="browse.html?keywords=<%=URLEncoder.encode("ArrayExpress AND \"Mus musculus\"",
					"UTF-8")%>">ArrayExpress
																										AND "Mus musculus"</a>
																								</td>
																							</tr>
																						</table>
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
								</td>
							</tr>
							<tr>
								<td class="news" style="width: 50%">
									<div class="light_form_outer">
										<div class="light_form_top">
											<div class="light_form_bottom">
												<div class="light_form_left">
													<div class="light_form_right">
														<div class="light_form_bottom_left">

															<div class="light_form_bottom_right">
																<div class="light_form_top_left">
																	<div class="light_form_top_right">
																		<div class="form_inner">
																			<iframe class="newsIF"
																				src="http://www.ebi.ac.uk/microarray-srv/biosd/left.html"
																				frameBorder="0" allowtransparency="true"
																				scrolling="yes"> </iframe>
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
								</td>

								<td class="sources" style="width: 50%">
									<div class="light_form_outer">
										<div class="light_form_top">
											<div class="light_form_bottom">
												<div class="light_form_left">
													<div class="light_form_right">
														<div class="light_form_bottom_left">
															<div class="light_form_bottom_right">
																<div class="light_form_top_left">
																	<div class="light_form_top_right">
																		<div class="form_inner">
																			<iframe class="sourcesIF"
																				src="http://www.ebi.ac.uk/microarray-srv/biosd/right.html"
																				frameBorder="0" allowtransparency="true"></iframe>
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


								</td>
							</tr>
						</table> <!-- InstanceEndEditable --> 

			</div>
			</div>
			<%@ include
							file="WEB-INF/server-assets/html/footer_ebi.html"%>
</body>
</html>
