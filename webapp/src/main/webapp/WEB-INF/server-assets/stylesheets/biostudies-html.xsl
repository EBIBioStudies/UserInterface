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

						<xsl:if test="count($section/files//file)>0">
							<xsl:call-template name="process_files">
								<xsl:with-param name="pAttributes" select="$section/files"></xsl:with-param>
							</xsl:call-template>
						</xsl:if>

						<xsl:if test="count($section/links//link)>0">
							<xsl:call-template name="process_links">
								<xsl:with-param name="pAttributes" select="$section/links"></xsl:with-param>
							</xsl:call-template>
						</xsl:if>

						<xsl:if test="count($section/subsections/section)>0">
							<xsl:call-template name="process_subsections">
								<xsl:with-param name="pSubsections" select="$section/subsections"></xsl:with-param>
							</xsl:call-template>
						</xsl:if>

					</table>
				</td>
			</tr>
		</table>

	</xsl:template>

	<xsl:template name="process_subsections">
		<xsl:param name="pSubsections" />
		<xsl:variable name="vSubsections" select="$pSubsections"></xsl:variable>


		<!-- <xsl:if test="count($vSubsections) &gt; 0"> -->
		<tr>
			<td class="col_title">
				<!-- <a href="javascript:ShwHid('SubsectionsHiddenDiv{generate-id($vSubsections)}')"> 
					Subsections (<xsl:value-of select="count($vSubsections/*)"></xsl:value-of>): 
					</a> -->
				&nbsp;
			</td>
			<td>
				<!-- <div class="mid" id="SubsectionsHiddenDiv{generate-id($vSubsections)}" 
					style="display: none"> -->
				<div class="mid" id="SubsectionsHiddenDiv{generate-id($vSubsections)}"
					style="">

					<!-- to show the biosamples subsections in a differente way - as a table -->
					<xsl:if test="count($vSubsections/section[@biosample])">
						<table id="bs_results_tabledetail_dyna">
							<tr>
								<td>
									<xsl:call-template name="process_subsections_biosamples">
										<xsl:with-param name="pSubsections"
											select="$vSubsections/section[@biosample]" />
									</xsl:call-template>
								</td>
							</tr>
						</table>
					</xsl:if>


					<table id="bs_results_tabledetail">
						<tr>
							<td>
								<xsl:for-each select="$vSubsections/section[not(@biosample)]">
									<xsl:call-template name="detail-table">
										<xsl:with-param name="id" select="@id" />
										<xsl:with-param name="section" select="." />
									</xsl:call-template>
								</xsl:for-each>

							</td>
						</tr>
					</table>

				</div>

			</td>
		</tr>
		<!-- </xsl:if> -->


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

		<xsl:choose>
			<xsl:when test="$pAttributeName='Title'">
				<tr>
					<td colspan="2">
						<b>
							<xsl:value-of select="$vAtt/value"></xsl:value-of>
						</b>
					</td>
				</tr>
			</xsl:when>
			<xsl:when test="$pAttributeName='Description'">
				<tr>
					<td colspan="2">
							<xsl:value-of select="$vAtt/value"></xsl:value-of>
							<br/>
					</td>
				</tr>
			</xsl:when>
			
				<xsl:when test="$pAttributeName='Related identifer'">
				<tr>
					<td class="col_title">
						<b>
							<xsl:value-of select="$pAttributeName"></xsl:value-of>
							:
						</b>
					</td>
					<td>
						<xsl:for-each select="$vAtt">
						<xsl:call-template name="process_reference">
								<xsl:with-param name="pName" select="./valqual[@name='type']" />
								<xsl:with-param name="pUrl" select="''" />
								<xsl:with-param name="pId" select="./value" />
							</xsl:call-template>
							<br/>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:when>		

			<xsl:otherwise>
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
			</xsl:otherwise>

		</xsl:choose>
	</xsl:template>

	<xsl:template name="process_attribute">
		<xsl:param name="pAttribute" />
		<xsl:param name="pAttributeName" />
		<xsl:variable name="vAtt" select="$pAttribute"></xsl:variable>
		<!-- ##<xsl:copy-of select="$pAttributeName"></xsl:copy-of>## -->
		<xsl:call-template name="highlight">
			<xsl:with-param name="pText" select="$vAtt/value" />
			<xsl:with-param name="pFieldName" select="lower-case($pAttributeName)" />
		</xsl:call-template>
	</xsl:template>



	<xsl:template name="process_files">
		<xsl:param name="pAttributes" />
		<tr>
			<td class="col_title">
				<a href="javascript:ShwHid('FileHiddenDiv{generate-id($pAttributes)}')">
					Files (<xsl:value-of select="count($pAttributes//file)"></xsl:value-of>):
				</a>
			</td>
			<td>
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
			</td>
		</tr>
	</xsl:template>




	<!-- show as a table -->
	<xsl:template name="process_file">
		<xsl:param name="pAttribute" />
		<xsl:call-template name="process_file_table">
			<xsl:with-param name="pAttribute" select="$pAttribute/.."></xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="process_file_old">
		<xsl:param name="pAttribute" />
		<table id="bs_results_tabledetail">
			<tr>
				<td>
					<a href="{@name}">
						<xsl:value-of select="@name"></xsl:value-of>
					</a>
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
							<a href="{string($vAttFile/@name)}">
								<xsl:copy-of select="string($vAttFile/@name)"></xsl:copy-of>
							</a>
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
		<tr>
			<td class="col_title">
				<a href="javascript:ShwHid('LinkHiddenDiv{generate-id($pAttributes)}')">
					Links (<xsl:value-of select="count($pAttributes//link)"></xsl:value-of>):
				</a>
			</td>
			<td>
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
			</td>
		</tr>
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
							<a href="{string($vAttLink/@url)}" target="ext">
								<xsl:copy-of select="string($vAttLink/@url)"></xsl:copy-of>
							</a>
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



