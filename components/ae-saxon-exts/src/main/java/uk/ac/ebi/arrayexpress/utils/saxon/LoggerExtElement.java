package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;

public class LoggerExtElement extends ExtensionInstruction
{
    // select attribute name
    private final String MESSAGE_ATTRIBUTE = "message";

    // expression
    private Expression messageExpression = null;

    public LoggerExtElement()
    {
    }

    public void prepareAttributes() throws XPathException
    {
        String msg = getAttributeValue(MESSAGE_ATTRIBUTE);
        if (null == msg) {
            reportAbsence(MESSAGE_ATTRIBUTE);
        } else {
            messageExpression = this.makeAttributeValueTemplate(msg);
        }
    }

    public void validate() throws XPathException
    {
        super.validate();
        messageExpression = typeCheck(MESSAGE_ATTRIBUTE, messageExpression);
    }

    public Expression compile(Executable exec) throws XPathException
    {
        return new LogInstruction(messageExpression);
    }

    private static class LogInstruction extends SimpleExpression
    {
        // logging machinery
        //private final Logger logger = LoggerFactory.getLogger(getClass());

        public LogInstruction(Expression expression)
        {
            Expression[] sub = {expression};
            setArguments(sub);
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
            return "aeext:log";
        }

        public int computeCardinality()
        {
            return StaticProperty.EMPTY;
        }

        public void process(XPathContext context) throws XPathException
        {
            Item item = this.arguments[0].evaluateItem(context);
            //logger.info(item.getStringValue());
        }
    }
}
