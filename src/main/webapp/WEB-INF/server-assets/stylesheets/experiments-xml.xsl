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
    <xsl:param name="userid"/>

    <!-- dynamically set by QueryServlet: host name (as seen from client) and base context path of webapp -->
    <xsl:param name="host"/>
    <xsl:param name="basepath"/>

    <xsl:variable name="vBaseUrl">http://<xsl:value-of select="$host"/><xsl:value-of select="$basepath"/></xsl:variable>

    <xsl:output omit-xml-declaration="yes" method="xml" indent="no"/>
    
    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logInfo select="[experiments-xml] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}]"/>
        <helper:logInfo select="[experiments-xml] Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($userid,$keywords,$wholewords,$species,$array,$exptype,$inatlas)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>

        <helper:logInfo select="[experiments-xml] Query filtered [{$vTotal}] experiments."/>

        <experiments version="1.1" revision="080925"
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
        <experiment>
            <xsl:copy-of select="*[not(name() = 'user') and not(name() = 'file') and not(name() = 'arraydesign')]"/>
            <xsl:for-each select="arraydesign">
                <arraydesign>
                    <xsl:copy-of select="*[not(name() = 'file')]"/>
                </arraydesign>
            </xsl:for-each>
            <xsl:if test="count(file)>0">
                <files>
                    <xsl:comment>
This section is deprecated, please use <xsl:value-of select="$vBaseUrl"/>/xml/files webservice to obtain
detailed information on files available for the experiment.

For more information, please go to:

    http://www.ebi.ac.uk/microarray/doc/help/programmatic_access.html                  
                    </xsl:comment>
                    <xsl:if test="count(file[kind='raw'])>0">
                        <raw name="{accession}.raw.zip"
                             count="{sum(bioassaydatagroup[isderived = '0']/bioassays)}"
                             celcount="{sum(bioassaydatagroup[isderived = '0'][contains(dataformat, 'CEL')]/bioassays)}"/>
                    </xsl:if>
                    <xsl:if test="count(file[kind='fgem'])>0">
                        <fgem name="{accession}.processed.zip"
                              count="{sum(bioassaydatagroup[isderived = '1']/bioassays)}"/>
                    </xsl:if>
                    <xsl:if test="count(file[kind='idf'])>0">
                        <idf name="{accession}.idf.txt"/>
                    </xsl:if>
                    <xsl:if test="count(file[kind='sdrf'])>0">
                        <sdrf name="{accession}.sdrf.txt"/>
                    </xsl:if>
                    <xsl:if test="count(file[kind='biosamples'])>0">
                        <biosamples>
                            <xsl:if test="count(file[kind='biosamples' and extension='png'])>0">
                                <png name="{accession}.biosamples.png"/>
                            </xsl:if>
                            <xsl:if test="count(file[kind='biosamples' and extension='svg'])>0">
                                <svg name="{accession}.biosamples.svg"/>
                            </xsl:if>
                        </biosamples>
                    </xsl:if>
                </files>
            </xsl:if>
        </experiment>
    </xsl:template>

</xsl:stylesheet>