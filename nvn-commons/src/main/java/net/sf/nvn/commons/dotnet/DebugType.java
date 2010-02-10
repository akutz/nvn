package net.sf.nvn.commons.dotnet;

import org.apache.commons.lang.StringUtils;

/**
 * The debug level.
 * 
 * @author akutz
 * 
 */
public enum DebugType
{
    Full,

    PdbOnly,

    None;

    /**
     * Parses a DebugType from an input string. This method is case insensitive.
     * 
     * @param value The input string.
     * @return A DebugType enumeration.
     */
    public static DebugType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return DebugType.None;
        }

        if (value.matches("(?i)full"))
        {
            return DebugType.Full;
        }
        else if (value.matches("(?i)pdbonly"))
        {
            return DebugType.PdbOnly;
        }

        return DebugType.None;
    }
}
