package net.sf.nvn.plugins.assemblyinfo;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven plug-in for creating .NET assembly information files.
 * 
 * @goal generate-assembly-info
 * @phase generate-sources
 * @description A Maven plug-in for creating .NET assembly information files.
 */
public class AssemblyInfoMojo extends AbstractMojo
{
    /**
     * The base directory from Maven's perspective.
     * 
     * @parameter expression="${basedir}"
     */
    File baseDir;

    /**
     * Set to true to skip this plug-in.
     * 
     * @parameter expression="${assemblyinfo.skip}" default-value="false"
     */
    boolean skip;

    /**
     * Set to true to force generation. Normally an AssemblyInfo file is only
     * generated when a csproj or vbproj file is detected. Settings skip to true
     * still skips this plug-in even if force is also set to true.
     * 
     * @parameter expression="${assemblyinfo.force}" default-value="false"
     */
    boolean force;

    /**
     * The value used for the AssemblyVersion, AssemblyFileVersion, and
     * AssemblyInformationVersion attributes. The default value is parsed from
     * "project.version". For the first two attributes all characters in the
     * version property after the last digit are stripped from the string in
     * order to comply with .NET version rules.
     * 
     * @parameter expression="${project.version}" default-value="0.0.0.0"
     */
    String version;

    /**
     * The value used for the AssemblyTitle and AssemblyProduct (which is formed
     * by combining this class's "company" and "name" properties separated by a
     * space) attributes. The default value is parsed from "project.name".
     * 
     * @parameter expression="${project.name}"
     */
    String name;

    /**
     * The value used for the AssemblyProduct attribute (which is formed by
     * combining this class's "company" and "name" properties separated by a
     * space). The default value is parsed from "project.organization.name".
     * 
     * @parameter expression="${project.organization.name}"
     */
    String company;

    /**
     * The value used for the AssemblyDescription attribute. The default value
     * is parsed from "project.description".
     * 
     * @parameter expression="${project.description}"
     */
    String description;

    /**
     * The location of the .NET AssemblyInfo file to output.
     * 
     * @parameter expression="${assemblyinfo.outputFile}"
     *            default-value="${basedir}/Properties/AssemblyInfo.cs"
     */
    File outputFile;

    /**
     * The .NET assembly's GUID. If left blank then a random GUID will be
     * generated.
     * 
     * @parameter expression="${assemblyinfo.guid}"
     */
    String guid;

    /**
     * The type of the output file that will be created.
     */
    OutputFileType outputFileType;

    /**
     * The safe version.
     */
    String safeVersion;

    public void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            return;
        }

        if (!this.force)
        {
            if (!shouldExecute())
            {
                return;
            }
        }

        createOutputDirectories();

        parseOutputFileType();

        parseSafeVersion();

        parseGuid();

        String assemblyInfoText = createAssemblyInfoText();

        try
        {
            FileUtils.writeStringToFile(this.outputFile, assemblyInfoText);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing "
                + this.outputFile.getAbsolutePath(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean shouldExecute() throws MojoExecutionException
    {
        Collection projFiles = FileUtils.listFiles(this.baseDir, new String[]
        {
            "csproj", "vbproj"
        }, false);

        return projFiles != null && projFiles.size() > 0;
    }

    public void createOutputDirectories() throws MojoExecutionException
    {
        File outputDir =
            new File(FilenameUtils.getFullPath(this.outputFile
                .getAbsolutePath()));

        // Create the directory specified in the outputFile path.
        if (!outputDir.exists() && !outputDir.mkdirs())
        {
            throw new MojoExecutionException("Error creating output directory "
                + outputDir.getAbsolutePath());
        }
    }

    public void parseGuid()
    {
        if (StringUtils.isEmpty(this.guid))
        {
            this.guid = UUID.randomUUID().toString();
        }
    }

    public void parseSafeVersion() throws MojoExecutionException
    {
        Pattern p = Pattern.compile("(?:\\d|\\.)+");
        Matcher m = p.matcher(this.version);

        if (m.find())
        {
            this.safeVersion = m.group();
        }
        else
        {
            throw new MojoExecutionException("Error parsing safe version from "
                + this.version);
        }
    }

    public void parseOutputFileType() throws MojoExecutionException
    {
        String filename = this.outputFile.getName();

        // Get the output file's type.
        String extension = FilenameUtils.getExtension(filename);

        if (extension.equalsIgnoreCase("cs"))
        {
            this.outputFileType = OutputFileType.CSharp;
        }
        else if (extension.equalsIgnoreCase("vb"))
        {
            this.outputFileType = OutputFileType.VisualBasic;
        }
        else
        {
            throw new MojoExecutionException(
                "The parameter outputFile's file type/extension "
                    + "must be either \"cs\" (C#) or \"vb\" (VisualBasic).");
        }
    }

    private void outImport(Writer out, String toWrite) throws IOException
    {
        if (this.outputFileType == OutputFileType.CSharp)
        {
            out.write("using " + toWrite + ";\r\n");
        }
        else if (this.outputFileType == OutputFileType.VisualBasic)
        {
            out.write("Imports " + toWrite + "\r\n");
        }
    }

    private void outAttr(Writer out, String toWrite) throws IOException
    {
        if (this.outputFileType == OutputFileType.CSharp)
        {
            out.write("[assembly : " + toWrite + "]\r\n");
        }
        else if (this.outputFileType == OutputFileType.VisualBasic)
        {
            out.write("<Assembly : " + toWrite + ">\r\n");
        }
    }

    public String createAssemblyInfoText() throws MojoExecutionException
    {
        try
        {
            StringWriter out = new StringWriter();

            outImport(out, "System.Reflection");
            outImport(out, "System.Runtime.InteropServices");

            out.write("\r\n");

            if (StringUtils.isNotEmpty(this.name))
            {
                outAttr(out, "AssemblyTitle(\"" + this.name + "\")");

                if (StringUtils.isNotEmpty(this.company))
                {
                    outAttr(out, "AssemblyProduct(\"" + this.company + " "
                        + this.name + "\")");
                }
            }

            if (StringUtils.isNotEmpty(this.description))
            {
                outAttr(out, "AssemblyDescription(\"" + this.description
                    + "\")");
            }

            outAttr(out, "Guid(\"" + this.guid + "\")");

            outAttr(out, "AssemblyVersion(\"" + this.safeVersion + "\")");
            outAttr(out, "AssemblyFileVersion(\"" + this.safeVersion + "\")");
            outAttr(out, "AssemblyInformationalVersionAttribute(\""
                + this.version + "\")");

            out.close();

            return out.toString();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error creating assembly information",
                e);
        }
    }
}
