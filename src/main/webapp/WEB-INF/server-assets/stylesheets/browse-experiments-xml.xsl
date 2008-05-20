<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">

    <xsl:param name="from">1</xsl:param>
    <xsl:param name="to">50</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:output omit-xml-declaration="yes" method="xml" indent="no"/>
    
    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>

        <xsl:variable name="vFrom" select="$from"/>
        <xsl:variable name="vTo">
            <xsl:choose>
                <xsl:when test="$to &gt; $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <helper:logInfo select="Query for '{$keywords}' filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>

        <experiments from="{$vFrom}" to="{$vTo}" total="{$vTotal}"
                     total-samples="{sum($vFilteredExperiments[samples/text()>0]/samples/text())}"
                     total-hybs="{sum($vFilteredExperiments[hybs/text()>0]/hybs/text())}">
            <xsl:call-template name="ae-sort-experiments">
                <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                <xsl:with-param name="pFrom" select="$vFrom"/>
                <xsl:with-param name="pTo" select="$vTo"/>
                <xsl:with-param name="pSortBy" select="$sortby"/>
                <xsl:with-param name="pSortOrder" select="$sortorder"/>
            </xsl:call-template>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>