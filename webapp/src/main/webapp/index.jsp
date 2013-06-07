<%@ page import="java.net.*"%>

<!doctype html>
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js ie6 oldie" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7 oldie" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8 oldie" lang="en"> <![endif]-->
<!-- Consider adding an manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!-->
<html class="no-js" lang="en">
<!--<![endif]-->
<head>
<meta charset="utf-8">

<!-- Use the .htaccess and remove these lines to avoid edge case issues.
       More info: h5bp.com/b/378 -->
<!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> -->
<!-- Not yet implemented -->

<title>BioSamples &lt; EMBL-EBI</title>
<meta name="description" content="EMBL-EBI">
<!-- Describe what this page is about -->
<meta name="keywords" content="bioinformatics, europe, institute">
<!-- A few keywords that relate to the content of THIS PAGE (not the whol project) -->
<meta name="author" content="EMBL-EBI">
<!-- Your [project-name] here -->

<!-- Mobile viewport optimized: j.mp/bplateviewport -->
<meta name="viewport" content="width=device-width,initial-scale=1">

<!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

<!-- CSS: implied media=all -->
<!-- CSS concatenated and minified via ant build script-->
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/boilerplate-style.css">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-global.css"
	type="text/css" media="screen">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-visual.css"
	type="text/css" media="screen">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/984-24-col-fluid.css"
	type="text/css" media="screen">

<!-- you can replace this with [projectname]-colours.css. See http://frontier.ebi.ac.uk/web/style/colour for details of how to do this -->
<!-- also inform ES so we can host your colour palette file -->
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/embl-petrol-colours.css"
	type="text/css" media="screen">

<link rel="stylesheet"
	href="assets/stylesheets/biosamples_common_10.css" type="text/css" />

<!-- for production the above can be replaced with -->
<!--
  <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/mini/ebi-fluid-embl.css">
  -->

<style type="text/css">
/* You have the option of setting a maximum width for your page, and making sure everything is centered */
/* body { max-width: 1600px; margin: 0 auto; } */
</style>

<!-- end CSS-->


<!-- All JavaScript at the bottom, except for Modernizr / Respond.
       Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
       For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->

<!-- Full build -->
<!-- <script src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.minified.2.1.6.js"></script> -->

