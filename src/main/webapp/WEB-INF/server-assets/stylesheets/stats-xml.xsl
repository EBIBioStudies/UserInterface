<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:template match="/">
        <experiments total="{experiments/@total}" total-hybs="{experiments/@total-hybs}" avail="{experiments/@total > 0}"/>
        <xsl:apply-templates select="whatever"/>
    </xsl:template>

</xsl:stylesheet>
