package net.sf.nvn.plugins.msbuild;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import static org.apache.commons.exec.util.StringUtils.quoteArgument;

/**
 * A Maven plug-in for building .NET solutions and/or projects with MSBuild.
 * 
 * @goal msbuild
 * @phase compile
 * @description A Maven plug-in for building .NET solutions and/or projects with
 *              MSBuild.
 */
public class MSBuildMojo extends AbstractMojo
{
    /**
     * The base directory.
     * 
     * @parameter expression="${basedir}"
     */
    File baseDir;
    /**
     * The path to the msbuild executable.
     * 
     * @parameter expression="${msbuild.msbuild}" default-value="msbuild.exe"
     */
    File msbuild;

    /**
     * The path to the solution or project to build.
     * 
     * @parameter expression="${msbuild.buildFile}"
     */
    File buildFile;

    /**
     * The number of milliseconds to wait before the msbuild process is
     * considered hung and destroyed.
     * 
     * @parameter expression="${msbuild.timeout}" default-value="300000"
     */
    Long timeout;

    /**
     * This content of this parameter, if specified, will override all other of
     * this plug-in's parameters and execute MSBuild with this string as its
     * sole command line argument(s).
     * 
     * @parameter expression="${msbuild.commandLineArgs}"
     */
    String commandLineArgs;

    /**
     * Set this parameter to true to specify that the msbuild process should
     * inherit the environment variables of the current process.
     * 
     * @parameter expression="${msbuild.inheritEnvVars}" default-value="true"
     */
    boolean inheritEnvVars;

    /**
     * Environment variables to specify for the msbuild process.
     * 
     * @parameter expression="${msbuild.envVars}"
     */
    Properties envVars;

    /**
     * Hides the startup banner and copyright message.
     * 
     * @parameter expression="${msbuild.noLogo}" default-value="false"
     */
    boolean noLogo;

    /**
     * Inserts command line settings from a text file. For more information see
     * <a href="http://msdn.microsoft.com/en-us/library/ms404301.aspx">MSBuild
     * Response Files</a>.
     * 
     * @parameter expression="${msbuild.commandFile}"
     */
    File[] commandFiles;

    /**
     * Does not auto-include the MSBuild.rsp file.
     * 
     * @parameter expression="${msbuild.noAutoResponse}" default-value="false"
     */
    boolean noAutoResponse;

    /**
     * Builds these targets in this project.
     * 
     * @parameter expression="${msbuild.targets}"
     */
    String[] targets;

    /**
     * Sets or overrides these project-level properties, where name is the
     * property name and value is the property value.
     * 
     * @parameter expression="${msbuild.properties}"
     */
    Properties properties;

    /**
     * <p>
     * Specifies the logger to use to log events from MSBuild.
     * </p>
     * 
     * <p>
     * The logger syntax is:
     * <em>[LoggerClass,]LoggerAssembly[;LoggerParameters]</em>.
     * </p>
     * 
     * <p>
     * The LoggerClass syntax is:
     * <em>[PartialOrFullNamespace.]LoggerClassName</em>.
     * </p>
     * <p>
     * You do not have to specify the logger class if there is exactly one
     * logger in the assembly.
     * </p>
     * <p>
     * The LoggerAssembly syntax is:
     * <em>{AssemblyName[,StrongName] | AssemblyFile}</em>.
     * </p>
     * <p>
     * Logger parameters are optional and are passed to the logger exactly as
     * you type them. For example:
     * <em>XMLLogger,C:\Loggers\MyLogger.dll;OutputAsHTML</em>
     * </p>
     * 
     * @parameter expression="${msbuild.loggers}"
     */
    String[] loggers;

