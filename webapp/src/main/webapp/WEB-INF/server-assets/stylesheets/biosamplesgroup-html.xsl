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
						href="{$basepath}/assets/stylesheets/ae_biosamples_20.css" type="text/css" />
					<script src="{$basepath}/assets/scripts/jquery-1.4.2.min.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/jquery.query-2.1.7m-ebi.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/ae_common_20.js"
						type="text/javascript" />
					<script src="{$basepath}/assets/scripts/ae_biosamples_20.js"
						type="text/javascript" />
				    <script src="{$basepath}/assets/scripts/jsdeferred.jquery-0.3.1.js" type="text/javascript"></script>
				    <script src="{$basepath}/assets/scripts/jquery.cookie-1.0.js" type="text/javascript"></script>
				    <script src="{$basepath}/assets/scripts/jquery.caret-range-1.0.js" type="text/javascript"></script>
				    <script src="{$basepath}/assets/scripts/jquery.autocomplete-1.1.0m-ebi.js" type="text/javascript"></script>
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

		<div id="ae_contents_box_100pc">
			<div id="ae_content">
				<div id="ae_navi">
					<a href="${interface.application.link.www_domain}/">EBI</a>
					<xsl:text> > </xsl:text>
					<a href="{$basepath}">ArrayExpress</a>
					<xsl:text> > </xsl:text>
					<a href="{$basepath}/biosamplesgroup">Bio Samples</a>
					<xsl:if test="not($vBrowseMode)">
						<xsl:text> > </xsl:text>
						<a href="{$basepath}/biosamplesgroup/{$id}">
							<xsl:value-of select="$id" />
						</a>
					</xsl:if>
				</div>
				<xsl:if test="$vBrowseMode">
					<div id="ae_query_box">
						<form id="ae_query_form" method="get" action="browse.html">
							<fieldset id="ae_keywords_fset">
								<label for="ae_keywords_field">
									Bio Samples [
									<a href="javascript:aeClearField('#ae_keywords_field')">clear</a>
									]
								</label>
								<input id="ae_keywords_field" type="text" name="keywords"
									value="{$keywords}" maxlength="255" class="ae_assign_font" />
							</fieldset>
							<div id="ae_submit_box">
								<input id="ae_query_submit" type="submit" value="Query" />
							</div>
							<div id="ae_results_stats">
								<div>
									<xsl:value-of select="$vTotal" />
									<xsl:text> sample</xsl:text>
									<xsl:if test="$vTotal != 1">
										<xsl:text>s</xsl:text>
									</xsl:if>
									<xsl:text> found</xsl:text>
									<xsl:if test="$vTotal > $vPageSize">
										<xsl:text>, displaying </xsl:text>
										<xsl:value-of select="$vFrom" />
										<xsl:text> - </xsl:text>
										<xsl:value-of select="$vTo" />
									</xsl:if>
									<xsl:text>.</xsl:text>
								</div>
								<xsl:if test="$vTotal > $vPageSize">
									<xsl:variable name="vTotalPages"
										select="floor( ( $vTotal - 1 ) div $vPageSize ) + 1" />
									<div id="ae_results_pager">
										<div id="total_pages">
											<xsl:value-of select="$vTotalPages" />
										</div>
										<div id="page">
											<xsl:value-of select="$vPage" />
										</div>
										<div id="page_size">
											<xsl:value-of select="$vPageSize" />
										</div>
									</div>
								</xsl:if>
							</div>
						</form>
					</div>
				</xsl:if>



				<xsl:choose>
					<xsl:when test="$vTotal&gt;0 and $vBrowseMode">
						<div id="ae_results_box">
							<table id="ae_results_table" border="0" cellpadding="0"
								cellspacing="0">
								<thead>
									<tr>
										<th class="col_id sortable">
											<xsl:text>Id</xsl:text>
											<xsl:call-template name="add-sort">
												<xsl:with-param name="pKind" select="'id'" />
											</xsl:call-template>
										</th>
										<th class="col_description sortable">
											<xsl:text>Description</xsl:text>
											<xsl:call-template name="add-sort">
												<xsl:with-param name="pKind" select="'description'" />
											</xsl:call-template>
										</th>
										<th class="col_samples sortable">
											<xsl:text>Samples</xsl:text>
											<xsl:call-template name="add-sort">
												<xsl:with-param name="pKind" select="'samples'" />
											</xsl:call-template>
										</th>
									</tr>
								</thead>
								<tbody>
									<xsl:apply-templates select="$vFiltered">
									</xsl:apply-templates>
								</tbody>
							</table>
						</div>
					</xsl:when>
					<xsl:when test="$vTotal&gt;0">
						<div id="ae_results_box">
							<table id="ae_results_table" border="0" cellpadding="0"
								cellspacing="0">
								<xsl:apply-templates select="$vFiltered">
								</xsl:apply-templates>
							</table>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="block-warning">
							<xsl:with-param name="pStyle" select="'ae_warn_area'" />
							<xsl:with-param name="pMessage">
								<xsl:text>There are no Samples matching your search criteria found in BioSamples Database.</xsl:text>
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

			<tr>
				<td width="300">
					<b>Databases:</b>
				</td>
				<td>
				<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="string-join(attribute/value[../@class='Databases']//value, ', ')" />
								<xsl:with-param name="pFieldName" select="'databases'" />
							</xsl:call-template>
	
				</td>
			</tr>
			<tr>
				<td width="300">
					<b>Organizations:</b>
				</td>
				<td>
				<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="string-join(attribute/value[../@class='Organizations']//value, ', ')" />
								<xsl:with-param name="pFieldName" select="'organizations'" />
							</xsl:call-template>
	
				</td>
			</tr>
			<tr>
				<td width="300">
					<b>Persons:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="string-join(attribute/value[../@class='Persons']//value, ', ')" />
								<xsl:with-param name="pFieldName" select="'persons'" />
							</xsl:call-template>
	
				</td>
			</tr>
			<tr>
				<td width="300">
					<b>Total/matched samples:</b>
				</td>
				<td>
					<xsl:value-of select="@samplecount"></xsl:value-of>
				</td>
			</tr>

			<tr>
				<td colspan="2">
					<!-- This div is used to keep all the samples -->
					<div id="ae_samples_list">

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


</xsl:stylesheet>
