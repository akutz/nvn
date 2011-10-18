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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.sf.nvn.commons.ProcessUtils;
import net.sf.nvn.commons.RegistryUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.CollectionUtils;

/**
 * The base class for all nvn MOJOs that invoke external programs.
 * 
 */
public abstract class AbstractExeMojo extends AbstractNvnMojo
{
    /**
     * This content of this parameter, if specified, will override all other of
     * this Mojo's configuration parameters and execute the specified command
     * with this string as its sole command line argument(s).
     * 
     * @parameter
     */
    String args;

    /**
     * Set this parameter to true to specify that the external process should
     * inherit the environment variables of the current process.
     * 
     * @parameter default-value="true"
     */
    boolean inheritEnvVars;

    /**
     * Environment variables to specify for the msbuild process.
     * 
     * @parameter
     */
    Properties envVars;

    /**
     * <p>
     * Setting this parameter to true causes this Mojo to attempt to detect the
     * .NET Framework (3.5, 4.0), Visual Studio (2008, 2010), the Windows SDK
     * (6.1, 7.0A, 7.1), the Windows Installer XML Toolset (WiX), and various
     * other software packages commonly used by Windows developers.
     * </p>
     * <p>
     * If the .NET 4.0 Framework is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * <ul>
     * <li><strong>Framework35Version</strong>=v3.5</li>
     * <li><strong>FrameworkDir</strong>=%SystemRoot%\Microsoft.NET\Framework</li>
     * <li>
     * <strong>LIBPATH</strong>=%SystemRoot%\Microsoft.NET\Framework\v4.0.30319
     * ;% SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>FrameworkVersion</strong>=v4.0.30319</li>
     * <li><strong>FrameworkVersion32</strong>=v4.0.30319</li>
     * </ul>
     * <p>
     * If the .NET 3.5 Framework is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li><strong>Framework35Version</strong>=v3.5</li>
     * <li><strong>FrameworkDir</strong>=%SystemRoot%\Microsoft.NET\Framework</li>
     * <li><strong>FrameworkVersion</strong>=v2.0.50727</li>
     * <li><strong>LIBPATH</strong>=%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>Path</strong>=%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727;</li>
     * </ul>
     * 
     * <p>
     * If the Visual Studio .NET 2010 is detected then the following variables
     * are appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li><strong>DevEnvDir</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 10.0\Common7\IDE</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 10.0\Common7\IDE;%ProgramFiles%\Microsoft Visual Studio
     * 10.0\Common7\Tools;</li>
     * <li><strong>VS100COMNTOOLS</strong>=%ProgramFiles%\Microsoft Visual
     * Studio 10.0\Common7\Tools\</li>
     * <li><strong>VSINSTALLDIR</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 10.0</li>
     * </ul>
     * 
     * <p>
     * If the Visual Studio .NET 2008 is detected then the following variables
     * are appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li><strong>DevEnvDir</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 9.0\Common7\IDE</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 9.0\Common7\IDE;%ProgramFiles%\Microsoft Visual Studio 9.0\Common7\Tools;
     * </li>
     * <li><strong>VS90COMNTOOLS</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 9.0\Common7\Tools\</li>
     * <li><strong>VSINSTALLDIR</strong>=%ProgramFiles%\Microsoft Visual Studio
     * 9.0</li>
     * </ul>
     * 
     * <p>
     * If the Windows SDK 7.1 is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li>
     * <strong>FxTools</strong>=%SystemRoot%\Microsoft.NET\Framework\v4.0.30319
     * ;%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>MSSdk</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.1</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.1\Bin</li>
     * <li><strong>SdkTools</strong>=%ProgramFiles%\Microsoft
     * SDKs\Windows\v7.1\Bin</li>
     * </ul>
     * 
     * <p>
     * If the Windows SDK 7.0A is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li>
     * <strong>FxTools</strong>=%SystemRoot%\Microsoft.NET\Framework\v4.0.30319
     * ;%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>MSSdk</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.0A</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.0A\Bin
     * </li>
     * <li><strong>SdkTools</strong>=%ProgramFiles%\Microsoft
     * SDKs\Windows\v7.0A\Bin</li>
     * </ul>
     * 
     * <p>
     * If the Windows SDK 7.0 is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li>
     * <strong>FxTools</strong>=%SystemRoot%\Microsoft.NET\Framework\v4.0.30319
     * ;%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>MSSdk</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.0</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v7.0\Bin</li>
     * <li><strong>SdkTools</strong>=%ProgramFiles%\Microsoft
     * SDKs\Windows\v7.0\Bin</li>
     * </ul>
     * 
     * <p>
     * If the Windows SDK 6.1 is detected then the following variables are
     * appended to the external process's environment automatically:
     * </p>
     * 
     * <ul>
     * <li><strong>FxTools</strong>=%SystemRoot%\Microsoft.NET\Framework\v3.5;%
     * SystemRoot%\Microsoft.NET\Framework\v2.0.50727</li>
     * <li><strong>MSSdk</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v6.1</li>
     * <li><strong>Path</strong>=%ProgramFiles%\Microsoft SDKs\Windows\v6.1\Bin</li>
     * <li><strong>SdkTools</strong>=%ProgramFiles%\Microsoft
     * SDKs\Windows\v6.1\Bin</li>
     * </ul>
     * 
     * <p>
     * Setting this value to true does <strong>not</strong> override the
     * <em>inheritEnvVars</em> parameter. The environment variables set when
     * this parameter is set to <strong>true</strong> are simply appended to the
     * executing process's environment.
     * </p>
     * 
     * <p>
     * Additionally, if a variable already exists due to inheriting the parent
     * process's environment, then the existing value will not be overridden.
     * For example, if the variable <strong>VSINSTALLDIR</strong> is already
     * present and set to <em>%ProgramFiles%\Microsoft Visual Studio 8.0</em>
     * and this parameter is set to true <strong>and</strong> Visual Studio .NET
     * 2008 is also installed, the variable <strong>VSINSTALLDIR</strong> will
     * not be overridden with
     * <em>%ProgramFiles%\Microsoft Visual Studio 9.0</em>.
     * </p>
     * 
     * <p>
     * The astute reader may notice that several common environment variables
     * have been omitted, such as INCLUDE and LIB. Their omission is not
     * accidental. Because these environment variables are used primarily for
     * C/C++ development and NVN targets C# and VisualBasic.NET development (for
     * now), the aforementioned variables are not being considered for automatic
     * inclusion at this time.
     * </p>
     * 
     * @parameter default-value="true"
     */
    boolean autoEnvVar;

