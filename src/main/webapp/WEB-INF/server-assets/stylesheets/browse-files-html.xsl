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
    <xsl:param name="kind"/>
    <xsl:param name="userid"/>

    <!-- dynamically set by QueryServlet: host name (as seen from client) and base context path of webapp -->
    <xsl:param name="host"/>
    <xsl:param name="basepath"/>

    <xsl:variable name="vBaseUrl">http://<xsl:value-of select="$host"/><xsl:value-of select="$basepath"/></xsl:variable>   
    
    <xsl:variable name="vAccession" select="helper:toUpperCase($accession)"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-html-page.xsl"/>

    <xsl:template match="/experiments">
        <html lang="en">
            <xsl:call-template name="page-header">
                <xsl:with-param name="pTitle">ArrayExpress Archive Files - <xsl:value-of select="$vAccession"/></xsl:with-param>
                <xsl:with-param name="pExtraCode">
                    <link rel="stylesheet" href="{$basepath}/assets/stylesheets/ae_common.css" type="text/css"/>
                    <link rel="stylesheet" href="{$basepath}/assets/stylesheets/ae_files.css" type="text/css"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="page-body"/>
        </html>
    </xsl:template>

    <xsl:template name="ae-contents">
        <helper:logInfo select="[browse-files-html] Parameters: accession [{$vAccession}], kind [{$kind}], userid [{$userid}]"/>

        <xsl:variable name="vExperiment" select="experiment[accession=$vAccession]"/>
        <div class="ae_left_container_100pc assign_font">
            <div id="ae_files_content">
                <xsl:choose>
                    <xsl:when test="$vExperiment">
                        <xsl:choose>
                            <xsl:when test="helper:isExperimentAccessible($vAccession, $userid)">
                                <div class="ae_accession">Experiment <xsl:value-of select="$vAccession"/></div>
                                <div class="ae_title"><xsl:value-of select="$vExperiment/name"/></div>
                                <xsl:call-template name="files-for-accession">
                                    <xsl:with-param name="pAccession" select="$vAccession"/>
                                </xsl:call-template>
                                <xsl:if test="($kind='raw' or $kind='fgem') and count($vExperiment/file[kind=$kind])>1">
                                    <div class="ae_comment">Note: Due to the large amount of data there are multiple archive files for download.</div>
                                </xsl:if>

                                <xsl:if test="$kind='' or $kind='all'">
                                    <xsl:for-each select="$vExperiment/arraydesign">
                                        <xsl:sort select="accession" order="ascending"/>
                                        <div class="ae_accession">Array Design <xsl:value-of select="accession"/></div>
                                        <div class="ae_title"><xsl:value-of select="name"/></div>
                                        <xsl:call-template name="files-for-accession">
                                            <xsl:with-param name="pAccession" select="accession"/>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <div id="ae_infotext">The access to experiment data for <xsl:value-of select="$vAccession"/> is restricted.</div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <div id="ae_infotext">The experiment with accession <xsl:value-of select="$vAccession"/> is not present in the archive.</div>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="files-for-accession">
        <xsl:param name="pAccession"/>
        <xsl:variable name="vFiles" select="helper:getFilesForAccession($pAccession)"/>
        <xsl:variable name="vKind"><xsl:if test="$kind!='all'"><xsl:value-of select="$kind"/></xsl:if></xsl:variable>

        <table class="ae_files_table" border="0" cellpadding="0" cellspacing="0">
            <tbody>
                <xsl:if test="not($vFiles/file[$vKind='' or @kind=$vKind])">
                    <tr><td class="td_all" colspan="3"><div>No files</div></td></tr>
                </xsl:if>
                <xsl:for-each select="$vFiles/file[$vKind='' or @kind=$vKind]">
                    <xsl:sort select="contains(helper:toUpperCase(@name), 'README')" order="descending"/>
                    <xsl:sort select="@kind='raw' or @kind='fgem'" order="descending"/>
                    <xsl:sort select="@kind='adf' or @kind='idf' or @kind='sdrf'" order="descending"/>
                    <xsl:sort select="@name" order="ascending"/>
                    <tr>
                        <td class="td_name"><a href="{$vBaseUrl}/files/{$pAccession}/{@name}"><xsl:value-of select="@name"/></a></td>
                        <td class="td_size"><xsl:value-of select="helper:fileSizeToString(@size)"/></td>
                        <td class="td_date"><xsl:value-of select="@lastmodified"/></td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    
</xsl:stylesheet>
