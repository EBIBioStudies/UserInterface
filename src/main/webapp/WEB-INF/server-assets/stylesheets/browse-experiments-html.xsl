<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
                version="1.0">

    <xsl:param name="from">1</xsl:param>
    <xsl:param name="to">50</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="vTotalHybs" select="sum($vFilteredExperiments[hybs/text()>0]/hybs/text())"/>

        <xsl:variable name="vFrom" select="$from"/>
        <xsl:variable name="vTo">
            <xsl:choose>
                <xsl:when test="$to &gt; $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <helper:logInfo select="Query for '{$keywords}' filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>
        <tr id="ae_results_summary_info">
            <td colspan="8">
                <div id="ae_resutls_total"><xsl:value-of select="$vTotal"/></div>
                <div id="ae_resutls_total_samples"><xsl:value-of select="$vTotalSamples"/></div>
                <div id="ae_resutls_total_hybs"><xsl:value-of select="$vTotalHybs"/></div>
                <div id="ae_resutls_from"><xsl:value-of select="$vFrom"/></div>
                <div id="ae_resutls_to"><xsl:value-of select="$vTo"/></div>
                <div id="ae_resutls_sortby"><xsl:value-of select="$sortby"/></div>
                <div id="ae_resutls_sortorder"><xsl:value-of select="$sortorder"/></div>
            </td>
        </tr>
        <xsl:call-template name="ae-sort-experiments">
            <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
            <xsl:with-param name="pFrom" select="$vFrom"/>
            <xsl:with-param name="pTo" select="$vTo"/>
            <xsl:with-param name="pSortBy" select="$sortby"/>
            <xsl:with-param name="pSortOrder" select="$sortorder"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <tr>
                <td><div class="ae_results_cell"><xsl:apply-templates select="accession" mode="highlight" /></div></td>
                <td><div class="ae_results_cell"><xsl:apply-templates select="name" mode="highlight" /></div></td>
                <td class="ae_results_td_right">
                    <div class="ae_results_cell"><xsl:apply-templates select="hybs" mode="highlight" /></div>
                </td>
                <td><div class="ae_results_cell">
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="." mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                </div></td>
                <td><div class="ae_results_cell"><xsl:apply-templates select="releasedate" mode="highlight" /></div></td>
                <td class="ae_results_td_center">
                    <div class="ae_results_cell">
                        <xsl:choose>
                            <xsl:when test="files/fgem/@count>0"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Processed data ({files/fgem/@count})"/></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td class="ae_results_td_center">
                    <div class="ae_results_cell">
                        <xsl:choose>
                            <xsl:when test="files/raw/@celcount>0"><img src="assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Affy data ({files/raw/@celcount})"/></xsl:when>
                            <xsl:when test="files/raw/@count>0"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Raw data ({files/raw/@count})"/></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td><div class="ae_results_cell">&#187; AE</div></td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*" mode="highlight">
        <xsl:variable name="markedtext" select="helper:markKeywords(text(),$keywords,$wholewords)"/>
        <xsl:call-template name="add_highlight_element">
            <xsl:with-param name="text" select="$markedtext"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="add_highlight_element">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text,'|*') and contains($text,'*|')">
                <xsl:value-of select="substring-before($text,'|*')"/>
                <span class="ae_text_highlight"><xsl:value-of select="substring-after(substring-before($text,'*|'),'|*')"/></span>
                <xsl:call-template name="add_highlight_element">
                    <xsl:with-param name="text" select="substring-after($text,'*|')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
