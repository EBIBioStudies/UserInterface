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
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.SequenceType;
import uk.ac.ebi.fg.biostudies.utils.saxon.search.Controller;

public class GetExperimentScoreFunction extends ExtensionFunctionDefinition
{
    private static final long serialVersionUID = 5743554447454470186L;

    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.AE_SEARCH_EXT, "getExperimentScore");

    private Controller searchController;

    public GetExperimentScoreFunction( Controller controller )
    {
        this.searchController = controller;
    }

    public StructuredQName getFunctionQName()
    {
        return qName;
    }

    public int getMinimumNumberOfArguments()
    {
        return 2;
    }

    public int getMaximumNumberOfArguments()
    {
        return 2;
    }

    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_NODE};
    }

    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes)
    {
        return SequenceType.OPTIONAL_FLOAT;
    }

    public ExtensionFunctionCall makeCallExpression()
    {
        return new GetExperimentScoreCall(searchController);
    }

    private static class GetExperimentScoreCall extends ExtensionFunctionCall
    {
        private static final long serialVersionUID = 3416074070408398101L;

        private Controller searchController;

        public GetExperimentScoreCall( Controller searchController )
        {
            this.searchController = searchController;
        }

        @Override
        public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException
        {
            String queryId = SequenceTool.getStringValue(arguments[0]);
            Item node = SequenceTool.asItem(arguments[1]);

            Integer intQueryId;
            try {
                intQueryId = Integer.decode(queryId);
            } catch (NumberFormatException x) {
                throw new XPathException("queryId [" + String.valueOf(queryId) + "] must be integer");
            }

            Float result = null;
//            try {
//                result = searchController.getRelevanceScore(intQueryId, node);
//            } catch (IOException x) {
//                throw new XPathException("Caught IOException while querying index", x);
//            }

            if (null != result) {
                return new FloatValue(result);
            } else {
                return EmptySequence.getInstance();
            }
        }
    }
}