<%@ page import="java.net.*"%>

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
					<li id="about" class="last"><a href="//www.ebi.ac.uk/about">About
							us</a></li>
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
					height="64"></a> <span style="padding-top: 5px"><h1>${interface.application.base.service.name}</h1></span>
			</div>
			<nav>
				<%
					String reqURI = request.getParameter("original-request-uri");
					//String x="{$interface.application.base.path}";
					String relativeReqURI = "";
					relativeReqURI = reqURI.substring(
							reqURI.indexOf("/biosamples") + 11, reqURI.length());
					String homeActive = relativeReqURI.equalsIgnoreCase("/") ? "active"
							: "";
					String samplesActive = relativeReqURI.startsWith("/browse.html")
							|| relativeReqURI.startsWith("/group")
							|| relativeReqURI.startsWith("/sample") ? "active" : "";
					String helpActive = relativeReqURI.startsWith("/help/") ? "active"
							: "";
					String aboutActive = relativeReqURI.startsWith("/about.html") ? "active"
							: "";
				%>
				<ul class="grid_24" id="local-nav">
					<li class="first <%=homeActive%>"><a href="/biosamples/"
						title="Biosamples">Home</a></li>
					<li class="<%=samplesActive%>"><a
						href="/biosamples/browse.html" title="Samples">Samples</a></li>
					<li class="<%=helpActive%>"><a
						href="/biosamples/help/index.html" title="Help">Help</a></li>
					<li class="last <%=aboutActive%>"><a
						href="/biosamples/about.html">About BioSamples</a></li>
					<li class=" functional last login"><a href="#">Login</a></li>
					<li class="functional feedback first"><a href="#">Feedback</a></li>
				</ul>
			</nav>
		</div>
	</div>
</div>

<div class="contents" id="contents">
	<section id="ae-login" style="display: none">
		<h3>
			BioSamples login<a id="ae-login-close" href="#"
				class="icon icon-functional" data-icon="x"></a>
		</h3>
		<form id="ae-login-form" method="post"
			action="https://www.ebi.ac.uk/biosamples/auth">
			<fieldset>
				<label for="ae-user-field">User name</label><input
					id="ae-user-field" name="u" maxlength="50">
			</fieldset>
			<fieldset>
				<label for="ae-pass-field">Password</label><input id="ae-pass-field"
					type="password" name="p" maxlength="50">
			</fieldset>
			<span id="ae-login-remember-option"><input
				id="ae-login-remember" name="r" type="checkbox"><label
				for="ae-login-remember">Remember me</label></span><input class="submit"
				type="submit" value="Login">
			<div class="ae-login-status" style="display: none">
				<span class="alert"></span>
			</div>
			<div id="ae-login-forgot">
				<a href="#">Forgot user name or password?</a>
			</div>
		</form>
		<form id="ae-forgot-form" method="post"
			action="https://www.ebi.ac.uk/biosamples/auth">
			<fieldset>
				<label for="ae-name-email-field">User name or email address</label><input
					id="ae-name-email-field" name="e" maxlength="50">
			</fieldset>
			<fieldset>
				<label for="ae-accession-field">Experiment accession
					associated with the account</label><input id="ae-accession-field" name="a"
					maxlength="14">
			</fieldset>
			<div>We will send you a reminder with your account information</div>
			<div class="ae-login-status" style="display: none">
				<span class="alert"></span>
			</div>
			<input class="submit" type="submit" value="Send">
		</form>
	</section>

	<section id="ae-feedback" style="display: none">
		<h3>
			Have your say<a id="ae-feedback-close" href="#"
				class="icon icon-functional" data-icon="x"></a>
		</h3>
		<form method="post" action="#" onsubmit="return false">
			<fieldset>
				<label for="ae-feedback-message">We value your feedback.
					Please leave your comment below.</label>
				<textarea id="ae-feedback-message" name="m"></textarea>
			</fieldset>
			<fieldset>
				<label for="ae-email-field">Optionally please enter your
					email address if you wish to get a response.<br>We will never
					share this address with anyone else.
				</label><input id="ae-email-field" name="e" maxlength="50">
			</fieldset>
			<input type="hidden" name="p"
				value="http://www.ebi.ac.uk/biosamples/"><input
				type="hidden" name="r" value="http://www.ebi.ac.uk/biosamples/"><input
				class="submit" type="submit" value="Send">
		</form>
	</section>