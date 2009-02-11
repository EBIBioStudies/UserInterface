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
    <xsl:param name="userid"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-html-page.xsl"/>

    <xsl:template match="/experiments">
        <html lang="en">
            <xsl:call-template name="page-header">
                <xsl:with-param name="pTitle">ArrayExpress Archive - /files/<xsl:value-of select="$accession"/></xsl:with-param>
                <xsl:with-param name="pExtraCode">
                    <link rel="stylesheet" href="assets/stylesheets/ae_common.css" type="text/css"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="page-body"/>
        </html>
    </xsl:template>

    <xsl:template name="ae-contents">
        <helper:logInfo select="[browse-files-html] Parameters: accession [{$accession}], userid [{$userid}]"/>

        <div class="ae_centered_container_100pc assign_font">
            <xsl:choose>
                <xsl:when test="starts-with(helper:toLowerCase($accession),'e')">
                    <div>Experiment - <xsl:value-of select="experiment[accession=$accession]/name"/></div>
                </xsl:when>
                <xsl:when test="starts-with(helper:toLowerCase($accession),'a')">
                    <div>Array Design - <xsl:value-of select="//arraydesign[accession=$accession][1]/name"/></div>

                </xsl:when>
            </xsl:choose>
            <xsl:for-each select="helper:getFilesForAccession($accession)/file">
                <xsl:sort select="@name" order="ascending"/>
                <div><a href="${interface.application.base.url}/files/{$accession}/{@name}"><xsl:value-of select="@name"/></a> - <xsl:value-of select="@kind"/> - <xsl:value-of select="@size"/> - <xsl:value-of select="@lastmodified"/></div>
            </xsl:for-each>
            <xsl:for-each select="experiment[accession=$accession]/arraydesign">
                <xsl:sort select="accession" order="ascending"/>
                <div>Array Design - <xsl:value-of select="name"/></div>
                <xsl:variable name="vAccession" select="accession"/>
                <xsl:for-each select="helper:getFilesForAccession($vAccession)/file">
                    <xsl:sort select="@name" order="ascending"/>
                    <div><a href="${interface.application.base.url}/files/{$vAccession}/{@name}"><xsl:value-of select="@name"/></a> - <xsl:value-of select="@size"/> - <xsl:value-of select="@lastmodified"/></div>
                </xsl:for-each>
            </xsl:for-each>
        </div>
    </xsl:template>


</xsl:stylesheet>
