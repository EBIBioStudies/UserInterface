<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
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

    <xsl:param name="detailedview"/>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-html-page.xsl"/>
    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <html lang="en">
            <xsl:call-template name="page-header">
                <xsl:with-param name="pTitle">ArrayExpress Search Results - Printer-friendly</xsl:with-param>
                <xsl:with-param name="pExtraCode">
                    <link rel="stylesheet" href="assets/stylesheets/ae_common.css" type="text/css"/>
                    <link rel="stylesheet" href="assets/stylesheets/ae_browse_printer.css" type="text/css"/>
                    <script src="assets/scripts/jquery-1.3.1.js" type="text/javascript"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="page-body"/>
        </html>
    </xsl:template>

    <xsl:template name="ae-contents">
        <helper:logInfo select="[browse-experiments-printer-html] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}], detailedview [{$detailedview}]"/>
        <helper:logInfo select="[browse-experiments-printer-html] Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($userid,$keywords,$wholewords,$species,$array,$exptype,$inatlas)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="vTotalAssays" select="sum($vFilteredExperiments[assays/text()>0]/assays/text())"/>

        <helper:logInfo select="[browse-experiments-printer-html] Query filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>

        <div class="ae_left_container_100pc assign_font">
            <xsl:choose>
                <xsl:when test="$vTotal&gt;0">
                    <table class="ae_experiments_table" border="0" cellpadding="0" cellspacing="0">
                        <tbody>
                            <xsl:call-template name="ae-sort-experiments">
                                <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                                <xsl:with-param name="pSortBy" select="$sortby"/>
                                <xsl:with-param name="pSortOrder" select="$sortorder"/>
                            </xsl:call-template>
                        </tbody>
                    </table>
                </xsl:when>
                <xsl:otherwise>
                    <div id="ae_infotext">
                        <xsl:choose>
                            <xsl:when test="helper:testRegexp($keywords,'^E-.+-\d+$','i')">
                                <div><strong>The experiment with accession number '<xsl:value-of select="$keywords"/>' is not available.</strong></div>
                                <div>If you believe this is an error, please do not hesitate to drop us a line to <strong>arrayexpress(at)ebi.ac.uk</strong> or use <a href="${interface.application.link.www_domain}/support/" title="EBI Support">EBI Support Feedback</a> form.</div>
                            </xsl:when>
                            <xsl:otherwise>
                                <div><strong>The query '<xsl:value-of select="$keywords"/>'<xsl:if test="string-length($species)>0">&#160;<em>and</em> species '<xsl:value-of select="$species"/>'</xsl:if>
                                    <xsl:if test="string-length($array)>0">&#160;<em>and</em> array '<xsl:value-of select="//arraydesign[id=$array]/name"/>'</xsl:if>
                                    <xsl:if test="string-length($exptype)>0">&#160;<em>and</em> experiment type '<xsl:value-of select="$exptype"/>'</xsl:if>
                                    returned no matches.</strong></div>
                                <div>Try shortening the query term e.g. 'embryo' will match embryo, embryoid, embryonic across all annotation fields.</div>
                                <div>Note that '*' is <strong>not</strong> supported as a wild card. More information available at <a href="http://www.ebi.ac.uk/microarray/doc/help/ae_help.html">ArrayExpress Browser Help</a>.</div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:variable name="vExpId" select="id"/>
        <tr class="">
            <td class="td_accession"><div><xsl:apply-templates select="accession" mode="highlight" /></div></td>
            <td class="td_name"><div><xsl:apply-templates select="name" mode="highlight" />&#160;</div></td>
            <td class="td_assays"><div><xsl:apply-templates select="assays" mode="highlight" />&#160;</div></td>
            <td class="td_species">
                <div>
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="." mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(species) = 0"><xsl:text>&#160;</xsl:text></xsl:if>
                </div>
            </td>
            <td class="td_releasedate"><div><xsl:apply-templates select="releasedate" mode="highlight" />&#160;</div></td>
            <td class="td_processed">
                <div>
                    <xsl:choose>
                        <xsl:when test="file[kind = 'fgem']"><img src="assets/images/silk_data_save.gif" width="16" height="16" alt="+"/></xsl:when>
                        <xsl:otherwise><img src="assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/></xsl:otherwise>
                    </xsl:choose>
                </div>
            </td>
            <td class="td_raw">
                <div>
                    <xsl:choose>
                            <xsl:when test="helper:isFileAvailableForDownload(files/raw/@name) and files/raw/@celcount>0"><a href="{concat('${interface.application.base.path}/download/',files/raw/@name)}" title="Click to download Affymetrix data"><img src="${interface.application.base.path}/assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Click to download Affymetrix data"/></a></xsl:when>
                            <xsl:when test="helper:isFileAvailableForDownload(files/raw/@name) and files/raw/@celcount=0"><a href="{concat('${interface.application.base.path}/download/',files/raw/@name)}" title="Click to download raw data"><img src="${interface.application.base.path}/assets/images/silk_data_save.gif" width="16" height="16" alt="Click to download raw data"/></a></xsl:when>
                            <xsl:when test="accession='E-TABM-185' and helper:isFileAvailableForDownload('E-TABM-185.raw_data_readme.txt')"><a href="${interface.application.base.path}/download/E-TABM-185.raw_data_readme.txt" title="Click to download Affymetrix data"><img src="${interface.application.base.path}/assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Click to download Affymetrix data"/></a></xsl:when>
                            <xsl:otherwise><img src="${interface.application.base.path}/assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
            </tr>
            <tr id="{$vExpId}_ext" class="ae_results_tr_ext" style="{$pDetailedViewExtStyle}">
                <td colspan="8">
                    <table class="ae_results_tr_ext_table" cellpadding="0" cellspacing="0" border="0">
                        <xsl:if test="name">
                            <tr>
                                <td class="name">Title:</td>
                                <td class="value"><xsl:apply-templates select="name" mode="highlight" /></td>
                            </tr>
                        </xsl:if>
                        <tr style="${interface.application.link.aer_old.experiment_link.style}">
                            <td class="name"/>
                            <td class="value">
                                <a href="${interface.application.link.aer_old.base.url}/result?queryFor=Experiment&amp;eAccession={accession/text()}"
                                            target="_blank" title="Opens in a new window">&#187; Advanced interface page for <xsl:value-of select="accession"/></a>
                            </td>
                        </tr>
                        <xsl:if test="count(secondaryaccession/text())&gt;0">
                            <tr>
                                <td class="name">Secondary&#160;accession<xsl:if test="count(secondaryaccession/text())&gt;1">s</xsl:if>:</td>
                                <td class="value"><xsl:call-template name="secondaryaccession"/></td>
                            </tr>
                        </xsl:if>

                        <xsl:if test="miamescores">
                            <tr>
                                <td class="name">MIAME&#160;score:</td>
                                <td class="value">
                                    <strong><xsl:value-of select="miamescores/overallscore"/></strong>
                                    <xsl:text> ( array: </xsl:text>
                                    <xsl:value-of select="miamescores/reportersequencescore"/>
                                    <xsl:text>, protocols: </xsl:text>
                                    <xsl:value-of select="miamescores/protocolscore"/>
                                    <xsl:text>, factors: </xsl:text>
                                    <xsl:value-of select="miamescores/factorvaluescore"/>
                                    <xsl:text>, raw data: </xsl:text>
                                    <xsl:value-of select="miamescores/measuredbioassaydatascore"/>
                                    <xsl:text>, processed data: </xsl:text>
                                    <xsl:value-of select="miamescores/derivedbioassaydatascore"/>
                                    <xsl:text> )</xsl:text>
                                </td>
                            </tr>
                        </xsl:if>

                        <tr>
                            <td class="name">Sample&#160;annotation:</td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="helper:isFileAvailableForDownload(files/twocolumns/@name)">
                                        <a href="{concat('${interface.application.base.path}/download/',files/twocolumns/@name)}"
                                            target="_blank" title="Opens in a new window">&#187; Tab-delimited spreadsheet</a>
                                    </xsl:when>
                                    <xsl:otherwise>Data is not yet available</xsl:otherwise>
                                </xsl:choose>
                           </td>
                        </tr>

                        <xsl:if test="count(arraydesign)&gt;0">
                            <tr>
                                <td class="name">Array<xsl:if test="count(arraydesign)&gt;1">s</xsl:if>:</td>
                                <td class="value">
                                  <div id="{$vExpId}_array">
                                        <xsl:for-each select="arraydesign">
                                            <xsl:apply-templates select="name" mode="highlight" />
                                            <xsl:text> (</xsl:text>
                                            <a href="${interface.application.link.aer_old.base.url}/result?queryFor=PhysicalArrayDesign&amp;aAccession={accession/text()}"
                                               target="_blank" title="Opens in a new window">
                                                <xsl:text>&#187; </xsl:text>
                                                <xsl:apply-templates select="accession" mode="highlight" />
                                            </a>
                                            <xsl:text>)</xsl:text>
                                            <xsl:if test="position()!=last()">, </xsl:if>
                                        </xsl:for-each>
                                  </div>
                                </td>
                            </tr>
                        </xsl:if>
                        <tr>
                            <td class="name">Downloads:</td>
                            <td class="value">
                                <p>
                                    <a href="ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/{substring(accession,3,4)}/{accession}"
                                       target="_blank" title="Opens in a new window">
                                        <xsl:text>&#187; FTP server direct link</xsl:text>
                                    </a>
                                </p>
                                <xsl:if test="accession!='E-TABM-185'">
                                    <a href="${interface.application.link.aer_old.base.url}/dataselection?expid={$vExpId}"
                                       target="_blank" title="Opens in a new window">
                                        <xsl:text>&#187; View detailed data retrieval page</xsl:text>
                                    </a>
                                </xsl:if>
                            </td>
                        </tr>
                        <xsl:if test="accession!='E-TABM-185'">
                            <tr>
                                <td class="name">Experiment&#160;design:</td>
                                <td class="value">
                                    <xsl:if test="helper:isFileAvailableForDownload(files/biosamples/png/@name)">
                                        <a href="{concat('${interface.application.base.path}/download/',files/biosamples/png/@name)}"
                                           target="_blank" title="Opens in a new window">
                                            <xsl:text>&#187; PNG</xsl:text>
                                        </a>
                                        <xsl:if test="helper:isFileAvailableForDownload(files/biosamples/svg/@name)">
                                            <xsl:text>, </xsl:text>
                                        </xsl:if>
                                    </xsl:if>
                                    <xsl:if test="helper:isFileAvailableForDownload(files/biosamples/svg/@name)">
                                        <a href="{concat('${interface.application.base.path}/download/',files/biosamples/svg/@name)}"
                                           target="_blank" title="Opens in a new window">
                                            <xsl:text>&#187; SVG</xsl:text>
                                        </a>
                                    </xsl:if>
                                    <xsl:if test="not (helper:isFileAvailableForDownload(files/biosamples/png/@name) or helper:isFileAvailableForDownload(files/biosamples/svg/@name))">
                                        <xsl:text>Data is not yet available</xsl:text>
                                    </xsl:if>
                                </td>
                            </tr>
                        </xsl:if>

                        <tr>
                            <td class="name">Protocols:</td>
                            <td class="value">
                                <a href="${interface.application.link.aer_old.base.url}/details?class=MAGE.Experiment_protocols&amp;criteria=Experiment%3D{$vExpId}&amp;contextClass=MAGE.Protocol&amp;templateName=Protocol.vm"
                                        target="_blank" title="Opens in a new window">
                                    <xsl:text>&#187; Experimental protocols</xsl:text>
                                </a>
                            </td>
                        </tr>

                        <xsl:if test="count(bibliography/*)&gt;0">
                            <tr>
                                <td class="name">Citation<xsl:if test="count(bibliography/*)&gt;1">s</xsl:if>:</td>
                                <td class="value"><xsl:apply-templates select="bibliography" /></td>
                            </tr>
                        </xsl:if>

                        <tr>
                            <td class="name">Detailed sample&#160;annotation:</td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="helper:isFileAvailableForDownload(files/sdrf/@name)">
                                        <a href="{concat('${interface.application.base.path}/download/',files/sdrf/@name)}"
                                            target="_blank" title="Opens in a new window">
                                            <xsl:text>&#187; Tab-delimited spreadsheet</xsl:text>
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:text>Data is not yet available</xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>

                        <xsl:if test="count(provider[role!='data_coder'])&gt;0">
                            <tr>
                                <td class="name">Contact<xsl:if test="count(provider[role!='data_coder'])&gt;1">s</xsl:if>:</td>
                                <td class="value">
                                    <xsl:call-template name="providers"/>
                                </td>
                            </tr>
                        </xsl:if>

                        <xsl:if test="count(experimentdesign)&gt;0">
                            <tr>
                                <td class="name">Design&#160;type<xsl:if test="count(experimentdesign)&gt;1">s</xsl:if>:</td>
                                <td class="value">
                                    <xsl:for-each select="experimentdesign">
                                        <xsl:apply-templates select="." mode="highlight"/>
                                        <xsl:if test="position()!=last()">, </xsl:if>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>

                        <xsl:if test="count(description[text/text()!='' and not(contains(text/text(),'Generated description'))])&gt;0">
                            <tr>
                                <td class="name">Description:</td>
                                <td class="value">
                                    <xsl:for-each select="description[text/text()!='' and not(contains(text/text(),'Generated description'))]">
                                        <xsl:call-template name="description">
                                            <xsl:with-param name="text" select="text/text()"/>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:if>

                        <xsl:if test="count(experimentalfactor/name)&gt;0">
                            <tr>
                                <td class="name">Experimental factor<xsl:if test="count(experimentalfactor/name)&gt;1">s</xsl:if>:</td>
                                <td class="attrs">
                                    <table cellpadding="0" cellspacing="2" border="0">
                                        <thead>
                                            <tr>
                                                <th class="attr_name">Factor name</th>
                                                <th class="attr_value">Factor value(s)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <xsl:for-each select="experimentalfactor">
                                                <tr>
                                                    <td class="attr_name">
                                                        <xsl:apply-templates select="name" mode="highlight"/>
                                                    </td>
                                                    <td class="attr_value">
                                                        <xsl:for-each select="value">
                                                            <xsl:apply-templates select="." mode="highlight"/>
                                                            <xsl:if test="position()!=last()">, </xsl:if>
                                                        </xsl:for-each>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </tbody>
                                    </table>
                               </td>
                            </tr>
                        </xsl:if>

                        <xsl:if test="count(sampleattribute/category)&gt;0">
                            <tr>
                                <td class="name">Sample attribute<xsl:if test="count(sampleattribute/category)&gt;1">s</xsl:if>:</td>
                                <td class="attrs">
                                    <table cellpadding="0" cellspacing="2" border="0">
                                        <thead>
                                            <tr>
                                                <th class="attr_name">Attribute name</th>
                                                <th class="attr_value">Attribute value(s)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <xsl:for-each select="sampleattribute">
                                                <tr>
                                                    <td class="attr_name">
                                                        <xsl:apply-templates select="category" mode="highlight"/>
                                                    </td>
                                                    <td class="attr_value">
                                                        <xsl:for-each select="value">
                                                            <xsl:apply-templates select="." mode="highlight"/>
                                                            <xsl:if test="position()!=last()">, </xsl:if>
                                                        </xsl:for-each>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </xsl:if>
                    </table>
                </td>
            </tr>

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
                    <a href="{uri}" target="_blank" title="Opens in a new window">&#187; <xsl:copy-of select="$publication_title"/></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$publication_title" />
                    <xsl:if test="uri/text()!=''">(<xsl:apply-templates select="uri" mode="highlight"/>)</xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="accession">
                <xsl:if test="number(accession)>0">(<a href="http://www.ncbi.nlm.nih.gov/pubmed/{accession}" target="_blank" title="Opens in a new window">&#187; PubMed <xsl:apply-templates select="accession" mode="highlight"/></a>)</xsl:if>
            </xsl:if>
        </p>
    </xsl:template>

    <xsl:template name="secondaryaccession">
        <xsl:for-each select="secondaryaccession">
            <xsl:choose>
                <xsl:when test="string-length(text())=0"/>
                <xsl:when test="substring(text(), 1, 3)='GSE' or substring(text(), 1, 3)='GDS'">
                    <a href="http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc={text()}"
                       target="_blank" title="Opens in a new window">&#187; GEO <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:when test="substring(text(), 1, 2)='E-' and substring(text(), 7, 1)='-'">
                    <a href="${interface.application.base.path}/experiments/{text()}"
                       target="_blank" title="Opens in a new window">&#187; <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="." mode="highlight" />
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="position()!=last() and string-length(text())&gt;0">, </xsl:if>
        </xsl:for-each>
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
