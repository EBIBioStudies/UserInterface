<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> <!ENTITY copy "©">]>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="html"
	exclude-result-prefixes="html" version="1.0">


	<xsl:template name="page-header">
		<xsl:param name="pTitle" />
		<xsl:param name="pExtraCode" />
		<head>
			<meta http-equiv="Content-Type" content="text/html;charset=windows-1252" />
			<meta http-equiv="Content-Language" content="en-GB" />
			<meta http-equiv="Window-target" content="_top" />
			<meta name="no-email-collection" content="http://www.unspam.com/noemailcollection/" />

			<title>
				<xsl:value-of select="$pTitle" />
			</title>

			<link rel="SHORTCUT ICON"
				href="${interface.application.link.www_domain}/bookmark.ico" />

			<link rel="stylesheet"
				href="${interface.application.link.www_domain.inc}/css/contents.css"
				type="text/css" />
			<link rel="stylesheet"
				href="${interface.application.link.www_domain.inc}/css/userstyles.css"
				type="text/css" />

			<link type="text/css" rel="stylesheet"
				href="//www.ebi.ac.uk/web_guidelines/css/mitigation/develop/ebi-mitigation.css" />
			<link type="text/css" rel="stylesheet"
				href="//www.ebi.ac.uk/web_guidelines/css/mitigation/develop/embl-petrol-colours.css" />
			<script defer="defer"
				src="//www.ebi.ac.uk/web_guidelines/js/cookiebanner.js"></script>
			<script defer="defer" src="http://www.ebi.ac.uk/web_guidelines/js/foot.js"></script>

			 <link rel="stylesheet"
				href="{$basepath}/assets/stylesheets/biosamples_common_10.css" type="text/css" /> 
			<!-- <link rel="stylesheet" href="{$basepath}/assets/stylesheets/biosamples_html_page_10.css" 
				type="text/css"/> -->

			<xsl:copy-of select="$pExtraCode" />

			<script type="text/javascript">
				<xsl:text>var contextPath = "</xsl:text>
				<xsl:value-of select="$basepath" />
				<xsl:text>";</xsl:text>
			</script>
			${interface.application.google.analytics}
		</head>

	</xsl:template>

	<xsl:template name="page-body">
		<body class="${interface.application.body.class}">
			<div class="headerdiv" id="headerdiv">
	<div class="header">
		<div id="global-masthead" class="masthead grid_24">
			<!--This has to be one line and no newline characters-->
			<a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage"><img
				src="//www.ebi.ac.uk/web_guidelines/images/logos/EMBL-EBI/EMBL_EBI_Logo_white.png"
				alt="EMBL European Bioinformatics Institute" /></a>
			<div class="nav">
				<ul id="global-nav">
					<!-- set active class as appropriate -->
					<li class="first active" id="services"><a
						href="//www.ebi.ac.uk/services">Services</a></li>
					<li id="research"><a href="//www.ebi.ac.uk/research">Research</a></li>
					<li id="training"><a href="//www.ebi.ac.uk/training">Training</a></li>
					<li id="industry"><a href="//www.ebi.ac.uk/industry">Industry</a></li>
					<li id="about" class="last"><a href="//www.ebi.ac.uk/about">About us</a></li>
				</ul>
			</div>
		</div>

		<div id="local-masthead" class="masthead grid_24">

			<!-- local-title -->
			<!-- NB: for additional title style patterns, see http://frontier.ebi.ac.uk/web/style/patterns -->

			<div id="local-title" class="grid_12 alpha logo-title">
				<a href="${interface.application.base.path}"
					title="Back to ${interface.application.base.service.name} homepage"><img
					src="${interface.application.base.service.logo}"
					alt="{interface.application.base.service.name} logo" width="64"
					height="64"/></a> <span style="padding-top: 5px"><h1>${interface.application.base.service.name}</h1></span>
			</div>
		</div>
	</div>
