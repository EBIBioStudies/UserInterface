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
	<xsl:variable name="vkeywords" select="$keywords" />
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



	<xsl:include href="ae-highlight.xsl" />

	<xsl:template match="/">

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

		<div id="ae_results_summary_info">
			<div id="ae_results_total">
				<xsl:value-of select="$vTotal" />
			</div>
			<div id="ae_results_from">
				<xsl:value-of select="$vFrom" />
			</div>
			<div id="ae_results_to">
				<xsl:value-of select="$vTo" />
			</div>
			<div id="ae_results_page">
				<xsl:value-of select="$vPage" />
			</div>
			<div id="ae_results_pagesize">
				<xsl:value-of select="$vPageSize" />
			</div>
		</div>
		<!-- This div is used to control the paging -->


		<div id="ae_results_pager_samples">
		</div>
		<br />
		<form id="ae_query_form2" method="get"
			action="javascript:searchSamples(this.ae_keywords2_field.value);">
			<fieldset id="ae_keywords_fset2">
				<label for="ae_keywords_field2">
					Search inside the
					<xsl:value-of select="$vTotal" />
					<xsl:text> sample</xsl:text>
					<xsl:if test="$vTotal != 1">
						<xsl:text>s</xsl:text>
					</xsl:if>
					of SampleGroup [
					<a href="javascript:aeClearField('#ae_keywords2_field')">clear</a>
					]
				</label>
				<input id="ae_keywords2_field" type="text" name="keywords"
					value="{$vkeywords}" maxlength="255" class="ae_assign_font" />
			</fieldset>
			<div id="ae_submit_box2">
				<input id="ae_query_submit2" type="submit" value="Query" />
			</div>
		</form>

		<table border="0" cellpadding="0" cellspacing="0">
			<thead>
				<tr>

					<th class="col_id sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Name'" />
							<xsl:with-param name="pKind" select="'name'" />
						</xsl:call-template>
					</th>
					<th class="col_accession sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Accession'" />
							<xsl:with-param name="pKind" select="'accession'" />
						</xsl:call-template>
					</th>
					<th class="col_age sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Age'" />
							<xsl:with-param name="pKind" select="'age'" />
						</xsl:call-template>
					</th>
					<th class="col_disease sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'DiseaseType'" />
							<xsl:with-param name="pKind" select="'disease'" />
						</xsl:call-template>
					</th>
					<th class="col_expansion sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'ExpansionLLot'" />
							<xsl:with-param name="pKind" select="'expansion'" />
						</xsl:call-template>
					</th>
					<th class="col_clinical sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'ClinicalHistory'" />
							<xsl:with-param name="pKind" select="'clinical'" />
						</xsl:call-template>
					</th>
					<th class="col_familyrelationship sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Relationship'" />
							<xsl:with-param name="pKind" select="'familyrelationship'" />
						</xsl:call-template>
					</th>
					<th class="col_familymember sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Member'" />
							<xsl:with-param name="pKind" select="'familymember'" />
						</xsl:call-template>
					</th>
					<th class="col_ethnicity sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Ethnicity'" />
							<xsl:with-param name="pKind" select="'ethnicity'" />
						</xsl:call-template>
					</th>
					<th class="col_family sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Family'" />
							<xsl:with-param name="pKind" select="'family'" />
						</xsl:call-template>
					</th>
					<th class="col_sex sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Sex'" />
							<xsl:with-param name="pKind" select="'sex'" />
						</xsl:call-template>
					</th>
					<th class="col_clinicallystatus sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'AffectedStatus'" />
							<xsl:with-param name="pKind" select="'clinicallystatus'" />
						</xsl:call-template>
					</th>
					<th class="col_organism sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'Organism'" />
							<xsl:with-param name="pKind" select="'organism'" />
						</xsl:call-template>
					</th>
					<th class="col_transformation sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'TransformationType'" />
							<xsl:with-param name="pKind" select="'transformation'" />
						</xsl:call-template>
					</th>
					<th class="col_biopsysite sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'BiopsySite'" />
							<xsl:with-param name="pKind" select="'biopsysite'" />
						</xsl:call-template>
					</th>
					<th class="col_sampletype sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'SampleType'" />
							<xsl:with-param name="pKind" select="'sampletype'" />
						</xsl:call-template>
					</th>
					<th class="col_celltype sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'CellType'" />
							<xsl:with-param name="pKind" select="'celltype'" />
						</xsl:call-template>
					</th>
					<th class="col_timeunit sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'TimeUnit'" />
							<xsl:with-param name="pKind" select="'timeunit'" />
						</xsl:call-template>
					</th>
					<th class="col_organismpart sortable">
						<xsl:call-template name="add-sort">
							<xsl:with-param name="pTitle" select="'OrganismPart'" />
							<xsl:with-param name="pKind" select="'organismpart'" />
						</xsl:call-template>
					</th>
				</tr>
			</thead>
			<tbody>
				<xsl:apply-templates select="//Sample"></xsl:apply-templates>
			</tbody>
		</table>
	</xsl:template>



	<xsl:template match="Sample">
		<tr>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Name']" />
						<xsl:with-param name="pFieldName" select="'name'" />
					</xsl:call-template>
			
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Sample Accession']" />
						<xsl:with-param name="pFieldName" select="'sampleaccession'" />
					</xsl:call-template>
			</td>
			<td>
		<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Age']" />
						<xsl:with-param name="pFieldName" select="'age'" />
					</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='DiseaseType']" />
						<xsl:with-param name="pFieldName" select="'disease'" />
					</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='ExpansionLLot']" />
						<xsl:with-param name="pFieldName" select="'expansion'" />
					</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='ClinicalHistory']" />
						<xsl:with-param name="pFieldName" select="'clinical'" />
					</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='FamilyRelationship']" />
						<xsl:with-param name="pFieldName" select="'familyrelationship'" />
					</xsl:call-template>
			</td>


			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='FamilyMember']" />
						<xsl:with-param name="pFieldName" select="'familymember'" />
					</xsl:call-template>
			</td>
			<td>
				<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Ethnicity']" />
						<xsl:with-param name="pFieldName" select="'ethnicity'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Family']" />
						<xsl:with-param name="pFieldName" select="'family'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Sex']" />
						<xsl:with-param name="pFieldName" select="'sex'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='ClinicallyAffectedStatus']" />
						<xsl:with-param name="pFieldName" select="'clinicallystatus'" />
					</xsl:call-template>
			</td>

			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='Organism']" />
						<xsl:with-param name="pFieldName" select="'organism'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='TransformationType']" />
						<xsl:with-param name="pFieldName" select="'transformation'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='BiopsySite']" />
						<xsl:with-param name="pFieldName" select="'biopsysite'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='SampleType']" />
						<xsl:with-param name="pFieldName" select="'sampletype'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='CellType']" />
						<xsl:with-param name="pFieldName" select="'celltype'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='TimeUnit']" />
						<xsl:with-param name="pFieldName" select="'timeunit'" />
					</xsl:call-template>
			</td>
			<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="attribute/value[../@class='OrganismPart']" />
						<xsl:with-param name="pFieldName" select="'organismpart'" />
					</xsl:call-template>
			</td>

		</tr>

	</xsl:template>



	<xsl:template name="add-sort">
		<xsl:param name="pTitle" />
		<xsl:param name="pKind" />


		<xsl:choose>
			<xsl:when test="$pKind = $vSortBy">
				<xsl:choose>
					<xsl:when test="'ascending' = $vSortOrder">
						<xsl:variable name="vSortInverted" select="'descending'"></xsl:variable>
						<a href="javascript:sort('{$pKind}','{$vSortInverted}')">
							<xsl:value-of select="$pTitle"></xsl:value-of>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="vSortInverted" select="'ascending'"></xsl:variable>
						<a href="javascript:sort('{$pKind}','{$vSortInverted}')">
							<xsl:value-of select="$pTitle"></xsl:value-of>
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="vSortInverted" select="'ascending'"></xsl:variable>
				<a href="javascript:sort('{$pKind}','{$vSortInverted}')">
					<xsl:value-of select="$pTitle"></xsl:value-of>
				</a>
			</xsl:otherwise>
		</xsl:choose>





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
