package net.sf.nvn.commons.dotnet;

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * The type of AssemblyInfo file to write.
 * 
 * @author akutz
 * 
 */
public enum OutputFileType
{
    /**
     * A C# (cs) file.
     */
    CSharp,

    /**
     * A VisualBasic (vb) file.
     */
    VisualBasic;

    /**
     * Parses an OutputFileType.
     * 
     * @param file The file to parse.
     * @return An OutputFileType.
     */
    public static OutputFileType parse(File file)
    {
        if (file == null)
        {
            return null;
        }

        return parse(file.getName());
    }

    /**
     * Parses an OutputFileType.
     * 
     * @param fileName The file name to parse.
     * @return An OutputFileType.
     */
    public static OutputFileType parse(String fileName)
    {
        if (StringUtils.isEmpty(fileName))
        {
            return null;
        }

        String fileExtension = FilenameUtils.getExtension(fileName);
        fileExtension = fileExtension.toUpperCase();

        if (fileExtension.equals("cs"))
        {
            return OutputFileType.CSharp;
        }
        else if (fileExtension.equals("vb"))
        {
            return OutputFileType.VisualBasic;
        }

        return null;
    }
}