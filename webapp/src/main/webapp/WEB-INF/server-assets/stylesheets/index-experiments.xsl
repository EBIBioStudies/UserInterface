<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
                extension-element-prefixes="ae"
                exclude-result-prefixes="ae"
                version="2.0">

    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>
    <xsl:template match="/experiments">
        <xsl:value-of select="ae:createIndex()"/>
        <xsl:apply-templates select="experiment"/>
        <xsl:value-of select="ae:commitIndex()"/>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:variable name="vIndexDocId" select="ae:createIndexDocument()"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'text', string-join(.//text(), ' '), false(), true(), false())"/>
        <xsl:for-each select="species">
            <xsl:value-of select="ae:addIndexField($vIndexDocId, 'species', ae:normalizeSpecies(.), false(), false(), false())"/>
        </xsl:for-each>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'user', user, false(), false(), false())"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'array_id', arraydesign/id, false(), false(), false())"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'exp_type', experimenttype, false(), false(), false())"/>
        <xsl:value-of select="ae:addDocumentToIndex($vIndexDocId)"/>
    </xsl:template>

</xsl:stylesheet>
    