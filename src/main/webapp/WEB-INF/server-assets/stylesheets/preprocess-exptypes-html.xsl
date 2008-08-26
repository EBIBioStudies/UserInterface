<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
        extension-element-prefixes="helper html"
        exclude-result-prefixes="helper html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-exptypes" match="experimenttype" use="helper:toLowerCase(text())"/>

    <xsl:template match="/experiments">
        <option value="">Any experiment type</option>
        <xsl:for-each select=".//experimenttype[generate-id(key('distinct-exptypes',helper:toLowerCase(text())))=generate-id()]">
            <xsl:sort select="text()"/>
            <option value="{helper:toLowerCase(text())}">
                <xsl:value-of select="text()"/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
