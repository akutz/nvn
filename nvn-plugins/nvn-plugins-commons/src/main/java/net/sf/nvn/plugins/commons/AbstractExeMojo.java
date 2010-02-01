package net.sf.nvn.plugins.commons;

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
 * An abstract Mojo for nvn Mojos that call external programs.
 * 
 */
public abstract class AbstractExeMojo extends AbstractNvnMojo
{
    /**
     * The command used to start the external process.
     * 
     * @parameter
     */
    File command;

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
     * .NET 3.5 Framework, Visual Studio 2008, the Windows SDK, and various
     * other software packages commonly used by Windows developers.
     * </p>
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
     * have been omitted, such as INCLUDE, LIB, and LIBPATH. Their omission is
     * not accidental. Because these environment variables are used primarily
     * for C/C++ development and NVN targets C# and VisualBasic.NET development
     * (for now), the aforementioned variables are not being considered for
     * automatic inclusion at this time.
     * </p>
     * 
     * @parameter default-value="true"
     */
    boolean autoEnvVar;

    /**
     * The environment variables to use for the executable process.
     */
    @SuppressWarnings("unchecked")
    Map procEnvVars;

    /**
     * Gets a string containing the arguments to pass to this mojo's command.
     * 
     * @return A string containing the arguments to pass to this mojo's command.
     */
    abstract String getArgs();

    /**
     * Builds the string that is executed by Runtime.exec(String, String[]).
     * 
     * @return The string that is executed by Runtime.exec(String, String[]).
     */
    final String buildCmdLineString()
    {
        String args = StringUtils.isEmpty(this.args) ? getArgs() : this.args;
        String cmd = String.format("%s %s", getPath(this.command), args);
        return cmd;
    }

    @Override
    final void nvnExecute() throws MojoExecutionException
    {
        loadEnvVars();
        String cmd = buildCmdLineString();
        info(cmd);
        exec(cmd);
    }

    /**
     * Executes the given command with Runtime.exec(String, String[]).
     * 
     * @param cmd The command line string to execute.
     * @throws MojoExecutionException When an error occurs.
     */
    final void exec(String cmd) throws MojoExecutionException
    {
        try
        {
            Process p = ProcessUtils.exec(cmd, this.procEnvVars);

            int exitCode = p.waitFor();

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
     * Loads the environment variables for the process that will be executed by
     * this mojo.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    void loadEnvVars() throws MojoExecutionException
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
            String.format("%1$s\\v3.5;%1$s\\v2.0.5727", dotnetDir);
        String path =
            this.procEnvVars.containsKey("Path") ? String
                .valueOf(this.procEnvVars.get("Path")) : "";

        if (existsDotNet35())
        {
            putEnvVar("Framework35Version", "v3.5");
            putEnvVar("FrameworkDir", dotnetDir);
            putEnvVar("FrameworkVersion", "v2.0.50727");
            putEnvVar("LIBPATH", dotnetDirs);
            path = dotnetDirs + ";" + path;
        }

        if (existsVSNet2008())
        {
            String installDir = getVSNet2008Dir();
            String rootDir = installDir.replace("\\Common7\\IDE", "");
            String toolsDir = installDir.replace("\\IDE", "\\Tools");
            putEnvVar("DevEnvDir", installDir);
            putEnvVar("VS90COMNTOOLS", toolsDir);
            putEnvVar("VSINSTALLDIR", rootDir);
            path = String.format("%s;%s;%s", installDir, toolsDir, path);
        }

        if (existsWinSdk61())
        {
            String installDir = getWinSdk61Dir();
            putEnvVar("FxTools", dotnetDirs);
            putEnvVar("MSSdk", installDir);
            putEnvVar("SdkTools", installDir + "\\Bin");
            path = String.format("%1$s\\Bin;%2$s", installDir, path);
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
            String.format("%s\\Microsoft.NET\\Framework\\v3.5", System
                .getenv("SystemRoot"));
        File f = new File(path);
        return f.exists();
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
