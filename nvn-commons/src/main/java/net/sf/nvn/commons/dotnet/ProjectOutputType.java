package net.sf.nvn.commons.dotnet;

import org.apache.commons.lang.StringUtils;

/**
 * The MSBuild project output types.
 * 
 * @author akutz
 * 
 */
public enum ProjectOutputType
{
    /**
     * A library.
     */
    Library,
    
    /**
     * A command-line executable.
     */
    Exe,
    
    /**
     * A graphical executable.
     */
    WinExe,
    
    /**
     * A module
     */
    Module;
    
    /**
     * Parses a ProjectOutputType from an input string. This method is case insensitive.
     * 
     * @param value The input string.
     * @return A ProjectOutputType enumeration.
     */
    public static ProjectOutputType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }

        value = value.toUpperCase();

        if (value.equals("LIBRARY"))
        {
            return ProjectOutputType.Library;
        }
        else if (value.equals("EXE"))
        {
            return ProjectOutputType.Exe;
        }
        else if (value.equals("WINEXE"))
        {
            return ProjectOutputType.WinExe;
        }
        else if (value.equals("MODULE"))
        {
            return ProjectOutputType.Module;
        }

        return null;
    }
}
