<?xml version="1.0" encoding="windows-1252"?>
<!-- cannot change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.ebi.ac.uk/biosamples/SampleGroupExport"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">


	<xsl:param name="id" />
	<xsl:param name="sampleslist" />

	<xsl:variable name="vSamplesList"
		select="if ($sampleslist and $sampleslist='true') then  1  else 0" />

	<xsl:strip-space elements="*" />
	<xsl:template match="//SampleGroup">
			<BioSampleGroup>
			<!-- cannot copy all because I have there samplescount -->
				<xsl:copy-of select="@id" /> 
				<!-- <xsl:apply-templates select="*[not(self::SampleAttributes)]"></xsl:apply-templates> -->
				 <xsl:apply-templates select="attribute/value[count(.//object[@class='Term Source'])>0]"></xsl:apply-templates>
            <xsl:apply-templates select="attribute[value/text() and count(.//object)=0]"></xsl:apply-templates>
            <xsl:apply-templates select="attribute/value[count(.//object[@class='Organization'])>0]"></xsl:apply-templates>
            <xsl:apply-templates select="attribute/value[count(.//object[@class='Person'])>0]"></xsl:apply-templates>
            <xsl:apply-templates select="attribute/value[count(.//object[@class='Database'])>0]"></xsl:apply-templates>
            <xsl:apply-templates select="attribute/value[count(.//object[@class='Publication'])>0]"></xsl:apply-templates>	
            <xsl:apply-templates select="//Samples"></xsl:apply-templates>		
			</BioSampleGroup>
	</xsl:template>


	<xsl:template match="//Samples">
		<xsl:if test="$vSamplesList=1">
			<xsl:for-each select="tokenize(.,' ')">
				<!-- <BioSample id="{.}" groupId="{$id}"></BioSample> -->
				<BioSample id="{.}"></BioSample>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>


	<xsl:template
		match="attribute/value[count(.//object[@class='Organization'])>0]">
		<Organization>
			<Name>
				<xsl:copy-of select=".//attribute[@class='Organization Name']/value/text()"></xsl:copy-of>
			</Name>
			<Address>
				<xsl:copy-of
					select=".//attribute[@class='Organization Address']/value/text()"></xsl:copy-of>
			</Address>
			<URI>
				<xsl:copy-of select=".//attribute[@class='Organization URI']/value/text()"></xsl:copy-of>
			</URI>
			<Email>
				<xsl:copy-of
					select=".//attribute[@class='Organization Email']/value/text()"></xsl:copy-of>
			</Email>
			<Role>
				<xsl:copy-of select=".//attribute[@class='Organization Role']/value/text()"></xsl:copy-of>
			</Role>
		</Organization>
	</xsl:template>



	<xsl:template match="attribute/value[count(.//object[@class='Person'])>0]">
		<Person>
			<FirstName>
				<xsl:copy-of select=".//attribute[@class='Person First Name']/value/text()"></xsl:copy-of>
			</FirstName>
			<LastName>
				<xsl:copy-of select=".//attribute[@class='Person Last Name']/value/text()"></xsl:copy-of>
			</LastName>
			<MidInitials>
				<xsl:copy-of select=".//attribute[@class='Person Initials']/value/text()"></xsl:copy-of>
			</MidInitials>
			<Email>
				<xsl:copy-of select=".//attribute[@class='Person Email']/value/text()"></xsl:copy-of>
			</Email>
			<Role>
				<xsl:copy-of select=".//attribute[@class='Person Role']/value/text()"></xsl:copy-of>
			</Role>
		</Person>
	</xsl:template>


	<xsl:template match="attribute/value[count(.//object[@class='Database'])>0]">
		<Database>
			<Name>
				<xsl:copy-of select=".//attribute[@class='Database Name']/value/text()"></xsl:copy-of>
			</Name>
			<ID>
				<xsl:copy-of select=".//attribute[@class='Database ID']/value/text()"></xsl:copy-of>
			</ID>
			<URI>
				<xsl:copy-of select=".//attribute[@class='Database URI']/value/text()"></xsl:copy-of>
			</URI>
		</Database>
	</xsl:template>


	<xsl:template
		match="attribute/value[count(.//object[@class='Publication'])>0]">
		<Publication>
			<DOI>
				<xsl:copy-of select=".//attribute[@class='Publication DOI']/value/text()"></xsl:copy-of>
			</DOI>
			<PubMedID>
				<xsl:copy-of
					select=".//attribute[@class='Publication PubMed ID']/value/text()"></xsl:copy-of>
			</PubMedID>
		</Publication>
	</xsl:template>



	<xsl:template
		match="attribute/value[count(.//object[@class='Term Source'])>0]">
		<TermSource>
			<Name>
				<xsl:copy-of select=".//attribute[@class='Term Source Name']/value/text()"></xsl:copy-of>
			</Name>
			<Description></Description>
			<URI>
				<xsl:copy-of select=".//attribute[@class='Term Source URI']/value/text()"></xsl:copy-of>
			</URI>
			<Version>
				<xsl:copy-of
					select=".//attribute[@class='Term Source Version']/value/text()"></xsl:copy-of>
			</Version>
			<TermSourceID>
				<xsl:copy-of select=".//attribute[@class='Term Source ID']/value/text()"></xsl:copy-of>
			</TermSourceID>
		</TermSource>
	</xsl:template>

	<xsl:template match="//attribute[value/text()]">
		<Property class="{@class}" characteristic="false" comment="false"
			type="{@dataType}">
			<!--<xsl:copy-of select="."></xsl:copy-of> -->
			<Value>
				<xsl:apply-templates select="value"></xsl:apply-templates>
			</Value>
		</Property>
	</xsl:template>

</xsl:stylesheet>













