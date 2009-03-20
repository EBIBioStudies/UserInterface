<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.AESaxonExtension"
    extension-element-prefixes="ae html"
    exclude-result-prefixes="ae html"
    version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-array" match="arraydesign" use="name"/>

    <xsl:template match="/experiments">
        <option value="">Any array</option>
        <optgroup label="Affymetrix arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Affy')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Agilent arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Agilent')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Amersham arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Amersham')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="BuG@S arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'BuG@S')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="CATMA arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'CATMA')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="EMBL arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'EMBL')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="ILSI arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'ILSI')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="MIT arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'MIT')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Sanger Institute arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Sanger')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Stanford (SMD) arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'SMD')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="TIGR arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'TIGR')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Utrecht (UMC) arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Utrecht')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Yale arrays">
                <xsl:apply-templates
                        select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][contains(name,'Yale')]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

            <optgroup label="Other arrays">
                <xsl:apply-templates select=".//arraydesign[generate-id(key('distinct-array',name))=generate-id()][not(ae:testRegexp(name/text(),'Affy|Agilent|Amersham|BuG@S|CATMA|EMBL|ILSI|MIT|Sanger|SMD|TIGR|Utrecht|Yale','i'))]">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
            </optgroup>

    </xsl:template>

    <xsl:template match="arraydesign">
        <option value="{id}">
            <xsl:value-of select="name"/>
        </option>
    </xsl:template>
</xsl:stylesheet>
