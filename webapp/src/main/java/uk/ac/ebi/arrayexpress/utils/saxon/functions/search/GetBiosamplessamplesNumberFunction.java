package uk.ac.ebi.arrayexpress.utils.saxon.functions.search;

/*
 * Copyright 2009-2013 European Molecular Biology Laboratory
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
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Value;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Controller;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesSample;

public class GetBiosamplessamplesNumberFunction extends ExtensionFunctionDefinition
{
    private static final long serialVersionUID = 7070707985404434594L;

    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.AE_SEARCH_EXT, "getBiosamplessamplesNumber");

    private Controller searchController;

    public GetBiosamplessamplesNumberFunction( Controller controller )
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
        return new GetBiosamplessamplesNumberCall(searchController);
    }

    private static class GetBiosamplessamplesNumberCall extends ExtensionFunctionCall
    {
        private static final long serialVersionUID = 2547530501711855449L;

        private Controller searchController;

        public GetBiosamplessamplesNumberCall( Controller searchController )
        {
            this.searchController = searchController;
        }

        public SequenceIterator<? extends Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException
        {
        	IndexEnvironmentBiosamplesSample indexExp=(IndexEnvironmentBiosamplesSample)searchController.getEnvironment("biosamplessample");
        	long longValue= indexExp.getCountDocuments();
            return Value.asIterator(StringValue.makeStringValue(Long.toString(longValue)));
        }
    }
}