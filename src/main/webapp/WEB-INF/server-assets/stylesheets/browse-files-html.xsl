<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
                version="1.0">

    <xsl:param name="accession"/>
    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-html-page.xsl"/>

    <xsl:template match="/experiments">
        <html lang="en">
            <xsl:call-template name="page-header">
                <xsl:with-param name="pTitle">ArrayExpress Files</xsl:with-param>
                <xsl:with-param name="pExtraCode">
                    <link rel="stylesheet" href="assets/stylesheets/ae_browse_noajax.css" type="text/css"/> 
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="page-body"/>
        </html>
    </xsl:template>

    <xsl:template name="ae-contents">
        <helper:logInfo select="[browse-files-html] Parameters: accession [{$accession}]"/>
        <xsl:variable name="vAccession" select="helper:toUpperCase($accession)"/>
        <xsl:variable name="vSelectedExperiment" select="experiment[accession/text()=$vAccession]"/>
        <helper:logInfo select="[browse-files-html] Query filtered {count($vSelectedExperiment)} experiments."/>


        <div class="ae_centered_container_100pc assign_font">
            <xsl:choose>
                <xsl:when test="count($vSelectedExperiment)&gt;0">
                    <xsl:apply-templates select="$vSelectedExperiment"/>
                </xsl:when>
                <xsl:otherwise>The experiment is not in the database</xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:variable name="files" select="helper:getFilesForExperiment(accession)"/>
        <xsl:for-each select="$files">
            <div><xsl:value-of select="@name"/> - <xsl:value-of select="@size"/> - <xsl:value-of select="@lastmodified"/></div>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
