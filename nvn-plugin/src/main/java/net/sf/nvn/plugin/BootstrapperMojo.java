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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
     * When this parameter is set to true the informational version will use the
     * NVN version instead of the Maven version.
     * 
     * @parameter
     */
    boolean useNvnInformationalVersion;

    /**
     * The bootstrapper's icon file.
     * 
     * @parameter
     * @required
     */
    File icon;

    /**
     * The bootstrapper's license file.
     * 
     * @parameter
     * @required
     */
    File license;

    /**
     * The background image used when displaying the license screen and final
     * screen.
     * 
     * @parameter
     * @required
     */
    File licensePageBackground;

    /**
     * The banner image used when displaying the installer's progress.
     * 
     * @parameter
     * @required
     */
    File installPageBackground;

    /**
     * @parameter
     * @required
     */
    String endPageUrlLink;

    /**
     * @parameter
     * @required
     */
    String endPageUrlText;

    /**
     * The pre-requisite configuration object.
     * 
     * @parameter
     * @required
     */
    InstallPackageConfig installPackageConfig;

    /**
     * @parameter
     */
    RegKeyConfig regKeyConfig;

    /**
     * @parameter
     */
    RegValueConfig regValueConfig;

    /**
     * @parameter
     */
    RunningProcessConfig runningProcessConfig;

    /**
     * @parameter
     */
    ProductRemoverConfig productRemoverConfig;

    /**
     * The name of the output file to create sans extension.
     * 
     * @parameter
     * @required
     */
    String outputFileName;

    /**
     * The name of the product the bootstrapper is responsible for installing.
     * 
     * @parameter
     * @required
     */
    String productName;

    /**
     * Settings this parameter to true embeds a special manifest inside the
     * bootstrapper executable that requests UAC privilege escalation on Windows
     * Vista and newer operating systems.
     * 
     * @parameter default-value="true"
     */
    boolean demandUac;

    private File outProjFile;

    private File manifest;

    private File bootstrapExe;

    @Override
    int getExecutions()
    {
        return 2;
    }

    protected boolean skipExec(int execution)
    {
        return execution == 1 && !this.demandUac;
    }

    @Override
    String getArgs(int execution)
    {
        if (execution == 0)
        {
            return quote(this.outProjFile.toString());
        }
        else
        {
            return String.format(
                "-manifest %s -outputresource:%s;#1",
                quote(getPath(this.manifest)),
                quote(getPath(this.bootstrapExe)));
        }
    }

    @Override
    File getCommand(int execution)
    {
        if (execution == 0)
        {
            return new File("msbuild.exe");
        }
        else
        {
            return new File("mt.exe");
        }
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

        initInstallPackagesFileParts();
        initProjFile();
        initResFile();
        initAssemblyInfoFile();
        initInstallResourcesFile();
        initEtcFiles();
    }

    private File tmpProjDir;

    private String getString(File file)
    {
        return file.getPath().replaceAll("\\\\", "\\\\\\\\");
    }

    private void initInstallPackagesFileParts() throws MojoExecutionException
    {
        for (int x = 0; x < this.installPackageConfig.installPackages.size(); ++x)
        {
            InstallPackage ip =
                this.installPackageConfig.installPackages.get(x);

            int chunks = calcChunks(ip);
            ip.fileParts = new File[chunks];

            FileInputStream fis;

            try
            {
                fis = new FileInputStream(ip.file);
            }
            catch (FileNotFoundException e)
            {
                throw new MojoExecutionException(
                    "Error creating input stream for: " + ip.file);
            }

            for (int y = 0; y < chunks; ++y)
            {
                byte[] page = new byte[4096];
                int read = 0;
                int total = 0;

                try
                {
                    ip.fileParts[y] =
                        new File(
                            super.mavenProject.getBuild().getDirectory(),
                            String.format("InstallPackage%02d_%02d", x, y));

                    ip.fileParts[y].delete();

                    FileOutputStream fos =
                        new FileOutputStream(ip.fileParts[y]);

                    while ((read = fis.read(page)) != -1)
                    {
                        fos.write(page, 0, read);

                        total += read;

                        if (total >= CHUNK_SIZE)
                        {
                            break;
                        }
                    }

                    fos.close();
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException("Error chunking file: "
                        + ip.file);
                }
            }

            try
            {
                fis.close();
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Error closing : " + ip.file);
            }
        }
    }

    private void initEtcFiles() throws MojoExecutionException
    {
        initFile("Properties/Resources.Designer.cs");
        initFile("RunningProcess.cs");
        initFile("InstallManager.cs");
        initFile("InstallPackage.cs");
        initFile("Program.cs");
        initFile("RegUtils.cs");
        initFile("RegValue.cs");
        initFile("RegKey.cs");
        initFile("MsiUtil.cs");
        initFile("ProductRemover.cs");
        initFile("InstallCheck.cs");
        initFile("ExtensionAttribute.cs");

        initFile("BeginAndEndPage.cs");
        initFile("BeginAndEndPage.Designer.cs");
        initFile("BeginAndEndPage.resx");

        initFile("EndPage.cs");
        initFile("EndPage.Designer.cs");
        initFile("EndPage.resx");

        initFile("Page.cs");
        initFile("Page.Designer.cs");
        initFile("Page.resx");

        initFile("LicensePage.cs");
        initFile("LicensePage.Designer.cs");
        initFile("LicensePage.resx");

        initFile("InstallPage.cs");
        initFile("InstallPage.Designer.cs");
        initFile("InstallPage.resx");

        initFile("MainForm.cs");
        initFile("MainForm.Designer.cs");
        initFile("MainForm.resx");
    }

    private void initFile(String path) throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream("/nvnbootstrapper/" + path);

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/nvnbootstrapper/" + path + "'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for " + path,
                e);
        }

        File outFile = new File(this.tmpProjDir, path);

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

    private void initProjFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream(
                "/nvnbootstrapper/NvnBootstrapper.csproj");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/nvnbootstrapper/NvnBootstrapper.csproj'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for NvnBootstrapper.csproj",
                e);
        }

        content = content.replaceAll("\\$\\{BuildConfig\\}", getBuildConfig());
        content =
            content.replaceAll("\\$\\{ProjectGuid\\}", UUID
                .randomUUID()
                .toString());
        content =
            content.replaceAll("\\$\\{AssemblyName\\}", this.outputFileName);

        StringBuilder buff = new StringBuilder();

        buff.append(String.format(
            "<None Include=\"%s\" />\n",
            getString(this.icon)));

        buff.append(String.format(
            "<None Include=\"%s\" />\n",
            getString(this.license)));

        buff.append(String.format(
            "<None Include=\"%s\" />\n",
            getString(this.licensePageBackground)));

        buff.append(String.format(
            "<None Include=\"%s\" />\n",
            getString(this.installPageBackground)));

        for (InstallPackage ip : this.installPackageConfig.installPackages)
        {
            for (File filePart : ip.fileParts)
            {
                buff.append(String.format(
                    "    <None Include=\"%s\" />\r\n",
                    getString(filePart)));
            }
        }

        content = content.replaceAll("\\$\\{Resources\\}", buff.toString());

        this.outProjFile = new File(this.tmpProjDir, "NvnBootstrapper.csproj");

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
                "/nvnbootstrapper/Properties/Resources.resx");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/nvnbootstrapper/Properties/Resources.resx'");
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

        content = content.replaceAll("\\$\\{Icon\\}", getString(this.icon));
        content =
            content.replaceAll("\\$\\{License\\}", getString(this.license));
        content =
            content.replaceAll(
                "\\$\\{LicensePageBackground\\}",
                getString(this.licensePageBackground));
        content =
            content.replaceAll(
                "\\$\\{InstallPageBackground\\}",
                getString(this.installPageBackground));

        StringBuilder buff = new StringBuilder();

        for (InstallPackage ip : this.installPackageConfig.installPackages)
        {
            for (File filePart : ip.fileParts)
            {
                String dataEntry =
                    String
                        .format(
                            "  <data name=\"%s\" "
                                + "type=\"System.Resources.ResXFileRef, "
                                + "System.Windows.Forms\"><value>%s;System.Byte[], "
                                + "mscorlib, Version=2.0.0.0,Culture=neutral, "
                                + "PublicKeyToken=b77a5c561934e089</value></data>\r\n",
                            FilenameUtils.getBaseName(filePart.toString()),
                            getString(filePart));
                buff.append(dataEntry);
            }
        }

        content =
            content.replaceAll("\\$\\{InstallPackages\\}", buff.toString());

        File outFile = new File(this.tmpProjDir, "Properties\\Resources.resx");

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

    private void initAssemblyInfoFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream(
                "/nvnbootstrapper/Properties/AssemblyInfo.cs");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/nvnbootstrapper/Properties/AssemblyInfo.cs'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for AssemblyInfo.cs",
                e);
        }

        content =
            content.replaceAll("\\$\\{AssemblyTitle\\}", this.productName);
        content =
            content.replaceAll("\\$\\{AssemblyProduct\\}", this.productName);
        content =
            content.replaceAll("\\$\\{AssemblyCompany\\}", super.mavenProject
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
            content.replaceAll("\\$\\{NvnVersion\\}", super
                .getNvnVersion()
                .toString());

        if (this.useNvnInformationalVersion)
        {
            content =
                content.replaceAll(
                    "\\$\\{NvnVersionWithPrefixAndSuffix\\}",
                    super.getNvnVersion().toString());
        }
        else
        {
            content =
                content.replaceAll(
                    "\\$\\{NvnVersionWithPrefixAndSuffix\\}",
                    super.getNvnVersion().toStringWithPrefixAndSuffix());
        }

        File outFile = new File(this.tmpProjDir, "Properties\\AssemblyInfo.cs");

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

    private void initInstallResourcesFile() throws MojoExecutionException
    {
        InputStream stream =
            this.getClass().getResourceAsStream(
                "/nvnbootstrapper/InstallResources.cs");

        if (stream == null)
        {
            throw new MojoExecutionException(
                "Error getting stream for '/nvnbootstrapper/InstallResources.cs'");
        }

        String content;

        try
        {
            content = IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error getting string for InstallResources.cs",
                e);
        }

        content = content.replaceAll("\\$\\{ProductName\\}", this.productName);
        content =
            content.replaceAll("\\$\\{EndPageUrlLink\\}", this.endPageUrlLink);
        content =
            content.replaceAll("\\$\\{EndPageUrlText\\}", this.endPageUrlText);

        StringBuilder buff = new StringBuilder();

        if (this.productRemoverConfig == null)
        {
            buff
                .append("public static ProductRemover[] ProductRemovers = new ProductRemovers[0];\r\n");
        }
        else
        {
            buff
                .append("public static ProductRemover[] ProductRemovers = new[]\r\n");
            buff.append("{\r\n");

            for (ProductRemover pr : this.productRemoverConfig.productRemovers)
            {
                String l =
                    String
                        .format(
                            "new ProductRemover {ProductCode=@\"%s\", Name=@\"%s\", Message=@\"%s\"},\r\n",
                            pr.productCode,
                            pr.name,
                            pr.message);
                buff.append(l);
            }

            buff.append("};\r\n");
        }

        if (this.regKeyConfig == null)
        {
            buff.append("public static RegKey[] RegKeys = new RegKey[0];\r\n");
        }
        else
        {
            buff.append("public static RegKey[] RegKeys = new[]\r\n");
            buff.append("{\r\n");

            for (RegKey rk : this.regKeyConfig.regKeys)
            {
                buff.append(rk.toString());
            }

            buff.append("};\r\n");
        }

        if (this.regValueConfig == null)
        {
            buff
                .append("public static RegValue[] RegValues = new RegValue[0];\r\n");
        }
        else
        {
            buff.append("public static RegValue[] RegValues = new[]\r\n");
            buff.append("{\r\n");

            for (RegValue rv : this.regValueConfig.regValues)
            {
                buff.append(rv.toString());
            }

            buff.append("};\r\n");
        }

        if (this.runningProcessConfig == null)
        {
            buff
                .append("public static RunningProcess[] RunningProcesses = new RunningProcess[0];\r\n");
        }
        else
        {
            buff
                .append("public static RunningProcess[] RunningProcesses = new[]\r\n");
            buff.append("{\r\n");

            for (RunningProcess rp : this.runningProcessConfig.runningProcesses)
            {
                buff.append(rp.toString());
            }

            buff.append("};\r\n");
        }

        buff
            .append("public static InstallPackage[] InstallPackages = new[]\r\n");
        buff.append("{\r\n");

        for (InstallPackage ip : this.installPackageConfig.installPackages)
        {
            buff.append(ip.toString());
        }

        buff.append("};\r\n");

        content =
            content.replaceAll("\\$\\{InstallResources\\}", buff.toString());

        File outFile = new File(this.tmpProjDir, "InstallResources.cs");

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

    private final static int CHUNK_SIZE = 10485760;

    private static int calcChunks(InstallPackage ip)
    {
        double fsizeB = ip.file.length();
        double chunks = Math.ceil(fsizeB / (double) CHUNK_SIZE);
        return (int) chunks;
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (executionException != null)
        {
            publishTeamCityArtifact(this.bootstrapExe);
        }
    }

    @Override
    void postExec(int execution, Process process) throws MojoExecutionException
    {
        if (execution == 0 && process.exitValue() == 0)
        {
            File tmpBootstrapExe =
                new File(String.format(
                    "%s\\bin\\%s\\%s.exe",
                    this.tmpProjDir,
                    getBuildConfig(),
                    this.outputFileName));

            this.bootstrapExe =
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

            /*
             * try { FileUtils.deleteDirectory(this.tmpProjDir); } catch
             * (IOException e) { throw new
             * MojoExecutionException("Error deleting " + this.tmpProjDir, e); }
             */

            if (this.demandUac)
            {
                final String format =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
                        + "<assembly xmlns=\"urn:schemas-microsoft-com:asm.v1\" manifestVersion=\"1.0\">\r\n"
                        + "   <assemblyIdentity version=\"%1$s\" processorArchitecture=\"X86\" name=\"%2$s\" type=\"win32\"/>\r\n"
                        + "    <description>%3$s</description>\r\n"
                        + "      <trustInfo xmlns=\"urn:schemas-microsoft-com:asm.v3\">\r\n"
                        + "         <security>\r\n"
                        + "            <requestedPrivileges>\r\n"
                        + "               <requestedExecutionLevel level=\"requireAdministrator\"/>\r\n"
                        + "            </requestedPrivileges>\r\n"
                        + "         </security>\r\n" + "      </trustInfo>\r\n"
                        + "</assembly>";

                String exePath = bootstrapExe.toString();
                String exeBaseName = FilenameUtils.getBaseName(exePath);
                String exeExtension = FilenameUtils.getExtension(exePath);

                String text =
                    String.format(
                        format,
                        super.getNvnVersion(),
                        exeBaseName,
                        super.mavenProject.getDescription());

                File tmpDir = new File(System.getProperty("java.io.tmpdir"));

                this.manifest =
                    new File(tmpDir, String.format(
                        "%s.%s.manifest",
                        exeBaseName,
                        exeExtension));

                try
                {
                    FileUtils.writeStringToFile(this.manifest, text);
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException(
                        "Error writing manifest file: " + this.manifest);
                }

                debug("wrote manifest file: " + this.manifest);
            }
        }
        else if (execution == 0 && process.exitValue() != 0)
        {
            debug(
                "disabling demanduac because bootstrapper exit code: %s",
                process.exitValue());
            this.demandUac = false;
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
