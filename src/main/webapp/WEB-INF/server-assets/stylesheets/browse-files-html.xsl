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
    <xsl:variable name="vAccession" select="helper:toUpperCase($accession)"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-html-page.xsl"/>

    <xsl:template match="/experiments">
        <html lang="en">
            <xsl:call-template name="page-header">
                <xsl:with-param name="pTitle">ArrayExpress Archive Files - <xsl:value-of select="$vAccession"/></xsl:with-param>
                <xsl:with-param name="pExtraCode">
                    <link rel="stylesheet" href="${interface.application.base.url}/assets/stylesheets/ae_common.css" type="text/css"/>
                    <link rel="stylesheet" href="${interface.application.base.url}/assets/stylesheets/ae_files.css" type="text/css"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="page-body"/>
        </html>
    </xsl:template>

    <xsl:template name="ae-contents">
        <helper:logInfo select="[browse-files-html] Parameters: accession [{$vAccession}], userid [{$userid}]"/>

        <div class="ae_centered_container_100pc assign_font">
            <div id="ae_files_content">
                <xsl:choose>
                    <xsl:when test="experiment[accession=$vAccession]">
                        <xsl:choose>
                            <xsl:when test="helper:isExperimentAccessible($vAccession, $userid)">
                                <div class="ae_accession">Experiment <xsl:value-of select="$vAccession"/></div>
                                <div class="ae_title"><xsl:value-of select="experiment[accession=$vAccession]/name"/></div>
                                <xsl:call-template name="files-for-accession">
                                    <xsl:with-param name="pAccession" select="$vAccession"/>
                                </xsl:call-template>

                                <xsl:for-each select="experiment[accession=$vAccession]/arraydesign">
                                    <xsl:sort select="accession" order="ascending"/>
                                    <div class="ae_accession">Array Design <xsl:value-of select="accession"/></div>
                                    <div class="ae_title"><xsl:value-of select="name"/></div>
                                    <xsl:call-template name="files-for-accession">
                                        <xsl:with-param name="pAccession" select="accession"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <div>The access to experiment data is prohibited.</div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <div>The experiment with the accession does not exist.</div>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="files-for-accession">
        <xsl:param name="pAccession"/>
        <xsl:variable name="vFiles" select="helper:getFilesForAccession($pAccession)"/>
        <table class="ae_files_table" border="0" cellpadding="0" cellspacing="0">
            <tbody>
                <xsl:if test="not($vFiles/file)">
                    <tr><td class="td_center" colspan="3">No files available</td></tr>
                </xsl:if>
                <xsl:for-each select="$vFiles/file">
                    <xsl:sort select="contains(helper:toUpperCase(@name), 'README')" order="descending"/>
                    <xsl:sort select="@kind='adf' or @kind='idf' or @kind='sdrf'" order="descending"/>
                    <xsl:sort select="@kind='raw' or @kind='fgem'" order="descending"/>
                    <xsl:sort select="@name" order="ascending"/>
                    <tr>
                        <td class="td_name"><a href="${interface.application.base.url}/files/{$pAccession}/{@name}"><xsl:value-of select="@name"/></a></td>
                        <td class="td_size"><xsl:value-of select="helper:fileSizeToString(@size)"/></td>
                        <td class="td_date"><xsl:value-of select="@lastmodified"/></td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    
</xsl:stylesheet>
