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
				<a href="{$context-path}/browse_samples.html">Samples</a>
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
				<xsl:copy-of select="string(@id)" />
			</h4>
			<table id="bs_results_tablesamplegroupdetail">

				<tr>
					<td class="col_title">
						<b>
							Organism
							:
						</b>
					</td>
					<td>
						<xsl:call-template name="process_efo">
							<xsl:with-param name="pAttribute"
								select="$vSample/attribute[@class='Organism']"></xsl:with-param>
							<xsl:with-param name="pField" select="'organism'"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td class="col_title">
						<b>
							Sample Name
							:
						</b>
					</td>
					<td>
						<xsl:call-template name="process_efo">
							<xsl:with-param name="pAttribute"
								select="$vSample/attribute[@class='Sample Name']"></xsl:with-param>
							<xsl:with-param name="pField" select="'name'"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td class="col_title">
						<b>
							Sample Description
							:
						</b>
					</td>
					<td>
						<xsl:call-template name="process_efo">
							<xsl:with-param name="pAttribute"
								select="$vSample/attribute[@class='Sample Description']"></xsl:with-param>
							<xsl:with-param name="pField" select="'description'"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				<xsl:for-each
					select="attribute[@class!='Sample Accession' and @class!='Organism' and @class!='Sample Name' and @class!='Sample Description']">
					<xsl:variable name="attribute" select="."></xsl:variable>
					<xsl:variable name="attributeClass" select="./@class"></xsl:variable>
					<tr>
						<td class="col_title">
							<b>
								<xsl:value-of select="$attributeClass"></xsl:value-of>
								:
							</b>
						</td>
						<td>
							<xsl:choose>
								<xsl:when test="$attributeClass='Derived From'">
									<xsl:call-template name="process_derived_from">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:when test="$attributeClass='Database URI'">
									<xsl:call-template name="process_uri">
										<xsl:with-param name="vSample" select="$vSample"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<!-- normal value -->
								<xsl:when
									test="count($attribute//attribute[@class='Term Source REF'])=0">
									<!-- <xsl:copy-of select="value"></xsl:copy-of> -->
									<xsl:call-template name="process_multiple_values">
										<xsl:with-param name="pField"
											select="lower-case(replace(@class,' ' , '-'))"></xsl:with-param>
										<xsl:with-param name="pValue"
											select="$attribute/simpleValue/value"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>

								<xsl:otherwise>
									<xsl:call-template name="process_efo">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
										<xsl:with-param name="pField"
											select="lower-case(replace($attributeClass,' ' , '-'))"></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>
						</td>
					</tr>

				</xsl:for-each>
				<tr>
					<td class="col_title">
						<b>Groups :
						</b>
					</td>
					<td>
						<xsl:for-each select="GroupIds/Id">
							<a href="../group/{.}">
								<xsl:apply-templates select="." mode="highlight">
									<xsl:with-param name="pFieldName" select="'groupaccession'" />
								</xsl:apply-templates>
							</a>
							<xsl:if test="position()!=last()">
								<xsl:copy-of select="', '" />
							</xsl:if>
						</xsl:for-each>
					</td>
				</tr>

			</table>
		</div>
	</xsl:template>


	<!-- I need to receive the entire sample, because I'm lookin to several 
		attributes at once -->
	<xsl:template name="process_uri">
		<xsl:param name="vSample" />

		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:choose>
					<!-- just one value inside database name -->

					<xsl:when
						test="count($vSample/attribute/simpleValue/value[../../@class='Database Name']/value)=1">
						<xsl:variable name="bdName"
							select="lower-case($vSample/attribute/simpleValue/value[../../@class='Database Name'])"></xsl:variable>
						<tr>
							<td id="td_nowrap">
								<xsl:choose>
									<xsl:when
										test="$bdName =('arrayexpress','ena sra','dgva','pride') and not($vSample/attribute/simpleValue/value[../../@class='Database URI']='')">
										<a
											href="{$vSample/attribute/simpleValue/value[../../@class='Database URI']}"
											target="ext">
											<img src="{$basepath}/assets/images/{$bdName}_logo.gif"
												alt="{$vSample/attribute/simpleValue/value[../../@class='Database Name']} Link"
												border="0" title="{$bdName}" />
										</a> &nbsp;
										<a
											href="{$vSample/attribute/simpleValue/value[../../@class='Database URI']}"
											target="ext">
											<xsl:value-of
												select="$vSample//attribute/simpleValue/value[../../@class='Database URI']"></xsl:value-of>
										</a>
									</xsl:when>
									<xsl:when
										test="not($vSample/attribute/simpleValue/value[../../@class='Database URI']='')">
										<a
											href="{$vSample/attribute/simpleValue/value[../../@class='Database URI']}"
											target="ext">
											<img src="{$basepath}/assets/images/generic_logo.gif"
												border="0" title="{$bdName}" />
										</a> &nbsp;
										<a href="{$vSample/attribute/value[../@class='Database URI']}"
											target="ext">
											<xsl:value-of
												select="$vSample//attribute/simpleValue/value[../../@class='Database URI']"></xsl:value-of>
										</a>
									</xsl:when>
								</xsl:choose>

							</td>
							<td width="100%">&nbsp;
							</td>
						</tr>
					</xsl:when>
					<!-- more than one value -->
					<xsl:otherwise>
						<xsl:for-each
							select="$vSample/attribute/simpleValue/value[../../@class='Database Name']">
							<xsl:variable name="bdName" select="lower-case(.)"></xsl:variable>
							<xsl:variable name="pos" select="position()"></xsl:variable>
							<tr>
								<td id="td_nowrap">
									<xsl:choose>
										<xsl:when
											test="$bdName =('arrayexpress','ena sra','dgva','pride') and not(($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]='')">
											<a
												href="{($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]}"
												target="ext">
												<img src="{$basepath}/assets/images/{$bdName}_logo.gif"
													alt="{bdName} Link" border="0" title="{$bdName}" />
											</a> &nbsp;
											<a
												href="{($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]}"
												target="ext">
												<xsl:value-of
													select="($vSample//attribute/simpleValue/value[../../@class='Database URI'])[$pos]"></xsl:value-of>
											</a>
										</xsl:when>
										<xsl:when
											test="not(($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]='')">
											<a
												href="{($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]}"
												target="ext">
												<img src="{$basepath}/assets/images/generic_logo.gif"
													border="0" title="{$bdName}" />
											</a> &nbsp;
											<a
												href="{($vSample/attribute/simpleValue/value[../../@class='Database URI'])[$pos]}"
												target="ext">
												<xsl:value-of
													select="($vSample//attribute/simpleValue/value[../../@class='Database URI'])[$pos]"></xsl:value-of>
											</a>
										</xsl:when>
									</xsl:choose>
								</td>
								<td width="100%">&nbsp;
								</td>
							</tr>

						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</tbody>
		</table>

		<!-- <a href="{$pValue}" target="ext"> <xsl:value-of select="$pValue"></xsl:value-of> 
			</a> -->
	</xsl:template>


	<xsl:template name="process_derived_from">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute//simpleValue/value">
			<a href="{$basepath}/sample/{.}">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="." />
					<xsl:with-param name="pFieldName" select="''" />
				</xsl:call-template>
				<!-- <xsl:copy-of select="."></xsl:copy-of> -->
			</a>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="process_efo">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<tr>
					<td>
						<xsl:choose>
							<xsl:when
								test="count($pAttribute//attribute/simpleValue/value[../../@class='Term Source URI'])=0">
								<xsl:call-template name="highlight">
									<xsl:with-param name="pText"
										select="$pAttribute/simpleValue/value" />
									<xsl:with-param name="pFieldName" select="$pField" />
								</xsl:call-template>
								<!-- <xsl:copy-of select="simpleValue/value"></xsl:copy-of> -->
							</xsl:when>
							<xsl:otherwise>

								<xsl:call-template name="process_efo_url">
									<xsl:with-param name="pAttribute" select="$pAttribute" />
									<xsl:with-param name="pField" select="$pField" />
								</xsl:call-template>
								<!-- <a href="{.//attribute/simpleValue/value[../../@class='Term 
									Source URI']}" target="ext"> <xsl:value-of select="simpleValue/value"></xsl:value-of> 
									</a> -->
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>


	<xsl:template name="process_efo_url">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />

		<xsl:choose>
			<xsl:when
				test="starts-with($pAttribute//attribute/simpleValue/value[../../@class='Term Source URI'],'http://www.ncbi.nlm.nih.gov/taxonomy')">
				<a
					href="http://www.ncbi.nlm.nih.gov/taxonomy/?term={$pAttribute//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<!-- <xsl:value-of select="simpleValue/value"></xsl:value-of> -->
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="$pAttribute/simpleValue/value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>

				</a>
			</xsl:when>
			<xsl:otherwise>
				<a
					href="{$pAttribute//attribute/simpleValue/value[../../@class='Term Source URI']}"
					target="ext">
					<!-- <xsl:value-of select="simpleValue/value"></xsl:value-of> -->
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="$pAttribute/simpleValue/value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
				</a>
				;
				<br />
				Term Source URI:
				<a
					href="{$pAttribute//attribute/simpleValue/value[../../@class='Term Source URI']}"
					target="ext">
					<xsl:copy-of
						select="$pAttribute//attribute/simpleValue/value[../../@class='Term Source URI']"></xsl:copy-of>
				</a>
				;
				<br />
				Term Source ID:
				<xsl:copy-of
					select="$pAttribute//attribute/simpleValue/value[../../@class='Term Source ID']"></xsl:copy-of>
				;
			</xsl:otherwise>
		</xsl:choose>
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
									select=".//attribute/simpleValue/value[../../@class='Term Source Name']" />
								<xsl:with-param name="pFieldName" select="'termsources'" />
							</xsl:call-template>
						</td>
						<td id="td_nowrap">
							<a href="{.//attribute/value[../@class='Term Source URI']}"
								target="ext">
								<xsl:value-of
									select=".//attribute/simpleValue/value[../../@class='Term Source URI']"></xsl:value-of>
							</a>
						</td>
						<td width="100%">&nbsp;
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>


	<xsl:template name="process_multiple_values">
		<xsl:param name="pValue" />
		<xsl:param name="pField" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue">
					<tr>
						<td width="900">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText" select="$pValue" />
								<xsl:with-param name="pFieldName" select="$pField" />
							</xsl:call-template>
						</td>
						<td>&nbsp;
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>



</xsl:stylesheet>