    /**
     * The environment variables to use for the executable process.
     */
    @SuppressWarnings(
    {
        "rawtypes"
    })
    Map procEnvVars;

    /**
     * Gets a string containing the arguments to pass to this mojo's command.
     * 
     * @param execution The execution count.
     * 
     * @return A string containing the arguments to pass to this mojo's command.
     */
    abstract String getArgs(int execution);

    /**
     * Gets a file with the command to execute.
     * 
     * @param execution The execution count.
     * 
     * @return A file with the command to execute.
     */
    abstract File getCommand(int execution);

    /**
     * Get the number of executions to process.
     * 
     * @return The number of executions to process.
     */
    int getExecutions()
    {
        return 1;
    }

    /**
     * Builds the string that is executed by Runtime.exec(String, String[]).
     * 
     * @param execution The execution index.
     * 
     * @return The string that is executed by Runtime.exec(String, String[]).
     */
    final String buildCmdLineString(int execution)
    {
        String args =
            StringUtils.isEmpty(this.args) ? getArgs(execution) : this.args;
        String cmd =
            String.format("%s %s", getPath(getCommand(execution)), args);
        return cmd;
    }

    protected boolean skipExec(int execution)
    {
        return false;
    }

    @Override
    final void nvnExecute() throws MojoExecutionException
    {
        initProcEnvVars();

        for (int x = 0; x < getExecutions(); ++x)
        {
            if (skipExec(x))
            {
                continue;
            }

            String cmd = buildCmdLineString(x);
            info("execution #%s: %s", x, cmd);
            exec(x, cmd);
        }
    }

