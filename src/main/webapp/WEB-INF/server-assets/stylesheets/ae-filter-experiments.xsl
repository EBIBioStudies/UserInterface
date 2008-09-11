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

        <xsl:choose>
            <xsl:when test="helper:testRegexp($pKeywords,'^E-.+-\d+$','i')">
                <xsl:variable name="queried_accnum" select="helper:toUpperCase(string($pKeywords))" />
                <func:result select="experiment[accession/text()=$queried_accnum or secondaryaccession/text()=$queried_accnum][user/text()='1' or $pUserId='0' or user/text()=$pUserId]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies!='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[helper:testSpecies(.,$pSpecies)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies!='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[helper:testSpecies(.,$pSpecies)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>

            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies!='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies!='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray!='' and $pSpecies='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas='' and $pArray='' and $pSpecies='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[helper:testExperimentType(.,$pExperimentType)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>

            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies!='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testSpecies(.,$pSpecies)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies!='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testSpecies(.,$pSpecies)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies='' and $pKeywords!='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>

            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies!='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies!='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testArray(.,$pArray)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies!='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testSpecies(.,$pSpecies)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray!='' and $pSpecies='' and $pKeywords!='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][helper:testArray(.,$pArray)][helper:testKeywords(.,$pKeywords,$pWholeWords)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies='' and $pKeywords='' and $pExperimentType!=''">
                <func:result select="experiment[@loadedinatlas='true'][helper:testExperimentType(.,$pExperimentType)][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:when test="$pInAtlas!='' and $pArray='' and $pSpecies='' and $pKeywords='' and $pExperimentType=''">
                <func:result select="experiment[@loadedinatlas='true'][user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result select="experiment[user/text()='1' or $userid='0' or user/text()=$userid]"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>
    
</xsl:stylesheet>