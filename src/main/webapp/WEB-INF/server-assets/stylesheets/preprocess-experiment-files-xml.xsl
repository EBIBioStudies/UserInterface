<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="helper"
                exclude-result-prefixes="helper"
                version="1.0">
    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

    <xsl:key name="distinct-raw-dataformat" match="bioassaydatagroup[isderived = '0']" use="concat(ancestor::experiment/id, dataformat)"/>
    <xsl:key name="distinct-fgem-dataformat" match="bioassaydatagroup[isderived = '1']" use="concat(ancestor::experiment/id, dataformat)"/>

    <xsl:template match="/experiments">
        <experiments version="{@version}" total="{@total}">
            <xsl:apply-templates select="experiment"/>
        </experiments>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:if test="@*">
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name()}">
                        <xsl:value-of select="." />
                    </xsl:attribute>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates/>
            <xsl:if test="(name() = 'experiment') or (name() = 'arraydesign')">
                <xsl:call-template name="add-files"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="add-files">
        <xsl:variable name="vAccession" select="accession"/>
        <xsl:variable name="vProcessedBioassays" select="sum(bioassaydatagroup[isderived = '1']/bioassays)"/>
        <xsl:variable name="vProcessedDataformats">
            <xsl:call-template name="list-fgem-dataformats"/>
        </xsl:variable>
        <xsl:variable name="vRawBioassays" select="sum(bioassaydatagroup[isderived = '0']/bioassays)"/>
        <xsl:variable name="vRawDataformats">
            <xsl:call-template name="list-raw-dataformats"/>
        </xsl:variable>
        <xsl:for-each select="helper:getFilesForExperiment(accession)/file">
            <xsl:element name="file">
                <xsl:for-each select="@*">
                    <xsl:element name="{name()}"><xsl:value-of select="."/></xsl:element>
                </xsl:for-each>
                <xsl:choose>
                    <xsl:when test="contains(@name, '.processed.zip')">
                        <xsl:element name="bioassays"><xsl:value-of select="$vProcessedBioassays"/></xsl:element>
                        <xsl:element name="dataformat"><xsl:value-of select="$vProcessedDataformats"/></xsl:element>
                    </xsl:when>
                    <xsl:when test="contains(@name, '.raw.zip')">
                        <xsl:element name="bioassays"><xsl:value-of select="$vRawBioassays"/></xsl:element>
                        <xsl:element name="dataformat"><xsl:value-of select="$vRawDataformats"/></xsl:element>
                    </xsl:when>
                </xsl:choose>
                <xsl:element name="url">
                    <xsl:text>http://www.ebi.ac.uk/microarray-as/ae/files/</xsl:text>
                    <xsl:value-of select="$vAccession"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@name"/>
                </xsl:element>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="list-fgem-dataformats">
        <xsl:for-each select="bioassaydatagroup[generate-id(key('distinct-fgem-dataformat', concat(ancestor::experiment/id, dataformat))) = generate-id()]">
            <xsl:value-of select="dataformat"/>
            <xsl:if test="position()!=last()"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="list-raw-dataformats">
        <xsl:for-each select="bioassaydatagroup[generate-id(key('distinct-raw-dataformat', concat(ancestor::experiment/id, dataformat))) = generate-id()]">
            <xsl:value-of select="dataformat"/>
            <xsl:if test="position()!=last()"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>