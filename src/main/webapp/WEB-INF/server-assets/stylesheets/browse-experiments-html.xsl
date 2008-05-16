<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
                version="1.0">

    <xsl:param name="from">1</xsl:param>
    <xsl:param name="to">50</xsl:param>
    <xsl:param name="perpage">50</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <func:function name="ae:filter-experiments">
        <xsl:choose>
            <xsl:when test="helper:testRegexp($keywords,'^E-.+-\d+$','i')">
                <xsl:variable name="queried_accnum" select="helper:toUpperCase(string($keywords))" />
                <func:result select="experiment[accession/text()=$queried_accnum or secondaryaccession/text()=$queried_accnum]"/>
            </xsl:when>
            <xsl:when test="$array!='' and $species!='' and $keywords!=''">
                <func:result select="experiment[contains(species/text(),$species)][arraydesign/id/text()=$array][helper:testKeywords(.,$keywords,$wholewords)]"/>
            </xsl:when>
            <xsl:when test="$array!='' and $species='' and $keywords=''">
                <func:result select="experiment[arraydesign/id/text()=$array]"/>
            </xsl:when>
            <xsl:when test="$array='' and $species!='' and $keywords=''">
                <func:result select="experiment[contains(species/text(),$species)]"/>
            </xsl:when>
            <xsl:when test="$array='' and $species='' and $keywords!=''">
                <func:result select="experiment[helper:testKeywords(.,$keywords,$wholewords)]"/>
            </xsl:when>
            <xsl:when test="$array!='' and $species!='' and $keywords=''">
                <func:result select="experiment[contains(species/text(),$species)][arraydesign/id/text()=$array]"/>
            </xsl:when>
            <xsl:when test="$array='' and $species!='' and $keywords!=''">
                <func:result select="experiment[contains(species/text(),$species)][helper:testKeywords(.,$keywords,$wholewords)]"/>
            </xsl:when>
            <xsl:when test="$array!='' and $species='' and $keywords!=''">
                <func:result select="experiment[arraydesign/id/text()=$array][helper:testKeywords(.,$keywords,$wholewords)]"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result select="experiment"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="filtered_experiments" select="ae:filter-experiments()"/>
        <xsl:variable name="TOTAL" select="count($filtered_experiments)"/>
        <xsl:variable name="total_samples" select="sum($filtered_experiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="total_hybs" select="sum($filtered_experiments[hybs/text()>0]/hybs/text())"/>

        <xsl:variable name="FROM" select="$from"/>
        <xsl:variable name="TO">
            <xsl:choose>
                <xsl:when test="$to &gt; $TOTAL"><xsl:value-of select="$TOTAL"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <helper:logInfo select="Query for '{$keywords}' filtered {$TOTAL} experiments. Will output from {$FROM} to {$TO}."/>
        <tr id="ae_results_summary_info">
            <td colspan="8">
                <div id="ae_resutls_total"><xsl:value-of select="$TOTAL"/></div>
                <div id="ae_resutls_total_samples"><xsl:value-of select="$total_samples"/></div>
                <div id="ae_resutls_total_hybs"><xsl:value-of select="$total_hybs"/></div>
                <div id="ae_resutls_from"><xsl:value-of select="$FROM"/></div>
                <div id="ae_resutls_to"><xsl:value-of select="$TO"/></div>
            </td>
        </tr>
        <xsl:choose>
            <xsl:when test="$sortby='accession'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort select="substring(accession/text(),3,4)" order="{$sortorder}"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" data-type="number" order="{$sortorder}"/>
                    <!-- sort by number -->
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='hybs'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort select="@hybs" data-type="number" order="{$sortorder}"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='releasedate'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort order="{$sortorder}" select="substring-before(releasedate/text(),'-')" data-type="number"/>
                    <!-- year -->
                    <xsl:sort order="{$sortorder}" select="substring-before(substring-after(releasedate/text(),'-'),'-')"
                              data-type="number"/>
                    <!-- month -->
                    <xsl:sort order="{$sortorder}" select="substring-after(substring-after(releasedate/text(),'-'),'-')"
                              data-type="number"/>
                    <!-- day -->
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='species'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort order="{$sortorder}" select="species[1]"/>
                    <xsl:sort order="{$sortorder}" select="species[2]"/>
                    <xsl:sort order="{$sortorder}" select="species[2]"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='type'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort order="{$sortorder}" select="experimentdesign[1]"/>
                    <xsl:sort order="{$sortorder}" select="experimentdesign[2]"/>
                    <xsl:sort order="{$sortorder}" select="experimentdesign[3]"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='fgem'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort order="{$sortorder}" select="fgem-count" data-type="number"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sortby='raw'">
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort order="{$sortorder}" select="cel-count" data-type="number"/>
                    <xsl:sort order="{$sortorder}"
                              select="raw-count>0 and cel-count=0 and count(bioassaydatagroup[arraydesignprovider/text()='AFFY'])=0"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$filtered_experiments">
                    <xsl:sort select="*[name()=$sortby]" order="{$sortorder}"/>
                    <xsl:with-param name="FROM" select="$FROM"/>
                    <xsl:with-param name="TO" select="$TO"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="FROM"/>
        <xsl:param name="TO"/>
        <xsl:if test="position() &gt;= $FROM and position() &lt;= $TO">
            <tr>
                <td><div class="ae_results_cell"><xsl:apply-templates select="accession" mode="highlight" /></div></td>
                <td><div class="ae_results_cell"><xsl:apply-templates select="name" mode="highlight" /></div></td>
                <td class="ae_results_td_right">
                    <div class="ae_results_cell"><xsl:apply-templates select="hybs" mode="highlight" /></div>
                </td>
                <td><div class="ae_results_cell">
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="node()" mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                </div></td>
                <td><div class="ae_results_cell"><xsl:apply-templates select="releasedate" mode="highlight" /></div></td>
                <td class="ae_results_td_center">
                    <div class="ae_results_cell">
                        <xsl:choose>
                            <xsl:when test="files/fgem/@count>0"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Processed data ({files/fgem/@count})"/></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td class="ae_results_td_center">
                    <div class="ae_results_cell">
                        <xsl:choose>
                            <xsl:when test="files/raw/@count>0"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Raw data ({files/raw/@count})"/></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td><div class="ae_results_cell">&#187; AE</div></td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*" mode="highlight">
        <xsl:variable name="markedtext" select="helper:markKeywords(text(),$keywords,$wholewords)"/>
        <xsl:call-template name="add_highlight_element">
            <xsl:with-param name="text" select="$markedtext"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="add_highlight_element">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text,'|*') and contains($text,'*|')">
                <xsl:value-of select="substring-before($text,'|*')"/>
                <span class="ae_text_highlight"><xsl:value-of select="substring-after(substring-before($text,'*|'),'|*')"/></span>
                <xsl:call-template name="add_highlight_element">
                    <xsl:with-param name="text" select="substring-after($text,'*|')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
