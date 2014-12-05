package uk.ac.ebi.fg.biostudies.utils.saxon.functions.search;

/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
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
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.Controller;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.IndexEnvironmentBioStudies;

public class GetBioStudiesNumberFunction extends ExtensionFunctionDefinition
{
    private static final long serialVersionUID = 7070707985404434594L;

    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.AE_SEARCH_EXT, "getBioStudiesNumber");

    private Controller searchController;

    public GetBioStudiesNumberFunction( Controller controller )
    {
        this.searchController = controller;
    }

    public StructuredQName getFunctionQName()
    {
        return qName;
    }

    public int getMinimumNumberOfArguments()
    {
        return 0;
    }

    public int getMaximumNumberOfArguments()
    {
        return 0;
    }

    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{};
    }

    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes)
    {
        return SequenceType.SINGLE_STRING;
    }

    public ExtensionFunctionCall makeCallExpression()
    {
        return new GetBiosamplesgroupsNumberCall(searchController);
    }

    private static class GetBiosamplesgroupsNumberCall extends ExtensionFunctionCall
    {
        private static final long serialVersionUID = 2547530501711855449L;

        private Controller searchController;

        public GetBiosamplesgroupsNumberCall( Controller searchController )
        {
            this.searchController = searchController;
        }

        @Override
        public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException
        {
        	IndexEnvironmentBioStudies indexExp = (IndexEnvironmentBioStudies)searchController.getEnvironment("biostudies");
        	long longValue = indexExp.getCountDocuments();
            return new StringValue(Long.toString(longValue));
        }
    }
}