    /**
     * <p>
     * Use this logger to log events from MSBuild.
     * </p>
     * <p>
     * The &lt;logger&gt; syntax is:
     * <em>[&lt;logger class&gt;,]<logger assembly>[;&lt;logger
     * parameters&gt;]</em>.
     * </p>
     * <p>
     * The &lt;logger class&gt; syntax is: <em>[&lt;partial or full
     * namespace&gt;.]&lt;logger class name&gt;</em>
     * </p>
     * <p>
     * The &lt;logger assembly&gt; syntax is:
     * <em>{&lt;assembly name&gt;[,&lt;strong name&gt;] |
     * &lt;assembly file&gt;}</em>
     * </p>
     * <p>
     * The &lt;logger parameters&gt; are optional, and are passed to the logger
     * exactly as you typed them. Examples:
     * <ul>
     * <li><em>XMLLogger,MyLogger,Version=1.0.2,Culture=neutral</em></li>
     * <li><em>MyLogger,C:\My.dll*ForwardingLogger,C:\Logger.dll</em></li>
     * </ul>
     * </p>
     * 
     * @parameter expression="${msbuild.distributedLoggers}"
     */
    String[] distributedLoggers;

    /**
     * Specifies the parameters to pass to the console logger. The available
     * parameters are as follows:
     * 
     * <ul>
     * <li><strong>PerformanceSummary</strong>: Displays the time spent in
     * tasks, targets, and projects.</li>
     * <li><strong>NoSummary</strong>: Hides the error and warning summary
     * displayed at the end of a build.</li>
     * <li><strong>NoItemAndPropertyList</strong>: Hides the list of items and
     * properties displayed at the start of each project build in diagnostic
     * verbosity.</li>
     * </ul>
     * 
     * @parameter expression="${msbuild.consoleLoggerParameters}"
     */
    String consoleLoggerParameters;

    /**
     * Displays this amount of information in the build log. Individual loggers
     * display events based upon the verbosity level. A logger can also be
     * configured to ignore the verbosity setting. The available verbosity
     * levels are:
     * 
     * <ul>
     * <li><strong>q[uiet]</strong></li>
     * <li><strong>m[inimal]</strong></li>
     * <li><strong>n[ormal]</strong></li>
     * <li><strong>d[etailed]</strong></li>
     * <li><strong>diag[nostic]</strong></li>
     * </ul>
     * 
     * @parameter expression="${msbuild.verbosity}"
     */
    String verbosity;

    /**
     * Disables the default console logger and does not log events to the
     * console.
     * 
     * @parameter expression="${msbuild.noConsoleLogger}" default-value="false"
     */
    boolean noConsoleLogger;

    /**
     * Validates the project file against the default schema.
     * 
     * @parameter expression="${msbuild.validate}" default-value="false"
     */
    boolean validate;

    /**
     * Validates the project file against the specified schema.
     * 
     * @parameter expression="${msbuild.schema}"
     */
    File schema;

    /**
     * Specifies the number of worker processes that are involved in the build.
     * 
     * @parameter expression="${msbuild.maxCpuCount}"
     */
    Integer maxCpuCount;

    /**
     * List of extensions to ignore when the project file to build is being
     * determined.
     * 
     * @parameter expression="${msbuild.ignoreProjectExtensions}"
     */
    String[] ignoreProjectExtensions;

    /**
     * Logs the build output to a single file ("msbuild.log") in the current
     * directory. The location of the file and other parameters for the
     * fileLogger can be specified through the addition of the
     * <em>fileLoggerParameters</em> parameter.
     * 
     * @parameter expression="${msbuild.fileLogger}"
     */
    boolean fileLogger;

    /**
     * Logs the build output to multiple log files, one log file per MSBuild
     * node. The initial location for these files is the current directory. By
     * default the files are called <em>MSBuild&lt;nodeid&gt;.log</em>. The
     * location of the files and other parameters for the fileLogger can be
     * specified with the addition of the <em>fileLoggerParameters</em>
     * parameter. If a log file name is set through the fileLoggerParameters
     * switch the distributed logger will use the fileName as a template and
     * append the node id to this fileName to create a log file for each node.
     * 
     * @parameter expression="${msbuild.distributedFileLogger}"
     */
    boolean distributedFileLogger;

