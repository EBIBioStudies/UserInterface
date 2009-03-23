<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.AESaxonExtension"
        extension-element-prefixes="ae html"
        exclude-result-prefixes="ae html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-species" match="species" use="ae:normalizeSpecies(text())"/>

    <xsl:template match="/experiments">
        <option value="">Any species</option>
        <xsl:for-each select=".//species[generate-id(key('distinct-species',ae:normalizeSpecies(text())))=generate-id()]">
            <xsl:sort select="ae:normalizeSpecies(text())"/>
            <option value="{ae:normalizeSpecies(text())}">
                <xsl:value-of select="ae:normalizeSpecies(text())"/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
