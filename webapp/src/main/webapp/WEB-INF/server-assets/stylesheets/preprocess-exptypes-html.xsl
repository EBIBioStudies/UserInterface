<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:html="http://www.w3.org/1999/xhtml"
        xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
        extension-element-prefixes="ae html"
        exclude-result-prefixes="ae html"
        version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-exptypes" match="experimenttype" use="ae:toLowerCase(.)"/>

    <xsl:template match="/experiments">
        <option value="">Any experiment type</option>
        <xsl:for-each select=".//experimenttype[generate-id(key('distinct-exptypes', ae:toLowerCase(.)))=generate-id()]">
            <xsl:sort select="ae:toLowerCase(.)"/>
            <option value="{.}">
                <xsl:value-of select="."/>
            </option>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>