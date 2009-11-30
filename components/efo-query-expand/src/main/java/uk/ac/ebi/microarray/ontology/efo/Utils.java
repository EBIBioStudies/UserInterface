package uk.ac.ebi.microarray.ontology.efo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Anna Zhukova
 *         Class providing methods of string normalization and map format conversion.
 */
public final class Utils
{
    /**
     * Checks if the given string is null, empty or of 1 char length.
     *
     * @param str String to check.
     * @return true if the given string is null, empty or of 1 char length.
     */
    public static boolean isStopWord( String str )
    {
        return null == str || str.length() < 2;
    }

    /**
     * For null string returns an empty one,
     * for not null strings returns its lowercased copy
     * with leading and trailing white spaces removed.
     *
     * @param str String to trim and lowercase.
     * @return an empty string if the given one was null,
     *         lowercased copy of the given one
     *         with leading and trailing white spaces removed otherwise.
     */
    public static String trimLowercaseString( String str )
    {
        return null == str ? "" : str.trim().toLowerCase();
    }


    /**
     * For null string returns an empty one,
     * for not null strings returns its lowercased copy
     * with leading and trailing white spaces and everything in braces removed
     * and all not-letter not-digit not-whitespace not '/' not '-' characters
     * replaced with whitespace ' '.
     * Example: "   Hello my [dear] {nice} () world! Yours, Anna-Maria/Mary\\\4u " -->
     * "hello my    world  yours  anna-maria/mary   4u"
     *
     * @param str String to normalize.
     * @return an empty string if the given one was null,
     *         otherwise lowercased copy of the given one
     *         with leading and trailing white spaces and everything in braces removed
     *         and all not-letter not-digit not-whitespace not '/' not '-' characters
     *         replaced with whitespace ' '.
     */
    public static String preprocessAlternativeTermString( String str )
    {
        if (null == str) {
            return "";
        }
        // removing everything in braces: "lala (la) al {a} [lalal]" --> "lala al "
        Pattern pattern = Pattern.compile("(\\[[^\\]]*\\])|(\\([^\\)]*\\))|(\\{[^\\}]*\\})");
        str = pattern.matcher(str).replaceAll("");
        // replacing all not-letter not-digit not-whitespace not '/' not '-' characters with whitespace
        pattern = Pattern.compile("[^(\\d\\w\\s'\\-/)]|( \\- )|( /)|(/ )|(NOS)");
        str = pattern.matcher(str).replaceAll("").trim().toLowerCase();
        return str;
    }

    /**
     * Converts Map&lt;String, String[]&gt; to Map&lt;String[], String[][]&gt;
     * by splitting each value into words aroung whitespaces.
     *
     * @param from Map&lt;String, String[]&gt; to convert.
     * @return converted Map&lt;String[], String[][]&gt;.
     */
    public static Map<String[], String[][]> string2ArrayOfStringMapToArrayOfString2ArrayOfArrayOfStringMap( Map<String, String[]> from )
    {
        Map<String[], String[][]> result = new HashMap<String[], String[][]>();
        for (Map.Entry<String, String[]> entry : from.entrySet()) {
            String[] value = entry.getValue();
            String[][] synonyms = new String[value.length][];
            int i = 0;
            for (String element : value) {
                synonyms[i++] = element.split(" ");
            }
            result.put(entry.getKey().split(" "), synonyms);
        }
        return result;
    }

    /**
     * Converts Map&lt;T, ? extends Collection&lt;S&gt;&gt;
     * to Map&lt;T[], S[]&gt;
     * by converting values from Collection type to Array.
     *
     * @param from              Map&lt;T, ? extends Collection&lt;S&gt;&gt; to convert.
     * @param sampleOfArrayType Parameter used only for deriving proper array type
     *                          of resulting map values.
     * @return converted Map&lt;T[], S[]&gt;.
     */
    public static <T, S> Map<T, S[]> a2CollectionOfBMapToA2ArrayOfBMap( Map<T, ? extends Collection<S>> from, S[] sampleOfArrayType )
    {
        Map<T, S[]> result = new HashMap<T, S[]>();
        for (Map.Entry<T, ? extends Collection<S>> elements : from.entrySet()) {
            S[] value = elements.getValue().toArray(sampleOfArrayType);
            if (value.length > 0) {
                result.put(elements.getKey(), value);
            }
        }
        return result;
    }

}
