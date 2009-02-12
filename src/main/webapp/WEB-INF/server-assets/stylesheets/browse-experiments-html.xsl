<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="func ae helper html"
                exclude-result-prefixes="func ae helper html"
                version="1.0">

    <xsl:param name="page">1</xsl:param>
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

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <helper:logInfo select="[browse-experiments-html] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}], detailedview [{$detailedview}]"/>
        <helper:logInfo select="[browse-experiments-html] Sort by: [{$sortby}], [{$sortorder}]"/>
        <xsl:variable name="vFilteredExperiments" select="ae:filter-experiments($userid,$keywords,$wholewords,$species,$array,$exptype,$inatlas)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments[samples/text()>0]/samples/text())"/>
        <xsl:variable name="vTotalAssays" select="sum($vFilteredExperiments[assays/text()>0]/assays/text())"/>

        <xsl:variable name="vFrom">
            <xsl:choose>
                <xsl:when test="$page &gt; 0"><xsl:value-of select="1 + ( $page - 1 ) * $pagesize"/></xsl:when>
                <xsl:when test="$vTotal = 0">0</xsl:when>
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
        <xsl:variable name="vDetailedViewMainTdClass">td_main<xsl:if test="$detailedview"> td_expanded</xsl:if></xsl:variable>

        <helper:logInfo select="[browse-experiments-html] Query filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>

        <tr id="ae_results_summary_info">
            <td colspan="9">
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
                    <xsl:with-param name="pDetailedViewMainTdClass" select="$vDetailedViewMainTdClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$vDetailedViewExtStyle"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <tr class="ae_results_tr_error">
                    <td colspan="9">
                        <xsl:choose>
                            <xsl:when test="helper:testRegexp($keywords,'^E-.+-\d+$','i')">
                                <div><strong>The experiment with accession number '<xsl:value-of select="$keywords"/>' is not available.</strong></div>
                                <div>If you believe this is an error, please do not hesitate to drop us a line to <strong>arrayexpress(at)ebi.ac.uk</strong> or use <a href="${interface.application.link.www_domain}/support/" title="EBI Support">EBI Support Feedback</a> form.</div>
                            </xsl:when>
                            <xsl:otherwise>
                                <div><strong>The query '<xsl:value-of select="$keywords"/>'<xsl:if test="string-length($species)>0">&#160;<em>and</em> species '<xsl:value-of select="$species"/>'</xsl:if>
                                    <xsl:if test="string-length($array)>0">&#160;<em>and</em> array <xsl:value-of select="$array"/></xsl:if>
                                    <xsl:if test="string-length($exptype)>0">&#160;<em>and</em> experiment type '<xsl:value-of select="$exptype"/>'</xsl:if>
                                    returned no matches.</strong></div>
                                <div>Try shortening the query term e.g. 'embryo' will match embryo, embryoid, embryonic across all annotation fields.</div>
                                <div>Note that '*' is <strong>not</strong> supported as a wild card. More information available at <a href="http://www.ebi.ac.uk/microarray/doc/help/ae_help.html">ArrayExpress Query Help</a>.</div>
                            </xsl:otherwise>
                        </xsl:choose>

                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:param name="pDetailedViewMainTdClass"/>
        <xsl:param name="pDetailedViewExtStyle"/>
        <xsl:variable name="vExpId" select="id"/>
        <xsl:if test="position() &gt;= $pFrom and position() &lt;= $pTo">
            <tr id="{$vExpId}_main" class="tr_main">
                <td class="{$pDetailedViewMainTdClass}"><div class="table_row_expand"/></td>
                <td class="{$pDetailedViewMainTdClass}">
                    <div class="table_row_accession"><xsl:apply-templates select="accession" mode="highlight" /></div>
                    <xsl:if test="not(user/text()='1')">
                        <div class="lock">&#160;</div>
                    </xsl:if>
                </td>
                <td class="{$pDetailedViewMainTdClass}"><div><xsl:apply-templates select="name" mode="highlight" /><xsl:if test="count(name)=0">&#160;</xsl:if></div></td>
                <td class="align_right {$pDetailedViewMainTdClass}">
                    <div><xsl:apply-templates select="assays" mode="highlight" /><xsl:if test="count(assays)=0">&#160;</xsl:if></div>
                </td>
                <td class="{$pDetailedViewMainTdClass}"><div>
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="." mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(species) = 0"><xsl:text>&#160;</xsl:text></xsl:if>
                </div></td>
                <td class="{$pDetailedViewMainTdClass}"><div><xsl:apply-templates select="releasedate" mode="highlight" /></div><xsl:if test="count(releasedate)=0">&#160;</xsl:if></td>
                <td class="td_main_img align_center {$pDetailedViewMainTdClass}">
                    <div>
                        <xsl:choose>
                            <xsl:when test="file[kind = 'fgem']"><a href="{concat('${interface.application.base.url}/files/', concat(accession, concat('/', file[kind = 'fgem']/name)))}" title="Click to download processed data"><img src="${interface.application.base.url}/assets/images/silk_data_save.gif" width="16" height="16" alt="Click to download processed data"/></a></xsl:when>
                            <xsl:otherwise><img src="${interface.application.base.url}/assets/images/silk_data_unavail.gif" width="16" height="16"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td class="td_main_img align_center {$pDetailedViewMainTdClass}">
                    <div>
                        <xsl:choose>
                            <xsl:when test="contains(file[kind = 'raw']/dataformat, 'CEL')"><a href="{concat('${interface.application.base.url}/files/', concat(accession, concat('/', file[kind = 'raw']/name)))}" title="Click to download Affymetrix data"><img src="${interface.application.base.url}/assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Click to download Affymetrix data"/></a></xsl:when>
                            <xsl:when test="file[kind = 'raw']"><a href="{concat('${interface.application.base.url}/files/', concat(accession, concat('/', file[kind = 'raw']/name)))}" title="Click to download raw data"><img src="${interface.application.base.url}/assets/images/silk_data_save.gif" width="16" height="16" alt="Click to download raw data"/></a></xsl:when>
                            <xsl:when test="accession='E-TABM-185'"><a href="${interface.application.base.url}/files/E-TABM-185/E-TABM-185.raw_data_readme.txt" title="Click to download Affymetrix data"><img src="${interface.application.base.url}/assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Click to download Affymetrix data"/></a></xsl:when>
                            <xsl:otherwise><img src="${interface.application.base.url}/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
                <td class="td_main_img align_center {$pDetailedViewMainTdClass}">
                    <div>
                        <xsl:choose>
                            <xsl:when test="@loadedinatlas"><a href="${interface.application.link.atlas.exp_query.url}{accession}" target="_blank" title="Click to query ArrayExpress Atlas for most differentially expressed genes in {accession}"><img src="${interface.application.base.url}/assets/images/silk_tick.gif" width="16" height="16" alt="*"/></a></xsl:when>
                            <xsl:otherwise><img src="${interface.application.base.url}/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
            </tr>
            <tr id="{$vExpId}_ext" style="{$pDetailedViewExtStyle}">
                <td colspan="9" class="td_ext">
                    <div class="tbl">
                        <table cellpadding="0" cellspacing="0" border="0">
                            <xsl:if test="count(description[text/text()!='' and not(contains(text/text(),'Generated description'))])&gt;0">
                                <tr>
                                    <td class="name"><div>Description</div></td>
                                    <td class="value">
                                        <xsl:for-each select="description[text/text()!='' and not(contains(text/text(),'Generated description'))]">
                                            <xsl:call-template name="description">
                                                <xsl:with-param name="text" select="text/text()"/>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="miamescores">
                                <tr>
                                    <td class="name"><div>MIAME score</div></td>
                                    <td class="value miame_score">
                                        <div>
                                            <xsl:call-template name="miame-star">
                                                <xsl:with-param name="stars" select="miamescores/overallscore"/>
                                                <xsl:with-param name="count">0</xsl:with-param>
                                            </xsl:call-template>
                                            <span>
                                                <xsl:text> (</xsl:text>
                                                <xsl:if test="miamescores/reportersequencescore = '1'">
                                                    <xsl:text>arrays</xsl:text>
                                                    <xsl:if test="miamescores/protocolscore = '1' or miamescores/factorvaluescore = '1' or miamescores/derivedbioassaydatascore = '1' or miamescores/measuredbioassaydatascore = '1'">
                                                        <xsl:text>, </xsl:text>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="miamescores/protocolscore = '1'">
                                                    <xsl:text>protocols</xsl:text>
                                                    <xsl:if test="miamescores/factorvaluescore = '1' or miamescores/derivedbioassaydatascore = '1' or miamescores/measuredbioassaydatascore = '1'">
                                                        <xsl:text>, </xsl:text>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="miamescores/factorvaluescore = '1'">
                                                    <xsl:text>factors</xsl:text>
                                                    <xsl:if test="miamescores/derivedbioassaydatascore = '1' or miamescores/measuredbioassaydatascore = '1'">
                                                        <xsl:text>, </xsl:text>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="miamescores/derivedbioassaydatascore = '1'">
                                                    <xsl:text>processed data</xsl:text>
                                                    <xsl:if test="miamescores/measuredbioassaydatascore = '1'">
                                                        <xsl:text>, </xsl:text>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="miamescores/measuredbioassaydatascore = '1'">raw data</xsl:if>
                                                <xsl:text>)</xsl:text>
                                            </span>
                                        </div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(provider[role!='data_coder'])&gt;0">
                                <tr>
                                    <td class="name"><div>Contact<xsl:if test="count(provider[role!='data_coder'])&gt;1">s</xsl:if></div></td>
                                    <td class="value">
                                        <div>
                                            <xsl:call-template name="providers"/>
                                        </div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(bibliography/*)&gt;0">
                                <tr>
                                    <td class="name"><div>Citation<xsl:if test="count(bibliography/*)&gt;1">s</xsl:if></div></td>
                                    <td class="value"><xsl:apply-templates select="bibliography" /></td>
                                </tr>
                            </xsl:if>

                            <tr>
                                <td class="name"><div>Links</div></td>
                                <td class="value">
                                    <xsl:if test="@loadedinatlas">
                                        <div>

                                            <a href="${interface.application.link.atlas.exp_query.url}{accession}"
                                                target="_blank" title="Opens in a new window">&#187; Query ArrayExpress Atlas</a>
                                            <xsl:text> </xsl:text>
                                            <img src="${interface.application.base.url}/assets/images/silk_new.gif" width="16" height="13" alt="new!"/>
                                        </div>
                                    </xsl:if>
                                    <div>
                                        <xsl:if test="count(secondaryaccession)&gt;0">
                                            <xsl:call-template name="secondaryaccession"/>
                                        </xsl:if>
                                    </div>
                                    <xsl:if test="count(arraydesign)&gt;0">
                                        <xsl:for-each select="arraydesign">
                                            <div>
                                                <a href="${interface.application.link.aer_old.base.url}/result?queryFor=PhysicalArrayDesign&amp;aAccession={accession}"
                                                   target="_blank" title="Opens in a new window">
                                                    <xsl:text>&#187; Array design </xsl:text>
                                                    <xsl:apply-templates select="accession" mode="highlight"/>
                                                    <xsl:text> - </xsl:text>
                                                    <xsl:apply-templates select="name" mode="highlight" />
                                                </a>
                                            </div>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <div>
                                        <a href="${interface.application.link.aer_old.base.url}/details?class=MAGE.Experiment_protocols&amp;criteria=Experiment%3D{$vExpId}&amp;contextClass=MAGE.Protocol&amp;templateName=Protocol.vm"
                                            target="_blank" title="Opens in a new window">
                                            <xsl:text>&#187; Experimental protocols</xsl:text>
                                        </a>
                                    </div>
                                    <div>
                                        <a href="${interface.application.link.aer_old.base.url}/result?queryFor=Experiment&amp;eAccession={accession}"
                                            target="_blank" title="Opens in a new window">&#187; ArrayExpress Advanced Interface</a>

                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="name"><div>Files</div></td>
                                <xsl:choose>
                                    <xsl:when test="file[kind='raw' or kind='fgem' or kind='adf' or kind='idf' or kind='sdrf' or kind='biosamples']">

                                        <td class="attrs">
                                            <div>
                                                <table cellpadding="0" cellspacing="0" border="0">
                                                    <tbody>
                                                        <xsl:call-template name="data-files"/>
                                                        <xsl:call-template name="magetab-files"/>
                                                        <xsl:call-template name="magetab-files-array"/>
                                                        <xsl:call-template name="image-files"/>
                                                    </tbody>
                                                </table>
                                            </div>
                                            <div>
                                                <a href="${interface.application.base.url}/files/{accession}"
                                                   target="_blank" title="Opens in a new window">
                                                    <xsl:text>&#187; Browse all available files</xsl:text>
                                                </a>
                                            </div>

                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td class="value">
                                            <div>
                                                <a href="${interface.application.base.url}/files/{accession}"
                                                   target="_blank" title="Opens in a new window">
                                                    <xsl:text>&#187; Browse all available files</xsl:text>
                                                </a>
                                            </div>    
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </tr>
                            <xsl:if test="(count(experimenttype) + count(experimentdesign))&gt;0">
                                <tr>
                                    <td class="name"><div>Experiment type<xsl:if test="(count(experimenttype) + count(experimentdesign))&gt;1">s</xsl:if></div></td>
                                    <td class="value"><div>
                                        <xsl:for-each select="experimenttype">
                                            <xsl:apply-templates select="." mode="highlight"/>
                                            <xsl:if test="position()!=last()">, </xsl:if>
                                        </xsl:for-each>
                                        <xsl:if test="count(experimenttype)&gt;0 and count(experimentdesign)&gt;0">, </xsl:if>
                                        <xsl:for-each select="experimentdesign">
                                            <xsl:apply-templates select="." mode="highlight"/>
                                            <xsl:if test="position()!=last()">, </xsl:if>
                                        </xsl:for-each></div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(experimentalfactor/name)&gt;0">
                                <tr>
                                    <td class="name"><div>Experimental factors</div></td>
                                    <td class="attrs"><div>
                                        <table cellpadding="0" cellspacing="0" border="0">
                                            <thead>
                                                <tr>
                                                    <th class="attr_name">Factor name</th>
                                                    <th class="attr_value">Factor values</th>
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
                                        </table></div>
                                   </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(sampleattribute/category)&gt;0">
                                <tr>
                                    <td class="name"><div>Sample attributes</div></td>
                                    <td class="attrs"><div>
                                        <table cellpadding="0" cellspacing="0" border="0">
                                            <thead>
                                                <tr>
                                                    <th class="attr_name">Attribute name</th>
                                                    <th class="attr_value">Attribute values</th>
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
                                        </table></div>
                                    </td>
                                </tr>
                            </xsl:if>
                        </table>
                    </div>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="bibliography">
        <div>
            <xsl:variable name="publication_title">
                <xsl:if test="title/text()!=''"><xsl:call-template name="highlight"><xsl:with-param name="pText" select="helper:trimTrailingDot(title)"/></xsl:call-template>. </xsl:if>
                <xsl:if test="authors/text()!=''"><xsl:call-template name="highlight"><xsl:with-param name="pText" select="helper:trimTrailingDot(authors)"/></xsl:call-template>. </xsl:if>
            </xsl:variable>
            <xsl:variable name="publication_link_title">
                <xsl:if test="publication/text()!=''"><em><xsl:apply-templates select="publication" mode="highlight"/></em>&#160;</xsl:if>
                <xsl:if test="volume/text()!=''"><xsl:apply-templates select="volume" mode="highlight"/><xsl:if test="issue/text()!=''">(<xsl:apply-templates select="issue" mode="highlight"/>)</xsl:if></xsl:if>
                <xsl:if test="pages/text()!=''">:<xsl:apply-templates select="pages" mode="highlight"/></xsl:if>
                <xsl:if test="year/text()!=''">&#160;(<xsl:apply-templates select="year" mode="highlight"/>)</xsl:if>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="uri[starts-with(., 'http')]">
                    <xsl:copy-of select="$publication_title"/>
                    <a href="{uri}" target="_blank" title="Opens in a new window"><xsl:copy-of select="$publication_link_title"/></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$publication_title"/>
                    <xsl:copy-of select="$publication_link_title"/>
                    <xsl:if test="uri/text()!=''"> (<xsl:apply-templates select="uri" mode="highlight"/>)</xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="accession">
                <xsl:if test="number(accession)>0">, <a href="http://www.ncbi.nlm.nih.gov/pubmed/{accession}" target="_blank" title="Opens in a new window">PubMed <xsl:apply-templates select="accession" mode="highlight"/></a></xsl:if>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="secondaryaccession">
        <xsl:for-each select="secondaryaccession">
            <xsl:choose>
                <xsl:when test="string-length(text())=0"/>
                <xsl:when test="substring(text(), 1, 3)='GSE' or substring(text(), 1, 3)='GDS'">
                    <a href="http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc={text()}"
                       target="_blank" title="Opens in a new window">&#187; GEO - <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:when test="substring(text(), 1, 2)='E-' and substring(text(), 7, 1)='-'">
                    <a href="${interface.application.base.url}/experiments/{text()}"
                       target="_blank" title="Opens in a new window">&#187; ArrayExpress - <xsl:apply-templates select="." mode="highlight" /></a>
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
            <xsl:choose>
                <xsl:when test="role='submitter' and string-length(email)&gt;0">
                    <xsl:apply-templates select="contact" mode="highlight"/> &lt;<a href="mailto:{email}"><xsl:apply-templates select="email" mode="highlight"/></a>&gt;
                </xsl:when>
                <xsl:otherwise><xsl:apply-templates select="contact" mode="highlight"/></xsl:otherwise>
            </xsl:choose>
            <xsl:if test="position()!=last()">, </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="description">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '&lt;br&gt;')">
                <div>
                    <xsl:call-template name="add_highlight_element">
                        <xsl:with-param name="text" select="helper:markKeywords(substring-before($text, '&lt;br&gt;'),$keywords,$wholewords)"/>
                    </xsl:call-template>
                </div>
                <xsl:call-template name="description">
                    <xsl:with-param name="text" select="substring-after($text,'&lt;br&gt;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <div>
                    <xsl:call-template name="add_highlight_element">
                        <xsl:with-param name="text" select="helper:markKeywords($text,$keywords,$wholewords)"/>
                    </xsl:call-template>
                </div>
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

    <xsl:template name="highlight">
        <xsl:param name="pText"/>
        <xsl:variable name="vText" select="normalize-space($pText)"/>
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

    <xsl:template name="miame-star">
        <xsl:param name="stars" />
        <xsl:param name="count" />
        <xsl:if test="$count&lt;5">
            <xsl:choose>
                <xsl:when test="$count&lt;$stars">
                    <img src="${interface.application.base.url}/assets/images/miame_star.gif" width="14" height="13" alt="*"/>
                </xsl:when>
                <xsl:otherwise>
                    <img src="${interface.application.base.url}/assets/images/miame_nostar.gif" width="14" height="13" alt="."/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="miame-star">
                <xsl:with-param name="stars" select="$stars"/>
                <xsl:with-param name="count" select="$count + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="data-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[extension='zip' and (kind='raw' or kind='fgem')]">
            <tr>
                <td class="attr_name">Data Archives</td>
                <td class="attr_value">
                    <xsl:for-each select="file[extension='zip' and (kind='raw' or kind='fgem')]">
                        <xsl:sort select="kind"/>
                        <a href="{url}">
                            <xsl:value-of select="name"/>
                        </a>
                        <xsl:if test="position()!=last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template name="magetab-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:for-each select="file[extension='txt' and (kind='idf' or kind='sdrf')]">
            <xsl:sort select="kind"/>
            <tr>
                <td class="attr_name">
                    <xsl:choose>
                        <xsl:when test="kind='idf'">Investigation Description</xsl:when>
                        <xsl:when test="kind='sdrf'">Sample and Data Relationship</xsl:when>
                    </xsl:choose>
                </td>
                <td class="attr_value">
                    <a href="{url}"
                       target="_blank" title="Opens in a new window">
                        <xsl:value-of select="name"/>
                    </a>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="magetab-files-array">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[extension='txt' and kind='adf']">
            <tr>
                <td class="attr_name">Array Design</td>
                <td class="attr_value">
                    <xsl:for-each select="file[extension='txt' and kind='adf']">
                        <xsl:sort select="name"/>
                        <a href="{url}"
                           target="_blank" title="Opens in a new window">
                            <xsl:value-of select="name"/>
                        </a>
                        <xsl:if test="position()!=last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template name="image-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[kind='biosamples' and (extension='png' or extension='svg')]">
            <tr>
                <td class="attr_name">Experiment Design Images</td>
                <td class="attr_value">
                    <xsl:for-each select="file[kind='biosamples' and (extension='png' or extension='svg')]">
                        <xsl:sort select="extension"/>
                        <a href="{url}"
                           target="_blank" title="Opens in a new window">
                            <xsl:value-of select="name"/>
                        </a>
                        <xsl:if test="position()!=last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
