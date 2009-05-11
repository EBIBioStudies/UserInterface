package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;

public class FieldElement extends ExtensionInstruction
{

    public FieldElement()
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
        return new FieldInstruction();
    }

    private static class FieldInstruction extends SimpleExpression
    {
        public FieldInstruction()
        {
        }

        /**
         * A subclass must provide one of the methods evaluateItem(), iterate(), or process().
         * This method indicates which of the three is provided.
         */

        public int getImplementationMethod()
        {
            return Expression.PROCESS_METHOD;
        }

        public String getExpressionType()
        {
            return "field";
        }

        public int computeCardinality()
        {
            return StaticProperty.EMPTY;
        }

        public void process(XPathContext context) throws XPathException
        {
            // do something here today
        }
    }
}
