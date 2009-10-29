<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
    <xsl:output method="xml" version="1.0" encoding="UTF8" indent="yes"/>

    <xsl:variable name="vRoot" select="/files/@root"/>
    
    <xsl:template match="/files">
        <files root="{@root}">
            <xsl:apply-templates/>
        </files>
    </xsl:template>

    <xsl:template match="folder">
        <xsl:variable name="vFolder">
            <xsl:analyze-string select="@location" regex="(array|experiment|protocol).*([AEP]-\w{{4}}-\d+)$" flags="i">
                <xsl:matching-substring>
                    <kind><xsl:value-of select="regex-group(1)"/></kind>
                    <accession><xsl:value-of select="upper-case(regex-group(2))"/></accession>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$vFolder/accession">
                <folder accession="{$vFolder/accession}" kind="{$vFolder/kind}" location="{@location}">
                    <xsl:apply-templates/>
                </folder>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="file">
        <file>
            <xsl:copy-of select="*|@*"/>
        </file>
    </xsl:template>

</xsl:stylesheet>
