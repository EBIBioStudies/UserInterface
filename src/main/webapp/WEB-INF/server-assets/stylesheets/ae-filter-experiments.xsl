<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">

    <func:function name="ae:filter-experiments">
        <xsl:param name="pUserId"/>
        <xsl:param name="pKeywords"/>
        <xsl:param name="pWholeWords"/>
        <xsl:param name="pSpecies"/>
        <xsl:param name="pArray"/>
        <xsl:param name="pExperimentType"/>
        <xsl:param name="pInAtlas"/>

        <func:result select="experiment[helper:testExperiment(., $pUserId, $pKeywords, $pWholeWords, $pSpecies, $pArray, $pExperimentType, $pInAtlas)]"/>
    </func:function>
    
</xsl:stylesheet>