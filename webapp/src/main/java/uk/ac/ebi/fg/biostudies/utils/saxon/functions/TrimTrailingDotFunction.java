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

package uk.ac.ebi.fg.biostudies.utils.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class TrimTrailingDotFunction extends ExtensionFunctionDefinition
{
    private static final long serialVersionUID = -7916816398895676395L;

    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.AE_EXT, "trimTrailingDot");

    public StructuredQName getFunctionQName()
    {
        return qName;
    }

    public int getMinimumNumberOfArguments()
    {
        return 1;
    }

    public int getMaximumNumberOfArguments()
    {
        return 1;
    }

    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{ SequenceType.OPTIONAL_STRING };
    }

    public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
    {
        return SequenceType.OPTIONAL_STRING;
    }

    public ExtensionFunctionCall makeCallExpression()
    {
        return new TrimTrailingDotCall();
    }

    private static class TrimTrailingDotCall extends ExtensionFunctionCall
    {
        private static final long serialVersionUID = 974920767172642082L;

        public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException
        {
            String str = SequenceTool.getStringValue(arguments[0]);

            if (str.endsWith(".")) {
                str = str.substring(0, str.length() - 1);
            }

            return new StringValue(str);
        }
    }
}