    /**
     * Specifies the parameters for the file logger and distributed file logger.
     * The available parameters are:
     * 
     * <ul>
     * <li><strong>LogFile</strong>: The path to the log file into which the
     * build log is written. The distributed file logger uses this as a prefix
     * for its log file names.</li>
     * <li><strong>Append</strong>: Determines if the build log is appended to
     * or overwrite the log file. When you set the switch, the build log is
     * appended to the log file. When you do not set the switch, the contents of
     * an existing log file are overwritten. The default is not to append to the
     * log file.</li>
     * <li><strong>Verbosity</strong>: Overrides the default verbosity setting
     * of detailed.</li>
     * <li><strong>Encoding</strong>: Specifies the encoding for the file, for
     * example, <em>UTF-8</em>.</li>
     * </ul>
     * 
     * @parameter expression="${msbuild.fileLoggerParameters}"
     */
    String[] fileLoggerParameters;

    /**
     * <p>
     * Specifies the version of the Toolset to use to build the project. This
     * command lets you build a project by using a version different from that
     * specified in the <a
     * href="http://msdn.microsoft.com/en-us/library/bcxfsh87.aspx">Project
     * Element (MSBuild)</a>. Valid values for version are as follows:
     * </p>
     * 
     * <ul>
     * <li><strong>2.0</strong></li>
     * <li><strong>3.0</strong></li>
     * <li><strong>3.5</strong></li>
     * </ul>
     * 
     * <p>
     * For more information about Toolsets, see <a
     * href="http://msdn.microsoft.com/en-us/library/bb397456.aspx">Building for
     * Specific .NET Frameworks.</a>
     * </p>
     * 
     * @parameter expression="${msbuild.toolsVersion}"
     */
    String toolsVersion;

    /**
     * Enables or Disables the re-use of MSBuild nodes. If true then the nodes
     * remain after the build completes and are reused by subsequent builds,
     * otherwise the nodes do not remain after the build completes.
     * 
     * @parameter expression="${msbuild.nodeReuse}"
     */
    boolean nodeReuse;

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        loadBuildFile();

        loadProperties();

        loadTargets();

        String cls = buildCommandLineString();

        getLog().info("NVN-MSBuild: " + cls.toString());

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

