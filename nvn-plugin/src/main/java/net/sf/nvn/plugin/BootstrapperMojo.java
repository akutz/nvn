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

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A MOJO for creating a bootstrapper for an MSI package and its prerequisites.
 * 
 * @goal bootstrap
 * @phase package
 * @description A MOJO for creating a bootstrapper for an MSI package and its
 *              prerequisites.
 * @requiresDependencyResolution
 */
public class BootstrapperMojo extends AbstractExeMojo
{
    /**
     * The bootstrapper's icon file.
     * 
     * @parameter
     * @required
     */
    File icon;

    /**
     * The MSI package's prerequisite files. This array's elements must have
     * 1-to-1 matches with preReqNames.
     * 
     * @parameter
     * @required
     */
    File[] preReqFiles;

    /**
     * The MSI package's prerequisite names. This array's elements must have
     * 1-to-1 matches with preReqFiles.
     * 
     * @parameter
     * @required
     */
    String[] preReqNames;

    /**
     * The MSI package.
     * 
     * @parameter
     * @required
     */
    File msi;

    /**
     * The name of the output file to create sans extension.
     * 
     * @parameter
     * @required
     */
    String outputFileName;

    private File outProjFile;

    @Override
    String getArgs(int execution)
    {
        return quote(this.outProjFile.toString());
    }

    @Override
    File getDefaultCommand()
    {
        return new File("msbuild.exe");
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        // Create a temporary directory for the project files.
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        this.tmpProjDir = new File(tmpDir, UUID.randomUUID().toString());

        if (!this.tmpProjDir.mkdir())
        {
            throw new MojoExecutionException(
                "Error creating project directory: " + tmpProjDir);
        }

        // Copy the icon file.
        File appIco = new File(tmpProjDir, "app.ico");
        try
        {
            FileUtils.copyFile(this.icon, appIco);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(String.format(
                "Error copying icon file from %s to %s",
                this.icon,
                appIco), e);
        }

        initProjFile();
        initResFile();
        initProgramFile();
    }

    private File tmpProjDir;

    private String getString(File file)
    {
        return file.getPath().replaceAll("\\\\", "\\\\\\\\");
    }

    private void initProjFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream(
                "/msibootstrapper/MsiBootstrapper.csproj");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/msibootstrapper/MsiBootstrapper.csproj'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for MsiBootstrapper.csproj",
                e);
        }

        content =
            content.replaceAll("\\$\\{BuildConfig\\}", getBuildConfigName());
        content =
            content.replaceAll("\\$\\{ProjectGuid\\}", UUID
                .randomUUID()
                .toString());
        content =
            content.replaceAll("\\$\\{AssemblyName\\}", this.outputFileName);

        StringBuilder buff = new StringBuilder();

        buff.append(String.format(
            "<None Include=\"%s\" />\n",
            getString(this.msi)));

        for (File f : this.preReqFiles)
        {
            buff.append(String.format(
                "    <None Include=\"%s\" />\n",
                getString(f)));
        }

        content = content.replaceAll("\\$\\{Resources\\}", buff.toString());

        this.outProjFile = new File(this.tmpProjDir, "MsiBootstrapper.csproj");

        try
        {
            FileUtils.writeStringToFile(this.outProjFile, content);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing data to: "
                + this.outProjFile, e);
        }

        info("Created project file at " + this.outProjFile);
    }

    private void initResFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream(
                "/msibootstrapper/Resources.resx");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/msibootstrapper/Resources.resx'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for Resources.resx",
                e);
        }

        StringBuilder buff = new StringBuilder();

        String targetNameEntry =
            String.format(
                "  <data name=\"TargetName\" "
                    + "xml:space=\"preserve\"><value>%s</value>" + "</data>\n",
                this.outputFileName);
        buff.append(targetNameEntry);

        String targetDataEntry =
            String.format(
                "  <data name=\"TargetData\" "
                    + "type=\"System.Resources.ResXFileRef, "
                    + "System.Windows.Forms\"><value>%s;System.Byte[], "
                    + "mscorlib, Version=2.0.0.0,Culture=neutral, "
                    + "PublicKeyToken=b77a5c561934e089</value></data>\n",
                getString(this.msi));
        buff.append(targetDataEntry);

        for (int x = 0; x < this.preReqNames.length; ++x)
        {
            String name = this.preReqNames[x];
            File file = this.preReqFiles[x];

            String nameEntry =
                String.format(
                    "  <data name=\"PreReq%02dName\" "
                        + "xml:space=\"preserve\"><value>%s</value>"
                        + "</data>\n",
                    x,
                    name);
            buff.append(nameEntry);

            String dataEntry =
                String.format(
                    "  <data name=\"PreReq%02dData\" "
                        + "type=\"System.Resources.ResXFileRef, "
                        + "System.Windows.Forms\"><value>%s;System.Byte[], "
                        + "mscorlib, Version=2.0.0.0,Culture=neutral, "
                        + "PublicKeyToken=b77a5c561934e089</value></data>\n",
                    x,
                    getString(file));
            buff.append(dataEntry);
        }

        content = content.replaceAll("\\$\\{Resources\\}", buff.toString());

        File outFile = new File(this.tmpProjDir, "Resources.resx");

        try
        {
            FileUtils.writeStringToFile(outFile, content);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing data to: "
                + outFile, e);
        }
    }

    private void initProgramFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream("/msibootstrapper/Program.cs");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/msibootstrapper/Program.cs'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for Program.cs",
                e);
        }

        content =
            content.replaceAll("\\$\\{AssemblyName\\}", this.outputFileName);
        content =
            content.replaceAll("\\$\\{OrganizationName\\}", super.mavenProject
                .getOrganization()
                .getName());
        content =
            content.replaceAll("\\$\\{Year\\}", new SimpleDateFormat("yyyy")
                .format(new Date())
                .toString());
        content =
            content.replaceAll("\\$\\{AssemblyGuid\\}", UUID
                .randomUUID()
                .toString());
        content =
            content.replaceAll(
                "\\$\\{StandardVersion\\}",
                super.getStandardVersion());
        content =
            content.replaceAll(
                "\\$\\{Version\\}",
                super.mavenProject.getVersion());

        File outFile = new File(this.tmpProjDir, "Program.cs");

        try
        {
            FileUtils.writeStringToFile(outFile, content);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error writing data to: "
                + outFile, e);
        }
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (executionException != null)
        {
            debug("Skipping post processing because error occurred");
            return;
        }

        File tmpBootstrapExe =
            new File(String.format(
                "%s\\bin\\%s\\%s.exe",
                this.tmpProjDir,
                getBuildConfigName(),
                this.outputFileName));

        File bootstrapExe =
            new File(
                super.mavenProject.getBuild().getDirectory(),
                tmpBootstrapExe.getName());

        try
        {
            FileUtils.copyFile(tmpBootstrapExe, bootstrapExe);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(String.format(
                "Error copying %s to %s",
                tmpBootstrapExe,
                bootstrapExe), e);
        }

        try
        {
            FileUtils.deleteDirectory(this.tmpProjDir);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error deleting "
                + this.tmpProjDir, e);
        }
    }

    @Override
    String getMojoName()
    {
        return "bootstrapper";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }
}
