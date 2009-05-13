package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;

public class CommitElement extends ExtensionInstruction
{

    public CommitElement()
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
        return new CommitInstruction();
    }

    private static class CommitInstruction extends SimpleExpression
    {
        public CommitInstruction()
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
            return "lucene:commit";
        }

        public int computeCardinality()
        {
            return StaticProperty.EMPTY;
        }

        public Item evaluateItem(XPathContext context) throws XPathException
        {
            // do something here today
            return null;
        }
    }
}