    /**
     * Invoked after exec(String cmd) has completed.
     * 
     * @param execution The execution index.
     * @param process The completed process that was executed.
     */
    void postExec(int execution, Process process) throws MojoExecutionException
    {
    }

    /**
     * Returns a flag indicating whether or not to show the process's output.
     * 
     * @return A flag indicating whether or not to show the process's output.
     */
    boolean showExecOutput()
    {
        return true;
    }

    /**
     * The stdout of the process is copied here when showExecOutput returns
     * false.
     */
    protected String stdout;

    /**
     * The stderr of the process is copied here when showExecOutput returns
     * false.
     */
    protected String stderr;

    /**
     * Executes the given command with Runtime.exec(String, String[]).
     * 
     * @param execution The execution index.
     * @param cmd The command line string to execute.
     * @throws MojoExecutionException When an error occurs.
     */
    final void exec(int execution, String cmd) throws MojoExecutionException
    {
        try
        {
            Process p =
                ProcessUtils.exec(cmd, this.procEnvVars, showExecOutput());

            ByteArrayOutputStream stdoutBos = null;
            ByteArrayOutputStream stderrBos = null;

            if (!showExecOutput())
            {
                stdoutBos = new ByteArrayOutputStream();
                stderrBos = new ByteArrayOutputStream();

                ProcessUtils.pipe(p.getInputStream(), stdoutBos);
                ProcessUtils.pipe(p.getErrorStream(), stderrBos);
            }

            int exitCode = p.waitFor();

            if (stdoutBos != null && stderrBos != null)
            {
                this.stdout = stdoutBos.toString();
                this.stderr = stderrBos.toString();
            }

            postExec(execution, p);

            if (exitCode != 0)
            {
                throw new MojoExecutionException(getMojoName()
                    + " exited with an unsuccessful error code: " + exitCode);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error running " + getMojoName()
                + ": ", e);
        }
    }

