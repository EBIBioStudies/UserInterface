<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
                version="1.0">

    <xsl:param name="page">1</xsl:param>
    <xsl:param name="pagesize">50</xsl:param>
    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:param name="detailedview"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logDebug select="Parameters: keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], detailedview [{$detailedview}]"/>
        <helper:logDebug select="Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="vTotalAssays" select="sum($vFilteredExperiments[assays/text()>0]/assays/text())"/>

        <xsl:variable name="vFrom">
            <xsl:choose>
                <xsl:when test="$page &gt; 0"><xsl:value-of select="1 + ( $page - 1 ) * $pagesize"/></xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="vTo">
            <xsl:choose>
                <xsl:when test="( $vFrom + $pagesize - 1 ) &gt; $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$vFrom + $pagesize - 1"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="vDetailedViewExtStyle"><xsl:if test="not($detailedview)">display:none</xsl:if></xsl:variable>
        <xsl:variable name="vDetailedViewMainClass">ae_results_tr_main<xsl:if test="$detailedview"> tr_main_expanded</xsl:if></xsl:variable>

        <helper:logInfo select="Query for '{$keywords}' filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>
        <helper:logDebug select="vDetailedViewExtStyle [{$vDetailedViewExtStyle}], vDetailedViewMailClass [{$vDetailedViewMainClass}]"/>
        
        <tr id="ae_results_summary_info">
            <td colspan="8">
                <div id="ae_results_total"><xsl:value-of select="$vTotal"/></div>
                <div id="ae_results_total_samples"><xsl:value-of select="$vTotalSamples"/></div>
                <div id="ae_results_total_assays"><xsl:value-of select="$vTotalAssays"/></div>
                <div id="ae_results_from"><xsl:value-of select="$vFrom"/></div>
                <div id="ae_results_to"><xsl:value-of select="$vTo"/></div>
                <div id="ae_results_page"><xsl:value-of select="$page"/></div>
                <div id="ae_results_pagesize"><xsl:value-of select="$pagesize"/></div>
            </td>
        </tr>
        <xsl:choose>
            <xsl:when test="$vTotal&gt;0">
                <xsl:call-template name="ae-sort-experiments">
                    <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                    <xsl:with-param name="pFrom" select="$vFrom"/>
                    <xsl:with-param name="pTo" select="$vTo"/>
                    <xsl:with-param name="pSortBy" select="$sortby"/>
                    <xsl:with-param name="pSortOrder" select="$sortorder"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$vDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$vDetailedViewExtStyle"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <tr class="ae_results_tr_error">
                    <td colspan="8">
                        <p><strong>The query '<xsl:value-of select="$keywords"/>'<xsl:if test="string-length($species)>0">&#160;<em>and</em> species '<xsl:value-of select="$species"/>'</xsl:if>
                            <xsl:if test="string-length($array)>0">&#160;<em>and</em> array <xsl:value-of select="$array"/></xsl:if>
                            returned no matches.</strong></p>
                        <p>Try shortening the query term e.g. 'embryo' will match embryo, embryoid, embryonic across all annotation fields.</p>
                        <p>Note that '*' is <strong>not</strong> supported as a wild card. More information available at <a href="http://www.ebi.ac.uk/microarray/doc/help/ae_help.html">ArrayExpress Query Help</a>.</p>    
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:param name="pDetailedViewMainClass"/>
        <xsl:param name="pDetailedViewExtStyle"/>
        <xsl:variable name="vExpId" select="id"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <tr id="{$vExpId}_main" class="{$pDetailedViewMainClass}">
                <td><div class="table_row_expand">&#160;</div></td>
                <td><div><xsl:apply-templates select="accession" mode="highlight" /></div></td>
                <td><div><xsl:apply-templates select="name" mode="highlight" /></div></td>
                <td class="align_right">
                    <div><xsl:apply-templates select="assays" mode="highlight" /></div>
                </td>
                <td><div>
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="." mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                </div></td>
                <td><div><xsl:apply-templates select="releasedate" mode="highlight" /></div></td>
                <td class="align_center">
                    <div>
                        <xsl:choose>
                            <xsl:when test="helper:isFileAvailableForDownload(files/fgem/@name)"><a href="{helper:getFileDownloadUrl(files/fgem/@name)}" title="Processed data ({files/fgem/@count})"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Processed data ({files/fgem/@count})"/></a></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td class="align_center">
                    <div>
                        <xsl:choose>
                            <xsl:when test="helper:isFileAvailableForDownload(files/raw/@name) and files/raw/@celcount>0"><a href="{helper:getFileDownloadUrl(files/raw/@name)}" title="Affy data ({files/raw/@count})"><img src="assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Affy data ({files/raw/@count})"/></a></xsl:when>
                            <xsl:when test="helper:isFileAvailableForDownload(files/raw/@name) and files/raw/@celcount=0"><a href="{helper:getFileDownloadUrl(files/raw/@name)}" title="Raw data ({files/raw/@count})"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="Raw data ({files/raw/@count})"/></a></xsl:when>
                            <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
            </tr>
            <tr id="{$vExpId}_ext" class="ae_results_tr_ext" style="{$pDetailedViewExtStyle}">
                <td colspan="8">
                    <dl>
                        <xsl:if test="name">
                            <dt>Title:</dt>
                            <dd>
                                <xsl:apply-templates select="name" mode="highlight" />
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(secondaryaccession/text())&gt;0">
                            <dt>Secondary&#160;accession<xsl:if test="count(secondaryaccession/text())&gt;1">s</xsl:if>:</dt>
                            <dd>
                                <xsl:for-each select="secondaryaccession">
                                    <xsl:choose>
                                        <xsl:when test="string-length(text())=0"/>
                                        <xsl:when test="substring(text(), 1, 3)='GSE' or substring(text(), 1, 3)='GDS'">
                                            <a href="http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc={text()}"
                                               target="_blank" title="Opens in a new window">&#187; GEO <xsl:apply-templates select="." mode="highlight" /></a>
                                        </xsl:when>
                                        <xsl:when test="substring(text(), 1, 2)='E-' and substring(text(), 7, 1)='-'">
                                            <a href="/microarray-as/aer/result?queryFor=Experiment&amp;eAccession={text()}"
                                               target="_blank" title="Opens in a new window">&#187; <xsl:apply-templates select="." mode="highlight" /></a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="." mode="highlight" />
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
                            <xsl:choose>
                                <xsl:when test="helper:isFileAvailableForDownload(files/twocolumns/@name)">
                                    <a href="{helper:getFileDownloadUrl(files/twocolumns/@name)}"
                                        target="_blank" title="Opens in a new window">&#187; Tab-delimited spreadsheet</a>
                                </xsl:when>
                                <xsl:otherwise>Data is not yet available</xsl:otherwise>
                            </xsl:choose>
                        </dd>

                        <xsl:if test="count(arraydesign)&gt;0">
                            <dt>Array<xsl:if test="count(arraydesign)&gt;1">s</xsl:if>:</dt>
                            <dd>
                                <div id="{$vExpId}_array">
                                    <xsl:for-each select="arraydesign">
                                        <xsl:apply-templates select="name" mode="highlight" />
                                        (<a href="/microarray-as/aer/result?queryFor=PhysicalArrayDesign&amp;aAccession={accession/text()}"
                                            target="_blank" title="Opens in a new window">&#187; <xsl:apply-templates select="accession" mode="highlight" />
                                        </a>)<xsl:if test="position()!=last()">, </xsl:if>
                                    </xsl:for-each>
                                </div>
                            </dd>
                        </xsl:if>
                        <dt>Downloads:</dt>
                        <dd>
                            <p>
                                <a href="ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/{substring(accession,3,4)}/{accession}"
                                   target="_blank" title="Opens in a new window">&#187; FTP server direct link...</a>
                            </p>
                            <xsl:if test="accession!='E-TABM-185'">
                                <a href="/microarray-as/aer/dataselection?expid={$vExpId}"
                                   target="_blank" title="Opens in a new window">&#187; View detailed data retrieval page...</a>
                            </xsl:if>
                        </dd>

                        <xsl:if test="accession!='E-TABM-185'">
                            <dt>Experiment&#160;design:</dt>
                            <dd><xsl:if test="helper:isFileAvailableForDownload(files/biosamples/png/@name)">
                                    <a href="{helper:getFileDownloadUrl(files/biosamples/png/@name)}"
                                       target="_blank" title="Opens in a new window">&#187; PNG</a>
                                    <xsl:if test="helper:isFileAvailableForDownload(files/biosamples/svg/@name)">, </xsl:if>
                                </xsl:if>
                                <xsl:if test="helper:isFileAvailableForDownload(files/biosamples/svg/@name)">
                                    <a href="{helper:getFileDownloadUrl(files/biosamples/svg/@name)}"
                                       target="_blank" title="Opens in a new window">&#187; SVG</a>
                                </xsl:if>
                                <xsl:if test="not (helper:isFileAvailableForDownload(files/biosamples/png/@name) or helper:isFileAvailableForDownload(files/biosamples/svg/@name))">Data is not yet available</xsl:if>
                            </dd>
                        </xsl:if>

                        <dt>Protocols:</dt>
                        <dd>
                            <a href="/microarray-as/aer/details?class=MAGE.Experiment_protocols&amp;criteria=Experiment%3D{$vExpId}&amp;contextClass=MAGE.Protocol&amp;templateName=Protocol.vm"
                               target="_blank" title="Opens in a new window">&#187; Experimental protocols</a>
                        </dd>

                        <xsl:if test="count(bibliography/*)&gt;0">
                            <dt>Citation<xsl:if test="count(bibliography/*)&gt;1">s</xsl:if>:</dt>
                            <dd>
                                <xsl:apply-templates select="bibliography" />
                            </dd>
                        </xsl:if>

                        <dt>Detailed sample&#160;annotation:</dt>
                        <dd>
                            <xsl:choose>
                                <xsl:when test="helper:isFileAvailableForDownload(files/sdrf/@name)">
                                    <a href="{helper:getFileDownloadUrl(files/sdrf/@name)}"
                                        target="_blank" title="Opens in a new window">&#187; Tab-delimited spreadsheet</a>
                                </xsl:when>
                                <xsl:otherwise>Data is not yet available</xsl:otherwise>
                            </xsl:choose>
                        </dd>

                        <xsl:if test="count(provider[role!='data_coder'])&gt;0">
                            <dt>Contact<xsl:if test="count(provider[role!='data_coder'])&gt;1">s</xsl:if>:</dt>
                            <dd>
                                <xsl:call-template name="providers"/>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(experimentdesign)&gt;0">
                            <dt>Design&#160;type<xsl:if test="count(experimentdesign)&gt;1">s</xsl:if>:</dt>
                            <dd>
                                <xsl:for-each select="experimentdesign">
                                    <xsl:apply-templates select="." mode="highlight"/><xsl:if test="position()!=last()">, </xsl:if>
                                </xsl:for-each>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(description[text/text()!='' and not(contains(text/text(),'Generated description'))])&gt;0">
                            <dt>Description:</dt>
                            <dd>
                                <xsl:for-each select="description[text/text()!='' and not(contains(text/text(),'Generated description'))]">
                                    <xsl:call-template name="description">
                                        <xsl:with-param name="text" select="text/text()"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(experimentalfactor/name)&gt;0">
                            <dt>Experimental factor<xsl:if test="count(experimentalfactor/name)&gt;1">s</xsl:if>:</dt>
                            <dd class="attrs">
                                <dl>
                                    <dt class="title">Factor name</dt>
                                    <dd class="title">Factor value</dd>
                                    <xsl:for-each select="experimentalfactor">
                                        <dt><xsl:apply-templates select="name" mode="highlight"/></dt>
                                        <dd><xsl:apply-templates select="value" mode="highlight"/></dd>
                                    </xsl:for-each>
                                </dl>
                            </dd>
                        </xsl:if>

                        <xsl:if test="count(sampleattribute/category)&gt;0">
                            <dt>Sample attribute<xsl:if test="count(sampleattribute/category)&gt;1">s</xsl:if>:</dt>
                            <dd class="attrs">
                                <dl>
                                    <dt class="title">Attribute name</dt>
                                    <dd class="title">Attribute value</dd>
                                    <xsl:for-each select="sampleattribute">
                                        <dt><xsl:apply-templates select="category" mode="highlight"/></dt>
                                        <dd><xsl:apply-templates select="value" mode="highlight"/></dd>
                                    </xsl:for-each>
                                </dl>
                            </dd>
                        </xsl:if>
                    </dl>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="bibliography">
        <p>
            <xsl:variable name="publication_title">
                <xsl:if test="authors/text()!=''"><xsl:apply-templates select="authors" mode="highlight"/>. </xsl:if>
                <xsl:if test="title/text()!=''"><xsl:apply-templates select="title" mode="highlight"/>. </xsl:if>
                <xsl:if test="publication/text()!=''"><em><xsl:apply-templates select="publication" mode="highlight"/></em>&#160;</xsl:if>
                <xsl:if test="volume/text()!=''"><xsl:apply-templates select="volume" mode="highlight"/><xsl:if test="issue/text()!=''">(<xsl:apply-templates select="issue" mode="highlight"/>)</xsl:if></xsl:if>
                <xsl:if test="pages/text()!=''">:<xsl:apply-templates select="pages" mode="highlight"/></xsl:if>
                <xsl:if test="year/text()!=''">&#160;(<xsl:apply-templates select="year" mode="highlight"/>)</xsl:if>
                <xsl:if test="publication/text()!=''">.</xsl:if>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="uri[starts-with(., 'http')]">
                    <a href="{uri}"
                       target="_blank" title="Opens in a new window">&#187; <xsl:copy-of select="$publication_title"/></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$publication_title" />
                    <xsl:if test="uri/text()!=''">(<xsl:apply-templates select="uri" mode="highlight"/>)</xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="accession">
                <xsl:if test="number(accession)>0">
                    (<a href="http://www.ncbi.nlm.nih.gov/pubmed/{accession}"
                       target="_blank" title="Opens in a new window">&#187; PubMed <xsl:apply-templates select="accession" mode="highlight"/></a>)
                </xsl:if>
            </xsl:if>
        </p>
    </xsl:template>

    <xsl:template name="providers">
        <xsl:for-each select="provider[not(contact=following-sibling::provider/contact) and role!='data_coder']">
            <xsl:sort select="role='submitter'" order="descending"/>
            <xsl:sort select="contact"/>
            <xsl:apply-templates select="contact" mode="highlight"/><xsl:if test="position()!=last()">, </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="description">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '&lt;br&gt;')">
                <p>
                    <xsl:call-template name="add_highlight_element">
                        <xsl:with-param name="text" select="helper:markKeywords(substring-before($text, '&lt;br&gt;'),$keywords,$wholewords)"/>
                    </xsl:call-template>
                </p>
                <xsl:call-template name="description">
                    <xsl:with-param name="text" select="substring-after($text,'&lt;br&gt;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:call-template name="add_highlight_element">
                        <xsl:with-param name="text" select="helper:markKeywords($text,$keywords,$wholewords)"/>
                    </xsl:call-template>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="highlight">
        <xsl:variable name="vText" select="normalize-space(text())"/>
        <xsl:choose>
            <xsl:when test="string-length($vText)!=0">
                <xsl:variable name="markedtext" select="helper:markKeywords($vText,$keywords,$wholewords)"/>
                <xsl:call-template name="add_highlight_element">
                    <xsl:with-param name="text" select="$markedtext"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>&#160;</xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="add_highlight_element">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text,'&#171;') and contains($text,'&#187;')">
                <xsl:value-of select="substring-before($text,'&#171;')"/>
                <span class="ae_text_highlight"><xsl:value-of select="substring-after(substring-before($text,'&#187;'),'&#171;')"/></span>
                <xsl:call-template name="add_highlight_element">
                    <xsl:with-param name="text" select="substring-after($text,'&#187;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
</xsl:stylesheet>
