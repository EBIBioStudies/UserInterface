<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">



<xsl:template match='/'>
<xsl:copy-of select='@*,*'/>
</xsl:template>

</xsl:stylesheet>