    /**
     * Initializes the field procEnvVars.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings(
    {
        "rawtypes", "unchecked"
    })
    void initProcEnvVars() throws MojoExecutionException
    {
        this.procEnvVars = new HashMap();

        if (this.inheritEnvVars && this.envVars != null)
        {
            this.procEnvVars =
                CollectionUtils.mergeMaps(this.envVars, System.getenv());
        }
        else if (this.inheritEnvVars && this.envVars == null)
        {
            this.procEnvVars =
                CollectionUtils.mergeMaps(this.procEnvVars, System.getenv());
        }
        else if (!this.inheritEnvVars && this.envVars != null)
        {
            this.procEnvVars =
                CollectionUtils.mergeMaps(this.procEnvVars, (Map) this.envVars);
        }

        if (!this.autoEnvVar)
        {
            return;
        }

        debug("settings up automatic environment variables");

        String systemRoot = System.getenv("SystemRoot");
        String dotnetDir = systemRoot + "\\Microsoft.NET\\Framework";
        String dotnetDirs =
            String.format(
                "%1$s\\v4.0.30319;%1$s\\v3.5;%1$s\\v2.0.5727",
                dotnetDir);
        String path =
            this.procEnvVars.containsKey("Path") ? String
                .valueOf(this.procEnvVars.get("Path")) : "";

        if (existsDotNet4() || existsDotNet35())
        {
            putEnvVar("Framework35Version", "v3.5");
            putEnvVar("FrameworkDir", dotnetDir);
            putEnvVar("LIBPATH", dotnetDirs);
            path = dotnetDirs + ";" + path;
        }

        if (existsDotNet4())
        {
            putEnvVar("FrameworkVersion", "v4.0.30319");
            putEnvVar("FrameworkVersion32", "v4.0.30319");
        }
        else if (existsDotNet35())
        {
            putEnvVar("FrameworkVersion", "v2.0.50727");
        }

        if (existsVSNet2010())
        {
            String installDir = getVSNet2010Dir();
            String rootDir = installDir.replace("\\Common7\\IDE", "");
            String toolsDir = installDir.replace("\\IDE", "\\Tools");
            putEnvVar("DevEnvDir", installDir);
            putEnvVar("VS100COMNTOOLS", toolsDir);
            putEnvVar("VSINSTALLDIR", rootDir);
            path = String.format("%s;%s;%s", installDir, toolsDir, path);
        }
        else if (existsVSNet2008())
        {
            String installDir = getVSNet2008Dir();
            String rootDir = installDir.replace("\\Common7\\IDE", "");
            String toolsDir = installDir.replace("\\IDE", "\\Tools");
            putEnvVar("DevEnvDir", installDir);
            putEnvVar("VS90COMNTOOLS", toolsDir);
            putEnvVar("VSINSTALLDIR", rootDir);
            path = String.format("%s;%s;%s", installDir, toolsDir, path);
        }

        if (existsWinSdk71())
        {
            String installDir = getWinSdk71Dir();
            putEnvVar("FxTools", dotnetDirs);
            putEnvVar("MSSdk", installDir);
            putEnvVar("SdkTools", installDir + "\\Bin");
            path = String.format("%1$s\\Bin;%2$s", installDir, path);
        }
        else if (existsWinSdk70A())
        {
            String installDir = getWinSdk70ADir();
            putEnvVar("FxTools", dotnetDirs);
            putEnvVar("MSSdk", installDir);
            putEnvVar("SdkTools", installDir + "\\Bin");
            path = String.format("%1$s\\Bin;%2$s", installDir, path);
        }
        else if (existsWinSdk70())
        {
            String installDir = getWinSdk70Dir();
            putEnvVar("FxTools", dotnetDirs);
            putEnvVar("MSSdk", installDir);
            putEnvVar("SdkTools", installDir + "\\Bin");
            path = String.format("%1$s\\Bin;%2$s", installDir, path);
        }
        else if (existsWinSdk61())
        {
            String installDir = getWinSdk61Dir();
            putEnvVar("FxTools", dotnetDirs);
            putEnvVar("MSSdk", installDir);
            putEnvVar("SdkTools", installDir + "\\Bin");
            path = String.format("%1$s\\Bin;%2$s", installDir, path);
        }

        if (existsWix35())
        {
            String installDir =
                String.format(
                    "%1$s\\Windows Installer XML v3.5\\bin",
                    System.getenv("ProgramFiles"));
            path = String.format("%1$s;%2$s", installDir, path);

            installDir =
                String.format(
                    "%1$s\\Windows Installer XML v3.5\\bin",
                    System.getenv("ProgramFiles(x86)"));
            path = String.format("%1$s;%2$s", installDir, path);

        }

        if (existsDotCover10())
        {
            String installDir = String.format("%s\\bin", getDotCover10Dir());
            path = String.format("%1$s;%2$s", installDir, path);
        }

        this.procEnvVars.put("Path", path);
        debug("Path=" + path);
    }

    /**
     * Puts a value in the process environment variable map if the value's key
     * does not already exist.
     * 
     * @param key An environment variable key.
     * @param val An environment variable value.
     */
    @SuppressWarnings("unchecked")
    void putEnvVar(String key, String val)
    {
        if (this.procEnvVars.containsKey(key))
        {
            return;
        }

        debug(String.format("putEnvVar(%s,%s)", key, val));
        this.procEnvVars.put(key, val);
    }

