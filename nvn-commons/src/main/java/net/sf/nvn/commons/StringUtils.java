package net.sf.nvn.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for working with strings.
 * 
 * @author akutz
 * 
 */
public class StringUtils
{
    /**
     * This pattern matches a delimited list.
     */
    private static Pattern COMMA_SEPERATED_ARGS_PATT =
        Pattern
            .compile("^\\s*((?:([\'\"])[^\'\"]*\\2(?:(?:[,;:]\\s*?)|(?=\\s?)))+)\\s*$");

    /**
     * This pattern matches text surrounded by white space.
     */
    private static Pattern TRIM_PATT =
        Pattern.compile("^\\s*(?:(?:(['\"])(.*)\\1)|(?:([^\\s].*?)))\\s*$");

    /**
     * Trims and quotes a string if necessary.
     * 
     * @param toQuote The string to trim and quote.
     * @return A trimmed and quoted string.
     */
    public static String quote(String toQuote)
    {
        Matcher m1 = COMMA_SEPERATED_ARGS_PATT.matcher(toQuote);

        if (m1.matches())
        {
            return m1.group(1);
        }

        Matcher m2 = TRIM_PATT.matcher(toQuote);

        if (!m2.matches())
        {
            return toQuote;
        }

        if (m2.group(2) != null)
        {
            return m2.group(2);
        }

        String text = m2.group(3);

        if (!text.contains(" "))
        {
            return text;
        }

        String toReturn;

        if (text.contains("\""))
        {
            toReturn = "'" + text + "'";
        }
        else
        {
            toReturn = "\"" + text + "\"";
        }

        return toReturn;
    }
}