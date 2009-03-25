<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:ae="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
    extension-element-prefixes="ae html"
    exclude-result-prefixes="ae html"
    version="1.0">

    <xsl:output omit-xml-declaration="yes" method="html"/>
    <xsl:key name="distinct-array" match="arraydesign" use="ae:toLowerCase(name)"/>

    <xsl:template match="/experiments">
        <option value="">Any array</option>
        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Affymetrix'"/>
            <xsl:with-param name="pGroupSignature" select="'affymetrix'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Agilent'"/>
            <xsl:with-param name="pGroupSignature" select="'agilent'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Amersham'"/>
            <xsl:with-param name="pGroupSignature" select="'amersham'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'BuG@S'"/>
            <xsl:with-param name="pGroupSignature" select="'bug@s'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'CATMA'"/>
            <xsl:with-param name="pGroupSignature" select="'catma'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'EMBL'"/>
            <xsl:with-param name="pGroupSignature" select="'embl'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Illumina'"/>
            <xsl:with-param name="pGroupSignature" select="'illumina'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'ILSI'"/>
            <xsl:with-param name="pGroupSignature" select="'[ilsi]'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'MIT'"/>
            <xsl:with-param name="pGroupSignature" select="'mit'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'NimbleGen'"/>
            <xsl:with-param name="pGroupSignature" select="'nimblegen'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Sanger Institute'"/>
            <xsl:with-param name="pGroupSignature" select="'sanger'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Stanford (SMD)'"/>
            <xsl:with-param name="pGroupSignature" select="'smd'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'TIGR'"/>
            <xsl:with-param name="pGroupSignature" select="'tigr'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Utrecht (UMC)'"/>
            <xsl:with-param name="pGroupSignature" select="'umc'"/>
        </xsl:call-template>

        <xsl:call-template name="optgroup">
            <xsl:with-param name="pGroupTitle" select="'Yale'"/>
            <xsl:with-param name="pGroupSignature" select="'yale'"/>
        </xsl:call-template>

        <optgroup label="Other arrays">
            <xsl:apply-templates select=".//arraydesign[generate-id(key('distinct-array',ae:toLowerCase(name)))=generate-id()][not(ae:testRegexp(name/text(),'affymetrix|agilent|amersham|bug@s|catma|embl|illumina|ilsi|mit|nimblegen|sanger|smd|tigr|umc|yale','i'))]">
                <xsl:sort select="ae:toLowerCase(name)"/>
            </xsl:apply-templates>
        </optgroup>

    </xsl:template>

    <xsl:template name="optgroup">
        <xsl:param name="pGroupTitle"/>
        <xsl:param name="pGroupSignature"/>
        <optgroup label="{$pGroupTitle} arrays">
            <xsl:apply-templates
                    select=".//arraydesign[generate-id(key('distinct-array',ae:toLowerCase(name)))=generate-id()][contains(ae:toLowerCase(name), $pGroupSignature)]">
                <xsl:sort select="ae:toLowerCase(name)"/>
            </xsl:apply-templates>
        </optgroup>
    </xsl:template>

    <xsl:template match="arraydesign">
        <option value="{id}">
            <xsl:value-of select="name"/>
        </option>
    </xsl:template>
</xsl:stylesheet>
