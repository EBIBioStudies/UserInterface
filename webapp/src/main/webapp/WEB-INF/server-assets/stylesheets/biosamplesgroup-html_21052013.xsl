<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:search="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs fn html escape"
	exclude-result-prefixes="xs fn html escape" version="2.0">



	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />


	<xsl:param name="id" />

	<!-- <xsl:param name="userid" /> -->
	<xsl:param name="basepath" />
	<xsl:param name="total" />


	<!-- <xsl:param name="host" /> <xsl:param name="context-path" /> <xsl:param 
		name="original-request-uri" /> <xsl:param name="referer" /> <xsl:param name="query-string" 
		/> <xsl:param name="username" /> -->

<!-- <xsl:output omit-xml-declaration="no" method="html" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD XHTML 4.01 Transitional//EN" /> -->

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


	<xsl:variable name="vBrowseMode" select="not($id)" />

	<xsl:variable name="vkeywords" select="$keywords" />

	<xsl:variable name="vSearchMode"
		select="fn:ends-with($relative-uri, 'search.html')" />
	<xsl:variable name="vQueryString"
		select="if ($query-string) then fn:concat('?', $query-string) else ''" />

 
	<!-- <xsl:output omit-xml-declaration="yes" method="xhtml"
		indent="no" encoding="windows-1252" doctype-public="-//W3C//DTD XHTML 1.1//EN" />  -->

	

	<!-- <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/> -->

	<!-- <xsl:output omit-xml-declaration="yes" method="xhtml" indent="no" encoding="windows-1252" 
		doctype-system="-//W3C//DTD XHTML 4.01 Transitional//EN" /> -->

	<!-- <xsl:output omit-xml-declaration="yes" method="xhtml" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD XHTML 1.1//EN" /> -->

	<!-- <xsl:output method="html" indent="yes" version="4.0"/> -->

	<xsl:template match="/">

		<xsl:call-template name="ae-page">
			<xsl:with-param name="pIsSearchVisible" select="fn:true()" />
			<xsl:with-param name="pSearchInputValue"
				select="if (fn:true()) then $keywords else ''" />
			<xsl:with-param name="pTitleTrail" select="$id" />
			<xsl:with-param name="pExtraCSS">
				<link rel="stylesheet"
					href="{$context-path}/assets/stylesheets/biosamples_detail_10.css"
					type="text/css" />
			</xsl:with-param>
			<xsl:with-param name="pBreadcrumbTrail">
				<a href="{$context-path}/browse.html">Samples</a>
				>
				<xsl:value-of select="$id" />
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
				<script src="{$context-path}/assets/scripts/biosamples_detail_10.js"
					type="text/javascript"></script>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="ae-content-section">

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


		<xsl:choose>
			<xsl:when test="$vTotal&gt;0">

				<xsl:apply-templates select="$vFiltered">
				</xsl:apply-templates>
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

	</xsl:template>

	<xsl:template match="SampleGroup">
		<tr>
			<td>
				<div class="detail_table">
					<xsl:call-template name="detail-table">
						<xsl:with-param name="id" select="id" />
						<xsl:with-param name="sampleGroup" select="." />
					</xsl:call-template>

				</div>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="detail-table">
		<xsl:param name="id" />
		<xsl:param name="sampleGroup" />
		<h4>
			Group Accession
			<xsl:choose>
				<!-- test="string-length($vkeywords)>0"> -->
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
		</h4>

		<table class="bs_results_tablesamplegroupdetail">
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
						<!-- <xsl:with-param name="pText" select="escape:escapeHtml(attribute/value[../@class='Submission 
							Description'])" /> -->
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Description']" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</td>
			</tr>
			<!-- <tr> <td class="col_title"> <b>Submission Version:</b> </td> <td> 
				<xsl:call-template name="highlight"> <xsl:with-param name="pText" select="attribute/value[../@class='Submission 
				Version']" /> <xsl:with-param name="pFieldName" select="'version'" /> </xsl:call-template> 
				</td> </tr> -->
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
					<b>Submission Update Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Update Date']" />
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

			<xsl:choose>
				<xsl:when test="count(attribute/value[../@class='Publications'])>0">

					<tr>
						<td class="col_title">
							<b>Publications:</b>
						</td>
						<td>
							<xsl:call-template name="process_publications">
								<xsl:with-param name="pValue"
									select="attribute/value[../@class='Publications']"></xsl:with-param>
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
					<br />
					<div id="bs_results_tbody" style=" display: none;visible:false"></div>
					<div id="bs_browse">
						<table width="100%" border="0" cellpadding="0"
							id="table_browse_samples" cellspacing="0">

							<tr>
								<td>
									<div id="bs_query_box">
										<form id="bs_query_form" method="get"
											action="javascript:searchSamples(document.forms['bs_query_form'].bs_keywords_field.value);">
											<table border="0" id="table-bs-query" width="100%">
												<tbody>
													<tr>
														<td colspan="3" id="td_no_padding">
															Search inside the
															<xsl:value-of select="@samplecount"></xsl:value-of>
															<xsl:text> sample</xsl:text>
															<xsl:if test="$vTotal != 1">
																<xsl:text>s</xsl:text>
															</xsl:if>
															of SampleGroup [
															<a href="javascript:aeClearField('#bs_keywords_field')">clear</a>
															]
														</td>
													</tr>
													<tr>
														<td valign="top" id="td_no_padding">
															<fieldset id="bs_keywords_fset">
																<xsl:variable name="vKeywordsAux"
																	select="if (fn:matches($vkeywords,'\s*\w\s*:\s*\w')) then ''  else $vkeywords" />
																<input id="bs_keywords_field" type="text" name="keywords"
																	value="{$vKeywordsAux}" maxlength="255" size="60"
																	autocomplete="off" />
															</fieldset>
														</td>
														<td valign="bottom" align="right" id="td_no_padding">
															<input type="submit" value="Search" class="submit" />
														</td>
														<td width='100%'>&nbsp;
														</td>
													</tr>
												</tbody>
											</table>
										</form>
									</div>

								</td>
							</tr>


							<tr>
								<td>
									<div id="bs_results_listsamples">
										<table class="persist-header" id="bs_samples_table"
											border="0" cellpadding="0" width="100%" cellspacing="0"
											style="visibility: visible;">
											<colgroup>
												<col class="col_left_fixed" style="width:120px;" />
												<col class="col_middle_scrollable" style="width:100%" />
												<col class="col_right_fixed" style="width: 100px;" />
											</colgroup>
											<thead>
												<tr>
													<th colspan="3" class="col_pager">
														<div class="bs-pager">
														</div>
														<div class="bs-page-size">
														</div>
														<div class="bs-stats">
														</div>
													</th>
												</tr>
											</thead>
											<tbody>
												<tr>
													<td id="left_fixed">
														<table id="src_name_table" border="0" cellpadding="0"
															cellspacing="0" width="100%">
															<thead>
																<tr>
																	<th
																		class="bs_results_accession sortable bs_results_Sample-Accession"
																		id="bs_results_header_sampleaccession">
																		<a href="javascript:aeSort('sampleaccession')"
																			title="Click to sort by Sample Accession">
																			<div class="table_header_inner">
																				Accession
																			</div>
																		</a>
																	</th>
																</tr>
															</thead>
															<tbody id="bs_results_tbody_left">
																<!-- Left content -->
															</tbody>
														</table>

													</td>
													<td id="middle_scrollable">
														<div class="attr_table_shadow_container">
															<div class="attr_table_scroll">
																<table id="attr_table" border="0" cellpadding="0"
																	cellspacing="0" width="100%">
																	<thead>
																		<tr>

																			<xsl:for-each
																				select="SampleAttributes/attribute/@class[.!='Sample Accession']">
																				<!-- <xsl:if test=".!='Sample Accession'"> -->
																				<xsl:choose>
																					<xsl:when test="position()=1">
																						<th
																							class="bs_results_accession begin_scroll sortable bs_results_{replace(.,' ' , '-')}"
																							id="bs_results_header_{position()}">
																							<a href="javascript:aeSort('{position()}');"
																								title="Click to sort by {.}">
																								<div class="table_header_inner">
																									<xsl:value-of select="."></xsl:value-of>&nbsp;
																								</div>
																							</a>
																							<!-- <a href="javascript:aeSort('{position()}');document.getElementById('bs_div_results_header_{position()}').focus()" 
																								title="Click to sort by {.}"> <div tabindex="11{position()}" id="bs_div_results_header_{position()}" 
																								class="table_header_inner"> <xsl:value-of select="."></xsl:value-of>&nbsp; 
																								</div> </a> -->
																						</th>
																					</xsl:when>
																					<xsl:when test="position()=last()">
																						<th
																							class="bs_results_accession end_scroll sortable bs_results_{replace(.,' ' , '-')}"
																							id="bs_results_header_{position()}">
																							<a href="javascript:aeSort('{position()}');"
																								title="Click to sort by {.}">
																								<div class="table_header_inner">
																									<xsl:value-of select="."></xsl:value-of>&nbsp;
																								</div>
																							</a>
																						</th>
																					</xsl:when>
																					<xsl:otherwise>
																						<th
																							class="bs_results_accession sortable bs_results_{replace(.,' ' , '-')}"
																							id="bs_results_header_{position()}">
																							<a href="javascript:aeSort('{position()}');"
																								title="Click to sort by {.}">
																								<div class="table_header_inner">
																									<xsl:value-of select="."></xsl:value-of>&nbsp;
																								</div>
																							</a>
																						</th>

																					</xsl:otherwise>
																				</xsl:choose>
																				<!-- </xsl:if> -->
																			</xsl:for-each>
																		</tr>
																	</thead>
																	<tbody id="bs_results_tbody_middle">
																		<!-- Middle content -->
																	</tbody>
																</table>
															</div>
															<div class="left_shadow" style="display: none;"></div>
															<div class="right_shadow" style="display: none;"></div>
														</div>
													</td>
													<td id="right_fixed">
														<table id="links_table" border="0" cellpadding="0"
															cellspacing="0" width="100%">
															<thead>
																<tr>
																	<!-- I will not allow to sort -->
																	<th id="align-middle">
																		<a href="javascript:void(0);">Link</a>
																	</th>
																</tr>
															</thead>
															<tbody id="bs_results_tbody_right">
																<!-- Right content -->
															</tbody>
														</table>
													</td>
												</tr>
												<!-- <tr> <td class="bottom_filler" style="height: 0px;"></td> 
													<td class="bottom_filler"></td> </tr> <tr> <td colspan="3" class="col_footer">&nbsp; 
													</td> </tr> -->
											</tbody>
										</table>

									</div>
								</td>
							</tr>
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

		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<tr>
					<td>
						<xsl:for-each select="$pValue/object">

							<xsl:choose>
								<xsl:when
									test="count(.//attribute/value[../@class='Person Email'])>0">
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/value[../@class='Person First Name'], ' ', .//attribute/value[../@class='Person Last Name'] , ' &lt;' ,.//attribute/value[../@class='Person Email'], '>')"></xsl:variable>

									<a href="mailto:{.//attribute/value[../@class='Person Email']}"
										class="icon icon-generic" data-icon="E">
										<xsl:call-template name="highlight">
											<xsl:with-param name="pText" select="$vStringContacts" />
											<xsl:with-param name="pFieldName" select="'persons'" />
										</xsl:call-template>
									</a>
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/value[../@class='Person First Name'], ' ', .//attribute/value[../@class='Person Last Name'])"></xsl:variable>
									<xsl:call-template name="highlight">
										<xsl:with-param name="pText" select="$vStringContacts" />
										<xsl:with-param name="pFieldName" select="'persons'" />
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>
							<xsl:choose>
								<xsl:when test="position()&lt;last()">
									,
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</td>
				</tr>
			</tbody>
		</table>

	</xsl:template>



	<xsl:template name="process_database">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue">
					<xsl:variable name="bdName"
						select="lower-case(.//attribute/value[../@class='Database Name'])"></xsl:variable>
					<tr>
						<td id="td_nowrap">
							<xsl:choose>
								<xsl:when
									test="$bdName =('arrayexpress','ena sra','dgva','pride') and not(.//attribute/value[../@class='Database URI']='')">
									<a href="{.//attribute/value[../@class='Database URI']}"
										target="ext">
										<img src="{$basepath}/assets/images/{$bdName}_logo.gif"
											alt="{.//attribute/value[../@class='Database Name']} Link"
											border="0" title="{$bdName}" />
									</a>
								</xsl:when>
								<xsl:when test="not(.//attribute/value[../@class='Database URI']='')">
									<a href="{.//attribute/value[../@class='Database URI']}"
										target="ext">
										<font class="icon icon-generic" data-icon="L" />
									</a>
								</xsl:when>
							</xsl:choose>
						</td>
						<td id="td_nowrap">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat('Name: ',.//attribute/value[../@class='Database Name'])" />
								<xsl:with-param name="pFieldName" select="'databases'" />
							</xsl:call-template>
						</td>
						<td id="td_nowrap">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat('Id: ',.//attribute/value[../@class='Database ID'])" />
								<xsl:with-param name="pFieldName" select="'databases'" />
							</xsl:call-template>
						</td>
						<td width="100%">&nbsp;
						</td>
						<!-- <td> <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
							select="concat('Name: ', .//attribute/value[../@class='Database Name'], '; 
							ID: ', .//attribute/value[../@class='Database ID'])" /> <xsl:with-param name="pFieldName" 
							select="'databases'" /> </xsl:call-template> ; URI: <a href="{.//attribute/value[../@class='Database 
							URI']}" target="ext"> <xsl:value-of select=".//attribute/value[../@class='Database 
							URI']"></xsl:value-of> </a> </td> -->
						<!-- <xsl:choose> <xsl:when test="position()&lt;last()"> <br /> </xsl:when> 
							</xsl:choose> -->
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>

	<!-- <td vertical-align="middle">&nbsp;Name: <xsl:copy-of select=".//attribute/value[../@class='Database 
		Name']"/>; ID: <xsl:copy-of select=".//attribute/value[../@class='Database 
		ID']"/>; URI: <a href="{.//attribute/value[../@class='Database URI']}" target="ext"> 
		<xsl:value-of select=".//attribute/value[../@class='Database URI']"></xsl:value-of> 
		</a> -->

	<xsl:template name="process_publications">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/object">

					<tr>
						<td>
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat('Publication DOI: ', .//attribute/value[../@class='Publication DOI'], ';  Publication PubMed ID: ', .//attribute/value[../@class='Publication PubMed ID'])" />
								<xsl:with-param name="pFieldName" select="'publications'" />
							</xsl:call-template>
							<xsl:choose>
								<xsl:when test="position()&lt;last()">
									<br />
								</xsl:when>
							</xsl:choose>
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>


	<xsl:template name="process_organization">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/object">
					<tr>
						<td>
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat('Name: ', .//attribute/value[../@class='Organization Name'],'; Address: ', .//attribute/value[../@class='Organization Address'], '; Role: ', .//attribute/value[../@class='Organization Role'], '; Email: ', .//attribute/value[../@class='Organization Email'])" />
								<xsl:with-param name="pFieldName" select="'organizations'" />
							</xsl:call-template>
							; URI:
							<a href="{.//attribute/value[../@class='Organization URI']}"
								target="ext">
								<xsl:value-of select=".//attribute/value[../@class='Organization URI']"></xsl:value-of>
							</a>
						</td>
						<!-- <td width="100%">&nbsp; </td> -->
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>



	<xsl:template name="process_termsource">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<!-- <thead> <th>Name </th> <th>URI</th> <th>&nbsp;</th> </thead> -->
			<tbody>
				<xsl:for-each select="$pValue/object">
					<tr>
						<td id="td_nowrap">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select=".//attribute/value[../@class='Term Source Name']" />
								<xsl:with-param name="pFieldName" select="'termsources'" />
							</xsl:call-template>
						</td>
						<td>
							<a href="{.//attribute/value[../@class='Term Source URI']}"
								target="ext">
								<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
							</a>
						</td>
						<td width="100%">&nbsp;
						</td>
					</tr>
					<!-- <xsl:choose> <xsl:when test="position()&lt;last()"> <div>&nbsp;</div> 
						</xsl:when> </xsl:choose> -->
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>


</xsl:stylesheet>