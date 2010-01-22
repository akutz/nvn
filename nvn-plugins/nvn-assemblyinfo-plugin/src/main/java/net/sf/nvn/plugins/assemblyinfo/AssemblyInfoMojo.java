package net.sf.nvn.plugins.assemblyinfo;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven plug-in for creating .NET assembly information files.
 * 
 * @goal generate-assembly-info
 * @phase generate-sources
 * @description A Maven plug-in for creating .NET assembly information files.
 */
public class AssemblyInfoMojo extends AbstractNvnMojo
{
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

    @Override
    public String getMojoName()
    {
        return "assemblyinfo";
    }

    @Override
    public void nvnExecute() throws MojoExecutionException
    {
        String assemblyInfoText = createAssemblyInfoText();

        try
        {
            if (this.outputFile.exists())
            {
                String oldText = FileUtils.readFileToString(this.outputFile);

                if (oldText.equals(assemblyInfoText))
                {
                    getLog()
                        .debug(
                            "nvn-"
                                + getMojoName()
                                + ": not generating assembly info file, same content as existing one");
                    return;
                }
            }
            
            createOutputDirectories();

            FileUtils.writeStringToFile(this.outputFile, assemblyInfoText);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing "
                + this.outputFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void prepareForExecute() throws MojoExecutionException
    {
        parseOutputFileType();

        loadVersion();

        parseSafeVersion();

        parseGuid();
    }

    public void loadVersion()
    {
        if (super.mavenProject.getVersion().equals("0"))
        {
            super.mavenProject.setVersion("0.0.0.0");
        }
    }

    @Override
    public boolean shouldExecute() throws MojoExecutionException
    {
        return true;
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
        Matcher m = p.matcher(super.mavenProject.getVersion());

        if (m.find())
        {
            this.safeVersion = m.group();
        }
        else
        {
            throw new MojoExecutionException("Error parsing safe version from "
                + super.mavenProject.getVersion());
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

            if (!super.mavenProject.getName().startsWith("Unnamed - unknown"))
            {
                outAttr(out, "AssemblyTitle(\"" + super.mavenProject.getName()
                    + "\")");

                if (StringUtils.isNotEmpty(super.mavenProject
                    .getOrganization()
                    .getName()))
                {
                    outAttr(out, "AssemblyProduct(\""
                        + super.mavenProject.getOrganization().getName() + " "
                        + super.mavenProject.getName() + "\")");
                }
            }

            if (StringUtils.isNotEmpty(super.mavenProject.getDescription()))
            {
                outAttr(out, "AssemblyDescription(\""
                    + super.mavenProject.getDescription() + "\")");
            }

            outAttr(out, "Guid(\"" + this.guid + "\")");

            outAttr(out, "AssemblyVersion(\"" + this.safeVersion + "\")");
            outAttr(out, "AssemblyFileVersion(\"" + this.safeVersion + "\")");
            outAttr(out, "AssemblyInformationalVersionAttribute(\""
                + super.mavenProject.getVersion() + "\")");

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

    @Override
    public boolean isProjectTypeValid()
    {
        return isProject();
    }
}
