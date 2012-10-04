<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->
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

<!-- 	<xsl:output omit-xml-declaration="yes" method="html" indent="no" 
		encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />
 -->		
<!-- 	<xsl:output omit-xml-declaration="yes" method="html" indent="no" 
		encoding="windows-1252" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" /> -->



	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />

	<xsl:template match="/">
	  <xsl:variable name="vFrom" as="xs:integer">
            <xsl:choose>
                <xsl:when test="$vPage > 0"><xsl:value-of select="1 + ( $vPage - 1 ) * $vPageSize"/></xsl:when>
                <xsl:when test="$vTotal = 0">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="vTo" as="xs:integer">
            <xsl:choose>
                <xsl:when test="( $vFrom + $vPageSize - 1 ) > $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$vFrom + $vPageSize - 1"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <tr id="bs_results_summary_info">
            <td colspan="3">
                <div id="bs_results_total"><xsl:value-of select="$vTotal"/></div>
                <div id="bs_results_from"><xsl:value-of select="$vFrom"/></div>
                <div id="bs_results_to"><xsl:value-of select="$vTo"/></div>
                <div id="bs_results_page"><xsl:value-of select="$vPage"/></div>
                <div id="bs_results_pagesize"><xsl:value-of select="$vPageSize"/></div>
            </td>
        </tr>
        
         <xsl:choose>
            <xsl:when test="$vTotal > 0">
            <xsl:apply-templates select="//all/SampleGroup"></xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
            <!--     <tr class="ae_results_tr_error">
                    <td colspan="3">
                            <div>There are no Samples matching your search criteria found in BioSample Database.</div>
                            <div>More information on query syntax available in <a href="${interface.application.link.query_help}">ArrayExpress Query Help</a>.</div>
                    </td>
                </tr> -->
            </xsl:otherwise>
        </xsl:choose>
        
	</xsl:template>





	<xsl:template match="SampleGroup">
		<tr>
			<td class="col_id">
				<div>
					<a href="{$basepath}/group/{id}?keywords={$vkeywords}">
						<xsl:call-template name="highlight">
							<xsl:with-param name="pText" select="id" />
							<xsl:with-param name="pFieldName" select="'id'" />
						</xsl:call-template>
					</a>
				</div>
			</td>

			<td class="col_description">
				<!-- <div class="ellipsis_class">
				<span id="ellipsis" class='ellipsis_text'> -->
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="description" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				<!-- </span>
				</div> -->
			</td>

			<td class="col_samples">
				<div>
					<xsl:value-of select="samples"></xsl:value-of>
					<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
						select="count(Sample)"/> <xsl:with-param name="pFieldName"/> </xsl:call-template> -->
				</div>
			</td>


		</tr>
	</xsl:template>


</xsl:stylesheet>
