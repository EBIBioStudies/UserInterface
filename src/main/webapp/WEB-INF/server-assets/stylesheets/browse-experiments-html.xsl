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
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="vTotalHybs" select="sum($vFilteredExperiments[hybs/text()>0]/hybs/text())"/>

        <xsl:variable name="vFrom" select="$from"/>
        <xsl:variable name="vTo">
            <xsl:choose>
                <xsl:when test="$to &gt; $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$to"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <helper:logInfo select="Query for '{$keywords}' filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>
        <tr id="ae_results_summary_info">
            <td colspan="8">
                <div id="ae_resutls_total"><xsl:value-of select="$vTotal"/></div>
                <div id="ae_resutls_total_samples"><xsl:value-of select="$vTotalSamples"/></div>
                <div id="ae_resutls_total_hybs"><xsl:value-of select="$vTotalHybs"/></div>
                <div id="ae_resutls_from"><xsl:value-of select="$vFrom"/></div>
                <div id="ae_resutls_to"><xsl:value-of select="$vTo"/></div>
                <div id="ae_resutls_sortby"><xsl:value-of select="$sortby"/></div>
                <div id="ae_resutls_sortorder"><xsl:value-of select="$sortorder"/></div>
            </td>
        </tr>
        <xsl:call-template name="ae-sort-experiments">
            <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
            <xsl:with-param name="pFrom" select="$vFrom"/>
            <xsl:with-param name="pTo" select="$vTo"/>
            <xsl:with-param name="pSortBy" select="$sortby"/>
            <xsl:with-param name="pSortOrder" select="$sortorder"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:variable name="vExpId" select="id"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <tr class="ae_results_main_row">
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
                            <xsl:when test="files/raw/@celcount>0"><img src="assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Affy data ({files/raw/@celcount})"/></xsl:when>
                            <xsl:when test="files/raw/@count>0"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Raw data ({files/raw/@count})"/></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td><div class="ae_results_cell">&#187; AE</div></td>
            </tr>
            <tr id="{$vExpId}_desc" class="ae_results_ext_row" style="">
                <td colspan="8">
                    <dl>
                        <dt>Title:</dt>
                        <dd id="{$vExpId}_name">
                            <xsl:apply-templates select="name" mode="highlight" />
                        </dd>

                        <xsl:if test="count(secondaryaccession/text())&gt;0">
                            <dt>Secondary&#160;accession(s):
                            </dt>
                            <dd id="{$vExpId}_secondary_accession">
                                <xsl:for-each select="secondaryaccession">
                                    <xsl:choose>
                                        <xsl:when test="string-length(text())=0"/>
                                        <xsl:when test="substring(text(), 1, 3)='GSE' or substring(text(), 1, 3)='GDS'">
                                            <a href="http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc={text()}"
                                               target="_blank" title="Opens in a new window">&#187; GEO <xsl:apply-templates select="node()" mode="highlight" /></a>
                                        </xsl:when>
                                        <xsl:when test="substring(text(), 1, 2)='E-' and substring(text(), 7, 1)='-'">
                                            <a href="result?queryFor=Experiment&amp;eAccession={text()}"
                                               target="_blank" title="Opens in a new window">&#187; <xsl:apply-templates select="node()" mode="highlight" /></a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="node()" mode="highlight" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="position()!=last() and string-length(text())&gt;0">, </xsl:if>
                                </xsl:for-each>
                            </dd>
                        </xsl:if>

                        <xsl:if test="miamescores">
                            <dt>MIAME&#160;score:</dt>
                            <dd>
                                <strong><xsl:value-of select="miamescores/overallscore"/></strong>
                                &#160;(array:&#160;<xsl:value-of select="miamescores/reportersequencescore"/>,
                                protocols:&#160;<xsl:value-of select="miamescores/protocolscore"/>,
                                factors:&#160;<xsl:value-of select="miamescores/factorvaluescore"/>,
                                raw data:&#160;<xsl:value-of select="miamescores/measuredbioassaydatascore"/>,
                                processed data:&#160;<xsl:value-of select="miamescores/derivedbioassaydatascore"/>)
                            </dd>
                        </xsl:if>

                        <dt>Sample&#160;annotation:</dt>
                        <dd>
                            <xsl:if test="files/twocolumns">
                                <a href="{files/twocolumns/@url}"
                                    target="_blank" title="Opens in a new window">&#187; Tab-delimited spreadsheet</a>
                                </xsl:if>
                            <xsl:if test="not (files/twocolumns)">Data is not yet available</xsl:if>
                        </dd>

                        <xsl:if test="count(arraydesign)&gt;0">
                            <dt>Array(s):</dt>
                            <dd>
                                <div id="{$vExpId}_array">
                                    <xsl:for-each select="arraydesign">
                                        <xsl:apply-templates select="name" mode="highlight" />
                                        (<a href="result?queryFor=PhysicalArrayDesign&amp;aAccession={accession/text()}"
                                            target="_blank" title="Opens in a new window">&#187; <xsl:apply-templates select="accession" mode="highlight" />
                                        </a>)<xsl:if test="position()!=last()">, </xsl:if>
                                    </xsl:for-each>
                                </div>
                            </dd>
                        </xsl:if>
                        <dt>Downloads:</dt>
                        <dd>
                            <a href="ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/{substring(accession,3,4)}/{accession}"
                               target="_blank" title="Opens in a new window">&#187; FTP server direct link...</a>
                            <xsl:if test="accession!='E-TABM-185'">
                                <br/>
                                <a href="dataselection?expid={@vExpId}"
                                   target="_blank" title="Opens in a new window">&#187; View detailed data retrieval page...</a>
                            </xsl:if>
                        </dd>

                        <xsl:if test="accession!='E-TABM-185'">
                            <dt>Experiment&#160;design:</dt>
                            <dd><xsl:if test="files/biosamples/png">
                                    <a href="{files/biosamples/png/@url}"
                                       target="_blank" title="Opens in a new window">&#187; PNG</a>
                                    <xsl:if test="files/biosamples/svg">, </xsl:if>
                                </xsl:if>
                                <xsl:if test="files/biosamples/svg">
                                    <a href="{files/biosamples/svg/@url}"
                                       target="_blank" title="Opens in a new window">&#187; SVG</a>
                                </xsl:if>
                                <xsl:if test="not (files/biosamples/png or files/biosamples/svg)">Data is not yet available</xsl:if>
                            </dd>
                        </xsl:if>
