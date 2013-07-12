<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs aejava html"
	exclude-result-prefixes="xs aejava html" version="2.0">

	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:variable name="vPage"
		select="if ($page) then $page cast as xs:integer else 1" />
	<xsl:variable name="vPageSize"
		select="if ($pagesize) then $pagesize cast as xs:integer else 25" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:variable name="vSortBy"
		select="if ($sortby) then $sortby else 'accession'" />
	<xsl:variable name="vSortOrder"
		select="if ($sortorder) then $sortorder else 'ascending'" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:variable name="vkeywords" select="$keywords" />
	<xsl:param name="id" />

	<xsl:param name="userid" />

	<xsl:param name="host" />
	<xsl:param name="basepath" />
	<xsl:param name="total" />
	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>



	<xsl:include href="biosamples-highlight.xsl" />

	<xsl:template match="/">

		<xsl:variable name="vFrom" as="xs:integer">
			<xsl:choose>
				<xsl:when test="$vPage > 0">
					<xsl:value-of select="1 + ( $vPage - 1 ) * $vPageSize" />
				</xsl:when>
				<xsl:when test="$vTotal = 0">
					0
				</xsl:when>
				<xsl:otherwise>
					1
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="vTo" as="xs:integer">
			<xsl:choose>
				<xsl:when test="( $vFrom + $vPageSize - 1 ) > $vTotal">
					<xsl:value-of select="$vTotal" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$vFrom + $vPageSize - 1" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>



		<div id="bs_results_summary_info">
			<div id="bs_results_total">
				<xsl:value-of select="$vTotal" />
			</div>
			<div id="bs_results_from">
				<xsl:value-of select="$vFrom" />
			</div>
			<div id="bs_results_to">
				<xsl:value-of select="$vTo" />
			</div>
			<div id="bs_results_page">
				<xsl:value-of select="$vPage" />
			</div>
			<div id="bs_results_pagesize">
				<xsl:value-of select="$vPageSize" />
			</div>
		</div>


		<xsl:apply-templates select="//Sample">
			<xsl:with-param name="pAttributes" select="//SampleAttributes"></xsl:with-param>
		</xsl:apply-templates>


	</xsl:template>



	<xsl:template match="Sample">
		<xsl:param name="pAttributes"></xsl:param>
		<!-- <xsl:param name="pAttributesinteger"></xsl:param> -->
		<xsl:variable name="vSample" select="."></xsl:variable>
		<div id="samplesleft{position()}">
			<bs_value_att>
				 <a href="../sample/{@id}">
					<xsl:copy-of
						select="$vSample/attribute/value[../@class='Sample Accession']" />
				 </a> 
			</bs_value_att>
		</div>

		<div id="samplesmiddle{position()}">
			<xsl:for-each select="$pAttributes/attribute/@class">
				<xsl:if test=".!='Sample Accession'">
					<bs_value_att>
						<xsl:variable name="token" select="."></xsl:variable>
						<xsl:variable name="value"
							select="$vSample/attribute/value[../@class=$token]" />
						<xsl:choose>


							<!-- LINKS -->
							<xsl:when test="$token='Database URI'">

								<a href="{$value}" target="ext">
									<xsl:value-of select="$value"></xsl:value-of>
								</a>
							</xsl:when>

							<xsl:when test="count($value/attribute[@class='Term Source REF'])=0">

								<xsl:call-template name="highlight">
									<xsl:with-param name="pText" select="string-join($value, ', ')" />
									<!-- <xsl:with-param name="pFieldName" select="'$token'" /> -->
									<xsl:with-param name="pFieldName" select="string(position())" />
								</xsl:call-template>
							</xsl:when>


							<xsl:otherwise>
								<!-- efo-><xsl:copy-of select="$value"></xsl:copy-of> -->
								<xsl:call-template name="process_efo">
									<xsl:with-param name="pValue" select="$value"></xsl:with-param>
								</xsl:call-template>
							</xsl:otherwise>

						</xsl:choose>

					</bs_value_att>




				</xsl:if>

			</xsl:for-each>

		</div>

		<div id="samplesright{position()}">
			<div>
				<!-- <tr> <td> -->
				<bs_value_att>
					<xsl:choose>
						<!-- firts I will see if the database is defined inside the Sample -->
						<xsl:when
							test="count($vSample/attribute/value[../@class='Database Name'])=1">
							<xsl:variable name="bdNameSample"
								select="lower-case($vSample/attribute/value[../@class='Database Name'])"></xsl:variable>
							<xsl:variable name="bdUriSample"
								select="$vSample/attribute/value[../@class='Database URI']"></xsl:variable>
							<xsl:choose>
								<!-- firts I will see if the database is defined inside the Sample -->
								<xsl:when
									test="$bdNameSample =('arrayexpress','ena sra','dgva','pride') and not($bdUriSample='')">
									<a href="{$bdUriSample}"
										target="ext">
										<img src="{$basepath}/assets/images/{$bdNameSample}_logo.gif"
											alt="{$bdNameSample} Link" title="{$bdNameSample}" valign="middle"
											border="0" />
									</a>
								</xsl:when>
								<xsl:when
									test="not($bdNameSample='') and not($bdUriSample='')">
									<a href="{$bdUriSample}"
										target="ext">
										<font class="icon icon-generic" data-icon="L" />
									</a>
								</xsl:when>
							</xsl:choose>
						</xsl:when>
						<!-- database inside the samplegroup -->
						<xsl:otherwise>
							<xsl:for-each select="../DatabaseGroup//object">
								<xsl:variable name="bdNameGroup"
									select="lower-case(.//attribute/value[../@class='Database Name'])"></xsl:variable>
								<xsl:variable name="bdUriGroup"
									select=".//attribute/value[../@class='Database URI']"></xsl:variable>
								<xsl:choose>
									<xsl:when
										test="$bdNameGroup =('arrayexpress','ena sra','dgva','pride') and not($bdUriGroup='')">
										<a href="{$bdUriGroup}" target="ext">
											<img src="{$basepath}/assets/images/{$bdNameGroup}_logo.gif"
												alt="{$bdNameGroup} Link" title="{$bdNameGroup}" valign="middle"
												border="0" />
										</a>
									</xsl:when>
									<xsl:when test="not ($bdUriGroup='')">
										<a href="{$bdUriGroup}" target="ext">
											<font class="icon icon-generic" data-icon="L" />
										</a>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
				</bs_value_att>
				<!-- </td> </tr> -->
			</div>
		</div>
		<!-- <xsl:for-each select="tokenize($pAttributes,' ')"> <xsl:if test=".!='Sample-Accession'"> 
			<td> <xsl:variable name="token" select="."></xsl:variable> <xsl:variable 
			name="value" select="$vSample/attribute/value[../@class=replace($token, '-' 
			, ' ')]" /> <xsl:choose> <xsl:when test="count($value/attribute[@class='Term 
			Source REF'])=0"> <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
			select="string-join($value, ', ')" /> <xsl:with-param name="pFieldName" select="'$token'" 
			/> <xsl:with-param name="pFieldName" select="'$token'" /> </xsl:call-template> 
			</xsl:when> <xsl:otherwise> <xsl:call-template name="process_efo"> <xsl:with-param 
			name="pValue" select="$value"></xsl:with-param> </xsl:call-template> </xsl:otherwise> 
			</xsl:choose> </td> </xsl:if> </xsl:for-each> -->




	</xsl:template>






	<xsl:template name="process_efo">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue">
			<xsl:call-template name="highlight">
				<!-- <xsl:with-param name="pText" select="$pValue/text()[last()]" /> -->
				<xsl:with-param name="pText" select="./text()[last()]" />
				<xsl:with-param name="pFieldName" select="''" />
			</xsl:call-template>
			<xsl:if test="position()!=last()">
				,
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_efo_25072012">
		<xsl:param name="pValue" />

		<xsl:for-each select="$pValue">
			<xsl:copy-of select="./text()" />
			<br />
			<xsl:variable name="textValue" select="./text()"></xsl:variable>
			<!-- <xsl:copy-of select="$textValue" /> -->
			<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
				select="replace($textValue,' ','')" /> <xsl:with-param name="pFieldName" 
				select="'keywords'" /> </xsl:call-template> -->

			<a href="{.//attribute/value[../@class='Term Source URI']}" target="ext">
				<xsl:value-of select=".//attribute/value[../@class='Term Source URI']"></xsl:value-of>
			</a>
			<br />
		</xsl:for-each>
	</xsl:template>





</xsl:stylesheet>
