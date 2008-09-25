<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://exslt.org/functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress"
                xmlns:helper="uk.ac.ebi.ae15.utils.AppXalanExtension"
                extension-element-prefixes="func ae helper"
                exclude-result-prefixes="func ae helper"
                version="1.0">

    <xsl:template name="ae-sort-experiments">
        <xsl:param name="pExperiments"/>
        <xsl:param name="pFrom"/>
        <xsl:param name="pTo"/>
        <xsl:param name="pSortBy"/>
        <xsl:param name="pSortOrder"/>
        <xsl:param name="pDetailedViewMainClass"/>
        <xsl:param name="pDetailedViewExtStyle"/>
        <xsl:choose>
            <xsl:when test="$pSortBy='accession'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="substring(accession/text(),3,4)" order="{$pSortOrder}"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="{$pSortOrder}" data-type="number"/>
                    <!-- sort by number -->
                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='assays'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="assays" order="{$pSortOrder}" data-type="number"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='releasedate'">
                <xsl:apply-templates select="$pExperiments">
                    <!-- year -->
                    <xsl:sort select="substring-before(releasedate/text(),'-')" order="{$pSortOrder}" data-type="number"/>
                    <!-- month -->
                    <xsl:sort select="substring-before(substring-after(releasedate/text(),'-'),'-')" order="{$pSortOrder}"
                              data-type="number"/>
                    <!-- day -->
                    <xsl:sort select="substring-after(substring-after(releasedate/text(),'-'),'-')" order="{$pSortOrder}"
                              data-type="number"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='species'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="species[1]" order="{$pSortOrder}"/>
                    <xsl:sort select="species[2]" order="{$pSortOrder}"/>
                    <xsl:sort select="species[2]" order="{$pSortOrder}"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='fgem'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="file[@type='fgem']/@bioassays" order="{$pSortOrder}" data-type="number"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='raw'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="file[@type='raw']/@bioassays" order="{$pSortOrder}" data-type="number"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$pSortBy='atlas'">
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="@loadedinatlas" order="{$pSortOrder}"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$pExperiments">
                    <xsl:sort select="*[name()=$pSortBy]" order="{$pSortOrder}"/>
                    <!-- then sort by accession -->
                    <xsl:sort select="substring(accession/text(),3,4)" order="ascending"/>
                    <!-- sort by experiment 4-letter code -->
                    <xsl:sort select="substring(accession/text(),8)" order="ascending" data-type="number"/>
                    <!-- sort by number -->

                    <xsl:with-param name="pFrom" select="$pFrom"/>
                    <xsl:with-param name="pTo" select="$pTo"/>
                    <xsl:with-param name="pDetailedViewMainClass" select="$pDetailedViewMainClass"/>
                    <xsl:with-param name="pDetailedViewExtStyle" select="$pDetailedViewExtStyle"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>