</div>

			<div class="contents" id="contents">
				<xsl:call-template name="ae-contents" />
				<noscript>
					<div id="ae_noscript" class="bs_contents_frame bs_assign_font bs_white_bg">
						<div class="bs_center_box">
							<div id="bs_contents_box_915px">
								<div class="ae_error_area">BioSamples uses JavaScript for better data
									handling and enhanced representation. Please enable JavaScript
									if you want to continue browsing BioSamples.</div>
							</div>
						</div>
					</div>
				</noscript>


				<div class="footerdiv" id="footerdiv">
					<div class="footer">
						<!-- Optional local footer (insert citation / project-specific copyright 
							/ etc here -->
						<!-- <div id="local-footer" class="grid_24 clearfix"> </div> -->
						<!-- End optional local footer -->

						<div id="global-footer" class="grid_24 clearfix">
							<div class="nav" id="global-nav-expanded">
								<div class="grid_4 alpha">
									<h3 class="embl-ebi">
										<a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage">EMBL-EBI</a>
									</h3>
								</div>

								<div class="grid_4">
									<h3 class="services">
										<a href="//www.ebi.ac.uk/services">Services</a>
									</h3>
								</div>

								<div class="grid_4">
									<h3 class="research">
										<a href="//www.ebi.ac.uk/research">Research</a>
									</h3>
								</div>

								<div class="grid_4">
									<h3 class="training">
										<a href="//www.ebi.ac.uk/training">Training</a>
									</h3>
								</div>

								<div class="grid_4">
									<h3 class="industry">
										<a href="//www.ebi.ac.uk/industry">Industry</a>
									</h3>
								</div>

								<div class="grid_4 omega">
									<h3 class="about">
										<a href="//www.ebi.ac.uk/about">About us</a>
									</h3>
								</div>
							</div>

							<div class="section" id="ebi-footer-meta">
								<p class="address">EMBL-EBI,
									Wellcome Trust Genome Campus, Hinxton, Cambridgeshire, CB10
									1SD,
									UK &nbsp; &nbsp;
									+44 (0)1223 49 44 44
								</p>
								<p>
									Copyright © EMBL-EBI 2012 | EBI is an Outstation of the
									<a href="http://www.embl.org">European Molecular Biology Laboratory</a>
									|
									<a href="/about/privacy">Privacy</a>
									|
									<a href="/about/cookies">Cookies</a>
									|
									<a href="/about/terms-of-use">Terms of use</a>
								</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</body>
	</xsl:template>

	<!-- no EBI common crap -->
	<xsl:template name="page-header-plain">
		<xsl:param name="pTitle" />
		<xsl:param name="pExtraCode" />
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=windows-1252" />
			<meta http-equiv="Content-Language" content="en-GB" />
			<meta http-equiv="Window-target" content="_top" />
			<meta name="no-email-collection" content="http://www.unspam.com/noemailcollection/" />

			<title>
				<xsl:value-of select="$pTitle" />
			</title>

			<link rel="SHORTCUT ICON"
				href="${interface.application.link.www_domain}/bookmark.ico" />

			<xsl:copy-of select="$pExtraCode" />
			${interface.application.google.analytics}
		</head>
	</xsl:template>

	<xsl:template name="page-body-plain">
		<body>
			<div id="bs_contents" class="bs_assign_font">
				<xsl:call-template name="ae-contents" />
			</div>
		</body>
	</xsl:template>

	<xsl:template name="block-warning">
		<xsl:param name="pStyle" />
		<xsl:param name="pMessage" />
		<div class="ae_center_box">
			<div id="ae_contents_box_915px">
				<div class="{$pStyle}">
					<div>
						<xsl:copy-of select="$pMessage" />
					</div>
					<div>
						We value your feedback. If you believe there was an error and wish
						to report it, please do not hesitate to drop us a line to
						<strong>biosamples(at)ebi.ac.uk</strong>
						or use
						<a href="${interface.application.link.www_domain}/support/"
							title="EBI Support">EBI Support Feedback</a>
						form.
					</div>
				</div>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="block-access-restricted">
		<xsl:call-template name="block-warning">
			<xsl:with-param name="pStyle" select="'ae_protected_area'" />
			<xsl:with-param name="pMessage">
				Sorry, the access to the resource you are requesting is restricted.
				You may wish to go
				<a href="javascript:history.back()" title="Click to go to the page you just left">back</a>
				, or to
				<a href="{$basepath}" title="Biosamples Home">Biosamples Home</a>
				.
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="block-not-found">
		<xsl:call-template name="block-warning">
			<xsl:with-param name="pStyle" select="'ae_warn_area'" />
			<xsl:with-param name="pMessage">
				The resource you are requesting is not found. You may wish to go
				<a href="javascript:history.back()" title="Click to go to the page you just left">back</a>
				, or to
				<a href="{$basepath}" title="Biosamples Home">Biosamples Home</a>
				.
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>