    boolean existsWix35() throws MojoExecutionException
    {
        String path =
            String.format(
                "%s\\Windows Installer XML v3.5",
                System.getenv("ProgramFiles"));
        File f = new File(path);

        if (f.exists())
        {
            return true;
        }
        else
        {
            path =
                String.format(
                    "%s\\Windows Installer XML v3.5",
                    System.getenv("ProgramFiles(x86)"));
            f = new File(path);

            return f.exists();
        }
    }

    /**
     * Gets a flag indicating whether or not the .NET 3.5 Framework has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the .NET 3.5 Framework has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsDotNet35() throws MojoExecutionException
    {
        String path =
            String.format(
                "%s\\Microsoft.NET\\Framework\\v3.5",
                System.getenv("SystemRoot"));
        File f = new File(path);
        return f.exists();
    }

    /**
     * Gets a flag indicating whether or not the .NET 4 Framework has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the .NET 4 Framework has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsDotNet4() throws MojoExecutionException
    {
        String path =
            String.format(
                "%s\\Microsoft.NET\\Framework\\v4.0.30319",
                System.getenv("SystemRoot"));
        File f = new File(path);
        return f.exists();
    }

    /**
     * Gets a flag indicating whether or not dotCover 1.0 has been detected on
     * this system.
     * 
     * @return A flag indicating whether or not dotCover 1.0 has been detected
     *         on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsDotCover10() throws MojoExecutionException
    {
        try
        {
            boolean exists =
                RegistryUtils
                    .exists(
                        "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\dotCover\\v1.0\\vs10.0",
                        "InstallDir")
                    || RegistryUtils
                        .exists(
                            "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\dotCover\\v1.0\\vs9.0",
                            "InstallDir");

            if (exists)
            {
                return true;
            }

            exists =
                RegistryUtils.exists(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\TeamCity\\Agent",
                    "InstallPath");

            if (!exists)
            {
                return false;
            }

            return StringUtils.isNotEmpty(getTeamCityDotCoverDir());
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for dotCover 1.0",
                e);
        }
    }

    /**
     * Gets the dotCover 1.0 installation directory.
     * 
     * @return The dotCover 1.0 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getDotCover10Dir() throws MojoExecutionException
    {
        String installDir = null;

        try
        {
            installDir =
                RegistryUtils
                    .read(
                        "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\dotCover\\v1.0\\vs10.0",
                        "InstallDir");
        }
        catch (Exception e)
        {
            // Do nothing
        }

        if (StringUtils.isEmpty(installDir))
        {
            try
            {
                installDir =
                    RegistryUtils
                        .read(
                            "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\dotCover\\v1.0\\vs9.0",
                            "InstallDir");

            }
            catch (Exception e)
            {
                // Do nothing
            }
        }

        if (StringUtils.isEmpty(installDir))
        {
            installDir = getTeamCityDotCoverDir();
        }

        if (StringUtils.isEmpty(installDir))
        {
            throw new MojoExecutionException(
                "Error getting dotCover 1.0 installation directory");
        }

        return installDir;
    }

    String getTeamCityDotCoverDir() throws MojoExecutionException
    {
        String path;

        try
        {
            path =
                RegistryUtils.read(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\JetBrains\\TeamCity\\Agent",
                    "InstallPath");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error reading TeamCity Agent registry key",
                e);
        }

        path = String.format("%s\\plugins\\dotCover\\bin", path);

        File installDir = new File(path);

        if (installDir.exists())
        {
            return path;
        }
        else
        {
            return null;
        }
    }

    String getMSTestExePath() throws MojoExecutionException
    {
        if (existsVSNet2010())
        {
            return getVSNet2010Dir() + "\\mstest.exe";
        }
        else if (existsVSNet2008())
        {
            return getVSNet2008Dir() + "\\mstest.exe";
        }

        return null;
    }

    /**
     * Gets a flag indicating whether or not Visual Studio .NET 2008 has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not Visual Studio .NET 2008 has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsVSNet2008() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils.exists(
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\VisualStudio\\9.0",
                "InstallDir");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Visual Studio .NET 2008",
                e);
        }
    }

    /**
     * Gets a flag indicating whether or not Visual Studio .NET 2010 has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not Visual Studio .NET 2010 has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsVSNet2010() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils.exists(
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\VisualStudio\\10.0",
                "InstallDir");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Visual Studio .NET 2010",
                e);
        }
    }

    /**
     * Gets the Visual Studio .NET 2008 installation directory.
     * 
     * @return The Visual Studio .NET 2008 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getVSNet2008Dir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils.read(
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\VisualStudio\\9.0",
                "InstallDir");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Visual Studio .NET 2008 installation directory.",
                e);
        }
    }

    /**
     * Gets the Visual Studio .NET 2010 installation directory.
     * 
     * @return The Visual Studio .NET 2010 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getVSNet2010Dir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils.read(
                "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\VisualStudio\\10.0",
                "InstallDir");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Visual Studio .NET 2010 installation directory.",
                e);
        }
    }

    /**
     * Gets the Windows SDK 6.1 installation directory.
     * 
     * @return The Windows SDK 6.1 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getWinSdk61Dir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .read(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v6.1",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 6.1 installation directory.",
                e);
        }
    }

    /**
     * Gets the Windows SDK 7.0 installation directory.
     * 
     * @return The Windows SDK 7.0 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getWinSdk70Dir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .read(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.0 installation directory.",
                e);
        }
    }

    /**
     * Gets the Windows SDK 7.0A installation directory.
     * 
     * @return The Windows SDK 7.0A installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getWinSdk70ADir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .read(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0A",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.0A installation directory.",
                e);
        }
    }

    /**
     * Gets the Windows SDK 7.1 installation directory.
     * 
     * @return The Windows SDK 7.1 installation directory.
     * @throws MojoExecutionException When an error occurs.
     */
    String getWinSdk71Dir() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .read(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.1",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.1 installation directory.",
                e);
        }
    }

    /**
     * Gets a flag indicating whether or not the Windows SDK 6.1 has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the Windows SDK 6.1 has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsWinSdk61() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .exists(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v6.1",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 6.1",
                e);
        }
    }

    /**
     * Gets a flag indicating whether or not the Windows SDK 7.0 has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the Windows SDK 7.0 has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsWinSdk70() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .exists(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.0",
                e);
        }
    }

    /**
     * Gets a flag indicating whether or not the Windows SDK 7.0A has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the Windows SDK 7.0A has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsWinSdk70A() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .exists(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0A",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.0A",
                e);
        }
    }

    /**
     * Gets a flag indicating whether or not the Windows SDK 7.1 has been
     * detected on this system.
     * 
     * @return A flag indicating whether or not the Windows SDK 7.1 has been
     *         detected on this system.
     * @throws MojoExecutionException When an error occurs.
     */
    boolean existsWinSdk71() throws MojoExecutionException
    {
        try
        {
            return RegistryUtils
                .exists(
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.1",
                    "InstallationFolder");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                "Error checking for Windows SDK 7.1",
                e);
        }
    }

    @Override
    String getFullPathFromPath(File file)
    {
        if (this.procEnvVars == null)
        {
            debug("getFullPathFromPath returning false because env vars is null");
            return null;
        }

        String fileName = file.getName();

        if (!this.procEnvVars.containsKey("Path"))
        {
            debug("getFullPathFromPath returning false because \"Path\" env var does not exist");
            return null;
        }

        String path = String.valueOf(this.procEnvVars.get("Path"));
        String[] pathParts = path.split("\\;|\\,");

        for (String pp : pathParts)
        {
            File ep = new File(pp);
            String p = ep + "\\" + fileName;
            File epf = new File(p);

            if (epf.exists())
            {
                debug("getFullPathFromPath returning true = " + p);
                return p;
            }
            else
            {
                debug("getFullPathFromPath did not find = " + p);
            }
        }

        debug("getFullPathFromPath returning false because file not found in path");
        return null;
    }
}
