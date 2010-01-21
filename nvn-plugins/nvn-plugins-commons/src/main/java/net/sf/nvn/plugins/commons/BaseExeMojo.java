package net.sf.nvn.plugins.commons;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.exec.util.MapUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A base Mojo for Mojos that call external programs.
 * 
 */
public abstract class BaseExeMojo extends AbstractMojo
{
    /**
     * The base directory.
     * 
     * @parameter expression="${basedir}"
     */
    public File baseDir;

    /**
     * The number of milliseconds to wait before the msbuild process is
     * considered hung and destroyed.
     * 
     * @parameter expression="${msbuild.timeout}" default-value="300000"
     */
    public Long timeout;

    /**
     * This content of this parameter, if specified, will override all other of
     * this plug-in's parameters and execute MSBuild with this string as its
     * sole command line argument(s).
     * 
     * @parameter expression="${msbuild.commandLineArgs}"
     */
    public String commandLineArgs;

    /**
     * Set this parameter to true to specify that the msbuild process should
     * inherit the environment variables of the current process.
     * 
     * @parameter expression="${msbuild.inheritEnvVars}" default-value="true"
     */
    public boolean inheritEnvVars;

    /**
     * Environment variables to specify for the msbuild process.
     * 
     * @parameter expression="${msbuild.envVars}"
     */
    public Properties envVars;

    abstract public String buildCommandLineString();

    abstract public String getExeDisplayName();

    @SuppressWarnings("unchecked")
    public void exec() throws MojoExecutionException
    {
        try
        {
            String cls = buildCommandLineString();
            
            getLog().info("nvn-" + getExeDisplayName() + ": " + cls);

            Map ev = loadEnvVars();

            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);

            ExecuteWatchdog watchdog = new ExecuteWatchdog(this.timeout);
            executor.setWatchdog(watchdog);

            ExecuteStreamHandler streamHandler =
                new PumpStreamHandler(System.out, System.err);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandLine.parse(cls);

            int exitCode = executor.execute(cl, ev);

            if (exitCode != 0)
            {
                throw new MojoExecutionException(getExeDisplayName()
                    + " exited with an unsuccessful error code: " + exitCode);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error running "
                + getExeDisplayName() + ": ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map buildEnvVars() throws IOException
    {
        Map ev;

        if (this.inheritEnvVars)
        {
            ev = EnvironmentUtils.getProcEnvironment();
        }
        else
        {
            ev = new HashMap();
        }

        if (this.envVars != null)
        {
            MapUtils.merge(ev, this.envVars);
        }

        return ev;
    }

    @SuppressWarnings("unchecked")
    public Map loadEnvVars() throws MojoExecutionException
    {
        Map ev;

        try
        {
            ev = buildEnvVars();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error building environment variable map",
                e);
        }

        return ev;
    }
}