        try
        {
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
                throw new MojoExecutionException(
                    "MSBuild exited with an unsuccessful error code: "
                        + exitCode);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error running msbuild: ", e);
        }
    }

    public String buildCommandLineString()
    {
        StringBuilder cmdLineBuff = new StringBuilder();

        if (StringUtils.isNotEmpty(this.commandLineArgs))
        {
            String cmd = this.msbuild.getName() + " " + this.commandLineArgs;

            return cmd;
        }

        cmdLineBuff.append(quoteArgument(this.msbuild.getName()));
        cmdLineBuff.append(" ");

        if (this.noLogo)
        {
            cmdLineBuff.append("/nologo");
            cmdLineBuff.append(" ");
        }

        if (this.commandFiles != null && this.commandFiles.length != 0)
        {
            for (File f : this.commandFiles)
            {
                cmdLineBuff.append("@");
                cmdLineBuff.append(quoteArgument(f.getName()));
                cmdLineBuff.append(" ");
            }
        }

        if (this.noAutoResponse)
        {
            cmdLineBuff.append("/noautoresponse");
        }

        cmdLineBuff.append("/target:");

        for (String s : this.targets)
        {
            cmdLineBuff.append(quoteArgument(s));
            cmdLineBuff.append(";");
        }

        cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
        cmdLineBuff.append(" ");

        cmdLineBuff.append("/property:");

        for (Object k : this.properties.keySet())
        {
            cmdLineBuff.append(quoteArgument(String.valueOf(k)));
            cmdLineBuff.append("=");
            String s = String.valueOf(this.properties.get(k));
            cmdLineBuff.append(quoteArgument(s));
            cmdLineBuff.append(";");
        }

        cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
        cmdLineBuff.append(" ");

        if (this.loggers != null && this.loggers.length != 0)
        {
            for (String s : this.loggers)
            {
                cmdLineBuff.append("/logger:");
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(" ");
            }
        }

        if (this.distributedLoggers != null
            && this.distributedLoggers.length != 0)
        {
            for (String s : this.distributedLoggers)
            {
                cmdLineBuff.append("/distributedlogger:");
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(" ");
            }
        }

        if (!StringUtils.isEmpty(this.consoleLoggerParameters))
        {
            cmdLineBuff.append("/consoleloggerparameters:");
            cmdLineBuff.append(this.consoleLoggerParameters);
            cmdLineBuff.append(" ");
        }

        if (!StringUtils.isEmpty(this.verbosity))
        {
            cmdLineBuff.append("/verbosity:");
            cmdLineBuff.append(this.verbosity);
            cmdLineBuff.append(" ");
        }

        if (this.noConsoleLogger)
        {
            cmdLineBuff.append("/noconsolelogger");
            cmdLineBuff.append(" ");
        }

        if (this.validate && this.schema == null)
        {
            cmdLineBuff.append("/validate");
            cmdLineBuff.append(" ");
        }
        else if (this.schema != null)
        {
            cmdLineBuff.append("/validate:");
            cmdLineBuff.append(quoteArgument(this.schema.getName()));
            cmdLineBuff.append(" ");
        }

        if (this.ignoreProjectExtensions != null
            && this.ignoreProjectExtensions.length != 0)
        {
            cmdLineBuff.append("/ignoreprojectextensions:");

            for (String s : this.ignoreProjectExtensions)
            {
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(";");
            }

            cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
            cmdLineBuff.append(" ");
        }

        if (this.fileLogger)
        {
            cmdLineBuff.append("/fileLogger");
            cmdLineBuff.append(" ");
        }

        if (this.distributedFileLogger)
        {
            cmdLineBuff.append("/distributedFileLogger");
            cmdLineBuff.append(" ");
        }

        if (this.fileLoggerParameters != null
            && this.fileLoggerParameters.length != 0)
        {
            cmdLineBuff.append("/fileloggerparameters:");

            for (String s : this.fileLoggerParameters)
            {
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(";");
            }

            cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.toolsVersion))
        {
            cmdLineBuff.append("/toolsversion:");
            cmdLineBuff.append(quoteArgument(this.toolsVersion));
            cmdLineBuff.append(" ");
        }

        if (this.nodeReuse)
        {
            cmdLineBuff.append("/nodeReuse:true");
            cmdLineBuff.append(" ");
        }
        else
        {
            cmdLineBuff.append("/nodeReuse:false");
            cmdLineBuff.append(" ");
        }

        cmdLineBuff.append(quoteArgument(this.buildFile.getName()));

        String clbs = cmdLineBuff.toString();

        return clbs;
    }

    @SuppressWarnings("unchecked")
    public File findBuildFile() throws MojoExecutionException
    {
        Collection slnFiles = FileUtils.listFiles(this.baseDir, new String[]
        {
            "sln"
        }, false);

        if (slnFiles != null && slnFiles.size() > 0)
        {
            return (File) slnFiles.iterator().next();
        }

        Collection projFiles = FileUtils.listFiles(this.baseDir, new String[]
        {
            "csproj", "vbproj"
        }, false);

        if (projFiles != null && projFiles.size() > 0)
        {
            return (File) projFiles.iterator().next();
        }

        throw new MojoExecutionException(
            "Error finding solution or project file");
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

    public void loadTargets()
    {
        if (this.targets != null && this.targets.length > 0)
        {
            return;
        }

        this.targets = new String[]
        {
            "Build"
        };
    }

    public void loadProperties()
    {
        if (this.properties != null && this.properties.size() > 0)
        {
            return;
        }

        this.properties = new Properties();
        this.properties.put("Configuration", "Debug");
        this.properties.put("Platform", "Any CPU");
    }

    public void loadBuildFile() throws MojoExecutionException
    {
        if (this.buildFile == null)
        {
            this.buildFile = findBuildFile();
        }
    }
}