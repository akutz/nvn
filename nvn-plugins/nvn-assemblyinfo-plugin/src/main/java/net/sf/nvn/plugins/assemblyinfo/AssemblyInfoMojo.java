package net.sf.nvn.plugins.assemblyinfo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A MOJO which creates a .NET AssemblyInfo file.
 * 
 * @goal generate-assembly-info
 * @phase generate-sources
 */
public class AssemblyInfoMojo extends AbstractMojo
{
    /**
     * The directory containing the pom.
     * 
     * @parameter expression="${basedir}"
     */
    private File basedir;

    /**
     * The value used for the AssemblyVersion, AssemblyFileVersion, and
     * AssemblyInformationVersion attributes. The default value is parsed from
     * "project.version". For the first two attributes all characters in the
     * version property after the last digit are stripped from the string in
     * order to comply with .NET version rules.
     * 
     * @parameter expression="${project.version}"
     */
    private String version;

    /**
     * The value used for the AssemblyTitle and AssemblyProduct (which is formed
     * by combining this class's "company" and "name" properties separated by a
     * space) attributes. The default value is parsed from "project.name".
     * 
     * @parameter expression="${project.name}"
     */
    private String name;

    /**
     * The value used for the AssemblyProduct attribute (which is formed by
     * combining this class's "company" and "name" properties separated by a
     * space). The default value is parsed from "project.organization.name".
     * 
     * @parameter expression="${project.organization.name}"
     */
    private String company;

    /**
     * The value used for the AssemblyDescription attribute. The default value
     * is parsed from "project.description".
     * 
     * @parameter expression="${project.description}"
     */
    private String description;

    /**
     * The location of the .NET AssemblyInfo file to output.
     * 
     * @parameter expression="${assemblyinfo.outputfile}"
     */
    private File outputFile;

    /**
     * Gets the directory containing the pom.
     * 
     * @return The directory containing the pom.
     */
    public File getBasedir()
    {
        return this.basedir;
    }

    /**
     * Sets the directory containing the pom.
     * 
     * @param toSet The directory containing the pom.
     */
    public void setBasedir(File toSet)
    {
        this.basedir = toSet;
    }

    /**
     * Gets the location of the .NET AssemblyInfo file to output.
     * 
     * @return The location of the .NET AssemblyInfo file to output.
     */
    public File getOutputFile()
    {
        return this.outputFile;
    }

    /**
     * Sets the location of the .NET AssemblyInfo file to output.
     * 
     * @param outputFile The location of the .NET AssemblyInfo file to output.
     */
    public void setOutputFile(File toSet)
    {
        this.outputFile = toSet;
    }

    /**
     * The .NET assembly's GUID. If left blank then a random GUID will be
     * generated.
     * 
     * @parameter expression="${assemblyinfo.guid}"
     */
    private String guid;

    /**
     * Gets the .NET assembly's GUID. If left blank then a random GUID will be
     * generated when the plug-in is executed.
     * 
     * @return The .NET assembly's GUID.
     */
    public String getGuid()
    {
        return this.guid;
    }

    /**
     * Sets the .NET assembly's GUID. If left blank then a random GUID will be
     * generated when the plug-in is executed.
     * 
     * @param toSet The .NET assembly's GUID.
     */
    public void setGuid(String toSet)
    {
        this.guid = toSet;
    }

    /**
     * Gets the value used for the AssemblyVersion, AssemblyFileVersion, and
     * AssemblyInformationVersion attributes. The default value is parsed from
     * "project.version". For the first two attributes all characters in the
     * version property after the last digit are stripped from the string in
     * order to comply with .NET version rules.
     * 
     * @return The value used for the AssemblyVersion, AssemblyFileVersion, and
     *         AssemblyInformationVersion attributes. The default value is
     *         parsed from "project.version". For the first two attributes all
     *         characters in the version property after the last digit are
     *         stripped from the string in order to comply with .NET version
     *         rules.
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Sets the value used for the AssemblyVersion, AssemblyFileVersion, and
     * AssemblyInformationVersion attributes. The default value is parsed from
     * "project.version". For the first two attributes all characters in the
     * version property after the last digit are stripped from the string in
     * order to comply with .NET version rules.
     * 
     * @param toSet The value used for the AssemblyVersion, AssemblyFileVersion,
     *        and AssemblyInformationVersion attributes. The default value is
     *        parsed from "project.version". For the first two attributes all
     *        characters in the version property after the last digit are
     *        stripped from the string in order to comply with .NET version
     *        rules.
     */
    public void setVersion(String toSet)
    {
        this.version = toSet;
    }

