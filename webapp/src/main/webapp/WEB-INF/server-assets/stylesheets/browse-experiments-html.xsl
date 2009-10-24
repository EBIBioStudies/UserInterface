<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
                xmlns:aeext="java:/uk.ac.ebi.arrayexpress.utils.saxon.ExtElements"  
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="ae aeext html"
                exclude-result-prefixes="ae aeext html"
                version="1.0">

    <xsl:param name="querykey"/>
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

    <!-- dynamically set by QueryServlet: host name (as seen from client) and base context path of webapp -->
    <xsl:param name="host"/>
    <xsl:param name="basepath"/>

    <xsl:variable name="vBaseUrl">http://<xsl:value-of select="$host"/><xsl:value-of select="$basepath"/></xsl:variable>

    <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" />

    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:variable name="vDetailedViewMainTrClass">tr_main<xsl:if test="'true'=$detailedview"> exp_expanded</xsl:if></xsl:variable>
    <xsl:variable name="vDetailedViewExtStyle"><xsl:if test="'true'!=$detailedview">display:none</xsl:if></xsl:variable>
    <xsl:variable name="vDetailedViewMainTdClass">td_main<xsl:if test="'true'=$detailedview"> td_expanded</xsl:if></xsl:variable>

    <xsl:template match="/experiments">

        <aeext:log message="[browse-experiments-html] Parameters: userid [{$userid}], keywords [{$keywords}], wholewords [{$wholewords}], array [{$array}], species [{$species}], exptype [{$exptype}], inatlas [{$inatlas}], detailedview [{$detailedview}]"/>
        <aeext:log message="[browse-experiments-html] Sort by: [{$sortby}], [{$sortorder}]"/>

        <xsl:variable name="vFilteredExperiments" select="ae:searchIndex($querykey)"/>
        <xsl:variable name="vTotal" select="count($vFilteredExperiments)"/>
        <xsl:variable name="vTotalSamples" select="sum($vFilteredExperiments/samples)"/>
        <xsl:variable name="vTotalAssays" select="sum($vFilteredExperiments/assays)"/>

        <xsl:variable name="vFrom">
            <xsl:choose>
                <xsl:when test="$page > 0"><xsl:value-of select="1 + ( $page - 1 ) * $pagesize"/></xsl:when>
                <xsl:when test="$vTotal = 0">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="vTo">
            <xsl:choose>
                <xsl:when test="( $vFrom + $pagesize - 1 ) > $vTotal"><xsl:value-of select="$vTotal"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$vFrom + $pagesize - 1"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <aeext:log message="[browse-experiments-html] Query filtered {$vTotal} experiments. Will output from {$vFrom} to {$vTo}."/>

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
            <xsl:when test="$vTotal > 0">
                <xsl:call-template name="ae-sort-experiments">
                    <xsl:with-param name="pExperiments" select="$vFilteredExperiments"/>
                    <xsl:with-param name="pFrom" select="$vFrom"/>
                    <xsl:with-param name="pTo" select="$vTo"/>
                    <xsl:with-param name="pSortBy" select="$sortby"/>
                    <xsl:with-param name="pSortOrder" select="$sortorder"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <tr class="ae_results_tr_error">
                    <td colspan="9">
                        <xsl:choose>
                            <xsl:when test="ae:testRegexp($keywords,'^E-.+-\d+$','i')">
                                <div><strong>The experiment with accession number '<xsl:value-of select="$keywords"/>' is not available.</strong></div>
                                <div>If you believe this is an error, please do not hesitate to drop us a line to <strong>arrayexpress(at)ebi.ac.uk</strong> or use <a href="${interface.application.link.www_domain}/support/" title="EBI Support">EBI Support Feedback</a> form.</div>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="vArrayName" select="//arraydesign[id=$array]/name"/>
                                <div>There are no experiments <strong><xsl:value-of select="ae:describeQuery($keywords,$wholewords,$species,$vArrayName,$exptype,$inatlas)"/></strong> found in ArrayExpress Archive.</div>
                                <div>Try shortening the query term e.g. 'embryo' will match embryo, embryoid, embryonic across all annotation fields.</div>
                                <div>Note that '*' is <strong>not</strong> supported as a wild card. More information available in <a href="${interface.application.link.query_help}">ArrayExpress Query Help</a>.</div>
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
        <xsl:variable name="vExpId" select="id"/>
        <xsl:if test="position() >= $pFrom and not(position() > $pTo)">
            <tr id="{$vExpId}_main" class="{$vDetailedViewMainTrClass}">
                <td class="{$vDetailedViewMainTdClass}"><div class="table_row_expand"/></td>
                <td class="{$vDetailedViewMainTdClass}">
                    <div class="table_row_accession"><xsl:apply-templates select="accession" mode="highlight" /></div>
                    <xsl:if test="not(user = '1')">
                        <div class="lock">&#160;</div>
                    </xsl:if>
                </td>
                <td class="{$vDetailedViewMainTdClass}"><div><xsl:apply-templates select="name" mode="highlight" /><xsl:if test="count(name)=0">&#160;</xsl:if></div></td>
                <td class="align_right {$vDetailedViewMainTdClass}">
                    <div><xsl:apply-templates select="assays" mode="highlight" /><xsl:if test="count(assays)=0">&#160;</xsl:if></div>
                </td>
                <td class="{$vDetailedViewMainTdClass}"><div>
                    <xsl:for-each select="species">
                        <xsl:apply-templates select="." mode="highlight" />
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(species) = 0"><xsl:text>&#160;</xsl:text></xsl:if>
                </div></td>
                <td class="{$vDetailedViewMainTdClass}"><div><xsl:apply-templates select="releasedate" mode="highlight" /></div><xsl:if test="count(releasedate)=0">&#160;</xsl:if></td>
                <td class="td_main_img align_center {$vDetailedViewMainTdClass}">
                    <div>
                        <xsl:call-template name="data-files-main"><xsl:with-param name="pKind" select="'fgem'"/></xsl:call-template>
                    </div>
                </td>
                <td class="td_main_img align_center {$vDetailedViewMainTdClass}">
                    <div>
                        <xsl:call-template name="data-files-main"><xsl:with-param name="pKind" select="'raw'"/></xsl:call-template>
                    </div>
                </td>
                <td class="td_main_img align_center {$vDetailedViewMainTdClass}">
                    <div>
                        <xsl:choose>
                            <xsl:when test="@loadedinatlas"><a href="${interface.application.link.atlas.exp_query.url}{accession}&amp;ref=aebrowse"><img src="{$basepath}/assets/images/silk_tick.gif" width="16" height="16" alt="*"/></a></xsl:when>
                            <xsl:otherwise><img src="{$basepath}/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/></xsl:otherwise>
                        </xsl:choose>
                    </div>
                </td>
            </tr>
            <tr id="{$vExpId}_ext" style="{$vDetailedViewExtStyle}">
                <td colspan="9" class="td_ext">
                    <div class="tbl">
                        <table cellpadding="0" cellspacing="0" border="0">
                            <xsl:if test="count(description[string-length(text) > 0 and not(contains(text, 'Generated description'))]) > 0">
                                <tr>
                                    <td class="name"><div>Description</div></td>
                                    <td class="value">
                                        <xsl:for-each select="description[string-length(text) > 0 and not(contains(text, 'Generated description'))]">
                                            <xsl:call-template name="description">
                                                <xsl:with-param name="text" select="text"/>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="miamescores">
                                <tr>
                                    <td class="name"><div>MIAME score</div></td>
                                    <td class="value">
                                        <div>
                                            <xsl:call-template name="miame-score">
                                                <xsl:with-param name="pScores" select="miamescores"/>
                                            </xsl:call-template>
                                        </div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(provider[role!='data_coder']) > 0">
                                <tr>
                                    <td class="name"><div>Contact<xsl:if test="count(provider[role!='data_coder']) > 1">s</xsl:if></div></td>
                                    <td class="value">
                                        <div>
                                            <xsl:call-template name="providers"/>
                                        </div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(bibliography/*) > 0">
                                <tr>
                                    <td class="name"><div>Citation<xsl:if test="count(bibliography/*) > 1">s</xsl:if></div></td>
                                    <td class="value"><xsl:apply-templates select="bibliography" /></td>
                                </tr>
                            </xsl:if>

                            <tr>
                                <td class="name"><div>Links</div></td>
                                <td class="value">
                                    <xsl:if test="@loadedinatlas">
                                        <div><a href="${interface.application.link.atlas.exp_query.url}{accession}&amp;ref=aebrowse">Query Gene Expression Atlas</a></div>
                                    </xsl:if>
                                    <div>
                                        <xsl:if test="count(secondaryaccession) > 0">
                                            <xsl:call-template name="secondaryaccession"/>
                                        </xsl:if>
                                    </div>
                                    <xsl:if test="count(arraydesign) > 0">
                                        <xsl:for-each select="arraydesign">
                                            <div>
                                                <a href="${interface.application.link.aer_old.base.url}/result?queryFor=PhysicalArrayDesign&amp;aAccession={accession}">
                                                    <xsl:text>Array design </xsl:text>
                                                    <xsl:apply-templates select="accession" mode="highlight"/>
                                                    <xsl:text> - </xsl:text>
                                                    <xsl:apply-templates select="name" mode="highlight" />
                                                </a>
                                            </div>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <div>
                                        <a href="${interface.application.link.aer_old.base.url}/details?class=MAGE.Experiment_protocols&amp;criteria=Experiment%3D{$vExpId}&amp;contextClass=MAGE.Protocol&amp;templateName=Protocol.vm">
                                            <xsl:text>Experimental protocols</xsl:text>
                                        </a>
                                    </div>
                                    <div>
                                        <a href="${interface.application.link.aer_old.base.url}/result?queryFor=Experiment&amp;eAccession={accession}">ArrayExpress Advanced Interface</a>

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
                                                        <xsl:call-template name="image-files"/>
                                                        <xsl:call-template name="magetab-files-array"/>
                                                    </tbody>
                                                </table>
                                            </div>
                                            <div>
                                                <a href="{$basepath}/files/{accession}">
                                                    <img src="{$basepath}/assets/images/silk_ftp.gif" width="16" height="16" alt=""/>
                                                    <xsl:text>Browse all available files</xsl:text>
                                                </a>
                                            </div>

                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td class="value">
                                            <div>
                                                <a href="{$basepath}/files/{accession}">
                                                    <img src="{$basepath}/assets/images/silk_ftp.gif" width="16" height="16" alt=""/>
                                                    <xsl:text>Browse all available files</xsl:text>
                                                </a>
                                            </div>    
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </tr>
                            <xsl:if test="(count(experimenttype) + count(experimentdesign)) > 0">
                                <tr>
                                    <td class="name"><div>Experiment type<xsl:if test="(count(experimenttype) + count(experimentdesign)) > 1">s</xsl:if></div></td>
                                    <td class="value"><div>
                                        <xsl:for-each select="experimenttype">
                                            <xsl:apply-templates select="." mode="highlight"/>
                                            <xsl:if test="position()!=last()">, </xsl:if>
                                        </xsl:for-each>
                                        <xsl:if test="count(experimenttype) > 0 and count(experimentdesign) > 0">, </xsl:if>
                                        <xsl:for-each select="experimentdesign">
                                            <xsl:apply-templates select="." mode="highlight"/>
                                            <xsl:if test="position()!=last()">, </xsl:if>
                                        </xsl:for-each></div>
                                    </td>
                                </tr>
                            </xsl:if>

                            <xsl:if test="count(experimentalfactor/name) > 0">
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

                            <xsl:if test="count(sampleattribute/category) > 0">
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
                <xsl:if test="string-length(title) > 0"><xsl:call-template name="highlight"><xsl:with-param name="pText" select="ae:trimTrailingDot(title)"/></xsl:call-template>. </xsl:if>
                <xsl:if test="string-length(authors) > 0"><xsl:call-template name="highlight"><xsl:with-param name="pText" select="ae:trimTrailingDot(authors)"/></xsl:call-template>. </xsl:if>
            </xsl:variable>
            <xsl:variable name="publication_link_title">
                <xsl:if test="string-length(publication) > 0"><em><xsl:apply-templates select="publication" mode="highlight"/></em>&#160;</xsl:if>
                <xsl:if test="string-length(volume) > 0"><xsl:apply-templates select="volume" mode="highlight"/><xsl:if test="string-length(issue) > 0">(<xsl:apply-templates select="issue" mode="highlight"/>)</xsl:if></xsl:if>
                <xsl:if test="string-length(pages) > 0">:<xsl:apply-templates select="pages" mode="highlight"/></xsl:if>
                <xsl:if test="string-length(year) > 0">&#160;(<xsl:apply-templates select="year" mode="highlight"/>)</xsl:if>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="uri[starts-with(., 'http')]">
                    <xsl:copy-of select="$publication_title"/>
                    <a href="{uri}"><xsl:copy-of select="$publication_link_title"/></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$publication_title"/>
                    <xsl:copy-of select="$publication_link_title"/>
                    <xsl:if test="string-length(uri) > 0"> (<xsl:apply-templates select="uri" mode="highlight"/>)</xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="accession">
                <xsl:if test="number(accession)>0">, <a href="http://www.ncbi.nlm.nih.gov/pubmed/{accession}">PubMed <xsl:apply-templates select="accession" mode="highlight"/></a></xsl:if>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="secondaryaccession">
        <xsl:for-each select="secondaryaccession">
            <xsl:choose>
                <xsl:when test="string-length(.) = 0"/>
                <xsl:when test="substring(., 1, 3)='GSE' or substring(., 1, 3)='GDS'">
                    <a href="http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc={.}">GEO - <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:when test="substring(., 1, 2)='E-' and substring(., 7, 1)='-'">
                    <a href="{$basepath}/experiments/{.}">ArrayExpress - <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:when test="substring(., 1, 3)='SRP'">
                    <a href="ftp://ftp.ncbi.nlm.nih.gov/sra/Studies/{substring(.,1,6)}/{.}/">NCBI SRA - <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:when test="substring(., 1, 3)='ERA'">
                    <a href="ftp://ftp.era.ebi.ac.uk/vol1/{substring(.,1,6)}/{.}/">EBI SRA Data - <xsl:apply-templates select="." mode="highlight" /></a>,
                    <a href="ftp://ftp.era-xml.ebi.ac.uk/{substring(.,1,6)}/{.}/">EBI SRA Meta-data - <xsl:apply-templates select="." mode="highlight" /></a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="." mode="highlight" />
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="position() != last() and string-length(.) > 0">, </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="providers">
        <xsl:for-each select="provider[not(contact=following-sibling::provider/contact) and role!='data_coder']">
            <xsl:sort select="role='submitter'" order="descending"/>
            <xsl:sort select="ae:toLowerCase(contact)"/>
            <xsl:choose>
                <xsl:when test="role='submitter' and string-length(email) > 0">
                    <a href="mailto:{email}"><xsl:apply-templates select="contact" mode="highlight"/> &lt;<xsl:apply-templates select="email" mode="highlight"/>&gt;</a>
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
                        <xsl:with-param name="text" select="ae:markKeywords(substring-before($text, '&lt;br&gt;'),$keywords)"/>
                    </xsl:call-template>
                </div>
                <xsl:call-template name="description">
                    <xsl:with-param name="text" select="substring-after($text,'&lt;br&gt;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <div>
                    <xsl:call-template name="add_highlight_element">
                        <xsl:with-param name="text" select="ae:markKeywords($text,$keywords)"/>
                    </xsl:call-template>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="highlight">
        <xsl:variable name="vText" select="normalize-space(.)"/>
        <xsl:choose>
            <xsl:when test="string-length($vText)!=0">
                <xsl:variable name="markedtext" select="ae:markKeywords($vText,$keywords)"/>
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
                <xsl:variable name="markedtext" select="ae:markKeywords($vText,$keywords)"/>
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
        <xsl:if test="$count &lt; 5">
            <xsl:choose>
                <xsl:when test="$count &lt; $stars">
                    <img src="{$basepath}/assets/images/miame_star.gif" width="14" height="13" alt="*"/>
                </xsl:when>
                <xsl:otherwise>
                    <img src="{$basepath}/assets/images/miame_nostar.gif" width="14" height="13" alt="."/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="miame-star">
                <xsl:with-param name="stars" select="$stars"/>
                <xsl:with-param name="count" select="$count + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="miame-desc">
        <xsl:param name="pScores"/>
        <xsl:param name="pFilter"/>

        <xsl:if test="$pScores/reportersequencescore = $pFilter">
            <xsl:text>arrays</xsl:text>
            <xsl:if test="($pScores//protocolscore = $pFilter) or ($pScores/factorvaluescore = $pFilter) or ($pScores/derivedbioassaydatascore = $pFilter) or ($pScores/measuredbioassaydatascore = $pFilter)">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$pScores/protocolscore = $pFilter">
            <xsl:text>protocols</xsl:text>
            <xsl:if test="($pScores/factorvaluescore = $pFilter) or ($pScores/derivedbioassaydatascore = $pFilter) or ($pScores/measuredbioassaydatascore = $pFilter)">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$pScores/factorvaluescore = $pFilter">
            <xsl:text>factors</xsl:text>
            <xsl:if test="($pScores/derivedbioassaydatascore = $pFilter) or ($pScores/measuredbioassaydatascore = $pFilter)">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$pScores/derivedbioassaydatascore = $pFilter">
            <xsl:text>processed data</xsl:text>
            <xsl:if test="$pScores/measuredbioassaydatascore = $pFilter">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$pScores/measuredbioassaydatascore = $pFilter">
            <xsl:text>raw data</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template name="miame-score">
        <xsl:param name="pScores"/>

        <table class="miame-tbl" border="0" cellpadding="0" cellspacing="0">
            <thead>
                <tr>
                    <th>Array designs</th>
                    <th>Protocols</th>
                    <th>Factors</th>
                    <th>Processed data</th>
                    <th>Raw data</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <xsl:call-template name="miame-tick">
                            <xsl:with-param name="pValue" select="$pScores/reportersequencescore"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:call-template name="miame-tick">
                            <xsl:with-param name="pValue" select="$pScores/protocolscore"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:call-template name="miame-tick">
                            <xsl:with-param name="pValue" select="$pScores/factorvaluescore"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:call-template name="miame-tick">
                            <xsl:with-param name="pValue" select="$pScores/derivedbioassaydatascore"/>
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:call-template name="miame-tick">
                            <xsl:with-param name="pValue" select="$pScores/measuredbioassaydatascore"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template name="miame-tick">
        <xsl:param name="pValue"/>

        <xsl:choose>
            <xsl:when test="$pValue='1'">
                <img src="{$basepath}/assets/images/silk_tick.gif" width="16" height="16" alt="*"/>
            </xsl:when>
            <xsl:otherwise>
                <img src="{$basepath}/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="data-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[kind = 'raw' or kind = 'fgem']">
            <tr>
                <td class="attr_name">Data Archives</td>
                <td class="attr_value">
                    <xsl:for-each select="file[kind = 'raw' or kind = 'fgem']">
                        <xsl:sort select="kind"/>
                        <xsl:sort select="ae:toLowerCase(name)"/>
                        <a href="{$vBaseUrl}/{relativepath}">
                            <xsl:value-of select="name"/>
                        </a>
                        <xsl:if test="position() != last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template name="magetab-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:for-each select="file[extension = 'txt' and (kind = 'idf' or kind = 'sdrf')]">
            <xsl:sort select="kind"/>
            <tr>
                <td class="attr_name">
                    <xsl:choose>
                        <xsl:when test="kind = 'idf'">Investigation Description</xsl:when>
                        <xsl:when test="kind = 'sdrf'">Sample and Data Relationship</xsl:when>
                    </xsl:choose>
                </td>
                <td class="attr_value">
                    <a href="{$vBaseUrl}/{relativepath}"><xsl:value-of select="name"/></a>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="magetab-files-array">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[extension = 'txt' and kind = 'adf']">
            <tr>
                <td class="attr_name">Array Design</td>
                <td class="attr_value">
                    <xsl:for-each select="file[extension = 'txt' and kind = 'adf']">
                        <xsl:sort select="name"/>
                        <a href="{$vBaseUrl}/{relativepath}"><xsl:value-of select="name"/></a>
                        <xsl:if test="position() != last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template name="image-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:if test="file[kind = 'biosamples' and (extension = 'png' or extension = 'svg')]">
            <tr>
                <td class="attr_name">Experiment Design Images</td>
                <td class="attr_value">
                    <xsl:for-each select="file[kind = 'biosamples' and (extension = 'png' or extension = 'svg')]">
                        <xsl:sort select="ae:toLowerCase(extension)"/>
                        <a href="{$vBaseUrl}/{relativepath}"><xsl:value-of select="name"/></a>
                        <xsl:if test="position() != last()">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template name="data-files-main">
        <xsl:param name="pKind"/>
        <xsl:variable name="vFiles" select="file[kind = $pKind]"/>
        <xsl:variable name="vImg">
            <xsl:choose>
                <xsl:when test="$pKind = 'raw' and contains($vFiles[1]/dataformat, 'CEL')">
                    <img src="{$basepath}/assets/images/silk_data_save_affy.gif" width="16" height="16" alt="Click to download Affymetrix data"/>
                </xsl:when>
                <xsl:when test="$pKind = 'raw'">
                    <img src="{$basepath}/assets/images/silk_data_save.gif" width="16" height="16" alt="Click to download raw data"/>
                </xsl:when>
                <xsl:otherwise>
                    <img src="{$basepath}/assets/images/silk_data_save.gif" width="16" height="16" alt="Click to download processed data"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="vLinkUrl">
            <xsl:choose>
                <xsl:when test="count($vFiles) > 1">
                    <xsl:value-of select="$basepath"/>
                    <xsl:text>/files/</xsl:text>
                    <xsl:value-of select="accession"/>
                    <xsl:text>?kind=</xsl:text>
                    <xsl:value-of select="$pKind"/>
                </xsl:when>
                <xsl:when test="$vFiles">
                    <xsl:value-of select="$vBaseUrl"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="$vFiles/relativepath"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="string-length($vLinkUrl)>0"><a href="{$vLinkUrl}"><xsl:copy-of select="$vImg"/></a></xsl:when>
            <xsl:otherwise><img src="{$basepath}/assets/images/silk_data_unavail.gif" width="16" height="16" alt="-"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
