<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:search="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
                extension-element-prefixes="search"
                exclude-result-prefixes="search"
                version="2.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:param name="queryid"/>
    <xsl:param name="total"/>
    <xsl:param name="totalsamples"/>
    <xsl:param name="totalassays"/>
    
    

    <xsl:template match="/">
        
         
        <biosamples groups="{search:getBiosamplesgroupsNumber()}"
                     samples="{search:getBiosamplessamplesNumber()}" />
    </xsl:template>

</xsl:stylesheet>