    /**
     * Gets the value used for the AssemblyTitle and AssemblyProduct (which is
     * formed by combining this class's "company" and "name" properties
     * separated by a space) attributes. The default value is parsed from
     * "project.name".
     * 
     * @return The value used for the AssemblyTitle and AssemblyProduct (which
     *         is formed by combining this class's "company" and "name"
     *         properties separated by a space) attributes. The default value is
     *         parsed from "project.name".
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the value used for the AssemblyTitle and AssemblyProduct (which is
     * formed by combining this class's "company" and "name" properties
     * separated by a space) attributes. The default value is parsed from
     * "project.name".
     * 
     * @param toSet The value used for the AssemblyTitle and AssemblyProduct
     *        (which is formed by combining this class's "company" and "name"
     *        properties separated by a space) attributes. The default value is
     *        parsed from "project.name".
     */
    public void setName(String toSet)
    {
        this.name = toSet;
    }

    /**
     * Gets the value used for the AssemblyProduct attribute (which is formed by
     * combining this class's "company" and "name" properties separated by a
     * space). The default value is parsed from "project.organization.name".
     * 
     * @return The value used for the AssemblyProduct attribute (which is formed
     *         by combining this class's "company" and "name" properties
     *         separated by a space). The default value is parsed from
     *         "project.organization.name".
     */
    public String getCompany()
    {
        return this.company;
    }

    /**
     * Sets the value used for the AssemblyProduct attribute (which is formed by
     * combining this class's "company" and "name" properties separated by a
     * space). The default value is parsed from "project.organization.name".
     * 
     * @param toSet The value used for the AssemblyProduct attribute (which is
     *        formed by combining this class's "company" and "name" properties
     *        separated by a space). The default value is parsed from
     *        "project.organization.name".
     */
    public void setCompany(String toSet)
    {
        this.company = toSet;
    }

    /**
     * Gets the value used for the AssemblyDescription attribute. The default
     * value is parsed from "project.description".
     * 
     * @return The value used for the AssemblyDescription attribute. The default
     *         value is parsed from "project.description".
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Sets the value used for the AssemblyDescription attribute. The default
     * value is parsed from "project.description".
     * 
     * @param toSet The value used for the AssemblyDescription attribute. The
     *        default value is parsed from "project.description".
     */
    public void setDescription(String toSet)
    {
        this.description = toSet;
    }

