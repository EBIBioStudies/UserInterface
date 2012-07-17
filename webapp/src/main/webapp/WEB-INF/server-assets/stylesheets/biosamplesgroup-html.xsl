<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">





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
	<xsl:param name="id" />

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

	<xsl:variable name="vBrowseMode" select="not($id)" />

	<xsl:variable name="vkeywords" select="$keywords" />

   <!-- <xsl:output omit-xml-declaration="yes" method="html" indent="no"
		encoding="windows-1252" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />  -->
		
		<xsl:output omit-xml-declaration="yes" method="xhtml" indent="no"
		encoding="windows-1252" doctype-public="-//W3C//DTD XHTML 1.1//EN" /> 
	

	<xsl:include href="biosamples-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />

<!--  <xsl:output method="html" indent="yes" version="4.0"/> -->
		
	<xsl:template match="/">
 		 <!-- <html lang="en" encoding="windows-1252"> -->
			<xsl:call-template name="page-header">
				<xsl:with-param name="pTitle">
					<xsl:value-of
						select="if (not($vBrowseMode)) then concat(upper-case($id), ' | ') else ''" />
					<xsl:text>BioSample | EBI</xsl:text>
				</xsl:with-param>

				<xsl:with-param name="pExtraCode">

					<link rel="stylesheet"
						href="{$basepath}/assets/stylesheets/biosamples_detail_10.css"
						type="text/css" />
					<script src="{$basepath}/assets/scripts/jquery-1.4.2.min.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/jquery.query-2.1.7m-ebi.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/biosamples_detail_10.js"
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
		<!--  </html>  -->
	</xsl:template>

	<xsl:template name="ae-contents">

		<xsl:variable name="vFiltered" select="//all/SampleGroup" />
		<xsl:variable name="vFrom" as="xs:integer">
			<xsl:choose>
				<xsl:when test="$vPage > 0">
					<xsl:value-of select="1 + ( $vPage - 1 ) * $vPageSize" />
				</xsl:when>
				<xsl:when test="$vTotal = 0">
					0
				</xsl:when>
				<xsl:otherwise>
					1
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="vTo" as="xs:integer">
			<xsl:choose>
				<xsl:when test="( $vFrom + $vPageSize - 1 ) > $vTotal">
					<xsl:value-of select="$vTotal" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$vFrom + $vPageSize - 1" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<div id="bs_contents_box_100pc">
			<div id="bs_content">
				<div id="bs_navi">
					<a href="${interface.application.link.www_domain}/">EBI</a>
					<xsl:text> > </xsl:text>
					<a href="{$basepath}/browse.html">BioSamples</a>
					<xsl:if test="not($vBrowseMode)">
						<xsl:text> > </xsl:text>
						<a href="{$basepath}/biosamplesgroup/{$id}">
							<xsl:value-of select="$id" />
						</a>
					</xsl:if>
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
								<xsl:text>There are no Samples matching your search criteria found in BioSample Database.</xsl:text>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>

			</div>
		</div>
	</xsl:template>

	<xsl:template match="SampleGroup">
		<tr>
			<td>
				<div class="detail_table">
					<xsl:call-template name="detail-table">
						<xsl:with-param name="id" select="id" />
						<xsl:with-param name="sampleGroup" select="."/>
					</xsl:call-template>

				</div>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="detail-table">
		<xsl:param name="id" />
		<xsl:param name="sampleGroup" />
		<table id="bs_results_tablesamplegroupdetail">
			<tr>
				<td class="col_title">
					<b>Name:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Name']" />
						<xsl:with-param name="pFieldName" select="'name'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Group Accession:</b>
				</td>
				<td>
					<xsl:choose>
						<xsl:when
							test="string-length($vkeywords)>0 and contains($vkeywords,'id:')">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="attribute/value[../@class='Group Accession']" />
								<xsl:with-param name="pFieldName" select="'id'" />
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="attribute/value[../@class='Group Accession']"></xsl:value-of>
						</xsl:otherwise>

					</xsl:choose>
					<!-- -->

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Title:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Title']" />
						<xsl:with-param name="pFieldName" select="'title'" />
					</xsl:call-template>

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Identifier:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Identifier']" />
						<xsl:with-param name="pFieldName" select="'identifier'" />
					</xsl:call-template>

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Description:</b>
				</td>
				<td>
					<!-- <xsl:value-of select="attribute/value[../@class='Submission Description']"></xsl:value-of> -->
					<xsl:call-template name="highlight">
						<!-- <xsl:with-param name="pText"
							select="escape:escapeHtml(attribute/value[../@class='Submission Description'])" /> -->
							<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Description']" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Version:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Version']" />
						<xsl:with-param name="pFieldName" select="'version'" />
					</xsl:call-template>

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Release Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Release Date']" />
						<xsl:with-param name="pFieldName" select="'releasedate'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Modification Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Modification Date']" />
						<xsl:with-param name="pFieldName" select="'modificationdate'" />
					</xsl:call-template>
				</td>
			</tr>

			<tr>
				<td class="col_title">
					<b>Submission Reference Layer:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Reference Layer']" />
						<xsl:with-param name="pFieldName" select="'referencelayer'" />
					</xsl:call-template>
				</td>
			</tr>

			<xsl:choose>
				<xsl:when test="count(attribute/value[../@class='Databases'])>0">
					<tr>
						<td class="col_title">
							<b>Databases:</b>
						</td>
						<td>
							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Databases']//value, ', ')" 
								/> <xsl:with-param name="pFieldName" select="'databases'" /> </xsl:call-template> -->

							<xsl:call-template name="process_database">
								<xsl:with-param name="pValue"
									select="attribute/value[../@class='Databases']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>



			<xsl:choose>
				<xsl:when test="count(attribute/value[../@class='Organizations'])>0">
					<tr>
						<td class="col_title">
							<b>Organizations:</b>
						</td>
						<td>
							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Organizations']//value, ', 
								')" /> <xsl:with-param name="pFieldName" select="'organizations'" /> </xsl:call-template> -->

							<xsl:call-template name="process_organization">
								<xsl:with-param name="pValue"
									select="attribute/value[../@class='Organizations']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>


			<xsl:choose>
				<xsl:when test="count(attribute/value[../@class='Term Sources'])>0">

					<tr>
						<td class="col_title">
							<b>Term Sources:</b>
						</td>
						<td>


							<xsl:call-template name="process_termsource">
								<xsl:with-param name="pValue"
									select="attribute/value[../@class='Term Sources']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>


			<xsl:choose>
				<xsl:when test="count(attribute/value[../@class='Persons'])>0">

					<tr>
						<td class="col_title">
							<b>Persons:</b>
						</td>
						<td>
							<xsl:call-template name="process_person">
								<xsl:with-param name="pValue"
									select="attribute/value[../@class='Persons']"></xsl:with-param>
							</xsl:call-template>


							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Persons']//value, ', ')" /> 
								<xsl:with-param name="pFieldName" select="'persons'" /> </xsl:call-template> -->

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>

			<tr>
				<td class="col_title">
					<b>Samples in group:</b>
				</td>
				<td>
					<xsl:value-of select="@samplecount"></xsl:value-of>
				</td>
			</tr>

			<tr>
				<td colspan="2">

					<div id="bs_query_box">
						<form id="bs_query_form" method="get"
							action="javascript:searchSamples(this.bs_keywords_field.value);">
							<fieldset id="bs_keywords_fset">
								<label for="bs_keywords_field">
									Search inside the
									<xsl:value-of select="@samplecount"></xsl:value-of>
									<xsl:text> sample</xsl:text>
									<xsl:if test="$vTotal != 1">
										<xsl:text>s</xsl:text>
									</xsl:if>
									of SampleGroup [
									<a href="javascript:aeClearField('#bs_keywords_field')">clear</a>
									]
								</label>
								<input id="bs_keywords_field" type="text" name="keywords"
									value="" maxlength="255" class="bs_assign_font" autocomplete="off" />
							</fieldset>
							<div id="bs_submit_box">
								<input id="bs_query_submit" type="submit" value="Query" />
							</div>
							<div id="bs_results_stats">
								<div id="bs_results_stats_fromto"></div>
								<div id="bs_results_pager"></div>
							</div>
						</form>
					</div>




					<!-- This div is used to keep all the samples -->
					<br />
					<div id="bs_results_listsamples">
						<table id="bs_samples_detail" cellpadding="0" cellspacing="0"
							width="100%">
							<thead>
							
							<tr>
									<th class="bs_results_accession sortable bs_results_Sample-Accession"
										id="bs_results_header_0">
										<a href="javascript:aeSort('0')" title="Click to sort by Sample-Accession">
											<div class="table_header_inner">
												Sample-Accession
											</div>
										</a>
									</th>
									<xsl:for-each select="SampleAttributes/attribute/replace(@class,' ' , '-')">
										<xsl:if test=".!='Sample-Accession'">
										
											<th class="bs_results_accession sortable bs_results_{replace(.,' ' , '-')}"
												id="bs_results_header_{position()}">
												<a href="javascript:aeSort('{position()}')" title="Click to sort by {.}">
													<div class="table_header_inner">
														<xsl:copy-of select="."></xsl:copy-of>
													</div>
												</a>
											</th>
										</xsl:if>

									</xsl:for-each>
								</tr>
								<!-- <tr>
									<th class="bs_results_accession sortable bs_results_Sample-Accession"
										id="bs_results_header_0">
										<a href="javascript:aeSort('0')" title="Click to sort by Sample-Accession">
											<div class="table_header_inner">
												Sample-Accession
											</div>
										</a>
									</th>
									<xsl:for-each select="tokenize(attributes,' ')">
										<xsl:if test=".!='Sample-Accession'">
										
											<th class="bs_results_accession sortable bs_results_{.}"
												id="bs_results_header_{position()}">
												<a href="javascript:aeSort('{position()}')" title="Click to sort by {.}">
													<div class="table_header_inner">
														<xsl:copy-of select="."></xsl:copy-of>
													</div>
												</a>
											</th>
										</xsl:if>

									</xsl:for-each>
								</tr> -->
							</thead>

							<tbody id="bs_results_tbody">


							</tbody>
						</table>
					</div>

				</td>
			</tr>
		</table>
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

	<xsl:template name="add-sort">
		<xsl:param name="pKind" />
		<xsl:if test="$pKind = $vSortBy">
			<xsl:choose>
				<xsl:when test="'ascending' = $vSortOrder">
					<img src="{$basepath}/assets/images/mini_arrow_up.gif" width="12"
						height="16" alt="^" />
				</xsl:when>
				<xsl:otherwise>
					<img src="{$basepath}/assets/images/mini_arrow_down.gif" width="12"
						height="16" alt="v" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>



	<xsl:template name="process_person">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue">

			<xsl:choose>
				<xsl:when test="count(.//attribute/value[../@class='Person Email'])>0">
					<xsl:variable name="vStringContacts"
						select="concat(.//attribute/value[../@class='Person First Name'], ' ', .//attribute/value[../@class='Person Last Name'] , ' &lt;' ,.//attribute/value[../@class='Person Email'], '>')"></xsl:variable>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="$vStringContacts" />
						<xsl:with-param name="pFieldName" select="'persons'" />
					</xsl:call-template>
					<xsl:choose>
						<xsl:when test="position()&lt;last()">
							,
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="vStringContacts"
						select="concat(.//attribute/value[../@class='Person First Name'], ' ', .//attribute/value[../@class='Person Last Name'])"></xsl:variable>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="$vStringContacts" />
						<xsl:with-param name="pFieldName" select="'persons'" />
					</xsl:call-template>
					<xsl:choose>
						<xsl:when test="position()&lt;last()">
							,
						</xsl:when>
					</xsl:choose>

				</xsl:otherwise>

			</xsl:choose>


		</xsl:for-each>
	</xsl:template>



	<xsl:template name="process_database">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue">

			<xsl:call-template name="highlight">
				<xsl:with-param name="pText"
					select="concat('Name: ', .//attribute/value[../@class='Database Name'], '; ID: ', .//attribute/value[../@class='Database ID'])" />
				<xsl:with-param name="pFieldName" select="'databases'" />
			</xsl:call-template>
			; URI:
			<a href="{.//attribute/value[../@class='Database URI']}" target="ext">
				<xsl:value-of select=".//attribute/value[../@class='Database URI']"></xsl:value-of>
			</a>
			<xsl:choose>
				<xsl:when test="position()&lt;last()">
					<br />
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>




	<xsl:template name="process_organization">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue">

			<xsl:call-template name="highlight">
				<xsl:with-param name="pText"
					select="concat('Name: ', .//attribute/value[../@class='Organization Name'], '; Role: ', .//attribute/value[../@class='Organization Role'])" />
				<xsl:with-param name="pFieldName" select="'organizations'" />
			</xsl:call-template>
			; URI:
			<a href="{.//attribute/value[../@class='Organization URI']}"
				target="ext">
				<xsl:value-of select=".//attribute/value[../@class='Organization URI']"></xsl:value-of>
			</a>
			<xsl:choose>
				<xsl:when test="position()&lt;last()">
					<br />
				</xsl:when>
			</xsl:choose>
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
