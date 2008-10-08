package uk.ac.ebi.ae15.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpHelper
{
    private Pattern pattern = null;

    public RegExpHelper( String regexp )
    {
        pattern = Pattern.compile(regexp);
    }

    public RegExpHelper( String regexp, String flags )
    {
        pattern = Pattern.compile(
                regexp
                , (flags.contains("m") ? Pattern.MULTILINE : 0)
                + (flags.contains("i") ? Pattern.CASE_INSENSITIVE : 0)
        );
    }

    public boolean test( String input )
    {
        return (null != pattern) && pattern.matcher(input).find();
    }

    public String matchFirst( String input )
    {
        if (null != pattern) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find() && 0 < matcher.groupCount()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    public String[] match( String input )
    {
        String result[] = new String[0];
        if (null != pattern) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find() && 0 < matcher.groupCount()) {
                result = new String[matcher.groupCount()];
                for (int i = 1; i <= matcher.groupCount(); ++i) {
                    result[i] = matcher.group(i);
                }
            }
        }
        return result;
    }

}
