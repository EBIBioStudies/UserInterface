<?xml version="1.0" encoding="UTF-8"?>

<!--
   - Copyright 2009-2015 European Molecular Biology Laboratory
   - Licensed under the Apache License, Version 2.0 (the "License");
   - you may not use this file except in compliance with the License.
   - You may obtain a copy of the License at
   -
   -  http://www.apache.org/licenses/LICENSE-2.0
   -
   - Unless required by applicable law or agreed to in writing, software
   - distributed under the License is distributed on an "AS IS" BASIS,
   - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   - See the License for the specific language governing permissions and
   - limitations under the License.
   -
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="fn html xs"
                exclude-result-prefixes="fn html xs"
                version="2.0">

    <xsl:include href="biostudies-html-page.xsl"/>
 

    <xsl:param name="error-code"/>
    <xsl:param name="error-request-uri"/>
    <xsl:param name="error-message"/>

    <xsl:variable name="vErrorTitle">
        <xsl:choose>
            <xsl:when test="$error-code = '400'">Bad request</xsl:when>
            <xsl:when test="$error-code = '404'">Sorry, this page is not available</xsl:when>
            <xsl:when test="$error-code = '403'">You don’t have access to that</xsl:when>
            <xsl:otherwise>Server error</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:template match="/">
        <xsl:call-template name="ae-page">
            <xsl:with-param name="pIsSearchVisible" select="fn:true()"/>
            <xsl:with-param name="pSearchInputValue"/>
            <xsl:with-param name="pTitleTrail" select="$vErrorTitle"/>
            <xsl:with-param name="pExtraCSS"/>
            <xsl:with-param name="pBreadcrumbTrail"/>
            <xsl:with-param name="pExtraJS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="ae-content-section">
        <section>
            <xsl:choose>
                <xsl:when test="$error-code = '400'">
                    <xsl:call-template name="block-warning-error">
                        <xsl:with-param name="pTitle">We’re sorry but we cannot process your request</xsl:with-param>
                        <xsl:with-param name="pMessage">There was a query syntax error in <span class="alert"><xsl:value-of select="$error-message"/></span>. Please try a different query or check our <a href="{$context-path}/help/how_to_search.html">query syntax help</a>.</xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$error-code = '403'">
                    <xsl:call-template name="block-warning-error">
                        <xsl:with-param name="pTitle">We’re sorry but you don’t have access to this page or file</xsl:with-param>
                        <xsl:with-param name="pMessage">Please log in to access <span class="alert"><xsl:value-of select="$error-request-uri"/></span>.</xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$error-code = '404'">
                    <xsl:call-template name="block-warning-error">
                        <xsl:with-param name="pTitle">We’re sorry but we can’t find the page or file you requested</xsl:with-param>
                        <xsl:with-param name="pMessage">The resource located at <span class="alert"><xsl:value-of select="$error-request-uri"/></span> may have been removed, had its name changed, or be temporarily unavailable.</xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="block-warning-error">
                        <xsl:with-param name="pTitle">Something has gone wrong with BioSamples</xsl:with-param>
                        <xsl:with-param name="pMessage">Our web server says this as a <span class="alert"><xsl:value-of select="$error-message"/></span>.
                            This problem means that the service you are trying to access is currently unavailable. We’re very sorry.</xsl:with-param>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </section>
    </xsl:template>

    <xsl:template name="block-warning-error">
        <xsl:param name="pTitle"/>
        <xsl:param name="pMessage"/>
        <section>
            <h2 class="alert"><xsl:copy-of select="$pTitle"/></h2>
            <p><xsl:copy-of select="$pMessage"/></p>
            <h3>BioSamples update</h3>
            <p>We’ve just updated BioSamples, so things have moved around a bit. Please <a href="#" onclick="$.aeFeedback(event)">contact us</a> and we will look into it for you.</p>
        </section>
    </xsl:template>
 
</xsl:stylesheet>
