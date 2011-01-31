package uk.ac.ebi.arrayexpress.utils.saxon.search;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;

public final class SearchExtension
{

    public static SequenceIterator queryIndex( XPathContext context, String queryId )
    {
        return ((NodeInfo)context.getContextItem()).iterateAxis(Axis.CHILD);
    }

    public static SequenceIterator queryIndex( XPathContext context, String indexId, String queryString )
    {
        return ((NodeInfo)context.getContextItem()).iterateAxis(Axis.CHILD);
    }

    public static String highlightQuery( String queryId, String fieldName, String text )
    {
        return text;
    }

    public static String getQueryString( String queryId )
    {
        return "sample=querystring";
    }
}