<!-- custom build (lacks most of the "advanced" HTML5 support -->
<script
	src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.custom.49274.js"></script>

</head>

<body class="level2" onload="updateBiosamplesStats()">
	<!-- add any of your classes or IDs -->
	<div id="skip-to">
		<ul>
			<li><a href="#content">Skip to main content</a></li>
			<li><a href="#local-nav">Skip to local navigation</a></li>
			<li><a href="#global-nav">Skip to EBI global navigation menu</a></li>
			<li><a href="#global-nav-expanded">Skip to expanded EBI
					global navigation menu (includes all sub-sections)</a></li>
		</ul>
	</div>

	<%@ include file="WEB-INF/server-assets/html/header_ebi.jsp"%>

	<div id="content" role="main" class="grid_24 clearfix">


		<div class="grid_24 alpha">
			<section>
				<h2>BioSamples</h2>
				<p class="intro justify">The BioSamples database aggregates
					sample information for reference samples e.g. Coriell Cell lines
					and samples for which data exist in one of the EBI's assay
					databases such as ArrayExpress, the European Nucleotide archive, or
					Pride. It provides links to assays for specific samples, and
					accepts direct submissions of samples.</p>
			</section>
		</div>


		<div class="grid_18 alpha">
			<section id="ae-news">
				<h3 class="icon icon-generic" data-icon="N">Latest News</h3>
				<p class="news">
					<iframe src="http://www.ebi.ac.uk/microarray-srv/biosd/left.html"
						frameBorder="0" allowtransparency="true" scrolling="no"
						width="100%" height="200"> </iframe>
				</p>
			</section>
		</div>


		<div class="grid_6 omega">
			<section>
				<h3 class="icon icon-generic" data-icon="g">Data Content</h3>
				<!-- <h5>Updated today at 06:00</h5> -->
				<ul>
					<li><span id="totalSamples"></span> Samples</li>
					<li><span id="totalGroups"></span> Groups</li>
					<!-- <li>XXXX Samples</li>
					<li>YYYYY groups</li>
					<li>XXXX Samples</li>
					<li>YYYYY groups</li>
					<li>XXXX Samples</li>
					<li>YYYYY groups</li>
					<li>XXXX Samples</li>
					<li>YYYYY groups</li>
					<li>XXXX Samples</li>
					<li>YYYYY groups</li>
					<li>XXXX Samples</li>
					<li>YYYYY groups</li> -->
				</ul>
			</section>
		</div>


		<div class="grid_24 alpha">
			<section>
				<div class="grid_8 alpha">
					<h3 class="icon icon-generic" data-icon="L">Links</h3>
					<p>
						Information about how to search BioSamples, understand search
						results, how to submit data and FAQ can be found in our <a
							href="/biosamples/help/index.html">Help section</a>.
					</p>
					<p>
						Find out more about the <a href="/about/people/alvis-brazma">Functional
							Genomics group</a>.
					</p>
				</div>
				<div class="grid_8">
					<h3 class="icon icon-functional" data-icon="t">Tools and
						Access</h3>
					<p>
						<a href="/biosamples/help/api.html">Programmatic access</a>: query
						and download data using web services.
					</p>
				</div>
				<div class="grid_8 omega">
					<h3 class="icon icon-generic" data-icon="L">Related Projects</h3>
					<p>
						Discover up and down regulated genes in numerous experimental
						conditions in the <a href="/gxa/">Expression Atlas</a>.
					</p>
					<p>
						Explore the <a href="/efo">Experimental Factor Ontology</a> used
						to support queries and annotation of ArrayExpress data.
					</p>
				</div>
			</section>
		</div>


		<%@ include file="WEB-INF/server-assets/html/footer_ebi.html"%>

	</div>
	<!--! end of #wrapper -->


	<!-- JavaScript at the bottom for fast page loading -->

	<!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
	<!--
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
  <script>window.jQuery || document.write('<script src="../js/libs/jquery-1.6.2.min.js"><\/script>')</script>
  -->


	<!-- Your custom JavaScript file scan go here... change names accordingly -->
	<!-- <script src="assets/scripts/jquery-1.8.2.min.js" type="text/javascript"></script> -->
	<!-- <script src="assets/scripts/jsdeferred.jquery-0.3.1.js"
		type="text/javascript"></script> -->
	
	<script src="assets/scripts/jquery-1.8.2.min.js" type="text/javascript"></script>
	<!-- <script src="assets/scripts/jsdeferred.jquery-0.3.1.js"
		type="text/javascript"></script>  -->
	<script src="assets/scripts/jquery.cookie-1.0.js"
		type="text/javascript"></script>
	<!-- <script src="assets/scripts/jquery.query-2.1.7m-ebi.js"
		type="text/javascript"></script> -->
	<script src="assets/scripts/jquery.autocomplete-1.1.0.130305.js"
		type="text/javascript"></script>
	<script src="assets/scripts/jquery.caret-range-1.0.js"
		type="text/javascript"></script>
	<script src="assets/scripts/biosamples_common_10.js"
		type="text/javascript"></script>


 <!--  <script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/plugins.js"></script>
  <script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/script.js"></script> -->
 
	<script defer="defer"
		src="//www.ebi.ac.uk/web_guidelines/js/cookiebanner.js"></script>
	<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/foot.js"></script>



	<script type="text/javascript">

	var contextPath = "<%=contextPath%>";

		function updateBiosamplesStats() {
			// gets aer stats and updates the page
			$.get("biosamples-stats.xml").always(onBiosamplesStatsSuccess);
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
	<!-- end scripts-->

	<!-- Google Analytics details... -->
	<!-- Change UA-XXXXX-X to be your site's ID -->
	${interface.application.google.analytics}
	<!--
 
  <script>
    window._gaq = [['_setAccount','UAXXXXXXXX1'],['_trackPageview'],['_trackPageLoadTime']];
    Modernizr.load({
      load: ('https:' == location.protocol ? '//ssl' : '//www') + '.google-analytics.com/ga.js'
    });
  </script>
  -->


	<!-- Prompt IE 6 users to install Chrome Frame. Remove this if you want to support IE 6.
       chromium.org/developers/how-tos/chrome-frame-getting-started -->
	<!--[if lt IE 7 ]>
    <script src="//ajax.googleapis.com/ajax/libs/chrome-frame/1.0.3/CFInstall.min.js"></script>
    <script>window.attachEvent('onload',function(){CFInstall.check({mode:'overlay'})})</script>
  <![endif]-->

</body>
</html>

