<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.AESaxonExtension"
                extension-element-prefixes="ae"
                exclude-result-prefixes="ae"
                version="1.0">

    <xsl:param name="pagesize">25</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>
    <xsl:param name="exptype"/>
    <xsl:param name="inatlas"/>
    <xsl:param name="userid"/>

    <xsl:param name="detailedview"/>

    <!-- dynamically set by QueryServlet: host name (as seen from client) and base context path of webapp -->
    <xsl:param name="host"/>
    <xsl:param name="basepath"/>

    <xsl:variable name="vBaseUrl">http://<xsl:value-of select="$host"/><xsl:value-of select="$basepath"/></xsl:variable>

    <xsl:output omit-xml-declaration="yes" method="xml" encoding="ISO-8859-1"/>

    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <!-- TODO
        <helper:logInfo select="[experiments-rss-xml] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}], detailedview [{$detailedview}]"/>
        <helper:logInfo select="[experiments-rss-xml] Sort by: [{$sortby}], [{$sortorder}]"/>
        -->
        <xsl:variable name="vFilteredExperiments" select="experiment[ae:testExperiment($userid, $keywords, $wholewords, $species, $array, $exptype, $inatlas)]"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>

        <!-- TODO
        <helper:logInfo select="[experiments-rss-xml] Query filtered {$vTotal} experiments. Will output first {$pagesize} entries."/>
        -->
        <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
            <channel>
                <xsl:variable name="vCurrentDate" select="ae:dateToRfc822()"/>
                <title>
                    <xsl:text>ArrayExpress Archive - Experiments</xsl:text>
                    <xsl:variable name="vArrayName" select="//arraydesign[id=$array]/name"/>
                    <xsl:variable name="vQueryDesc" select="ae:describeQuery($keywords,$wholewords,$species,$vArrayName,$exptype,$inatlas)"/>
                    <xsl:if test="string-length($vQueryDesc)>0">
                        <xsl:text> </xsl:text><xsl:value-of select="$vQueryDesc"/>
                    </xsl:if>
                    <xsl:if test="$pagesize &lt; $vTotal">
                        <xsl:text> (first </xsl:text>
                        <xsl:value-of select="$pagesize"/>
                        <xsl:text> of </xsl:text>
                        <xsl:value-of select="$vTotal"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>
                </title>
                <link>
                    <xsl:value-of select="$vBaseUrl"/>
                    <xsl:if test="(string-length($keywords)&gt;0) or (string-length($species)&gt;0) or (string-length($array)&gt;0)">
                        <xsl:text>/browse.html?keywords=</xsl:text>
                        <xsl:value-of select="$keywords"/>
                        <xsl:if test="'true'=$wholewords">
                            <xsl:text>&amp;wholewords=on</xsl:text>
                        </xsl:if>
                        <xsl:text>&amp;species=</xsl:text>
                        <xsl:value-of select="$species"/>
                        <xsl:text>&amp;array=</xsl:text>
                        <xsl:value-of select="$array"/>
                        <xsl:text>&amp;pagesize=</xsl:text>
                        <xsl:value-of select="$pagesize"/>
                    </xsl:if>
               </link>
                <description><xsl:text>ArrayExpress is a public repository for transcriptomics data, which is aimed at storing MIAME- and MINSEQE- compliant data in accordance with MGED recommendations.</xsl:text></description>
                <language><xsl:text>en</xsl:text></language>
                <pubDate><xsl:value-of select="$vCurrentDate"/></pubDate>
                <lastBuildDate><xsl:value-of select="$vCurrentDate"/></lastBuildDate>
                <docs><xsl:text>http://blogs.law.harvard.edu/tech/rss</xsl:text></docs>
                <generator><xsl:text>ArrayExpress</xsl:text></generator>
                <managingEditor><xsl:text>arrayexpress@ebi.ac.uk (ArrayExpress Team)</xsl:text></managingEditor>
                <webMaster><xsl:text>arrayexpress@ebi.ac.uk (ArrayExpress Team)</xsl:text></webMaster>
                <atom:link href="{$vBaseUrl}/rss/experiments" rel="self" type="application/rss+xml" />
                <xsl:call-template name="ae-sort-experiments">
                    <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                    <xsl:with-param name="pFrom" select="1"/>
                    <xsl:with-param name="pTo" select="$pagesize"/>
                    <xsl:with-param name="pSortBy" select="$sortby"/>
                    <xsl:with-param name="pSortOrder" select="$sortorder"/>
                </xsl:call-template>

            </channel>
        </rss>

    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <item>
                <title>
                    <xsl:value-of select="accession"/>
                    <xsl:text> - </xsl:text>
                    <xsl:choose>
                        <xsl:when test="string-length(name/text())&gt;0">
                            <xsl:value-of select="name"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>Untitled experiment</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </title>
                <link>
                    <xsl:value-of select="$vBaseUrl"/>/experiments/<xsl:value-of select="accession"/>
                </link>
                <guid>
                    <xsl:attribute name="isPermaLink">true</xsl:attribute>
                    <xsl:value-of select="$vBaseUrl"/>/experiments/<xsl:value-of select="accession"/>
                </guid>

                <description>
                    <xsl:for-each select="description[contains(text/text(),'Generated description')]">
                        <xsl:value-of select="substring(text/text(),25)"/>
                        <xsl:if test="position()!=last()">
                            <xsl:text>&lt;br/&gt;</xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="(count(description[contains(text/text(),'Generated description')])&gt;0) and (1 = 1)">
                        <xsl:text>&lt;br/&gt;&lt;br/&gt;</xsl:text>
                    </xsl:if>
                    <xsl:for-each select="description[text/text()!='' and not(contains(text/text(),'Generated description'))]">
                        <xsl:copy-of select="text/text()"/>
                        <xsl:if test="position()!=last()">
                            <xsl:text>&lt;br/&gt;</xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </description>
                <xsl:for-each select="experimentdesign">
                    <category domain="{$vBaseUrl}">
                        <xsl:value-of select="."/>
                    </category>
                </xsl:for-each>
                <pubDate>
                    <xsl:value-of select="ae:dateToRfc822(releasedate)"/>
                </pubDate>
            </item>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
