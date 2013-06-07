<?xml version="1.0" encoding="UTF-8"?>
<!-- cannot change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs fn html"
	exclude-result-prefixes="xs fn html" version="2.0">

	<xsl:param name="page" />
	<xsl:param name="pagesize" />
	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />
	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="accession" />
	<xsl:param name="basepath" />
	<xsl:param name="total" />



	<xsl:include href="biosamples-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />

	<xsl:variable name="vPage"
		select="if ($page) then $page cast as xs:integer else 1" />
	<xsl:variable name="vPageSize"
		select="if ($pagesize) then $pagesize cast as xs:integer else 25" />


	<xsl:variable name="vSortBy"
		select="if ($sortby) then $sortby else 'accession'" />
	<xsl:variable name="vSortOrder"
		select="if ($sortorder) then $sortorder else 'ascending'" />


	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>


	<xsl:variable name="vkeywords" select="$keywords" />


	<xsl:variable name="vSearchMode"
		select="fn:ends-with($relative-uri, 'search.html')" />



	<xsl:template match="/">

		<xsl:call-template name="ae-page">
			<xsl:with-param name="pIsSearchVisible" select="fn:true()" />
			<xsl:with-param name="pSearchInputValue"
				select="if (fn:true()) then $keywords else ''" />
			<xsl:with-param name="pTitleTrail" select="$accession" />
			<xsl:with-param name="pExtraCSS">
				<link rel="stylesheet"
					href="{$context-path}/assets/stylesheets/biosamples_detail_10.css"
					type="text/css" />
			</xsl:with-param>
			<xsl:with-param name="pBreadcrumbTrail">
				<a href="{$context-path}/browse.html">Samples</a>
				>
				<xsl:value-of select="$accession" />
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="ae-content-section">

		<xsl:variable name="vFiltered" select="//all/Sample" />

		<div id="bs_content">

			<xsl:choose>
				<xsl:when test="$vTotal&gt;0">
					<xsl:apply-templates select="$vFiltered">
					</xsl:apply-templates>
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

	</xsl:template>

	<xsl:template match="Sample">
		<xsl:variable name="vSample" select="."></xsl:variable>
		<div class="detail_table">
			<h4>
				Sample Accession
				<xsl:copy-of select="attribute/value[../@class='Sample Accession']" />
			</h4>
			<table id="bs_results_tablesamplegroupdetail">
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
		<xsl:variable name="bdName"
			select="lower-case($vSample/attribute/value[../@class='Database Name'])"></xsl:variable>


		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<tr>
					<td id="td_nowrap">
						<xsl:choose>
							<xsl:when
								test="$bdName =('arrayexpress','ena sra','dgva','pride') and not($vSample/attribute/value[../@class='Database URI']='')">
									<a href="{$vSample/attribute/value[../@class='Database URI']}"
										target="ext">
										<img src="{$basepath}/assets/images/{$bdName}_logo.gif"
											alt="{$vSample/attribute/value[../@class='Database Name']} Link"
											border="0" title="{$bdName}" />
									</a> &nbsp;
									<a href="{$vSample/attribute/value[../@class='Database URI']}"
										target="ext">
										<xsl:value-of
											select="$vSample//attribute/value[../@class='Database URI']"></xsl:value-of></a>
							</xsl:when>
							<xsl:when
								test="not($vSample/attribute/value[../@class='Database URI']='')">
									<a href="{$vSample/attribute/value[../@class='Database URI']}"
										target="ext">
										<img src="{$basepath}/assets/images/generic_logo.gif"
											border="0" title="{$bdName}" />
									</a> &nbsp;
									<a href="{$vSample/attribute/value[../@class='Database URI']}"
										target="ext">
										<xsl:value-of
											select="$vSample//attribute/value[../@class='Database URI']"></xsl:value-of>
									</a>
							</xsl:when>
						</xsl:choose>
						
						</td>
						<td width="100%">&nbsp;
						</td>

				</tr>
			</tbody>
		</table>

		<!-- <a href="{$pValue}" target="ext"> <xsl:value-of select="$pValue"></xsl:value-of> 
			</a> -->
	</xsl:template>

	<xsl:template name="process_efo">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue">
					<tr>
						<td id="td_nowrap">
							<xsl:copy-of select="./text()" />
						</td>
						<xsl:variable name="textValue" select="./text()"></xsl:variable>
						<td id="td_nowrap">
							<xsl:choose>
								<xsl:when test="count(.//object[@id='NCBI Taxonomy'])=0">

									<a href="{.//attribute/value[../@class='Term Source URI']}"
										target="ext">
										<xsl:value-of
											select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
									</a>

								</xsl:when>

								<xsl:otherwise>
									<a
										href="{.//attribute/value[../@class='Term Source URI']}?term={.//attribute/value[../@class='Term Source ID']}"
										target="ext">
										<xsl:value-of
											select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
										?term=
										<xsl:value-of select=".//attribute/value[../@class='Term Source ID']"></xsl:value-of>
									</a>

								</xsl:otherwise>
							</xsl:choose>
						</td>
						<td width="100%">&nbsp;
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>



	<xsl:template name="process_termsource">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue">
					<tr>
						<td id="td_nowrap">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select=".//attribute/value[../@class='Term Source Name']" />
								<xsl:with-param name="pFieldName" select="'termsources'" />
							</xsl:call-template>
						</td>
						<td id="td_nowrap">
							<a href="{.//attribute/value[../@class='Term Source URI']}"
								target="ext">
								<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
							</a>
						</td>
						<td width="100%">&nbsp;
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>


</xsl:stylesheet>
