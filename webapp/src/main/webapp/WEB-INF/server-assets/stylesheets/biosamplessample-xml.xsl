<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.ebi.ac.uk/biosamples/SampleGroupExport"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">

	<xsl:strip-space elements="*" />
	<xsl:template match="//Sample">
			<BioSample>
				<!-- I dont want to copy the groupId attribute  -->
				<xsl:copy-of select="@id" />
				<xsl:apply-templates select="./attribute"></xsl:apply-templates>
			</BioSample>
	</xsl:template>




 <xsl:template match="attribute[value/text() and count(.//attribute[@class='Term Source REF'])=0]">
        <Property class="{@class}" characteristic="false" comment="false" type="{@dataType}">
                <!--<xsl:copy-of select="."></xsl:copy-of>-->
                <Value><xsl:apply-templates select="value"></xsl:apply-templates></Value>       
        </Property>
    </xsl:template>
    
    
    
    <!--<attribute class="Term Source REF" classDefined="true" dataType="OBJECT">-->
        
        <xsl:template match="attribute[count(.//attribute[@class='Term Source REF'])>0]">
            
            <Property class="{@class}" characteristic="false" comment="false" type="{@dataType}">
                <Value><xsl:copy-of select="./value/text()"></xsl:copy-of></Value>
                <TermSourceREF>
                    <Name><xsl:copy-of select=".//attribute[@class='Term Source Name']/value/text()"></xsl:copy-of></Name>
                    <Description></Description>
                    <URI><xsl:copy-of select=".//attribute[@class='Term Source URI']/value/text()"></xsl:copy-of></URI>
                    <Version><xsl:copy-of select=".//attribute[@class='Term Source Version']/value/text()"></xsl:copy-of></Version>
                    <TermSourceID><xsl:copy-of select=".//attribute[@class='Term Source ID']/value/text()"></xsl:copy-of></TermSourceID>
                </TermSourceREF>
            </Property>  
        </xsl:template>


</xsl:stylesheet>
