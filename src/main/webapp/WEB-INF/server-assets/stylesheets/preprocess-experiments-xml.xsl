<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="helper"
                exclude-result-prefixes="helper"
                version="1.0">
    <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

    <xsl:key name="experiment-species-by-name" match="sampleattribute[@category='Organism']" use="concat(ancestor::experiment/@id, @value)"/>
    <xsl:key name="experiment-sampleattribute-by-category" match="sampleattribute" use="concat(ancestor::experiment/@id, @category)"/>
    <xsl:key name="experiment-experimentalfactor-by-name" match="experimentalfactor" use="concat(ancestor::experiment/@id, @name)"/>

    <xsl:template match="/experiments">
        <experiments
            version="{@version}" total="{count(experiment)}">

            <xsl:apply-templates select="experiment">
                <xsl:sort order="descending" select="substring-before(@releasedate, '-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-before(substring-after(@releasedate, '-'), '-')" data-type="number"/>
                <xsl:sort order="descending" select="substring-after(substring-after(@releasedate, '-'), '-')" data-type="number"/>
            </xsl:apply-templates>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <experiment>
            <xsl:for-each select="@*">
                <xsl:element name="{helper:toLowerCase(name())}">
                    <xsl:value-of select="." />
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="sampleattribute[@category = 'Organism'][generate-id() = generate-id(key('experiment-species-by-name',concat(ancestor::experiment/@id, @value))[1])]">
                <species><xsl:value-of select="@value"/></species>
            </xsl:for-each>
            <samples>
                <xsl:value-of select="substring-before(substring-after(description[contains(., '(Generated description)')], 'using '), ' samples')"/>
            </samples>
            <assays>
                <xsl:value-of select="substring-before( substring-after(description[contains(., '(Generated description)')], 'with '), ' hybridizations')"/>
            </assays>
            <files>
                <fgem name="{concat(@accession, '.processed.zip')}"
                      count="{sum(bioassaydatagroup[@isderived = '1']/@bioassays)}"/>
                <raw name="{concat(@accession, '.raw.zip')}"
                     count="{sum(bioassaydatagroup[@isderived = '0']/@bioassays)}"
                     celcount="{sum(bioassaydatagroup[@isderived = '0'][contains(@dataformat, 'CEL')]/@bioassays)}"/>
                <twocolumns name="{concat(@accession, '.2columns.txt')}"/>
                <sdrf name="{concat(@accession, '.sdrf.txt')}"/>
                <biosamples>
                    <png name="{concat(@accession, '.biosamples.png')}"/>
                    <svg name="{concat(@accession, '.biosamples.svg')}"/>
                </biosamples>
            </files>
            <xsl:for-each select="sampleattribute[@category][generate-id() = generate-id(key('experiment-sampleattribute-by-category', concat(ancestor::experiment/@id, @category))[1])]">
                <xsl:sort select="@category" order="ascending"/>
                <sampleattribute>
                    <category><xsl:value-of select="@category"/></category>
                    <xsl:for-each select="key('experiment-sampleattribute-by-category', concat(ancestor::experiment/@id, @category))">
                        <xsl:sort select="@value" order="ascending"/>
                        <value><xsl:value-of select="@value"/></value>
					</xsl:for-each>
                </sampleattribute>
            </xsl:for-each>
            <xsl:for-each select="experimentalfactor[@name][generate-id() = generate-id(key('experiment-experimentalfactor-by-name', concat(ancestor::experiment/@id, @name))[1])]">
                <xsl:sort select="@name" order="ascending"/>
                <experimentalfactor>
                    <name><xsl:value-of select="@name"/></name>
                    <xsl:for-each select="key('experiment-experimentalfactor-by-name', concat(ancestor::experiment/@id, @name))">
                        <xsl:sort select="@value" order="ascending"/>
                        <value><xsl:value-of select="@value"/></value>
					</xsl:for-each>
                </experimentalfactor>
            </xsl:for-each>
            <xsl:apply-templates select="*[name() != 'sampleattribute' and name() != 'experimentalfactor']" mode="copy" />
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

    <xsl:template match="bibliography[@pages = '' or @pages = '-']" mode="copy">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:if test="helper:toLowerCase(name()) != 'pages'">
                    <xsl:element name="{helper:toLowerCase(name())}">
                        <xsl:value-of select="." />
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="description" mode="copy">
        <description>
            <id><xsl:value-of select="@id"/></id>
            <text><xsl:value-of select="text()"/></text>
        </description>
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
