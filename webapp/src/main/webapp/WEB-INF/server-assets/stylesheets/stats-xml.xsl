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
                 xmlns:search="http://www.ebi.ac.uk/biostudies/XSLT/SearchExtension"
                extension-element-prefixes="search"
                exclude-result-prefixes="search"
                version="2.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:param name="queryid"/>
    <xsl:param name="total"/>
    <xsl:param name="totalsamples"/>
    <xsl:param name="totalassays"/>
    
    

    <xsl:template match="/">
        
         
        <!-- <biosamples groups="{search:getBiosamplesgroupsNumber()}"
                     samples="{search:getBiosamplessamplesNumber()}" /> -->
                     
               <biostudies studies="{search:getBioStudiesNumber()}" />    
    </xsl:template>

</xsl:stylesheet>
