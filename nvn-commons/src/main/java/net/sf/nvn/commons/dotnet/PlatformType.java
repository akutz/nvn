package net.sf.nvn.commons.dotnet;

import org.apache.commons.lang.StringUtils;

/**
 * The build's platform type.
 * 
 * @author akutz
 * 
 */
public enum PlatformType
{
    AnyCPU,

    x86,

    x64,

    Itanium;

    /**
     * Parses a PlatformType from an input string. This method is case
     * insensitive.
     * 
     * @param value The input string.
     * @return A PlatformType enumeration.
     */
    public static PlatformType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }

        if (value.matches("(?i)any\\s?cpu"))
        {
            return PlatformType.AnyCPU;
        }
        else if (value.matches("(?i)x86"))
        {
            return PlatformType.x86;
        }
        else if (value.matches("(?i)x64"))
        {
            return PlatformType.x86;
        }
        else if (value.matches("(?i)itanium"))
        {
            return PlatformType.x86;
        }

        return null;
    }
}
