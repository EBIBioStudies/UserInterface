<?xml version="1.0" encoding="windows-1252"?>
<!-- cannot change the enconding to ISO-8859-1 or UTF-8 -->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs aejava html"
	exclude-result-prefixes="xs aejava html" version="2.0">

	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:variable name="vPage"
		select="if ($page) then $page cast as xs:integer else 1" />
	<xsl:variable name="vPageSize"
		select="if ($pagesize) then $pagesize cast as xs:integer else 25" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:variable name="vSortBy"
		select="if ($sortby) then $sortby else 'accession'" />
	<xsl:variable name="vSortOrder"
		select="if ($sortorder) then $sortorder else 'ascending'" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="accession" />

	<xsl:param name="userid" />

	<xsl:param name="host" />
	<xsl:param name="basepath" />
	<xsl:param name="total" />

	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>


	<xsl:variable name="vkeywords" select="$keywords" />

	<xsl:output omit-xml-declaration="yes" method="xhtml"
		indent="no" encoding="windows-1252" doctype-public="-//W3C//DTD XHTML 1.1//EN" />

	<xsl:include href="biosamples-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />

	<xsl:template match="/">
		<html lang="en">
			<xsl:call-template name="page-header">
				<xsl:with-param name="pTitle">
					<xsl:value-of
						select="if ($accession) then concat(upper-case($accession), ' &lt; ') else ''" />
					<xsl:text>BioSamples &lt; EMBL-EBI</xsl:text>
				</xsl:with-param>

				<xsl:with-param name="pExtraCode">

					<link rel="stylesheet"
						href="{$basepath}/assets/stylesheets/biosamples_detail_10.css"
						type="text/css" />
					<script src="{$basepath}/assets/scripts/jquery-1.4.2.min.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/jquery.query-2.1.7m-ebi.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/biosamples_common_10.js"
						type="text/javascript" />
					<!-- <script src="{$basepath}/assets/scripts/biosamples_browse_10.js" 
						type="text/javascript" /> -->
					<script src="{$basepath}/assets/scripts/jsdeferred.jquery-0.3.1.js"
						type="text/javascript"></script>
					<script src="{$basepath}/assets/scripts/jquery.cookie-1.0.js"
						type="text/javascript"></script>
					<script src="{$basepath}/assets/scripts/jquery.caret-range-1.0.js"
						type="text/javascript"></script>
					<script
						src="{$basepath}/assets/scripts/jquery.autocomplete-1.1.0m-ebi.js"
						type="text/javascript"></script>
				</xsl:with-param>

			</xsl:call-template>
			<xsl:call-template name="page-body" />
		</html>
	</xsl:template>

	<xsl:template name="ae-contents">

		<xsl:variable name="vFiltered" select="//all/Sample" />

		<div id="bs_contents_box_100pc">
			<div id="bs_content">
				<div id="bs_navi">
					<a href="${interface.application.link.www_domain}/">EBI</a>
					<xsl:text> > </xsl:text>
					<a href="{$basepath}/browse.html">BioSamples</a>
					<xsl:text> > </xsl:text>
					<a href="{$basepath}/sample/{$vFiltered/@id}">
						<xsl:value-of select="$vFiltered/@id" />
					</a>
					<!-- <a href="{$basepath}/biosamplesgroup/{$vFiltered/@groupId}"> <xsl:value-of 
						select="$vFiltered/@groupId" /> </a> <xsl:text> > </xsl:text> <a href="{$basepath}/biosamplessample/detail/{$vFiltered/@groupId}/{$vFiltered/@id}"> 
						<xsl:value-of select="$vFiltered/@id" /> </a> -->
				</div>

				<xsl:choose>
					<xsl:when test="$vTotal&gt;0">
						<div id="bs_results_box">
							<table id="bs_results_table" border="0" cellpadding="0"
								cellspacing="0">
								<xsl:apply-templates select="$vFiltered">
								</xsl:apply-templates>
							</table>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="block-warning">
							<xsl:with-param name="pStyle" select="'bs_warn_area'" />
							<xsl:with-param name="pMessage">
								<xsl:text>There are no Samples matching your search criteria found in BioSamples Database.</xsl:text>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>

			</div>
		</div>
	</xsl:template>

	<xsl:template match="Sample">
		<xsl:variable name="vSample" select="."></xsl:variable>
		<tr>
			<td>
				<div class="detail_table">
					<table id="bs_results_tablesamplegroupdetail">

						<td colspan="2">
							<div id="bs_title">
								Sample Accession
								<xsl:copy-of select="attribute/value[../@class='Sample Accession']" />
							</div>
						</td>
						<xsl:for-each select="attribute[@class!='Sample Accession']">
							<tr>
								<td class="col_title">
									<b>
										<xsl:value-of select="./@class"></xsl:value-of>
										:
									</b>
								</td>
								<td>
									<xsl:choose>

										<xsl:when test="count(.[@class='Database URI'])&gt;0">
											<xsl:call-template name="process_uri">
												<xsl:with-param name="vSample" select="$vSample"></xsl:with-param>
											</xsl:call-template>
										</xsl:when>

										<xsl:when test="count(.//attribute[@class='Term Source REF'])=0">
											<xsl:copy-of select="value"></xsl:copy-of>
										</xsl:when>





										<xsl:otherwise>
											<xsl:call-template name="process_efo">
												<xsl:with-param name="pValue" select="value"></xsl:with-param>
											</xsl:call-template>
										</xsl:otherwise>

									</xsl:choose>
								</td>
							</tr>



						</xsl:for-each>
					</table>
				</div>
			</td>
		</tr>
	</xsl:template>




	<xsl:template name="detail-row">
		<xsl:param name="pName" />
		<xsl:param name="pFieldName" />
		<xsl:param name="pValue" />
		<xsl:if test="$pValue/node()">
			<xsl:call-template name="detail-section">
				<xsl:with-param name="pName" select="$pName" />
				<xsl:with-param name="pContent">
					<xsl:for-each select="$pValue">
						<div>
							<xsl:apply-templates select="." mode="highlight">
								<xsl:with-param name="pFieldName" select="$pFieldName" />
							</xsl:apply-templates>
						</div>
					</xsl:for-each>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="detail-section">
		<xsl:param name="pName" />
		<xsl:param name="pContent" />
		<tr>
			<td class="detail_name">
				<div class="outer">
					<xsl:value-of select="$pName" />
				</div>
			</td>
			<td class="detail_value">
				<div class="outer">
					<xsl:copy-of select="$pContent" />
				</div>
			</td>
		</tr>
	</xsl:template>




	<xsl:template name="process_uri">
		<xsl:param name="vSample" />
		<xsl:choose>
		<xsl:when test="count($vSample/attribute[@class!='Database URI'])=1">
		<xsl:variable name="bdName"
			select="lower-case($vSample/attribute/value[../@class='Database Name'])"></xsl:variable>
		<table id="bs_table_databases">
			<tr>
				<xsl:choose>
					<xsl:when
						test="$bdName =('arrayexpress','ena sra','dgva','pride') and not($vSample/attribute/value[../@class='Database URI']='')">
						<td>
							<a href="{$vSample/attribute/value[../@class='Database URI']}"
								target="ext">
								<img src="{$basepath}/assets/images/{$bdName}_logo.gif"
									alt="{$vSample/attribute/value[../@class='Database Name']} Link"
									border="0" title="{$bdName}" />
							</a> &nbsp; <a href="{$vSample/attribute/value[../@class='Database URI']}" target="ext">
				<xsl:value-of select="$vSample//attribute/value[../@class='Database URI']"></xsl:value-of>
			</a>
						</td>
					</xsl:when>
					<xsl:when
						test="not($vSample/attribute/value[../@class='Database URI']='')">
						<td>
							<a href="{$vSample/attribute/value[../@class='Database URI']}"
								target="ext">
								<img src="{$basepath}/assets/images/generic_logo.gif"
									border="0" title="{$bdName}" />
							</a> &nbsp; <a href="{$vSample/attribute/value[../@class='Database URI']}" target="ext">
				<xsl:value-of select="$vSample//attribute/value[../@class='Database URI']"></xsl:value-of>
			</a>
						</td>
					</xsl:when>
				</xsl:choose>
			</tr>
		</table>
		</xsl:when>
		<xsl:otherwise>
		 <xsl:for-each select="$vSample/attribute/value[../@class!='Database URI']">
		 <a href="{.}" target="ext">
										<xsl:value-of select="."></xsl:value-of>
									</a>
									<xsl:if test="position()!=last()">
										<br/>
									</xsl:if>
		 </xsl:for-each>
		</xsl:otherwise>
		</xsl:choose>
		<!-- <a href="{$pValue}" target="ext"> <xsl:value-of select="$pValue"></xsl:value-of> 
			</a> -->
	</xsl:template>

	<xsl:template name="process_efo">
		<xsl:param name="pValue" />

		<xsl:for-each select="$pValue">
			<xsl:copy-of select="./text()" />
			<br />
			<xsl:variable name="textValue" select="./text()"></xsl:variable>
			<!-- <xsl:copy-of select="$textValue" /> -->
			<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
				select="replace($textValue,' ','')" /> <xsl:with-param name="pFieldName" 
				select="'keywords'" /> </xsl:call-template> -->


			<xsl:choose>
				<xsl:when test="count(.//object[@id='NCBI Taxonomy'])=0">

					<a href="{.//attribute/value[../@class='Term Source URI']}"
						target="ext">
						<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
					</a>

				</xsl:when>

				<xsl:otherwise>
					<a
						href="{.//attribute/value[../@class='Term Source URI']}?term={.//attribute/value[../@class='Term Source ID']}"
						target="ext">
						<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
						?term=
						<xsl:value-of select=".//attribute/value[../@class='Term Source ID']"></xsl:value-of>
					</a>

				</xsl:otherwise>
			</xsl:choose>
			<br />
		</xsl:for-each>
	</xsl:template>



	<xsl:template name="process_termsource">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue">

			<xsl:call-template name="highlight">
				<xsl:with-param name="pText"
					select="concat('Name: ', .//attribute/value[../@class='Term Source Name'])" />
				<xsl:with-param name="pFieldName" select="'termsources'" />
			</xsl:call-template>
			; URI:
			<a href="{.//attribute/value[../@class='Term Source URI']}" target="ext">
				<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
			</a>
			<xsl:choose>
				<xsl:when test="position()&lt;last()">
					<br />
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>


</xsl:stylesheet>
