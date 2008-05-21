<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                extension-element-prefixes="helper"
                exclude-result-prefixes="helper"
                version="1.0">
    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

    <xsl:key name="expt-species" match="sampleattribute[@category='Organism']" use="concat(ancestor::experiment/@accession,@value)"/>

    <xsl:template match="/experiments">
        <experiments
            version="{@version}"
            total="{count(experiment)}"
            total-samples="{sum(experiment[@samples>0]/@samples)}"
            total-hybs="{sum(experiment[@hybs>0]/@hybs)}">

            <xsl:apply-templates select="experiment">
                <xsl:sort order="descending" select="substring-before(@releasedate,'-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-before(substring-after(@releasedate,'-'),'-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-after(substring-after(@releasedate,'-'),'-')" data-type="number"/>
            </xsl:apply-templates>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <experiment>
            <xsl:for-each select="@*">
                <xsl:element name="{helper:toLowerCase(name())}">
                    <xsl:value-of select="." />
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="sampleattribute[@category='Organism'][generate-id(key('expt-species',concat(ancestor::experiment/@accession,@value)))=generate-id()]">
                <species><xsl:value-of select="@value"/></species>
            </xsl:for-each>
            <samples>
                <xsl:value-of select="substring-before(substring-after(description[contains(.,'(Generated description)')],'using '),' samples')"/>
            </samples>
            <hybs>
                <xsl:value-of select="substring-before( substring-after(description[contains(.,'(Generated description)')], 'with '),' hybridizations')"/>
            </hybs>
            <files>
                <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.processed.zip'))">
                    <fgem>
                        <xsl:attribute name="url"><xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.processed.zip'))"/></xsl:attribute>
                        <xsl:attribute name="count"><xsl:value-of select="sum(bioassaydatagroup[@isderived='1']/@bioassays)"/></xsl:attribute>
                    </fgem>
                </xsl:if>
                <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.raw.zip'))">
                    <raw>
                        <xsl:attribute name="url"><xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.raw.zip'))"/></xsl:attribute>
                        <xsl:attribute name="count"><xsl:value-of select="sum(bioassaydatagroup[@isderived='0']/@bioassays)"/></xsl:attribute>
                        <xsl:attribute name="celcount"><xsl:value-of select="sum(bioassaydatagroup[@isderived='0'][contains(@dataformat,'CEL')]/@bioassays)"/></xsl:attribute>
                    </raw>
                </xsl:if>
                <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.2columns.txt'))">
                    <twocolumns>
                        <xsl:attribute name="url">
                            <xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.2columns.txt'))"/>
                        </xsl:attribute>
                    </twocolumns>
                </xsl:if>
                <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.sdrf.txt'))">
                    <sdrf>
                        <xsl:attribute name="url">
                            <xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.sdrf.txt'))"/>
                        </xsl:attribute>
                    </sdrf>
                </xsl:if>
                <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.biosamples.png')) or helper:isFileAvailableForDownload(concat(@accession,'.biosamples.svg'))">
                    <biosamples>
                        <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.biosamples.png'))">
                            <png>
                                <xsl:attribute name="url"><xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.biosamples.png'))"/></xsl:attribute>
                            </png>
                        </xsl:if>
                        <xsl:if test="helper:isFileAvailableForDownload(concat(@accession,'.biosamples.svg'))">
                            <svg>
                                <xsl:attribute name="url"><xsl:value-of select="helper:getFileDownloadUrl(concat(@accession,'.biosamples.svg'))"/></xsl:attribute>
                            </svg>
                        </xsl:if>
                    </biosamples>
                </xsl:if>
            </files>

            <xsl:apply-templates mode="copy" />
        </experiment>
    </xsl:template>

    <xsl:template match="miamescore" mode="copy">
        <miamescores>
            <xsl:for-each select="score">
                <xsl:element name="{helper:toLowerCase(@name)}">
                    <xsl:value-of select="@value"/>
                </xsl:element>
            </xsl:for-each>
            <overallscore>
                <xsl:value-of select="sum(score/@value)"/>
            </overallscore>
        </miamescores>
    </xsl:template>

    <xsl:template match="bibliography[@pages='' or @pages='-']" mode="copy">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:if test="helper:toLowerCase(name())!='pages'">
                    <xsl:element name="{helper:toLowerCase(name())}">
                        <xsl:value-of select="." />
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="description" mode="copy">
        <description>
            <id><xsl:value-of select="@id"/></id>
            <text><xsl:value-of select="text()"/></text>
        </description>
    </xsl:template>

    <xsl:template match="*" mode="copy">
        <xsl:copy>
            <xsl:if test="@*">
                <xsl:for-each select="@*">
                    <xsl:element name="{helper:toLowerCase(name())}">
                        <xsl:value-of select="." />
                    </xsl:element>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates mode="copy" />
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
