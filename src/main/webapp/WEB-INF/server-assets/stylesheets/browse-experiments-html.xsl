<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="html" encoding="ISO-8859-1" indent="no"/>

    <xsl:param name="keywords"/>

    <xsl:template match="/experiments">
        <keywords><xsl:value-of select="$keywords"/></keywords>
        <experiments total="{count(experiment)}">
            <xsl:apply-templates/>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">

        <experiment>
            <xsl:copy-of select="*|@*"/>
        </experiment>
    </xsl:template>
</xsl:stylesheet>
