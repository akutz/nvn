/*******************************************************************************
 * Copyright (c) 2010, Schley Andrew Kutz
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Schley Andrew Kutz nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

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
