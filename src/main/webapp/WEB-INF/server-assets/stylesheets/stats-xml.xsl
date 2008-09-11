<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>
    <xsl:param name="userid"/>

    <xsl:template match="/experiments">
        <experiments total="{count(experiment[user/text()='1' or $userid='0' or user/text()=$userid])}"
                     total-samples="{sum(experiment[user/text()='1' or $userid='0' or user/text()=$userid][samples/text()>0]/samples/text())}"
                     total-assays="{sum(experiment[user/text()='1' or $userid='0' or user/text()=$userid][assays/text()>0]/assays/text())}"/>
    </xsl:template>

</xsl:stylesheet>
