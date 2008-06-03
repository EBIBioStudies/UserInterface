<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
        extension-element-prefixes="helper html"
        exclude-result-prefixes="helper html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-species" match="species" use="helper:normalizeSpecies(text())"/>

    <xsl:template match="/experiments">
        <option value="">Any species</option>
        <xsl:for-each select=".//species[generate-id(key('distinct-species',helper:normalizeSpecies(text())))=generate-id()]">
            <xsl:sort select="helper:normalizeSpecies(text())"/>
            <option value="{helper:normalizeSpecies(text())}">
                <xsl:value-of select="helper:normalizeSpecies(text())"/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
