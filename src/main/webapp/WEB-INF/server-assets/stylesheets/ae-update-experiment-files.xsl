<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="helper"
                exclude-result-prefixes="helper"
                version="1.0">

    <xsl:key name="distinct-raw-dataformat" match="bioassaydatagroup[@isderived = '0']" use="concat(ancestor::experiment/@id, @dataformat)"/>
    <xsl:key name="distinct-fgem-dataformat" match="bioassaydatagroup[@isderived = '1']" use="concat(ancestor::experiment/@id, @dataformat)"/>

    <!-- should be executed in the experiment context -->
    <xsl:template name="update-files">
        <xsl:copy>
            <xsl:for-each select="helper:getFilesForExperiment(@accession)/file">
                <xsl:element name="file">
                    <xsl:for-each select="@*">
                        <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                    </xsl:for-each>
                    <xsl:choose>
                        <xsl:when test="contains(@name, '.processed.zip')">
                            <xsl:attribute name="bioassays"><xsl:value-of select="sum(bioassaydatagroup[@isderived = '1']/@bioassays)"/></xsl:attribute>
                            <xsl:attribute name="dataformat">
                                <xsl:call-template name="list-fgem-dataformats"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="contains(@name, '.raw.zip')">
                            <xsl:attribute name="bioassays"><xsl:value-of select="sum(bioassaydatagroup[@isderived = '0']/@bioassays)"/></xsl:attribute>
                            <xsl:attribute name="dataformat">
                                <xsl:call-template name="list-raw-dataformats"/>
                            </xsl:attribute>
                        </xsl:when>
                    </xsl:choose>
                </xsl:element>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="list-fgem-dataformats">
        <xsl:for-each select="bioassaydatagroup[generate-id(key('distinct-fgem-dataformat', concat(ancestor::experiment/@id, @dataformat))) = generate-id()]">
            <xsl:value-of select="@dataformat"/>
            <xsl:if test="position()!=last()"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="list-raw-dataformats">
        <xsl:for-each select="bioassaydatagroup[generate-id(key('distinct-raw-dataformat', concat(ancestor::experiment/@id, @dataformat))) = generate-id()]">
            <xsl:value-of select="@dataformat"/>
            <xsl:if test="position()!=last()"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>