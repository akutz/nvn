/*******************************************************************************
 * Copyright (c) 2010, Schley Andrew Kutz All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of the Schley Andrew Kutz nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.sf.nvn.plugin;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.nvn.commons.dotnet.OutputFileType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A MOJO for creating .NET assembly information files.
 * 
 * @goal assembly-info
 * @phase generate-sources
 * @description A MOJO for creating .NET assembly information files.
 */
public class AssemblyInfoMojo extends AbstractNvnMojo
{
    /**
     * The pattern used to match the assembly GUID in an assembly information
     * file.
     */
    private static Pattern ASSEM_GUID_ATTR_PATT = Pattern
        .compile("(?i)\\[assembly\\s*:\\s*Guid\\(\"(.*)\"\\)\\]");

    /**
     * The value of the AssemblyTitle attribute.
     * 
     * @parameter default-value="${project.name}"
     */
    String assemblyTitle;

    /**
     * The value of the AssemblyProduct attribute.
     * 
     * @parameter default-value="${project.organization.name} ${project.name}"
     */
    String assemblyProduct;

    /**
     * The value of the AssemblyCompany attribute.
     * 
     * @parameter default-value="${project.organization.name}"
     */
    String assemblyCompany;

    /**
     * The value of the AssemblyDescription attribute.
     * 
     * @parameter default-value="${project.description}"
     */
    String assemblyDescription;

    /**
     * The value of the AssemblyInformationalVersion attribute.
     * 
     * @parameter default-value="${project.version}"
     */
    String assemblyInformationalVersion;

    /**
     * The location of the .NET AssemblyInfo file to output.
     * 
     * @parameter default-value="${basedir}/Properties/AssemblyInfo.cs"
     */
    File outputFile;

    /**
     * The .NET assembly's GUID. If left blank then a random GUID will be
     * generated.
     * 
     * @parameter
     */
    String guid;

    /**
     * The type of the output file that will be created.
     */
    OutputFileType outputFileType;

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
        parseGuid();
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
    void parseGuid() throws MojoExecutionException
    {
        if (StringUtils.isNotEmpty(this.guid))
        {
            return;
        }

        this.guid = UUID.randomUUID().toString();

        if (this.outputFile == null)
        {
            return;
        }

        if (!this.outputFile.exists())
        {
            return;
        }

        String fileContents;

        try
        {
            fileContents = FileUtils.readFileToString(this.outputFile);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(String.format(
                "Unable to read assembly information file %s",
                this.outputFile), e);
        }

        Matcher m = ASSEM_GUID_ATTR_PATT.matcher(fileContents);

        if (!m.find())
        {
            error(
                "Error parsing assembly information file %s for GUID",
                this.outputFile);
            return;
        }

        this.guid = m.group(1);
        info(
            "Parsed existing GUID, %s, from assembly information file %s",
            this.guid,
            this.outputFile);
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

    private void outAttr(Writer out, String assemblyAttr, String value)
        throws IOException
    {
        String s =
            StringUtils.isEmpty(value) ? assemblyAttr : String.format(
                "%s(\"%s\")",
                assemblyAttr,
                value);

        outAttr(out, s);
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

            if (!this.assemblyTitle.startsWith("Unnamed - unknown"))
            {
                outAttr(out, "AssemblyTitle", this.assemblyTitle);

                if (StringUtils.isNotEmpty(this.assemblyCompany))
                {
                    outAttr(out, "AssemblyCompany", this.assemblyCompany);
                    outAttr(out, "AssemblyProduct", this.assemblyProduct);
                }
            }

            if (StringUtils.isNotEmpty(this.assemblyDescription))
            {
                outAttr(out, "AssemblyDescription", this.assemblyDescription);
            }

            outAttr(out, "Guid", this.guid);
            outAttr(out, "AssemblyVersion", getStandardVersion());
            outAttr(out, "AssemblyFileVersion", getStandardVersion());
            outAttr(
                out,
                "AssemblyInformationalVersion",
                this.assemblyInformationalVersion);

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
