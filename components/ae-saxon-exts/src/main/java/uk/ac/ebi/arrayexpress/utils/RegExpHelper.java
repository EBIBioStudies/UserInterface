package uk.ac.ebi.arrayexpress.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpHelper
{
    private Pattern pattern = null;
    private String flags = "";

    public RegExpHelper( String regexp )
    {
        pattern = Pattern.compile(regexp);
    }

    public RegExpHelper( String regexp, String aFlags )
    {
        if (null != aFlags) {
            flags = aFlags.toLowerCase();
        }

        if (null != regexp) {
            pattern = Pattern.compile(
                    regexp
                    , (flags.contains("m") ? Pattern.MULTILINE : 0)
                    + (flags.contains("i") ? Pattern.CASE_INSENSITIVE : 0)
            );
        }
    }

    public boolean test( String input )
    {
        return (null != pattern) && (null != input) && pattern.matcher(input).find();
    }

    public String matchFirst( String input )
    {
        if (null != pattern && null != input) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find() && 0 < matcher.groupCount()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    public String[] match( String input )
    {
        String result[] = null;
        if (null != pattern && null != input) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find() && 0 < matcher.groupCount()) {
                result = new String[matcher.groupCount()];
                for (int i = 1; i <= matcher.groupCount(); ++i) {
                    result[i-1] = matcher.group(i);
                }
            }
        }
        return result;
    }

    public String replace( String input, String replace )
    {
        if (null == replace) {
            replace = "";
        }
        if (null != pattern && null != input) {
            Matcher matcher = pattern.matcher(input);
            return flags.contains("g") ? matcher.replaceAll(replace) : matcher.replaceFirst(replace);
        } else {
            return input;
        }
    }
}
