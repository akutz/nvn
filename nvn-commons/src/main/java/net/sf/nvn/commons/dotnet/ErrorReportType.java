package net.sf.nvn.commons.dotnet;

import org.apache.commons.lang.StringUtils;

/**
 * Values indicating how the MSBuild compiler task should report internal compiler errors.
 * @author akutz
 *
 */
public enum ErrorReportType
{
    Prompt,
    
    Send,
    
    None;
    
    /**
     * Parses an ErrorReportType from an input string. This method is case insensitive.
     * 
     * @param value The input string.
     * @return An ErrorReportType enumeration.
     */
    public static ErrorReportType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return ErrorReportType.None;
        }

        if (value.matches("(?i)prompt"))
        {
            return ErrorReportType.Prompt;
        }
        else if (value.matches("(?i)send"))
        {
            return ErrorReportType.Send;
        }

        return ErrorReportType.None;
    }
}