<xsl:template name="process_reference">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pId" />

		<!-- I will compare only with the first word - it works for the myequivalents service names -->
		<!-- sometimes the dbname just have one string, so i need to add a space to assure that the substring-before works -->
		<xsl:variable name="bdName" select="lower-case(substring-before(concat($pName,' '),' '))"></xsl:variable>
		
		<!-- ¢¢<xsl:copy-of select="$bdName"></xsl:copy-of>¢¢ -->

		<xsl:variable name="bdBaseLink">
		<xsl:choose>
			<xsl:when test="$bdName='arrayexpress'">
				<xsl:value-of select="'http://www.ebi.ac.uk/arrayexpress/experiments/'"/>
			</xsl:when>
			<xsl:when test="$bdName='europe'">
				<xsl:value-of select="'http://europepmc.org/search?query='"/>
			</xsl:when>
			<xsl:when test="$bdName='pdb'">
				<xsl:value-of select="'http://www.ebi.ac.uk/pdbe-srv/view/entry/'"/>
			</xsl:when>
			<xsl:when test="$bdName='ena'">
				<xsl:value-of select="'http://www.ebi.ac.uk/ena/data/view/'"/>
			</xsl:when>
			<xsl:when test="$bdName='doi'">
				<xsl:value-of select="'http://doi.org/'"/>
			</xsl:when>
			<xsl:when test="$bdName='refspn'">
				<xsl:value-of select="'http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs='"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''"/>
			</xsl:otherwise>		
		</xsl:choose>
		</xsl:variable>
		
		
		<xsl:choose>
			<xsl:when
				test="$bdName=('arrayexpress','ena')">

				<a href="{$bdBaseLink}{$pId}" target="ext" id="iconelink">
					<img src="{$basepath}/assets/images/dblinkslogos/{$bdName}_logo.gif" alt="{$pName} Link"
						border="0" title="{$pName}" />
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a href="{$bdBaseLink}{$pId}" target="ext" title="{$pName}" id="iconelink">
					<font class="icon icon-generic" data-icon="L" title="{$pName}" />
					<xsl:copy-of select="$pName"></xsl:copy-of>
				</a>
			</xsl:otherwise>
		</xsl:choose>
		&nbsp;<a href="{$bdBaseLink}{$pId}" target="ext">
			<xsl:copy-of select="$pId"></xsl:copy-of>
		</a>
	</xsl:template>



</xsl:stylesheet>
