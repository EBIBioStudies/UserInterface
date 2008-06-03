<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        extension-element-prefixes="html"
        exclude-result-prefixes="html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-species" match="species" use="text()"/>

    <xsl:template match="/experiments">
        <option value="">Any species</option>
        <xsl:for-each select=".//species[generate-id(key('distinct-species',text()))=generate-id()]">
            <xsl:sort select="text()"/>
            <option value="{text()}">
                <xsl:value-of select="text()"/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
