<?xml version="1.0" encoding="windows-1252"?>
<!-- * Copyright 2009-2013 European Molecular Biology Laboratory * * Licensed 
	under the Apache License, Version 2.0 (the "License"); * you may not use 
	this file except in compliance with the License. * You may obtain a copy 
	of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless 
	required by applicable law or agreed to in writing, software * distributed 
	under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES 
	OR CONDITIONS OF ANY KIND, either express or implied. * See the License for 
	the specific language governing permissions and * limitations under the License. 
	* -->
	<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:ae="http://www.ebi.ac.uk/arrayexpress/XSLT/Extension" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="html xs fn ae" exclude-result-prefixes="html xs fn ae"
	version="2.0">

	<xsl:param name="host" />
	<xsl:param name="context-path" />
	<xsl:param name="original-request-uri" />
	<xsl:param name="referer" />
	<xsl:param name="query-string" />
	<xsl:param name="userid" />
	<xsl:param name="username" />


	<!--###### DO NOT CHANGE THE OUTPUT METHOD FROM XHTML to HTML ###### -->
	<!--############ -->
	<!--############ -->

	<!-- we need that to allow hexadecimal characters - look at sampleGroup:SAMEG30886 
		<attribute class="Person First Name" classDefined="true" dataType="STRING"> 
		<value>Toma�&#x85;¾</value> -->

	<!-- <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="windows-1252"/> -->
	<xsl:output omit-xml-declaration="yes" method="xhtml"
		indent="no" encoding="windows-1252" doctype-public="-//W3C//DTD XHTML 1.1//EN" />
	<!--###### DO NOT CHANGE THE OUTPUT METHOD FROM XHTML to HTML ###### -->
	<!--############ -->
	<!--############ -->



	<xsl:variable name="relative-uri"
		select="fn:substring-after($original-request-uri, $context-path)" />

	<xsl:variable name="relative-referer"
		select="if (fn:starts-with($referer, '/')) then fn:substring-after($referer, $context-path) else ''" />
	<xsl:variable name="secure-host"
		select="if (fn:matches($host, '^http[:]//www(dev)?[.]ebi[.]ac[.]uk$')) then fn:replace($host, '^http[:]//', 'https://') else ''" />

	<xsl:template name="ae-page">
		<xsl:param name="pIsSearchVisible" as="xs:boolean" />
		<xsl:param name="pSearchInputValue" as="xs:string" />
		<xsl:param name="pTitleTrail" as="xs:string" />
		<xsl:param name="pBreadcrumbTrail" />
		<xsl:param name="pExtraCSS" />
		<xsl:param name="pExtraJS" />
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		<!-- http://paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
		<xsl:comment>[if
			lt IE 7]>&lt;html class="no-js ie6 oldie" lang="en">&lt;![endif]
		</xsl:comment>
		<xsl:comment>[if
			IE 7]>&lt;html class="no-js ie7 oldie" lang="en">&lt;![endif]
		</xsl:comment>
		<xsl:comment>[if
			IE 8]>&lt;html class="no-js ie8 oldie" lang="en">&lt;![endif]
		</xsl:comment>
		<xsl:comment>[if
			gt IE 8]>&lt;!
		</xsl:comment>
		<html class="no-js" lang="en">
			<xsl:comment>
				&lt;![endif]
			</xsl:comment>
			<xsl:call-template name="ae-page-head">
				<xsl:with-param name="pTitleTrail" select="$pTitleTrail" />
				<xsl:with-param name="pExtraCode" select="$pExtraCSS" />
			</xsl:call-template>
			<xsl:call-template name="ae-page-body">
				<xsl:with-param name="pIsSearchVisible" select="$pIsSearchVisible" />
				<xsl:with-param name="pSearchInputValue" select="$pSearchInputValue" />
				<xsl:with-param name="pBreadcrumbTrail" select="$pBreadcrumbTrail" />
				<xsl:with-param name="pExtraCode" select="$pExtraJS" />
			</xsl:call-template>
		</html>

	</xsl:template>

	<xsl:template name="ae-page-head">
		<xsl:param name="pTitleTrail" />
		<xsl:param name="pExtraCode" />
		<head>
			<meta charset="windows-1252" />

			<title>
				<xsl:if test="$pTitleTrail">
					<xsl:value-of select="$pTitleTrail" />
					&lt;
				</xsl:if>
				<xsl:text>BioSamples &lt; EMBL-EBI</xsl:text>
			</title>
			<meta name="description" content="EMBL-EBI" />   <!-- Describe what this page is about -->
			<meta name="keywords" content="bioinformatics, europe, institute" /> <!-- A few keywords that relate to the content of THIS PAGE (not the whol 
				project) -->
			<meta name="author" content="EMBL-EBI" />        <!-- Your [project-name] here -->

			<!-- Mobile viewport optimized: http://j.mp/bplateviewport -->
			<meta name="viewport" content="width=device-width,initial-scale=1" />

			<!-- Place favicon.ico and apple-touch-icon.png in the root directory: 
				http://mathiasbynens.be/notes/touch-icons -->

			<!-- CSS: implied media=all -->
			<!-- CSS concatenated and minified via ant build script -->
			<link rel="stylesheet"
				href="//www.ebi.ac.uk/web_guidelines/css/compliance/mini/ebi-fluid-embl.css"
				type="text/css" />
			<link rel="stylesheet" href="{$context-path}/assets/stylesheets/font-awesome.css"
				type="text/css" />
			<link rel="stylesheet"
				href="{$context-path}/assets/stylesheets/biosamples_common_10.css"
				type="text/css" />

			<xsl:copy-of select="$pExtraCode" />
			<!-- end CSS -->

			<!-- All JavaScript at the bottom, except for Modernizr / Respond. Modernizr 
				enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width 
				CSS3 Media Queries For optimal performance, use a custom Modernizr build: 
				http://www.modernizr.com/download/ -->

			<!-- Full build -->
			<!-- <script src="../js/libs/modernizr.minified.2.1.6.js"></script> -->

			<!-- custom build (lacks most of the "advanced" HTML5 support -->
			<script
				src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.custom.49274.js" />
		</head>
	</xsl:template>

	<xsl:template name="ae-page-body">
		<xsl:param name="pIsSearchVisible" />
		<xsl:param name="pSearchInputValue" />
		<xsl:param name="pBreadcrumbTrail" />
		<xsl:param name="pExtraCode" />

		<body>   <!-- add any of your classes or IDs -->
			<xsl:attribute name="class">
                <xsl:text>level2 ${interface.application.body.class}</xsl:text>
             </xsl:attribute>

			<div id="skip-to">
				<ul>
					<li>
						<a href="#content" title="">Skip to main content</a>
					</li>
					<li>
						<a href="#local-nav" title="">Skip to local navigation</a>
					</li>
					<li>
						<a href="#global-nav" title="">Skip to EBI global navigation menu</a>
					</li>
					<li>
						<a href="#global-nav-expanded" title="">Skip to expanded EBI global
							navigation menu (includes all sub-sections)</a>
					</li>
				</ul>
			</div>

			<div id="wrapper" class="container_24">
				<header>
					<div id="global-masthead" class="masthead grid_24">
						<!--This has to be one line and no newline characters -->
						<a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage">
							<img
								src="//www.ebi.ac.uk/web_guidelines/images/logos/EMBL-EBI/EMBL_EBI_Logo_white.png"
								alt="EMBL European Bioinformatics Institute" />
						</a>

						<nav>
							<ul id="global-nav">
								<!-- set active class as appropriate -->
								<li class="first active" id="services">
									<a href="//www.ebi.ac.uk/services">Services</a>
								</li>
								<li id="research">
									<a href="//www.ebi.ac.uk/research">Research</a>
								</li>
								<li id="training">
									<a href="//www.ebi.ac.uk/training">Training</a>
								</li>
								<li id="industry">
									<a href="//www.ebi.ac.uk/industry">Industry</a>
								</li>
								<li id="about" class="last">
									<a href="//www.ebi.ac.uk/about">About us</a>
								</li>
							</ul>
						</nav>

					</div>
					<div id="local-masthead" class="masthead grid_24 nomenu">

						<!-- local-title -->
						<!-- NB: for additional title style patterns, see http://frontier.ebi.ac.uk/web/style/patterns -->

						<div id="local-title" class="grid_12 alpha logo-title">
							<a href="${interface.application.base.path}"
								title="Back to ${interface.application.base.service.name} homepage">
								<img src="${interface.application.base.service.logo}" alt="{interface.application.base.service.name} logo"
									width="64" height="64" />
							</a>
							<span style="padding-top: 5px">
								<h1>${interface.application.base.service.name}</h1>
							</span>
						</div>
						<!-- /local-title -->

						<!-- local-search -->
						<!-- NB: if you do not have a local-search, delete the following div, 
							and drop the class="grid_12 alpha" class from local-title above -->

						<div class="grid_12 omega">
							<form id="local-search" name="local-search"
								action="browse.html" method="get">

								<fieldset>

									<div class="left">
										<label>
											<input type="text" name="keywords" id="local-searchbox" />
										</label>
										<!-- Include some example searchterms - keep them short and few! -->
										<span class="examples">
											Search by:
											<select id="biosamples_index" name="biosamples_index"
												onchange="changeSearch($('#local-search'),$('#biosamples_index option:selected').val())">
												<option value="${interface.application.base.path}/browse_samples.html">Samples</option>
												<xsl:choose>
													<xsl:when
														test="fn:starts-with($relative-uri, '/browse.html')  or fn:starts-with($relative-uri, '/group')">
														<option value="${interface.application.base.path}/browse.html" selected="true">Groups</option>
													</xsl:when>
													<xsl:otherwise>
														<option value="${interface.application.base.path}/browse.html">Samples</option>
													</xsl:otherwise>
												</xsl:choose>
											</select> &nbsp;&nbsp;Examples:
											<a
												href="javascript:submitFormForExamples($('#local-search'),$('#biosamples_index option:selected').val(),'leukemia')">leukemia</a>
											,
											<a
												href="javascript:submitFormForExamples($('#local-search'),$('#biosamples_index option:selected').val(),'ArrayExpress')">ArrayExpress</a>
										</span>
									</div>

									<div class="right">
										<input type="submit" name="submitb" value="Search"
											class="submit" />
										<!-- If your search is more complex than just a keyword search, 
											you can link to an Advanced Search, with whatever features you want available -->
										<!-- <span class="adv"> <a href="${interface.application.base.path}/browse.html" 
											id="adv-search" title="Advanced">Advanced</a> </span> -->
									</div>

								</fieldset>

							</form>
						</div>


						<!-- /local-search -->
						<!-- local-nav -->
						<nav>
							<ul class="grid_24" id="local-nav">
								<li>
									<xsl:attribute name="class">first<xsl:if
										test="$relative-uri = '/'"> active</xsl:if></xsl:attribute>
									<a href="{$context-path}/" title="Biosamples ${project.version}.r${buildNumber}">Home</a>
								</li>
								<li>
									<xsl:if
										test="fn:starts-with($relative-uri, '/browse_samples.html')  or fn:starts-with($relative-uri, '/sample')">
										<xsl:attribute name="class">active</xsl:attribute>
									</xsl:if>
									<a href="{$context-path}/browse_samples.html" title="Samples">Samples</a>
								</li>
								<li>
									<xsl:if
										test="fn:starts-with($relative-uri, '/browse.html') or fn:starts-with($relative-uri, '/group')">
										<xsl:attribute name="class">active</xsl:attribute>
									</xsl:if>
									<a href="{$context-path}/browse.html" title="Groups">Sample Groups</a>
								</li>
								<li>
									<xsl:if test="fn:starts-with($relative-uri, '/help/')">
										<xsl:attribute name="class">active</xsl:attribute>
									</xsl:if>
									<a href="{$context-path}/help/index.html" title="Help">Help</a>
								</li>
								<li class="last">
									<xsl:if test="$relative-uri = '/about.html'">
										<xsl:attribute name="class">active</xsl:attribute>
									</xsl:if>
									<a href="{$context-path}/about.html">About
										BioSamples</a>
								</li> <!-- If you need to include functional (as opposed to purely navigational) 
									links in your local menu, add them here, and give them a class of "functional". 
									Remember: you'll need a class of "last" for whichever one will show up last... 
									For example: -->
								<!-- <li class="functional last login"> <a href="#" class="icon icon-functional" 
									data-icon="l"> <xsl:choose> <xsl:when test="$userid = '1'"> Login </xsl:when> 
									<xsl:when test="fn:exists($username)"> Logout [ <xsl:value-of select="$username" 
									/> ] </xsl:when> <xsl:otherwise> Logout </xsl:otherwise> </xsl:choose> </a> 
									</li> -->
								<li class="functional feedback">
									<a href="#" class="icon icon-static" data-icon="\">Feedback</a>
								</li>
							</ul>
						</nav>
						<!-- /local-nav -->
					</div>
				</header>
				<div class="contents" id="contents">
					<section id="ae-login" style="display: none">
						<h3>
							BioSamples login
							<a id="ae-login-close" href="#" class="icon icon-functional"
								data-icon="x"></a>
						</h3>
						<form id="ae-login-form" method="post"
							action="{$secure-host}{$context-path}/auth">
							<fieldset>
								<label for="ae-user-field">User name</label>
								<input id="ae-user-field" name="u" maxlength="50" />
							</fieldset>
							<fieldset>
								<label for="ae-pass-field">Password</label>
								<input id="ae-pass-field" type="password" name="p"
									maxlength="50" />
							</fieldset>
							<span id="ae-login-remember-option">
								<input id="ae-login-remember" name="r" type="checkbox" />
								<label for="ae-login-remember">Remember me</label>
							</span>
							<input class="submit" type="submit" value="Login" />
							<div class="ae-login-status" style="display: none">
								<span class="alert"></span>
								<span class="alert"></span>
							</div>
							<div id="ae-login-forgot">
								<a href="#">Forgot user name or password?</a>
							</div>
						</form>
						<form id="ae-forgot-form" method="post"
							action="{$secure-host}{$context-path}/auth">
							<fieldset>
								<label for="ae-name-email-field">User name or email address</label>
								<input id="ae-name-email-field" name="e" maxlength="50" />
							</fieldset>
							<fieldset>
								<label for="ae-accession-field">Experiment accession
									associated with the account</label>
								<input id="ae-accession-field" name="a" maxlength="14" />
							</fieldset>
							<div>We will send you a reminder with your account information</div>
							<div class="ae-login-status" style="display: none">
								<span class="alert"></span>
								<span class="alert"></span>
							</div>
							<input class="submit" type="submit" value="Send" />
						</form>
					</section>

					<section id="ae-feedback" style="display:none;">
						<h3>
							Have your say
							<a id="ae-feedback-close" href="#" class="icon icon-functional"
								data-icon="x"></a>
						</h3>
						<form method="post" action="#" onsubmit="return false">
							<fieldset>
								<label for="ae-feedback-message">We value your feedback.
									Please leave your
									comment below.</label>
								<textarea id="ae-feedback-message" name="m"></textarea>
							</fieldset>
							<fieldset>
								<label for="ae-email-field">
									Optionally please enter your
									email address if you wish to get a
									response.
									<br />
									We will never
									share this address with anyone else.
								</label>
								<input id="ae-email-field" name="e" maxlength="50" />
							</fieldset>
							<input type="hidden" name="p"
								value="{$host}{$context-path}{$relative-uri}{if ($query-string) then fn:concat('?', $query-string) else ''}" />
							<input type="hidden" name="r"
								value="{$host}{$context-path}{$relative-referer}" />
							<input class="submit" type="submit" value="Send" />
						</form>
					</section>
				</div>
				<div id="content" role="main" class="grid_24 clearfix">
					<!-- If you require a breadcrumb trail, its root should be your service. 
						You don't need a breadcrumb trail on the homepage of your service... -->
					<xsl:if test="fn:boolean($pBreadcrumbTrail)">
						<nav id="breadcrumb">
							<p>
								<a href="{$context-path}/">BioSamples</a>
								&gt;
								<xsl:copy-of select="$pBreadcrumbTrail" />
							</p>
						</nav>
					</xsl:if>


					<xsl:call-template name="ae-content-section" />
					<!-- Suggested layout containers -->
					<!-- <section> <h2>[Page title]</h2> <p>Your content</p> </section> 
						<section> <h3>[Another title]</h3> <p>More content in a full-width container.</p> 
						</section> -->
					<!-- End suggested layout containers -->
				</div>
				<footer>
					<!-- Optional local footer (insert citation / project-specific copyright 
						/ etc here -->
					<!-- <div id="local-footer" class="grid_24 clearfix"> <p>How to reference 
						this page: ...</p> </div> -->
					<!-- End optional local footer -->
					<div id="global-footer" class="grid_24">
						<nav id="global-nav-expanded">
							<div class="grid_4 alpha">
								<h3 class="embl-ebi">
									<a href="//www.ebi.ac.uk/" title="EMBL-EBI">EMBL-EBI</a>
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
						</nav>
						<section id="ebi-footer-meta">
							<p class="address">EMBL-EBI, Wellcome Trust Genome Campus, Hinxton,
								Cambridgeshire, CB10 1SD, UK &#160; &#160; +44 (0)1223 49 44 44</p>
							<p class="legal">
								Copyright &#169; EMBL-EBI 2013 | EBI is an Outstation of the
								<a href="http://www.embl.org">European Molecular Biology Laboratory</a>
								|
								<a href="/about/privacy">Privacy</a>
								|
								<a href="/about/cookies">Cookies</a>
								|
								<a href="/about/terms-of-use">Terms of use</a>
							</p>
						</section>
					</div>
				</footer>
			</div>  <!--! end of #wrapper -->

			<script defer="defer"
				src="//www.ebi.ac.uk/web_guidelines/js/cookiebanner.js" />
			<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/foot.js" />
			<script type="text/javascript">
				<xsl:text>var contextPath = "</xsl:text>
				<xsl:value-of select="$context-path" />
				<xsl:text>";</xsl:text>
			</script>
			<script src="{$context-path}/assets/scripts/jquery-1.8.2.min.js"
				type="text/javascript"></script>
			<script src="{$context-path}/assets/scripts/jsdeferred.jquery-0.3.1.js"
				type="text/javascript"></script>
			<script src="{$context-path}/assets/scripts/jquery.cookie-1.0.js"
				type="text/javascript"></script>
			<script src="{$context-path}/assets/scripts/jquery.query-2.1.7m-ebi.js"
				type="text/javascript"></script>
			<script
				src="{$context-path}/assets/scripts/jquery.autocomplete-1.1.0.130305.js"
				type="text/javascript"></script>
			<script src="{$context-path}/assets/scripts/jquery.caret-range-1.0.js" />
			<script src="{$context-path}/assets/scripts/biosamples_common_10.js"
				type="text/javascript"></script>
			<xsl:copy-of select="$pExtraCode" />
			${interface.application.google.analytics}
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

</xsl:stylesheet>