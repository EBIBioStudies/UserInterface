<?xml version="1.0" encoding="UTF-8"?>
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

	<xsl:output omit-xml-declaration="yes" method="html" indent="no"
		encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />

	<xsl:include href="ae-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="ae-highlight.xsl" />

	<xsl:template match="/">
		<html lang="en">
			<xsl:call-template name="page-header">
				<xsl:with-param name="pTitle">
					<xsl:value-of
						select="if (not($vBrowseMode)) then concat(upper-case($id), ' | ') else ''" />
					<xsl:text>Bio Samples | ArrayExpress Archive | EBI</xsl:text>
				</xsl:with-param>

				<xsl:with-param name="pExtraCode">
				
					<link rel="stylesheet"
						href="{$basepath}/assets/stylesheets/biosamples_browse_10.css"
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
		</html>
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
			<xsl:if test="not($vBrowseMode)">
				<xsl:attribute name="class">expanded</xsl:attribute>
			</xsl:if>
			<td class="col_id">
				<div>
					<a href="{$basepath}/biosamplesgroup/{id}?keywords={$vkeywords}">
						<xsl:call-template name="highlight">
							<xsl:with-param name="pText" select="id" />
							<xsl:with-param name="pFieldName" select="'id'" />
						</xsl:call-template>
					</a>
				</div>
			</td>

			<td class="col_description">
				<div>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="description" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</div>
			</td>

			<td class="col_samples">
				<div>
					<xsl:value-of select="samples"></xsl:value-of>
					<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
						select="count(Sample)"/> <xsl:with-param name="pFieldName"/> </xsl:call-template> -->
				</div>
			</td>


		</tr>

		<xsl:if test="not($vBrowseMode)">
			<tr>
				<td class="col_detail" colspan="4">
					<div class="detail_table">
						<xsl:call-template name="detail-table">
							<xsl:with-param name="id" select="id" />
						</xsl:call-template>

					</div>
				</td>
			</tr>
		</xsl:if>

	</xsl:template>

	<xsl:template name="detail-table">
		<xsl:param name="id" />
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="300">
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
				<td width="300">
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
				<td width="300">
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
				<td width="300">
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
				<td width="300">
					<b>Submission Description:</b>
				</td>
				<td>
					<!-- <xsl:value-of select="attribute/value[../@class='Submission Description']"></xsl:value-of> -->
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/value[../@class='Submission Description']" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td width="300">
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
				<td width="300">
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
				<td width="300">
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
				<td width="300">
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
						<td width="300">
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
						<td width="300">
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
						<td width="300">
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
						<td width="300">
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
				<td width="300">
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
									value="{$vkeywords}" maxlength="255" class="bs_assign_font" />
							</fieldset>
							<div id="bs_submit_box">
								<input id="bs_query_submit" type="submit" value="Query" />
							</div>

						</form>
					</div>




					<!-- This div is used to keep all the samples -->
					<div id="ae_samples_list">

						<div id="ae_results_area">
							<div id="ae_results_box">
								<div class="table_box_top">
									<div class="table_box_bottom">
										<div class="table_box_left">
											<div class="table_box_right">
												<div class="table_box_bottom_left">
													<div class="table_box_bottom_right">
														<div class="table_box_top_left">
															<div class="table_box_top_right">
																<div class="table_padding_box">
																	<div class="table_inner_box">
																		<div id="ae_results_tbl" class="ae_multisource">
																			<div id="ae_results_hdr_filler" class="table_header_filler">
																				<div class="table_header_border_left"></div>
																			</div>
																			<div id="ae_results_hdr" style="right: 15px">
																				<div id="ae_results_hdr_inner">
																					<table border="0" cellpadding="0"
																						cellspacing="0">
																						<thead>
																							<tr>
																								<!-- This is the only one that is fixed the other 
																									ones are dynamic -->
																								<th
																									class="table_header_box sortable ae_results_accession"
																									id="ae_results_header_accession">
																									<a href="javascript:aeSort('accession')"
																										title="Click to sort by accession">
																										<div class="table_header_border_left">
																											<div class="table_header_border_right">
																												<div class="table_header_inner">
																													<div class="table_header_label">
																														Accession
																													</div>
																												</div>
																											</div>
																										</div>
																									</a>
																								</th>

																								<xsl:for-each select="tokenize(attributes,' ')">

																									<xsl:choose>
																										<xsl:when test="position()&lt;7">
																											<th
																												class="table_header_box sortable ae_results_{.}"
																												id="ae_results_header_{.}">
																												<a href="javascript:aeSort('{.}')" title="Click to sort by {.}">
																													<div class="table_header_border_left">
																														<div class="table_header_border_right">
																															<div class="table_header_inner">
																																<div class="table_header_label">
																																	<xsl:copy-of select="."></xsl:copy-of>
																																</div>
																															</div>
																														</div>
																													</div>
																												</a>
																											</th>
																										</xsl:when>
																										<xsl:when test="position()=7">
																											<th class="table_header_box col_moredetail">

																												<div class="table_header_label">
																													...
																												</div>
																											</th>
																										</xsl:when>


																										<xsl:otherwise>
																										</xsl:otherwise>
																									</xsl:choose>

																								</xsl:for-each>


																								<!-- <th class="table_header_box sortable ae_results_accession" 
																									id="ae_results_header_accession"><a href="javascript:aeSort('accession')" 
																									title="Click to sort by accession"><div class="table_header_border_left"><div 
																									class="table_header_border_right"><div class="table_header_inner"><div class="table_header_label">ID</div></div></div></div></a></th> 
																									<th class="table_header_box sortable ae_results_description" id="ae_results_header_description"><a 
																									href="javascript:aeSort('description')" title="Click to sort by description"><div 
																									class="table_header_border_left"><div class="table_header_border_right"><div 
																									class="table_header_inner"><div class="table_header_label">Description</div></div></div></div></a></th> 
																									<th class="table_header_box sortable ae_results_samples" id="ae_results_header_samples"><a 
																									href="javascript:aeSort('samples')" title="Click to sort by number of samples"><div 
																									class="table_header_border_left"><div class="table_header_border_right"><div 
																									class="table_header_inner"><div class="table_header_label">Samples</div></div></div></div></a></th> -->
																							</tr>
																						</thead>
																					</table>
																				</div>
																			</div>

																			<div id="ae_results_body">
																				<div id="ae_results_body_inner" class="ae_results_tbl_loading">
																					<table id="ae_results_table" border="0"
																						cellpadding="0" cellspacing="0">
																						<thead style="visibility: collapse; height: 0">
																							<tr>
																								<!-- This is the only one that is fixed the other 
																									ones are dynamic -->
																								<th class="table_header_box_fake ae_results_accession"></th>


																								<xsl:for-each select="tokenize(attributes,' ')">


																									<xsl:choose>
																										<xsl:when test="position()&lt;7">
																											<th class="table_header_box_fake ae_results_{.}"></th>
																										</xsl:when>
																										<xsl:when test="position()=7">
																											<th class="table_header_box_fake col_moredetail"></th>
																										</xsl:when>


																										<xsl:otherwise>
																										</xsl:otherwise>
																									</xsl:choose>

																								</xsl:for-each>
																								<!-- <th class="table_header_box_fake ae_results_accession"></th> 
																									<th class="table_header_box_fake ae_results_description"></th> <th class="table_header_box_fake 
																									ae_results_samples"></th> -->
																							</tr>
																						</thead>
																						<!-- Here I will put my results os samples -->
																						<tbody id="ae_results_tbody">





																						</tbody>
																					</table>
																				</div>
																			</div>
																			<div id="ae_results_ftr" class="table_footer_box">
																				<div class="table_footer_left">
																					<div class="table_footer_right">
																						<div class="table_footer_inner">

																							<!-- <div id="ae_results_print" class="status_icon"><a 
																								href="" title="Print results"><img src="assets/images/silk_print.gif" alt="Print 
																								results"/></a></div> <div id="ae_results_save" class="status_icon"><a href="" 
																								title="Save results in a Tab-delimited format"><img src="assets/images/silk_save_txt.gif" 
																								alt="Save results in a Tab-delimited format"/></a></div> <div id="ae_results_save_xls" 
																								class="status_icon"><a href="" title="Open results table in Excel"><img src="assets/images/silk_save_xls.gif" 
																								alt="Open results table in Excel"/></a></div> <div id="ae_results_save_feed" 
																								class="status_icon"><a href="" title="Get RSS feed with first page results 
																								matching selected criteria"><img src="assets/images/silk_save_feed.gif" alt="Open 
																								results as RSS feed"/></a></div> -->
																							<div id="ae_results_status"></div>
																							<div id="ae_results_pager"></div>
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
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<!-- <xsl:apply-templates select="//Sample"></xsl:apply-templates> -->

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
