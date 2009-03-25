<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
        extension-element-prefixes="ae html"
        exclude-result-prefixes="ae html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-species" match="species" use="ae:normalizeSpecies(.)"/>

    <xsl:template match="/experiments">
        <option value="">Any species</option>
        <xsl:for-each select=".//species[generate-id(key('distinct-species', ae:normalizeSpecies(.))) = generate-id()]">
            <xsl:sort select="ae:normalizeSpecies(.)"/>
            <option value="{ae:normalizeSpecies(.)}">
                <xsl:value-of select="ae:normalizeSpecies(.)"/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
