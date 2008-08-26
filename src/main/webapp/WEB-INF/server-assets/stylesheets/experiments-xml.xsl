<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">

    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>
    <xsl:param name="exptype"/>
    <xsl:param name="inatlas"/>

    <xsl:output omit-xml-declaration="yes" method="xml" indent="no"/>
    
    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logInfo select="[experiments-xml] Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}]"/>
        <helper:logInfo select="[experiments-xml] Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array,$exptype,$inatlas)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>

        <helper:logInfo select="[experiments-xml] Query filtered {$vTotal} experiments."/>

        <experiments version="1.1"
                     total="{$vTotal}"
                     total-samples="{sum($vFilteredExperiments[samples/text()>0]/samples/text())}"
                     total-assays="{sum($vFilteredExperiments[assays/text()>0]/assays/text())}">
            <xsl:call-template name="ae-sort-experiments">
                <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                <xsl:with-param name="pSortBy" select="$sortby"/>
                <xsl:with-param name="pSortOrder" select="$sortorder"/>
            </xsl:call-template>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <experiment><xsl:copy-of select="*"/></experiment>
    </xsl:template>

</xsl:stylesheet>