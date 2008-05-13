<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:helper="uk.ac.ebi.ae15.HelperXsltExtension"
                extension-element-prefixes="helper"
                exclude-result-prefixes="helper"
                version="1.0">
    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

    <xsl:key name="expt-species" match="sampleattribute[@category='Organism']" use="concat(ancestor::experiment/@accession,@value)"/>
    <xsl:key name="expt-array" match="arraydesign" use="concat(ancestor::experiment/@accession,@name)"/>

    <xsl:template match="/experiments">
        <experiments total="{count(experiment)}">
            <xsl:apply-templates select="experiment">
                <xsl:sort order="descending" select="substring-before(@releasedate,'-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-before(substring-after(@releasedate,'-'),'-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-after(substring-after(@releasedate,'-'),'-')" data-type="number"/>
            </xsl:apply-templates>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">

        <xsl:variable name="species">
            <xsl:for-each
                select="sampleattribute[@category='Organism'][generate-id(key('expt-species',concat(ancestor::experiment/@accession,@value)))=generate-id()]">
                <xsl:value-of select="@value"/>
                <xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="arrays">
            <xsl:for-each
                select="arraydesign[generate-id(key('expt-array',concat(ancestor::experiment/@accession,@name)))=generate-id()]">
                <xsl:value-of select="@name"/>
                <xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <experiment>
            <xsl:for-each select="@*">
                <xsl:element name="{helper:toLowerCase(name())}">
                    <xsl:value-of select="." />
                </xsl:element>
            </xsl:for-each>
            <species><xsl:value-of select="$species"/></species>
            <arrays><xsl:value-of select="$arrays"/></arrays>
            <samples><xsl:value-of select="substring-before(substring-after(description[contains(.,'(Generated description)')],'using '),' samples')"/></samples>
            <hybs><xsl:value-of select="substring-before( substring-after(description[contains(.,'(Generated description)')], 'with '),' hybridizations')"/></hybs>
            

            <xsl:if test="helper:isOnFtp(concat(@accnum,'.processed.zip'))">
                <xsl:attribute name="fgem">download/mageml/<xsl:value-of select="@accnum"/>.processed.zip</xsl:attribute>
                <xsl:attribute name="fgem-count"><xsl:value-of select="sum(.//bioassaydatagroup[@is_derived='1']/@bioassay_count)"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="helper:isOnFtp(concat(@accnum,'.raw.zip'))">
                <xsl:attribute name="raw">download/mageml/<xsl:value-of select="@accnum"/>.raw.zip</xsl:attribute>
                <xsl:attribute name="raw-count"><xsl:value-of select="sum(.//bioassaydatagroup[@is_derived='0']/@bioassay_count)"/></xsl:attribute>
                <xsl:attribute name="cel-count"><xsl:value-of select="sum(bioassaydatagroups/bioassaydatagroup[@is_derived='0'][starts-with(@dataformat,'CEL')]/@bioassay_count)"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="helper:isOnFtp(concat(@accnum,'.2columns.txt'))">
                <xsl:attribute name="two-columns">download/mageml/<xsl:value-of select="@accnum"/>.2columns.txt</xsl:attribute>
            </xsl:if>
            <xsl:if test="helper:isOnFtp(concat(@accnum,'.sdrf.txt'))">
                <xsl:attribute name="sdrf">download/mageml/<xsl:value-of select="@accnum"/>.sdrf.txt</xsl:attribute>
            </xsl:if>
            <xsl:if test="helper:isOnFtp(concat(@accnum,'.biosamples.png'))">
                <xsl:attribute name="biosamples-png">download/mageml/<xsl:value-of select="@accnum"/>.biosamples.png</xsl:attribute>
            </xsl:if>
            <xsl:if test="helper:isOnFtp(concat(@accnum,'.biosamples.svg'))">
                <xsl:attribute name="biosamples-svg">download/mageml/<xsl:value-of select="@accnum"/>.biosamples.svg</xsl:attribute>
            </xsl:if>

            <xsl:apply-templates mode="copy" />
        </experiment>
    </xsl:template>

    <xsl:template match="miamescore" mode="copy">
        <miamescores>
            <xsl:for-each select="score">
                <xsl:element name="{helper:toLowerCase(@name)}">
                    <xsl:value-of select="@value"/>
                </xsl:element>
            </xsl:for-each>
            <overallscore>
                <xsl:value-of select="sum(score/@value)"/>
            </overallscore>
        </miamescores>
    </xsl:template>

    <xsl:template match="*" mode="copy">
        <xsl:copy>
            <xsl:if test="@*">
                <xsl:for-each select="@*">
                    <xsl:element name="{helper:toLowerCase(name())}">
                        <xsl:value-of select="." />
                    </xsl:element>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates mode="copy" />
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
