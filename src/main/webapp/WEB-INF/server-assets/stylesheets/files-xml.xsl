<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.AESaxonExtension"
                extension-element-prefixes="ae"
                exclude-result-prefixes="ae"
                version="1.0">

    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>
    <xsl:param name="exptype"/>
    <xsl:param name="inatlas"/>
    <xsl:param name="userid"/>

    <!-- dynamically set by QueryServlet: host name (as seen from client) and base context path of webapp -->
    <xsl:param name="host"/>
    <xsl:param name="basepath"/>

    <xsl:variable name="vBaseUrl">http://<xsl:value-of select="$host"/><xsl:value-of select="$basepath"/></xsl:variable>

    <xsl:output omit-xml-declaration="yes" method="xml" indent="no"/>

    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <!-- TODO
        <helper:logInfo select="[experiments-xml] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}]"/>
        <helper:logInfo select="[experiments-xml] Sort by: [{$sortby}], [{$sortorder}]"/>
        -->
        <xsl:variable name="vFilteredExperiments" select="experiment[ae:testExperiment($userid, $keywords, $wholewords, $species, $array, $exptype, $inatlas)]"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <!-- TODO
        <helper:logInfo select="[experiments-xml] Query filtered [{$vTotal}] experiments."/>
        -->
        <files version="1.1" revision="080925"
                     total-experiments="{$vTotal}">
            <xsl:call-template name="ae-sort-experiments">
                <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                <xsl:with-param name="pFrom"/>
                <xsl:with-param name="pTo"/>
                <xsl:with-param name="pSortBy" select="$sortby"/>
                <xsl:with-param name="pSortOrder" select="$sortorder"/>
            </xsl:call-template>
        </files>
    </xsl:template>

    <xsl:template match="experiment">
        <experiment>
            <xsl:apply-templates select="accession|file" mode="copy"/>
        </experiment>
    </xsl:template>

    <xsl:template match="accession" mode="copy">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="file" mode="copy">
        <xsl:element name="file">
            <xsl:for-each select="*[name() != 'relativepath']">
                <xsl:copy-of select="."/>
            </xsl:for-each>
            <xsl:element name="url">
                <xsl:value-of select="$vBaseUrl"/>/<xsl:value-of select="relativepath"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>