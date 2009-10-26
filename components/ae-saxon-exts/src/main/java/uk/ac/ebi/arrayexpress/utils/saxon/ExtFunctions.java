package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeListIterator;
import net.sf.saxon.om.SequenceIterator;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;

import javax.xml.transform.TransformerException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtFunctions
{
    public static String capitalize(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String fileSizeToString(long size)
    {
        StringBuilder str = new StringBuilder();
        if (922L > size) {
            str.append(size).append(" B");
        } else if (944128L > size) {
            str.append(String.format("%.0f KB", (Long.valueOf(size).doubleValue() / 1024.0)));
        } else if (1073741824L > size) {
            str.append(String.format("%.1f MB", (Long.valueOf(size).doubleValue() / 1048576.0)));
        } else if (1099511627776L > size) {
            str.append(String.format("%.2f GB", (Long.valueOf(size).doubleValue() / 1073741824.0)));
        }
        return str.toString();
    }

    private static boolean testCheckbox(String check)
    {
        return (null != check && (check.toLowerCase().equals("true") || check.toLowerCase().equals("on")));
    }

    public static String describeQuery(String keywords, String species, String array, String experimentType, String inAtlas)
    {
        StringBuilder desc = new StringBuilder();
        if (!keywords.trim().equals("")) {
            desc.append("'").append(keywords).append("'");
        }
        if (!species.trim().equals("")) {
            if (0 != desc.length()) {
                desc.append(" and ");
            }
            desc.append("species '").append(species).append("'");
        }
        if (!array.trim().equals("")) {
            if (0 != desc.length()) {
                desc.append(" and ");
            }
            desc.append("array '").append(array).append("'");
        }
        if (!experimentType.trim().equals("")) {
            if (0 != desc.length()) {
                desc.append(" and ");
            }
            desc.append("experiment type '").append(experimentType).append("'");
        }

        if (0 != desc.length()) {
            desc.insert(0, "matching ");
        }

        if (testCheckbox(inAtlas)) {
            if (0 != desc.length()) {
                desc.append(" and ");
            }
            desc.append("present in ArrayExpress Atlas");
        }
        return desc.toString();
    }

    public static String normalizeSpecies(String species)
    {
        // if more than one word: "First second", otherwise "First"
        String[] spArray = species.trim().split("\\s");
        if (0 == spArray.length) {
            return "";
        } else if (1 == spArray.length) {
            return capitalize(spArray[0]);
        } else {
            return capitalize(spArray[0] + ' ' + spArray[1]);
        }
    }

    public static String[] normalizeAuthors(String authors)
    {
        String[] authorsArray = authors.trim().split("([,;] and )|( and )|([,;]and )|[,;]");
        String[] resultsArray = new String[authorsArray.length];
        int counter = 0;
        for (String author : authorsArray) {
            StringBuilder authorString = new StringBuilder();
            String[] nameArray = author.trim().split("\\s");
            for (String name : nameArray) {
                if (!"".equals(name)) {
                    authorString.append(capitalize(name)).append(" ");
                }
            }
            resultsArray[counter++] = authorString.toString();
        }
        return resultsArray;
    }


    public static String trimTrailingDot(String str)
    {
        if (str.endsWith(".")) {
            return str.substring(0, str.length() - 2);
        } else
            return str;
    }

    public static String dateToRfc822()
    {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date());
    }

    public static String dateToRfc822(String dateString)
    {
        if (null != dateString && 0 < dateString.length()) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                dateString = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(date);
            } catch (Throwable x) {
                //logger.debug("Caught an exception:", x);
            }
        } else {
            dateString = "";
        }

        return dateString;
    }

    public static SequenceIterator searchIndex(XPathContext context, String queryId)
    {
        return ((NodeInfo)context.getContextItem()).iterateAxis(Axis.CHILD);
    }

    /* ***************************************************** */
    public static boolean isExperimentInWarehouse(String accession)
    {
        return true;
    }

    public static boolean isExperimentAccessible(String accession, String userId)
    {
        return true;
    }

    public static boolean testRegexp(String input, String pattern, String flags)
    {
        boolean result = false;

        try {
            return new RegExpHelper(pattern, flags).test(input);
        } catch (Throwable t) {
            //logger.debug("Caught an exception:", t);
        }

        return result;

    }

    public static String markKeywords(String queryId, String input)
    {
        return input;
    }
}