<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension">

    <xsl:param name="species"/>
    <xsl:param name="array"/>
    <xsl:param name="keywords"/>
    <xsl:param name="wholewords"/>

    <xsl:param name="sortby">releasedate</xsl:param>
    <xsl:param name="sortorder">descending</xsl:param>
    
    <xsl:output method="text" indent="no" encoding="ISO-8859-1"/>

    <xsl:include href="ae-filter-experiments.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/experiments">
        <xsl:text>Accession</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Title</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Assays</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Species</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Release Date</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Processed Data</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>Raw Data</xsl:text>
        <xsl:text>&#9;</xsl:text>
        <xsl:text>ArrayExpress URL</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:call-template name="ae-sort-experiments">
            <xsl:with-param name="pExperiments" select="ae:filter-experiments($keywords,$wholewords,$species,$array)"/>
            <xsl:with-param name="pSortBy" select="$sortby"/>
            <xsl:with-param name="pSortOrder" select="$sortorder"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="experiment">
        <xsl:value-of select="accession"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="assays"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="list-species"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="releasedate" />
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="list-data">
            <xsl:with-param name="node" select="files/fgem"/>
        </xsl:call-template>
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="list-data">
            <xsl:with-param name="node" select="files/raw"/>
        </xsl:call-template>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="concat('http://www.ebi.ac.uk/microarray-as/ae/experiments/',accession)"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template name="list-species">
        <xsl:for-each select="species">
            <xsl:value-of select="."/>
                <xsl:if test="position() != last()">, </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="list-data">
        <xsl:param name="node"/>
        <xsl:choose>
            <xsl:when test="helper:isFileAvailableForDownload($node/@name)"><xsl:value-of select="concat('http://www.ebi.ac.uk/microarray-as/ae/download/',$node/@name)"/></xsl:when>
            <xsl:otherwise><xsl:text>Data is not yet available</xsl:text></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
