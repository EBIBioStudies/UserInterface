<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:regexp="com.linkwerk.util.Regexp"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                exclude-result-prefixes="func ae regexp helper"
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

    <!--
    <func:function name="ae:concat-all">
        <xsl:param name="what"/>
        <xsl:variable name="c">
            <xsl:for-each select="$what/@*">
                <xsl:value-of select="concat(string(.),' ')"/>
            </xsl:for-each>
            <xsl:for-each select="$what/descendant::*">
                <xsl:for-each select="@*">
                    <xsl:value-of select="concat(string(.),' ')"/>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:value-of select="text()"/>
        </xsl:variable>
        <func:result select="$c"/>
    </func:function>
    -->
    <func:function name="ae:get-regexp">
        <xsl:param name="string"/>
        <xsl:variable name="first_keyword_from_string">
            <xsl:choose>
                <xsl:when test="contains($string,' ')">
                    <xsl:value-of select="substring-before($string,' ')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$string"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="regexp">
            <xsl:choose>
                <xsl:when test="$whole_words">\b<xsl:value-of select="$first_keyword_from_string"/>\b</xsl:when>
                <xsl:otherwise><xsl:value-of select="$first_keyword_from_string"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <func:result select="string($regexp)"/>
    </func:function>

    <func:function name="ae:keyword-filter">
        <xsl:param name="nodelist"/>
        <xsl:param name="keywords"/>
        <xsl:choose>
            <xsl:when test="string-length($keywords)>0">
                <func:result select="helper:testRegexp($nodelist,ae:get-regexp($keywords),'i') and ae:keyword-filter($nodelist,substring-after($keywords,' '))"/>
            </xsl:when>
            <xsl:otherwise><func:result select="true()"/></xsl:otherwise>
        </xsl:choose>
    </func:function>

    <func:function name="ae:filter-experiments">
        <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
        <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <xsl:choose>
            <xsl:when test="helper:testRegexp($filter_keyword,'^E-.+-\d+$','i')">
                <xsl:variable name="queried_accnum" select="translate($filter_keyword,$lcletters,$ucletters)" />
                <func:result select="experiment[$queried_accnum=@accnum or $queried_accnum=@secondaryaccession]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism!='' and $filter_keyword!=''">
                <func:result select="experiment[contains(@species,$filter_organism)][.//arraydesign/@id=$filter_array][ae:keyword-filter(.,$filter_keyword)]"/>
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
                <func:result select="experiment[contains(@species,$filter_organism)][ae:keyword-filter(node(),$filter_keyword)]"/>
            </xsl:when>
            <xsl:when test="$filter_array!='' and $filter_organism='' and $filter_keyword!=''">
                <func:result select="experiment[.//arraydesign/@id=$filter_array][ae:keyword-filter(node(),$filter_keyword)]"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result select="experiment"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>

    <xsl:template match="/experiments">
        <xsl:message>Parameters - organism: '<xsl:value-of select="$filter_organism"/>', keywords: '<xsl:value-of select="$filter_keyword"/>' ( whole words: '<xsl:value-of select="$whole_words"/>' ), array: '<xsl:value-of select="$filter_array"/>'</xsl:message>
        <xsl:message>Sort: <xsl:value-of select="$sort_by"/>, <xsl:value-of select="$sort_order"/></xsl:message>

        <xsl:variable name="filtered_experiments" select="ae:filter-experiments()"/>
        <xsl:variable name="TOTAL" select="count($filtered_experiments)"/>

        <xsl:variable name="FROM" select="$from"/>
        <xsl:variable name="TO">
            <xsl:choose>
                <xsl:when test="$to &gt; $TOTAL"><xsl:value-of select="$TOTAL"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:message>Experiments: <xsl:value-of select="count(experiment)"/>, filtered: <xsl:value-of select="$TOTAL"/>, will output from: <xsl:value-of select="$FROM"/> to: <xsl:value-of select="$TO"/></xsl:message>

        <experiments from="{$FROM}" to="{$TO}" total="{$TOTAL}"
                     total-samples="{sum($filtered_experiments[@samples>0]/@samples)}"
                     total-hybs="{sum($filtered_experiments[@hybs>0]/@hybs)}">
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
                        <xsl:sort order="{$sort_order}" select="substring-before(@releasedate,'-')" data-type="number"/>
                        <!-- year -->
                        <xsl:sort order="{$sort_order}" select="substring-before(substring-after(@releasedate,'-'),'-')"
                                  data-type="number"/>
                        <!-- month -->
                        <xsl:sort order="{$sort_order}" select="substring-after(substring-after(@releasedate,'-'),'-')"
                                  data-type="number"/>
                        <!-- day -->
                        <xsl:with-param name="FROM" select="$FROM"/>
                        <xsl:with-param name="TO" select="$TO"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$sort_by='type'">
                    <xsl:message>sorting up...</xsl:message>
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
                    <xsl:message>sorting by<xsl:value-of select="$sort_by"/>,
                        <xsl:value-of select="$sort_order"/>
                    </xsl:message>
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
            <xsl:copy-of select="." />
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
