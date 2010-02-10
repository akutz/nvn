package net.sf.nvn.commons.dotnet;

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A project's language type.
 * 
 * @author akutz
 * 
 */
public enum ProjectLanguageType
{
    CSharp,

    VisualBasic;
    
    /**
     * Parses a ProjectLanguageType.
     * 
     * @param file The file to parse.
     * @return A ProjectLanguageType.
     */
    public static ProjectLanguageType parse(File file)
    {
        if (file == null)
        {
            return null;
        }

        return parse(file.getName());
    }

    /**
     * Parses a ProjectLanguageType.
     * 
     * @param fileName The file name to parse.
     * @return A ProjectLanguageType.
     */
    public static ProjectLanguageType parse(String fileName)
    {
        if (StringUtils.isEmpty(fileName))
        {
            return null;
        }

        String fileExtension = FilenameUtils.getExtension(fileName);
        
        if (fileExtension.matches("(?i)csproj"))
        {
            return ProjectLanguageType.CSharp;
        }
        else if (fileExtension.matches("(?i)vbproj"))
        {
            return ProjectLanguageType.VisualBasic;
        }

        return null;
    }
}
