package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;

public class CreateElement extends ExtensionInstruction
{
    private Expression name;
    private Expression storage;

    public CreateElement()
    {
    }

    // TODO: not sure what that means
    public boolean mayContainSequenceConstructor()
    {
        return false;
    }

    public void prepareAttributes() throws XPathException
    {
        String nameAtt = getAttributeValue("", "name");
        if (null != nameAtt) {
            name = makeAttributeValueTemplate(nameAtt);
        } else {
            reportAbsence("name");
        }

        String storageAtt = getAttributeValue("", "storage");
        if (null != storageAtt) {
            storage = makeAttributeValueTemplate(storageAtt);
        } else {
            reportAbsence("storage");
        }

    }

    public void validate() throws XPathException
    {
        super.validate();

        name = typeCheck("name", name);
        storage = typeCheck("storage", storage);
    }

    public Expression compile(Executable exec) throws XPathException
    {
        return new CreateInstruction(name, storage);
    }

    private static class CreateInstruction extends SimpleExpression
    {
        public static final int NAME_ARG = 0;
        public static final int STORAGE_ARG = 1;

        public CreateInstruction(Expression name, Expression storage)
        {
            Expression[] subs = {name, storage};
            setArguments(subs);
        }

        /**
         * A subclass must provide one of the methods evaluateItem(), iterate(), or process().
         * This method indicates which of the three is provided.
         */

        public int getImplementationMethod()
        {
            return Expression.EVALUATE_METHOD;
        }

        public int computeCardinality()
        {
            return StaticProperty.EXACTLY_ONE;
        }

        public String getExpressionType()
        {
            return "lucene:create";
        }

        public Item evaluateItem(XPathContext context) throws XPathException
        {
            String indexName = arguments[NAME_ARG].evaluateAsString(context).toString();
            String storage = arguments[STORAGE_ARG].evaluateAsString(context).toString();

            if (storage.equalsIgnoreCase("memory")) {

            } else {
                dynamicError("Attribute \"storage\" of element \"create\" must have one of the following values: \"memory\", \"fs\"", "", context);
            }
            return new ObjectValue(new Object());
        }
    }
}

