<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                version="2.0">
    <xsl:output method="xml" version="1.0" encoding="UTF8" indent="yes"/>

    <xsl:variable name="vRoot" select="/files/@root"/>
    <xsl:variable name="vExperiments" select="doc('experiments.xml')"/>

    <xsl:template match="/files">
        <files root="{$vRoot}">
            <xsl:apply-templates/>
        </files>
    </xsl:template>

    <xsl:template match="folder">
        <xsl:variable name="vFolder">
            <xsl:analyze-string select="@location" regex="[/\\](array|experiment|protocol)[/\\].*([AEP]-\w{{4}}-\d+)$" flags="i">
                <xsl:matching-substring>
                    <kind><xsl:value-of select="regex-group(1)"/></kind>
                    <accession><xsl:value-of select="upper-case(regex-group(2))"/></accession>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$vFolder/accession">
                <folder accession="{$vFolder/accession}" kind="{$vFolder/kind}" location="{replace(@location, $vRoot, '')}">
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
            <xsl:if test="@kind = 'raw' or @kind = 'fgem'">
                <xsl:call-template name="add-dataformat-attribute">
                    <xsl:with-param name="pName" select="@name"/>
                    <xsl:with-param name="pKind" select="@kind"/>
                </xsl:call-template>
            </xsl:if>
        </file>
    </xsl:template>

    <xsl:template name="add-dataformat-attribute">
        <xsl:param name="pName"/>
        <xsl:param name="pKind"/>
    
        <xsl:variable name="vAccession" select="substring-before($pName, '.')"/>

        <xsl:attribute name="dataformat" select="ae:dataformats($vAccession, $pKind)"/>
        
    </xsl:template>
    
    
    <xsl:function name="ae:dataformats">
        <xsl:param name="pAccession"/>
        <xsl:param name="pKind"/>
        <xsl:variable name="vBDG" select="$vExperiments/experiments/experiment[accession = $pAccession]/bioassaydatagroup"/>
        <xsl:variable name="vIsDerived">
            <xsl:choose>
                <xsl:when test="$pKind = 'fgem'">
                    <xsl:text>1</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>0</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="string-join(distinct-values($vBDG[isderived = $vIsDerived]/dataformat), ', ')"/>
    </xsl:function>
</xsl:stylesheet>
