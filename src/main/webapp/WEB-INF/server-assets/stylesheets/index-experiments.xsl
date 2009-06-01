<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
                xmlns:lucene="java:/uk.ac.ebi.arrayexpress.utils.saxon.search.LuceneElementFactory"
                extension-element-prefixes="ae lucene"
                exclude-result-prefixes="ae lucene"
                version="1.0">
    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>
    <xsl:template match="/experiments">
        <xsl:value-of select="ae:createIndex()"/>
        <xsl:apply-templates select="experiment"/>
        <xsl:value-of select="ae:commitIndex()"/>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:variable name="vIndexDocId" select="ae:createIndexDocument()"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'text', .//*, true(), true(), false())"/>
        <xsl:for-each select="species">
            <xsl:value-of select="ae:addIndexField($vIndexDocId, 'species', ae:normalizeSpecies(.), false(), false(), false())"/>
        </xsl:for-each>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'user', user, false(), false(), false())"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'array_id', arraydesign/id, false(), false(), false())"/>
        <xsl:value-of select="ae:addIndexField($vIndexDocId, 'exp_type', experimenttype, false(), false(), false())"/>
        <xsl:value-of select="ae:addDocumentToIndex($vIndexDocId)"/>
    </xsl:template>

<!--
    <xsl:template match="/experiments">
        <xsl:variable name="expIndex" as="java:java.lang.Object" xmlns:java="http://saxon.sf.net/java-type">
            <lucene:create name="experiments" storage="memory">
                <xsl:fallback>
                    <xsl:message terminate="yes">Lucene extension is not installed</xsl:message>
                </xsl:fallback>
            </lucene:create>
        </xsl:variable>
        <xsl:apply-templates select="experiment">
            <xsl:with-param name="index" select="$expIndex"/>
        </xsl:apply-templates>
        <lucene:commit index="$expIndex"/>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:param name="index"/>
        <lucene:document index="$expIndex" select=".">
            <lucene:field name="text" select="." analyzed="true"/>
        </lucene:document>
    </xsl:template>
-->

</xsl:stylesheet>
    