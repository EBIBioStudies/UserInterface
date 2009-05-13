package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;

public class DocumentElement extends ExtensionInstruction
{

    public DocumentElement()
    {
    }

    public void prepareAttributes() throws XPathException
    {
    }

    public void validate() throws XPathException
    {
        super.validate();
    }

    public Expression compile(Executable exec) throws XPathException
    {
        return new DocumentInstruction();
    }

    private static class DocumentInstruction extends SimpleExpression
    {
        public DocumentInstruction()
        {
        }

        /**
         * A subclass must provide one of the methods evaluateItem(), iterate(), or process().
         * This method indicates which of the three is provided.
         */

        public int getImplementationMethod()
        {
            return Expression.EVALUATE_METHOD;
        }

        public String getExpressionType()
        {
            return "lucene:document";
        }

        public int computeCardinality()
        {
            return StaticProperty.ALLOWS_ZERO_OR_MORE;
        }

        public Item evaluateItem(XPathContext context) throws XPathException
        {
            // do something here today
            return null;
        }
    }
}

