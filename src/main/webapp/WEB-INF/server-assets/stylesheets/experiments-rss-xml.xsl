<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">


    <xsl:param name="pagesize">25</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:param name="detailedview"/>
    
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="ISO-8859-1"/>

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], detailedview [{$detailedview}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>

        <helper:logInfo select="Query for '{$keywords}' filtered {$vTotal} experiments. Will output first {$pagesize} entries."/>

        <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
            <channel>
                <xsl:variable name="vCurrentDate" select="helper:dateToRfc822()"/>
                <title>
                    <xsl:text>ArrayExpress Experiments</xsl:text>
                    <xsl:if test="string-length($keywords)&gt;0">
                        <xsl:text> matching keywords '</xsl:text>
                        <xsl:value-of select="$keywords"/>
                        <xsl:text>'</xsl:text>
                    </xsl:if>
                    <xsl:if test="string-length($species)&gt;0">
                        <xsl:choose>
                            <xsl:when test="string-length($keywords)&gt;0">
                                <xsl:text> and</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text> matching </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> species '</xsl:text>
                        <xsl:value-of select="$species"/>
                        <xsl:text>'</xsl:text>
                    </xsl:if>
                    <xsl:if test="string-length($array)&gt;0">
                        <xsl:choose>
                            <xsl:when test="(string-length($keywords)&gt;0) or (string-length($species)&gt;0)">
                                <xsl:text> and</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text> matching </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> array id '</xsl:text>
                        <xsl:value-of select="$array"/>
                        <xsl:text>'</xsl:text>
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
                    <xsl:text>http://www.ebi.ac.uk/arrayexpress</xsl:text>
                    <xsl:if test="(string-length($keywords)&gt;0) or (string-length($species)&gt;0) or (string-length($array)&gt;0)">
                        <xsl:text>/browse.html?keywords=</xsl:text>
                        <xsl:value-of select="$keywords"/>
                        <xsl:if test="$wholewords">
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
                <atom:link href="http://www.ebi.ac.uk${interface.application.base.url}/rss/experiments" rel="self" type="application/rss+xml" />
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
                    <xsl:text>http://www.ebi.ac.uk/arrayexpress/experiments/</xsl:text>
                    <xsl:value-of select="accession"/>
                </link>
                <guid>
                    <xsl:attribute name="isPermaLink">true</xsl:attribute>
                    <xsl:text>http://www.ebi.ac.uk/arrayexpress/experiments/</xsl:text>
                    <xsl:value-of select="accession"/>
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
                    <category domain="http://www.ebi.ac.uk/arrayexpress">
                        <xsl:value-of select="."/>
                    </category>
                </xsl:for-each>
                <pubDate>
                    <xsl:value-of select="helper:dateToRfc822(releasedate)"/>
                </pubDate>
            </item>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
