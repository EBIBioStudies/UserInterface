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
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="string(@id)" />
						<xsl:with-param name="pFieldName" select="'accession'" />
					</xsl:call-template>
				</a>
			</bs_value_att>
		</div>

		<div id="samplesmiddle{position()}">

			<!-- Organism and Description and Name are estatic -->

			<bs_value_att>
				<xsl:call-template name="process_efo">
					<xsl:with-param name="pAttribute"
						select="$vSample/attribute[@class='Organism']"></xsl:with-param>
					<xsl:with-param name="pField"
										select="'organism'"></xsl:with-param>
				</xsl:call-template>
				<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
					select="string($vSample/attribute/simpleValue/value[../../@class='Organism'])" 
					/> <xsl:with-param name="pFieldName" select="'organism'" /> </xsl:call-template> -->
			</bs_value_att>
			<bs_value_att>
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText"
						select="string($vSample/attribute/simpleValue/value[../../@class='Sample Name'])" />
					<xsl:with-param name="pFieldName" select="'name'" />
				</xsl:call-template>
			</bs_value_att>
			<bs_value_att>
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText"
						select="string($vSample/attribute/simpleValue/value[../../@class='Sample Description'])" />
					<xsl:with-param name="pFieldName" select="'description'" />
				</xsl:call-template>
			</bs_value_att>
			<xsl:for-each select="$pAttributes/attribute/@class">
				<xsl:variable name="attributeClass" select="."></xsl:variable>
				<xsl:variable name="attribute"
					select="$vSample/attribute[@class=$attributeClass]"></xsl:variable>
				<xsl:if
					test=".!='Sample Accession' and .!='Organism' and .!='Sample Name' and .!='Sample Description'">
					<bs_value_att>
						<xsl:choose>
							<xsl:when test="$attributeClass='Derived From'">
								<xsl:call-template name="process_derived_from">
									<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
								</xsl:call-template>
							</xsl:when>
							<xsl:when test="$attributeClass='Database URI'">
								<a href="{simpleValue/value}" target="ext">
									<xsl:copy-of select="simpleValue/value"></xsl:copy-of>
								</a>
							</xsl:when>
							<!-- normal value -->
							<xsl:when
								test="count($attribute//attribute[@class='Term Source REF'])=0">

								<xsl:call-template name="process_multiple_values">
									<xsl:with-param name="pField"
										select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
									<xsl:with-param name="pValue" select="$attribute"></xsl:with-param>
								</xsl:call-template>
							</xsl:when>

							<xsl:otherwise>
								<xsl:call-template name="process_efo">
									<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
									<xsl:with-param name="pField"
										select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
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
							test="count($vSample/attribute/simpleValue/value[../../@class='Database Name'])=1">
							<xsl:variable name="bdNameSample"
								select="lower-case($vSample/attribute/simpleValue/value[../../@class='Database Name'])"></xsl:variable>
							<xsl:variable name="bdUriSample"
								select="$vSample/attribute/simpleValue/value[../../@class='Database URI']"></xsl:variable>
							<xsl:choose>
								<!-- firts I will see if the database is defined inside the Sample -->
								<xsl:when
									test="$bdNameSample =('arrayexpress','ena sra','dgva','pride') and not($bdUriSample='')">
									<a href="{$bdUriSample}" target="ext">
										<img src="{$basepath}/assets/images/{$bdNameSample}_logo.gif"
											alt="{$bdNameSample} Link" title="{$bdNameSample}" valign="middle"
											border="0" />
									</a>
								</xsl:when>
								<xsl:when test="not($bdNameSample='') and not($bdUriSample='')">
									<a href="{$bdUriSample}" target="ext">
										<font class="icon icon-generic" data-icon="L" />
									</a>
								</xsl:when>
							</xsl:choose>
						</xsl:when>
						<!-- database inside the samplegroup -->
						<xsl:otherwise>
							<xsl:for-each select="../DatabaseGroup//object">
								<xsl:variable name="bdNameGroup"
									select="lower-case(.//attribute/simpleValue/value[../../@class='Database Name'])"></xsl:variable>
								<xsl:variable name="bdUriGroup"
									select=".//attribute/simpleValue/value[../../@class='Database URI']"></xsl:variable>
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


	</xsl:template>





	<xsl:template name="process_multiple_values">
		<xsl:param name="pValue" />
		<xsl:param name="pField" />
		<xsl:for-each select="$pValue//value">
			<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
				select="$pValue" /> <xsl:with-param name="pFieldName" select="concat('attributes:',$pField)" 
				/> </xsl:call-template> -->

			<xsl:call-template name="highlight">
				<xsl:with-param name="pText" select="." />
				<xsl:with-param name="pFieldName" select="$pField" />
			</xsl:call-template>
			<!-- <xsl:copy-of select="."></xsl:copy-of> -->
			<xsl:if test="position()!=last()">
				,
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_derived_from">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute//simpleValue/value">
			<a href="{$basepath}/sample/{.}">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="." />
					<xsl:with-param name="pFieldName" select="''" />
				</xsl:call-template>

				<!-- <xsl:copy-of select="."></xsl:copy-of> -->
			</a>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="process_efo">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />
		<xsl:choose>
			<xsl:when
				test="count($pAttribute//attribute/simpleValue/value[../../@class='Term Source URI'])=0">
				<!-- <xsl:copy-of select="$pAttribute/simpleValue/value"></xsl:copy-of> -->
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="$pAttribute/simpleValue/value" />
					<xsl:with-param name="pFieldName" select="$pField" />
				</xsl:call-template>

			</xsl:when>
			<xsl:otherwise>

				<xsl:call-template name="process_efo_url">
					<xsl:with-param name="pAttribute" select="$pAttribute" />
					<xsl:with-param name="pField" select="$pField" />
				</xsl:call-template>
				<!-- <a href="{.//attribute/simpleValue/value[../../@class='Term Source 
					URI']}" target="ext"> <xsl:value-of select="simpleValue/value"></xsl:value-of> 
					</a> -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="process_efo_url">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />
		<xsl:choose>
			<xsl:when
				test="starts-with($pAttribute//attribute/simpleValue/value[../../@class='Term Source URI'],'http://www.ncbi.nlm.nih.gov/taxonomy')">
				<a
					href="http://www.ncbi.nlm.nih.gov/taxonomy/?term={$pAttribute//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="$pAttribute/simpleValue/value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
					<!-- <xsl:value-of select="$pAttribute/simpleValue/value"></xsl:value-of> -->
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a
					href="{$pAttribute//attribute/simpleValue/value[../../@class='Term Source URI']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="$pAttribute/simpleValue/value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
					<!-- <xsl:value-of select="$pAttribute/simpleValue/value"></xsl:value-of> -->
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>