    public void execute() throws MojoExecutionException
    {
        String outputFileType = getOutputFileType();

        String outputFilePath =
            this.basedir.getAbsolutePath() + "/" + outputFile.getPath();
        File basedirAndOutputFile = new File(outputFilePath);

        File outputDir =
            new File(FilenameUtils.getFullPath(basedirAndOutputFile.getPath()));

        // Create the directory specified in the outputFile path.
        if (!outputDir.exists() && !outputDir.mkdirs())
        {
            throw new MojoExecutionException("Error creating output directory "
                + outputDir.getAbsolutePath());
        }

        String assemblyInfoText = null;

        if (outputFileType.equals("cs"))
        {
            assemblyInfoText = createCSharpAssemblyInfoText();
        }
        else if (outputFileType.equals("vb"))
        {
            assemblyInfoText = createVisualBasicAssemblyInfoText();
        }

        try
        {
            FileUtils.writeStringToFile(basedirAndOutputFile, assemblyInfoText);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing "
                + this.outputFile.getAbsolutePath(), e);
        }
    }

    public static String parseSafeVersion(String unsafeVersion)
    {
        Pattern p = Pattern.compile("(?:\\d|\\.)+");
        Matcher m = p.matcher(unsafeVersion);

        if (m.find())
        {
            return m.group();
        }
        else
        {
            return null;
        }
    }

    public String getOutputFileType() throws MojoExecutionException
    {
        if (this.outputFile == null)
        {
            this.outputFile = new File("Properties/AssemblyInfo.cs");
        }

        String filename = this.outputFile.getName();

        // Get the output file's type.
        String outputFileType =
            FilenameUtils.getExtension(filename).toLowerCase();

        if (!(outputFileType.equals("cs") || outputFileType.equals("vb")))
        {
            throw new MojoExecutionException(
                "The parameter outputFile's file type/extension "
                    + "must be either \"cs\" (C#) or \"vb\" (VisualBasic).");
        }

        return outputFileType;
    }

    public String createCSharpAssemblyInfoText()
    {
        StringBuilder out = new StringBuilder();

        out.append("using System.Reflection;\r\n");
        out.append("using System.Runtime.InteropServices;\r\n\r\n");

        if (this.name != null && !this.name.equals(""))
        {
            out.append("[assembly : AssemblyTitle(\"" + this.name + "\")]\r\n");

            if (this.company != null && !this.company.equals(""))
            {
                out.append("[assembly : AssemblyProduct(\"" + this.company
                    + " " + this.name + "\")]\r\n");
            }
        }

        if (this.description != null && !this.description.equals(""))
        {
            out.append("[assembly : AssemblyDescription(\"" + this.description
                + "\")]\r\n");
        }

        if (this.guid == null || this.guid.equals(""))
        {
            this.guid = UUID.randomUUID().toString();
        }

        out.append("[assembly : Guid(\"" + this.guid + "\")]\r\n");

        if (this.version != null && !this.version.equals(""))
        {
            String safeVersion = parseSafeVersion(this.version);

            if (safeVersion == null)
            {
                safeVersion = "0.0.0.1";
                version = "0.0.0.1";
            }

            out.append("[assembly : AssemblyVersion(\"" + safeVersion
                + "\")]\r\n");

            out.append("[assembly : AssemblyFileVersion(\"" + safeVersion
                + "\")]\r\n");
            out.append("[assembly : AssemblyInformationalVersionAttribute(\""
                + version + "\")]");

        }

        return out.toString();
    }

    public String createVisualBasicAssemblyInfoText()
    {
        StringBuilder out = new StringBuilder();

        out.append("Imports System.Reflection\r\n");
        out.append("Imports System.Runtime.InteropServices\r\n\r\n");

        if (this.name != null && !this.name.equals(""))
        {
            out.append("<Assembly : AssemblyTitle(\"" + this.name + "\")>\r\n");

            if (this.company != null && !this.company.equals(""))
            {
                out.append("<Assembly : AssemblyProduct(\"" + this.company
                    + " " + this.name + "\")>\r\n");
            }
        }

        if (this.description != null && !this.description.equals(""))
        {
            out.append("<Assembly : AssemblyDescription(\"" + this.description
                + "\")>\r\n");
        }

        if (this.guid == null || this.guid.equals(""))
        {
            this.guid = UUID.randomUUID().toString();
        }

        out.append("<Assembly : Guid(\"" + this.guid + "\")>\r\n");

        if (this.version != null && !this.version.equals(""))
        {
            String safeVersion = parseSafeVersion(this.version);

            if (safeVersion == null)
            {
                safeVersion = "0.0.0.1";
                version = "0.0.0.1";
            }

            out.append("<Assembly : AssemblyVersion(\"" + safeVersion
                + "\")>\r\n");

            out.append("<Assembly : AssemblyFileVersion(\"" + safeVersion
                + "\")>\r\n");
            out.append("<Assembly : AssemblyInformationalVersionAttribute(\""
                + version + "\")>");

        }

        return out.toString();
    }
}