<!--
                        <dt>Protocols:</dt>
                        <dd>
                            <a href="details?class=MAGE.Experiment_protocols&amp;criteria=Experiment%3D{@id}&amp;contextClass=MAGE.Protocol&amp;templateName=Protocol.vm"
                               class="httplink" target="_blank" title="Opens in a new window">&#187; Experimental protocols</a>
                        </dd>

                        <dt>Citation:</dt>
                        <dd id="{$exp_id}-full-publication">
                            <xsl:apply-templates select="bibliography"/>
                        </dd>

                        <dt>Detailed sample&#160;annotation:</dt>
                        <dd><xsl:if test="@sdrf">
                                <a href="{@sdrf}"
                                    class="httplink" target="_blank" title="Opens in a new window">&#187; Tab-delimited spreadsheet</a>
                            </xsl:if>
                            <xsl:if test="not (@sdrf)">Data is not yet available</xsl:if>
                        </dd>

                        <xsl:if test="count(providers/provider[@role!='data_coder'])&gt;0">
                            <dt>Contact(s):</dt>
                            <dd id="{$exp_id}-full-contact">
                                <xsl:apply-templates select="providers"/>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(experimentdesigns/*)&gt;0">
                            <dt>Design&#160;type(s):</dt>
                            <dd id="{$exp_id}-full-designtypes">
                                <xsl:for-each select="experimentdesigns/*">
                                    <xsl:value-of select="@type"/><xsl:if test="position()!=last()">, </xsl:if>
                                </xsl:for-each>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(description[text()!='' and not(contains(.,'Generated description'))])&gt;0">
                            <dt>Description:</dt>
                            <dd id="{$exp_id}-full-description">
                                <xsl:for-each select="description[text()!='' and not(contains(.,'Generated description'))]">
                                    <xsl:call-template name="break"/>
                                </xsl:for-each>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(factorvalues/factorvalue/@*)&gt;0">
                            <dt>Factor values:</dt>
                            <dd id="{$exp_id}-efvs" style="padding: 0em 0.2em 0.2em 0.2em">
                                <dl class="table-display-attrs">
                                    <dt style="font-weight:bold">Factor name</dt>
                                    <dd style="font-weight:bold">Factor value</dd>
                                    <xsl:for-each select="factorvalues/factorvalue">
                                        <dt>
                                            <xsl:value-of select="@FACTORNAME"/>
                                        </dt>
                                        <dd>
                                            <xsl:value-of select="@FV_MEASUREMENT"/>
                                            <xsl:value-of select="@FV_OE"/>
                                        </dd>
                                    </xsl:for-each>
                                </dl>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(sampleattributes/sampleattribute/@*)&gt;0">
                            <dt>Sample attributes:</dt>
                            <dd id="{$exp_id}-samattrs" style="padding: 0em 0.2em 0.2em 0.2em">
                                <dl class="table-display-attrs">
                                    <dt style="font-weight:bold">Attribute name</dt>
                                    <dd style="font-weight:bold">Attribute value</dd>
                                    <xsl:for-each select="sampleattributes/sampleattribute">
                                        <dt>
                                            <xsl:value-of select="@CATEGORY"/>
                                        </dt>
                                        <dd>
                                            <xsl:value-of select="@VALUE"/>
                                        </dd>
                                    </xsl:for-each>
                                </dl>
                            </dd>
                        </xsl:if> -->
                    </dl>
                </td>
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
