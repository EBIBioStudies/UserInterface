<?xml version="1.0" encoding="windows-1252"?>


<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:search="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="html xs fn  escape search"
	exclude-result-prefixes="html xs fn escape search" version="2.0">



	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="sampleskeywords" />


	<xsl:param name="accession" />

	<!-- <xsl:param name="userid" /> -->
	<xsl:param name="basepath" />
	<xsl:param name="total" />



	<xsl:include href="biostudies-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biostudies-highlight.xsl" />
	<xsl:include href="biostudies-process-attributes.xsl" />


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


	<xsl:variable name="vBrowseMode" select="not($accession)" />

	<xsl:variable name="vkeywords" select="$keywords" />

	<xsl:variable name="vsampleskeywords" select="$sampleskeywords" />

	<xsl:variable name="vSearchMode"
		select="fn:ends-with($relative-uri, 'search.html')" />
	<xsl:variable name="vQueryString"
		select="if ($query-string) then fn:concat('?', $query-string) else ''" />


	<!-- <xsl:output omit-xml-declaration="no" method="html" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD XHTML 4.01 Transitional//EN" /> -->

	<xsl:template match="/">

		<xsl:call-template name="ae-page">
			<xsl:with-param name="pIsSearchVisible" select="fn:true()" />
			<xsl:with-param name="pSearchInputValue"
				select="if (fn:true()) then $keywords else ''" />
			<xsl:with-param name="pTitleTrail" select="$accession" />
			<xsl:with-param name="pExtraCSS">
				<link rel="stylesheet"
					href="{$context-path}/assets/stylesheets/biostudies_detail_10.css"
					type="text/css" />
				<link rel="stylesheet"
					href="{$context-path}/assets/stylesheets/biostudies_browse_dyntable_10.css"
					type="text/css" />
				<!-- need this to have the scrollbars on Chrome/firefox/safari - overwrite 
					the ebi css definition [PT:53620963] -->
				<style type="text/css">
					html {overflow-y:auto;}
				</style>
			</xsl:with-param>
			<xsl:with-param name="pBreadcrumbTrail">
				<a href="{$context-path}/browse.html">BioStudy</a>
				>
				<xsl:value-of select="$accession" />
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
				<!-- <script src="{$context-path}/assets/scripts/jsdeferred.jquery-0.3.1.js" 
					type="text/javascript"></script> -->
				<script src="{$context-path}/assets/scripts/biostudies_detail_10.js"
					type="text/javascript"></script>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="ae-content-section">

		<xsl:variable name="vFiltered" select="//all/section" />
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
						<xsl:text>There are no BioStudies matching your search criteria found in BioStudies Database.</xsl:text>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="section">
		<tr>
			<td>
				<div id="bs_detail" class="detail_table">
					<xsl:call-template name="detail-table">
						<xsl:with-param name="id" select="$accession" />
						<xsl:with-param name="section" select="." />
					</xsl:call-template>

				</div>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="detail-table">
		<xsl:param name="id" />
		<xsl:param name="section" />
		<table id="bs_results_tabledetail">
			<tr>
				<td>
					<h4>
						Accession
						<xsl:choose>
							<!-- test="string-length($vkeywords)>0"> -->
							<xsl:when
								test="string-length($vkeywords)>0 and contains($vkeywords,'accession:')">
								<xsl:call-template name="highlight">
									<xsl:with-param name="pText" select="$id" />
									<xsl:with-param name="pFieldName" select="'accession'" />
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@id"></xsl:value-of>
							</xsl:otherwise>
						</xsl:choose>
					</h4>
					<table id="bs_results_tablesamplegroupdetail">
					<xsl:call-template name="process_attributes">
						<xsl:with-param name="pAttributes" select="$section/attributes"></xsl:with-param>
					</xsl:call-template>

					<xsl:call-template name="process_files">
						<xsl:with-param name="pAttributes" select="$section/files"></xsl:with-param>
					</xsl:call-template>

					<xsl:call-template name="process_links">
						<xsl:with-param name="pAttributes" select="$section/links"></xsl:with-param>
					</xsl:call-template>

					<xsl:if test="count($section/subsections/section) &gt; 0">
						<div style="margin-left:40px">

							<!--  to show the biosamples subsections in a differente way - as a table -->
							<xsl:if test="count($section/subsections/section[@biosample])">						
							<table id="bs_results_tabledetail_dyna">
								<tr>
									<td>
										<b>Subsections biosamples</b>
									</td>
								</tr>
								<tr>
									<td>
										<xsl:call-template name="process_subsections_biosamples">
											<xsl:with-param name="pSubsections"
												select="$section/subsections/section[@biosample]" />
										</xsl:call-template>
									</td>
								</tr>
							</table>
							</xsl:if>
							
							
							<table id="bs_results_tabledetail">
								<tr>
									<td>
										<b>Subsections</b>
										<xsl:for-each select="$section/subsections/section[not(@biosample)]">
											<xsl:call-template name="detail-table">
												<xsl:with-param name="id" select="@id" />
												<xsl:with-param name="section" select="." />
											</xsl:call-template>
										</xsl:for-each>

									</td>
								</tr>
							</table>
						</div>
					</xsl:if>
				</table>
				</td>
			</tr>
		</table>
		
	</xsl:template>



	<xsl:template name="process_subsections_biosamples">
		<xsl:param name="pSubsections" />

		<div id="bs_results_listsamples">
			<table class="persist-header" id="bs_samples_table" border="0"
				cellpadding="0" width="100%" cellspacing="0" style="visibility: visible;">
				<colgroup>
					<col class="col_left_fixed" style="width:120px;" />
					<col class="col_middle_scrollable" style="width:100%" />
					<col class="col_right_fixed" style="width: 150px;" />
				</colgroup>
				<tbody>
					<tr>
						<td id="left_fixed">
							<!-- just to have the scroll height size to be aligned with the attributes 
								table (middle area) -->
							<div id="wrapper_top_scroll_sides">
								<div id="div_top_scroll_sides">
								</div>
							</div>
							<table id="src_name_table" border="0" cellpadding="0"
								cellspacing="0" width="100%">
								<thead>
									<tr>
										<th class="bs_results_accession sortable bs_results_Accession"
											id="bs_results_header_accession">
											Accession
										</th>
									</tr>
								</thead>
								<tbody id="bs_results_tbody_left">
									<xsl:for-each select="$pSubsections">
										<tr>
											<td>
												<xsl:copy-of select="string(./@id)" />
											</td>
										</tr>
									</xsl:for-each>

								</tbody>
							</table>

						</td>



						<td id="middle_scrollable">
							<div id="wrapper_top_scroll">
								<div id="div_top_scroll">
								</div>
							</div>
							<div class="attr_table_shadow_container">
								<div class="attr_table_scroll">
									<table id="attr_table" border="0" cellpadding="0"
										cellspacing="0" width="100%">
										<thead>
											<tr>

												<xsl:for-each
													select="distinct-values($pSubsections/attributes/attribute/@name[not(../../@biosample)])">
													<th>
														<span class="table_header_inner_att">
															<!-- <xsl:value-of select="replace(.,' ' , '_')"></xsl:value-of>&nbsp; -->
															<xsl:copy-of select="." />
														</span>
													</th>
												</xsl:for-each>
											</tr>
										</thead>
										<tbody id="bs_results_tbody_middle">
											<xsl:for-each select="$pSubsections[@biosample]">
												<xsl:variable name="vSection" select="."></xsl:variable>
												<tr>
													<xsl:for-each
														select="distinct-values($pSubsections/attributes/attribute/@name[not(../../@biosample)])">
														<xsl:variable name="vAtt" select="."></xsl:variable>
														<td>
															<xsl:copy-of
																select="$vSection/attributes/attribute[@name=$vAtt]"></xsl:copy-of>
														</td>
													</xsl:for-each>

												</tr>
											</xsl:for-each>

										</tbody>
									</table>
								</div>
								<div class="left_shadow" style="display: none;"></div>
								<div class="right_shadow" style="display: none;"></div>
							</div>
						</td>


						<td id="right_fixed">
							<!-- just to have the scroll height size to be aligned with the attributes 
								table (middle area) -->
							<div id="wrapper_top_scroll_sides">
								<div id="div_top_scroll_sides">
								</div>
							</div>
							<table id="links_table" border="0" cellpadding="0"
								cellspacing="0" width="100%">
								<thead>
									<tr>
										<!-- I will not allow to sort -->
										<th class="bs_results_database sortable bs_results_files"
											id="bs_results_header_files">
											Files
										</th>
										<th class="bs_results_database sortable bs_results_links"
											id="bs_results_header_links">
											Links
										</th>
									</tr>
								</thead>
								<tbody id="bs_results_tbody_right">
									<xsl:for-each select="$pSubsections[@biosample]">
										<tr>
											<td>
												<xsl:copy-of select="'file'" />
											</td>
											<td>
												<xsl:copy-of select="'link'" />
											</td>
										</tr>
									</xsl:for-each>

								</tbody>
							</table>
						</td>
					</tr>

				</tbody>
			</table>
		</div>

	</xsl:template>

	<xsl:template name="process_subsections_biosamples_old">
		<xsl:param name="pSubsections" />

		<table>
			<thead>
				<tr>
					<xsl:for-each
						select="distinct-values($pSubsections/attributes/attribute/@name[not(../../@biosample)])">
						<th>
							<xsl:copy-of select="."></xsl:copy-of>
						</th>
					</xsl:for-each>
				</tr>
			</thead>

			<tbody>

				<xsl:for-each select="$pSubsections[@biosample]">
					<xsl:variable name="vSection" select="."></xsl:variable>
					<tr>
						<xsl:for-each
							select="distinct-values($pSubsections/attributes/attribute/@name[not(../../@biosample)])">
							<xsl:variable name="vAtt" select="."></xsl:variable>
							<td>
								<xsl:copy-of select="$vSection/attributes/attribute[@name=$vAtt]"></xsl:copy-of>
							</td>
						</xsl:for-each>

					</tr>
				</xsl:for-each>

			</tbody>

		</table>

	</xsl:template>

	<xsl:template name="process_attributes">
		<xsl:param name="pAttributes" />
		<xsl:variable name="vAtt" select="$pAttributes"></xsl:variable>
		<!-- <table id="bs_results_tabledetail"> -->
			<!-- <tr> <td colspan="2"> <b> <u>Attributes</u> </b> </td> </tr> -->


			<!-- I will process all the same attributes at same time -->
			<xsl:for-each select="distinct-values($vAtt/attribute/@name)">
				<xsl:variable name="vAttName" select="."></xsl:variable>
				<xsl:call-template name="process_bulk_attribute">
					<xsl:with-param name="pAttributes"
						select="$vAtt/attribute[@name=$vAttName]" />
					<xsl:with-param name="pAttributeName" select="$vAttName" />
				</xsl:call-template>
			</xsl:for-each>


		<!-- </table> -->
	</xsl:template>



	<xsl:template name="process_bulk_attribute">
		<xsl:param name="pAttributes" />
		<xsl:param name="pAttributeName" />
		<xsl:variable name="vAtt" select="$pAttributes"></xsl:variable>
		<tr>
			<td class="col_title">
				<b>
					<xsl:value-of select="$pAttributeName"></xsl:value-of>
					:
				</b>
			</td>
			<td>

				<xsl:for-each select="$vAtt">
					<xsl:call-template name="process_attribute">
						<xsl:with-param name="pAttribute" select="." />
						<xsl:with-param name="pAttributeName" select="$pAttributeName" />
					</xsl:call-template>
					<xsl:if test="position()!=last()">
						<br />
					</xsl:if>
				</xsl:for-each>

			</td>
		</tr>
	</xsl:template>

	<xsl:template name="process_attribute">
		<xsl:param name="pAttribute" />
		<xsl:param name="pAttributeName" />
		<xsl:variable name="vAtt" select="$pAttribute"></xsl:variable>
		<!-- ##<xsl:copy-of select="$pAttribute"></xsl:copy-of>## -->
		<xsl:call-template name="highlight">
			<xsl:with-param name="pText" select="$vAtt/value" />
			<xsl:with-param name="pFieldName" select="$pAttributeName" />
		</xsl:call-template>
	</xsl:template>



	<xsl:template name="process_files">
		<xsl:param name="pAttributes" />

		<table id="bs_results_tabledetail">
			<tr>
				<td align="left">
					<b>
						<a href="javascript:ShwHid('FileHiddenDiv{generate-id($pAttributes)}')">
							Files(<xsl:value-of select="count($pAttributes//file)"></xsl:value-of>):
						</a>
					</b>
				</td>
			</tr>
		</table>
		<div class="mid" id="FileHiddenDiv{generate-id($pAttributes)}"
			style="display: none">
			<table id="bs_results_tabledetail">
				<tr>
					<td colspan="2" align="left">
						<xsl:for-each select="$pAttributes/table">
							<xsl:call-template name="process_file_table">
								<xsl:with-param name="pAttribute" select="." />
							</xsl:call-template>
						</xsl:for-each>
						<xsl:for-each select="$pAttributes/file">
							<xsl:call-template name="process_file">
								<xsl:with-param name="pAttribute" select="." />
							</xsl:call-template>
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>



	<xsl:template name="process_file">
		<xsl:param name="pAttribute" />
		<table id="bs_results_tabledetail">
			<tr>
				<td>
					<xsl:value-of select="@id"></xsl:value-of>
				</td>
			</tr>
			<xsl:if test="count($pAttribute/attributes/attribute)>0">
				<tr>
					<td>
						<table id="bs_results_tabledetail">
							<xsl:for-each select="$pAttribute/attributes/attribute">
								<xsl:call-template name="process_attribute_files">
									<xsl:with-param name="pAttribute" select="." />
								</xsl:call-template>
							</xsl:for-each>
						</table>
					</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>


	<xsl:template name="process_file_table">
		<xsl:param name="pAttribute" />
		<xsl:variable name="vAtt" select="$pAttribute"></xsl:variable>


		<table id="bs_results_tabledetail">
			<thead>
				<tr>
					<th>File</th>
					<xsl:for-each select="distinct-values($vAtt//attribute/@name)">
						<th>
							<xsl:copy-of select="."></xsl:copy-of>
						</th>
					</xsl:for-each>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="$vAtt/file">
					<xsl:variable name="vAttFile" select="."></xsl:variable>
					<tr>
						<td>
							<xsl:copy-of select="string($vAttFile/@name)"></xsl:copy-of>
						</td>
						<xsl:for-each select="distinct-values($vAtt//attribute/@name)">
							<xsl:variable name="vAttName" select="."></xsl:variable>
							<td>
								<xsl:copy-of
									select="string($vAttFile//attribute/value[../@name=$vAttName])"></xsl:copy-of>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>

			</tbody>

		</table>

	</xsl:template>



	<xsl:template name="process_attribute_files">
		<xsl:param name="pAttribute" />

		<tr>
			<td class="col_title">
				<b>
					<xsl:value-of select="@name"></xsl:value-of>
				</b>
			</td>
			<td>
				<xsl:value-of select="."></xsl:value-of>
			</td>
		</tr>

	</xsl:template>



	<xsl:template name="process_links">
		<xsl:param name="pAttributes" />
		<table id="bs_results_tabledetail">
			<tr>
				<td align="left">
					<b>
						<a href="javascript:ShwHid('LinkHiddenDiv{generate-id($pAttributes)}')">
							Links(<xsl:value-of select="count($pAttributes//link)"></xsl:value-of>):
						</a>
					</b>
				</td>
			</tr>
		</table>
		<div class="mid" id="LinkHiddenDiv{generate-id($pAttributes)}"
			style="display: none">
			<table id="bs_results_tabledetail">
				<tr>
					<td colspan="2" align="left">
						<xsl:for-each select="$pAttributes/table">
							<xsl:call-template name="process_link_table">
								<xsl:with-param name="pAttribute" select="." />
							</xsl:call-template>
						</xsl:for-each>
						<xsl:for-each select="$pAttributes/link">
							<xsl:call-template name="process_link">
								<xsl:with-param name="pAttribute" select="." />
							</xsl:call-template>
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>


	<xsl:template name="process_link_table">
		<xsl:param name="pAttribute" />
		<xsl:variable name="vAtt" select="$pAttribute"></xsl:variable>


		<table id="bs_results_tabledetail">
			<thead>
				<tr>
					<th>Link</th>
					<xsl:for-each select="distinct-values($vAtt//attribute/@name)">
						<th>
							<xsl:copy-of select="."></xsl:copy-of>
						</th>
					</xsl:for-each>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="$vAtt/link">
					<xsl:variable name="vAttLink" select="."></xsl:variable>
					<tr>
						<td>
							<a href="{string($vAttLink/@url)}" target="ext"><xsl:copy-of select="string($vAttLink/@url)"></xsl:copy-of></a>
						</td>
						<xsl:for-each select="distinct-values($vAtt//attribute/@name)">
							<xsl:variable name="vAttName" select="."></xsl:variable>
							<td>
								<xsl:copy-of
									select="string($vAttLink//attribute/value[../@name=$vAttName])"></xsl:copy-of>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>

			</tbody>

		</table>

	</xsl:template>


	<xsl:template name="process_link">
		<xsl:param name="pAttribute" />
		<table id="bs_results_tabledetail">
			<tr>
				<td>
					<a href="{@url}">
						<xsl:value-of select="@url"></xsl:value-of>
					</a>
				</td>
			</tr>
			<xsl:if test="count($pAttribute/attributes/attribute)>0">
				<tr>
					<td>
						<table id="bs_results_tabledetail">
							<xsl:for-each select="$pAttribute/attributes/attribute">
								<xsl:call-template name="process_attribute_links">
									<xsl:with-param name="pAttribute" select="." />
								</xsl:call-template>
							</xsl:for-each>
						</table>
					</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>


	<xsl:template name="process_attribute_links">
		<xsl:param name="pAttribute" />

		<tr>
			<td class="col_title">
				<b>
					<xsl:value-of select="@name"></xsl:value-of>
				</b>
			</td>
			<td>
				<xsl:value-of select="."></xsl:value-of>
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
									test="count(.//attribute/simpleValue/value[../../@class='Person Email'])>0 and (.//attribute/simpleValue/value[../../@class='Person Email']!='')">
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/simpleValue/value[../../@class='Person First Name'], ' ', .//attribute/simpleValue/value[../../@class='Person Last Name'] , ' &lt;' ,.//attribute/simpleValue/value[../../@class='Person Email'], '>')"></xsl:variable>

									<a
										href="mailto:{.//attribute/simpleValue/value[../../@class='Person Email']}"
										class="icon icon-generic" data-icon="E">
										<xsl:call-template name="highlight">
											<xsl:with-param name="pText" select="$vStringContacts" />
											<xsl:with-param name="pFieldName" select="'persons'" />
										</xsl:call-template>
									</a>
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/simpleValue/value[../../@class='Person First Name'], ' ', .//attribute/simpleValue/value[../../@class='Person Last Name'])"></xsl:variable>
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



	<xsl:template name="process_databases">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/objectValue">
			<xsl:call-template name="process_database">
				<xsl:with-param name="pName"
					select=".//attribute[@class='Database Name']/simpleValue/value" />
				<xsl:with-param name="pUrl"
					select=".//attribute[@class='Database URI']/simpleValue/value" />
				<xsl:with-param name="pId"
					select=".//attribute[@class='Database ID']/simpleValue/value" />
			</xsl:call-template>
			<br />
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_database">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pId" />

		<xsl:variable name="bdName" select="lower-case($pName)"></xsl:variable>
		<xsl:choose>
			<xsl:when
				test="$bdName=('arrayexpress','ena','ena sra','dgva','pride') and not($pUrl='')">
				<!-- PRIDE changed the user interface: this is temporary -->
				<xsl:variable name="pUrl"
					select="replace($pUrl,'http://www.ebi.ac.uk/pride/showExperiment.do\?experimentAccessionNumber','http://www.ebi.ac.uk/pride/archive/simpleSearch?q')"></xsl:variable>

				<a href="{$pUrl}" target="ext">
					<img src="{$basepath}/assets/images/{$bdName}_logo.gif" alt="{$pName} Link"
						border="0" title="{$pName}" />
				</a>
			</xsl:when>
			<xsl:when test="not($pUrl='')">
				<a href="{$pUrl}" target="ext" title="{$pName}">
					<font class="icon icon-generic" data-icon="L" title="{$pName}" />
					<xsl:copy-of select="$pName"></xsl:copy-of>
				</a>
			</xsl:when>
		</xsl:choose>
		<br />
		URI:
		<a href="{$pUrl}" target="ext">
			<xsl:copy-of select="$pUrl"></xsl:copy-of>
		</a>
		;
		<br />
		ID:
		<xsl:copy-of select="$pId"></xsl:copy-of>
		;

	</xsl:template>

	<!-- <td vertical-align="middle">&nbsp;Name: <xsl:copy-of select=".//attribute/value[../@class='Database 
		Name']"/>; ID: <xsl:copy-of select=".//attribute/value[../@class='Database 
		ID']"/>; URI: <a href="{.//attribute/value[../@class='Database URI']}" target="ext"> 
		<xsl:value-of select=".//attribute/value[../@class='Database URI']"></xsl:value-of> 
		</a> -->

	<xsl:template name="process_ftplocation">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/simpleValue">
			<a href="{./value}" target="ext">
				<xsl:copy-of select="./value"></xsl:copy-of>
			</a> &nbsp;
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_publications">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/object">

					<tr>
						<td>
							Publication DOI:
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select=".//attribute/simpleValue/value[../../@class='Publication DOI']" />
								<xsl:with-param name="pFieldName" select="'publications'" />
							</xsl:call-template>
							Publication PubMed ID:
							<a
								href="http://europepmc.org/abstract/MED/{.//attribute/simpleValue/value[../../@class='Publication PubMed ID']}"
								target="ext">
								<xsl:call-template name="highlight">
									<xsl:with-param name="pText"
										select=".//attribute/simpleValue/value[../../@class='Publication PubMed ID']" />
									<xsl:with-param name="pFieldName" select="'publications'" />
								</xsl:call-template>
							</a>
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
									select="concat('Name: ', .//attribute/simpleValue/value[../../@class='Organization Name'],'; Address: ', .//attribute/simpleValue/value[../../@class='Organization Address'], '; Role: ', .//attribute/simpleValue/value[../../@class='Organization Role'], '; Email: ', .//attribute/simpleValue/value[../../@class='Organization Email'])" />
								<xsl:with-param name="pFieldName" select="'organizations'" />
							</xsl:call-template>
							; URI:
							<a href="{.//attribute/value[../@class='Organization URI']}"
								target="ext">
								<xsl:value-of
									select=".//attribute/simpleValue/value[../../@class='Organization URI']"></xsl:value-of>
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
									select=".//attribute/simpleValue/value[../../@class='Term Source Name']" />
								<xsl:with-param name="pFieldName" select="'termsources'" />
							</xsl:call-template>
						</td>
						<td>
							<a
								href="{.//attribute/simpleValue/value[../../@class='Term Source URI']}"
								target="ext">
								<xsl:value-of
									select=".//attribute/simpleValue/value[../../@class='Term Source URI']"></xsl:value-of>
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



	<xsl:template name="process_common_attributes">
		<xsl:param name="pAttributes" />


		<!-- £££££££ -->
		<!-- <div id="wrapper_top_scroll"> <div id="div_top_scroll"> </div> </div> -->
		<div class="attr_table_shadow_container">
			<div class="attr_table_sample_group_scroll">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					style="table-layout:fixed;border:0px;margin:0px;">
					<tr>
						<td>
							<table id="attr_common_table" border="0" cellpadding="0"
								cellspacing="0" width="100%">
								<thead>
									<tr>
										<xsl:for-each
											select="$pAttributes/attribute[count(.//simpleValue)>0]">
											<th>
												<xsl:copy-of select="string(./@class)"></xsl:copy-of>
											</th>
										</xsl:for-each>
									</tr>
								</thead>
								<tbody id="bs_results_tbody_common">
									<!-- <tr> <xsl:for-each select="$pAttributes/attribute[count(.//simpleValue)>0]"> 
										<td> <xsl:variable name="attributeClass" select="@class"></xsl:variable> 
										<xsl:variable name="attribute" select="."></xsl:variable> <xsl:choose> <xsl:when 
										test="$attributeClass='Derived From'"> <xsl:call-template name="process_derived_from"> 
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param> </xsl:call-template> 
										</xsl:when> <xsl:when test="count($attribute//attribute[@class='Term Source 
										REF'])=0 and count($attribute//attribute[@class='Unit'])=0 "> <xsl:call-template 
										name="process_multiple_values"> <xsl:with-param name="pField" select="lower-case(replace(@attributeClass,' 
										' , '-'))"></xsl:with-param> <xsl:with-param name="pValue" select="$attribute"></xsl:with-param> 
										</xsl:call-template> </xsl:when> <xsl:when test="count($attribute//attribute[@class='Unit'])>0"> 
										<xsl:call-template name="process_unit"> <xsl:with-param name="pAttribute" 
										select="$attribute"></xsl:with-param> <xsl:with-param name="pField" select="lower-case(replace($attributeClass,' 
										' , '-'))"></xsl:with-param> </xsl:call-template> </xsl:when> <xsl:otherwise> 
										<xsl:call-template name="process_efo"> <xsl:with-param name="pAttribute" 
										select="$attribute"></xsl:with-param> <xsl:with-param name="pField" select="lower-case(replace(@attributeClass,' 
										' , '-'))"></xsl:with-param> </xsl:call-template> </xsl:otherwise> </xsl:choose> 
										</td> </xsl:for-each> </tr> -->
								</tbody>
							</table>
						</td>
					</tr>
				</table>



			</div>
			<div class="left_shadow" style="display: none;"></div>
			<div class="right_shadow" style="display: none;"></div>
		</div>

	</xsl:template>




</xsl:stylesheet>
