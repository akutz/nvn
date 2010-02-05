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
import org.apache.maven.project.MavenProject;

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
                    debug("not generating assembly info file, same content as existing one");
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
    public void preExecute() throws MojoExecutionException
    {
        parseOutputFileType();

        initVersion();

        parseSafeVersion();

        parseGuid();
    }

    /**
     * Initializes this project's version.
     */
    void initVersion()
    {
        if (super.mavenProject.getVersion().equals(
            MavenProject.EMPTY_PROJECT_VERSION))
        {
            super.mavenProject.setVersion("0.0.0.0");
        }
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    /**
     * Creates the output directories necessary for the assembly information
     * file.
     * 
     * @throws MojoExecutionException
     */
    void createOutputDirectories() throws MojoExecutionException
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

    /**
     * Parses the GUID.
     */
    void parseGuid()
    {
        if (StringUtils.isEmpty(this.guid))
        {
            this.guid = UUID.randomUUID().toString();
        }
    }

    /**
     * Parses the numeric version from the project's version.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void parseSafeVersion() throws MojoExecutionException
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

    /**
     * Parses the output file type from the output file's extension.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void parseOutputFileType() throws MojoExecutionException
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

    /**
     * Writes an import statement.
     * 
     * @param out The out stream.
     * @param toWrite The text to write.
     * @throws IOException When error occurs.
     */
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

    /**
     * Writes an attribute.
     * 
     * @param out The out stream.
     * @param toWrite The text to write.
     * @throws IOException When error occurs.
     */
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

    /**
     * Creates the assembly information text.
     * 
     * @return The assembly information text.
     * @throws MojoExecutionException When an error occurs.
     */
    String createAssemblyInfoText() throws MojoExecutionException
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
        return isCSProject() || isVBProject();
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }
}
