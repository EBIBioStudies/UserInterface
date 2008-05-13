<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">

    <xsl:param name="from">1</xsl:param>
    <xsl:param name="to">40</xsl:param>
    <xsl:param name="perpage">40</xsl:param>
    <xsl:param name="sort_by">releasedate</xsl:param>
    <xsl:param name="sort_order">descending</xsl:param>

    <xsl:param name="filter_organism"/>
    <xsl:param name="filter_array"/>
    <xsl:param name="filter_keyword"/>
    <xsl:param name="whole_words"/>

    <xsl:output omit-xml-declaration="yes" method="xml" indent="no"/>

    <func:function name="ae:filter-experiments">
        <xsl:choose>
            <xsl:when test="helper:testRegexp($filter_keyword,'^E-.+-\d+$','i')">
                <xsl:variable name="queried_accnum" select="helper:toUpperCase(string($filter_keyword))" />
                <func:result select="experiment[accession/text()=$queried_accnum or secondaryaccession/text()=$queried_accnum]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism!='' and $filter_keyword!=''">
                <func:result select="experiment[contains(species/text(),$filter_organism)][.//arraydesign/@id=$filter_array][helper:testKeywords(.,$filter_keyword,$whole_words)]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism='' and $filter_keyword=''">
                <func:result select="experiment[.//arraydesign/@id=$filter_array]"/>
            </xsl:when>
            <xsl:when test="$filter_array='' and $filter_organism!='' and $filter_keyword=''">
                <func:result select="experiment[contains(@species,$filter_organism)]"/>
            </xsl:when>
            <xsl:when test="$filter_array='' and $filter_organism='' and $filter_keyword!=''">
                <func:result select="experiment[helper:testKeywords(.,$filter_keyword,$whole_words)]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism!='' and $filter_keyword=''">
                <func:result select="experiment[contains(@species,$filter_organism)][.//arraydesign/@id=$filter_array]"/>
            </xsl:when>
            <xsl:when test="$filter_array='' and $filter_organism!='' and $filter_keyword!=''">
                <func:result select="experiment[contains(@species,$filter_organism)][helper:testKeywords(.,$filter_keyword,$whole_words)]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism='' and $filter_keyword!=''">
                <func:result select="experiment[.//arraydesign/@id=$filter_array][helper:testKeywords(.,$filter_keyword,$whole_words)]"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result select="experiment"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: filter_keyword [{$filter_keyword}], whole_words [{$whole_words}], filter_array [{$filter_array}], filter_organism [{$filter_organism}]"/>
        <helper:logDebug select="Sort by: [{$sort_by}], [{$sort_order}]"/>
        <xsl:variable name="filtered_experiments" select="ae:filter-experiments()"/>
        <xsl:variable name="TOTAL" select="count($filtered_experiments)"/>

        <xsl:variable name="FROM" select="$from"/>
        <xsl:variable name="TO">
            <xsl:choose>
                <xsl:when test="$to &gt; $TOTAL"><xsl:value-of select="$TOTAL"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <helper:logInfo select="Query for '{$filter_keyword}' filtered {$TOTAL} experiments. Will output from {$FROM} to {$TO}."/>

        <experiments from="{$FROM}" to="{$TO}" total="{$TOTAL}"
                     total-samples="{sum($filtered_experiments[samples/text()>0]/samples/text())}"
                     total-hybs="{sum($filtered_experiments[hybs/text()>0]/hybs/text())}">
            <xsl:choose>
                <xsl:when test="$sort_by='accnum'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort select="substring(@accnum,3,4)" order="{$sort_order}"/>
                        <!-- sort by experiment 4-letter code -->
                        <xsl:sort select="substring(@accnum,8)" data-type="number" order="{$sort_order}"/>
                        <!-- sort by number -->
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='hybs'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort select="@hybs" data-type="number" order="{$sort_order}"/>
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='releasedate'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort order="{$sort_order}" select="substring-before(releasedate/text(),'-')" data-type="number"/>
                        <!-- year -->
                        <xsl:sort order="{$sort_order}" select="substring-before(substring-after(releasedate/text(),'-'),'-')"
                                  data-type="number"/>
                        <!-- month -->
                        <xsl:sort order="{$sort_order}" select="substring-after(substring-after(releasedate/text(),'-'),'-')"
                                  data-type="number"/>
                        <!-- day -->
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='type'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort order="{$sort_order}" select="experimentdesigns/experimentdesign[1]/@type"/>
                        <xsl:sort order="{$sort_order}" select="experimentdesigns/experimentdesign[2]/@type"/>
                        <xsl:sort order="{$sort_order}" select="experimentdesigns/experimentdesign[3]/@type"/>
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='fgem'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort order="{$sort_order}" select="@fgem-count" data-type="number"/>
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='raw'">
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort order="{$sort_order}" select="@cel-count" data-type="number"/>
                        <xsl:sort order="{$sort_order}"
                                  select="@raw-count>0 and @cel-count=0 and count(.//bioassaydatagroup[@arraydesign='AFFY'])=0"/>
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$filtered_experiments">
                        <xsl:sort select="@*[local-name()=$sort_by]" order="{$sort_order}"/>
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="FROM"/>
        <xsl:param name="TO"/>
        <xsl:if test="position() &gt;= $FROM and position() &lt;= $TO">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
