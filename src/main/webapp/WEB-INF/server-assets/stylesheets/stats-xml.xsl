<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:template match="/experiments">
        <experiments total="{@total}"
                     total-samples="{sum(experiment[samples/text()>0]/samples/text())}"
                     total-hybs="{sum(experiment[hybs/text()>0]/hybs/text())}"/>
    </xsl:template>

</xsl:stylesheet>
