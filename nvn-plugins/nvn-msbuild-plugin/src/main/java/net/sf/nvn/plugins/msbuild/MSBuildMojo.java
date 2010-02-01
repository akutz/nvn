package net.sf.nvn.plugins.msbuild;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * A Maven plug-in for building .NET solutions and/or projects with MSBuild.
 * 
 * @goal msbuild
 * @phase compile
 * @description A Maven plug-in for building .NET solutions and/or projects with
 *              MSBuild.
 */
public class MSBuildMojo extends AbstractExeMojo
{
    /**
     * The path to the solution or project to build.
     * 
     * @parameter expression="${msbuild.buildFile}"
     */
    File buildFile;

    /**
     * A list of additional directories to resolve your project's references
     * against.
     */
    @SuppressWarnings("unchecked")
    List referencePaths;

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

    @Override
    boolean shouldExecute()
    {
        if (super.mavenProject.isExecutionRoot())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    void prepareForExecute() throws MojoExecutionException
    {
        loadBuildFile();

        loadProperties();

        loadReferencePaths();

        loadTargets();
    }

    @Override
    String getArgs()
    {
        StringBuilder cmdLineBuff = new StringBuilder();

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
                cmdLineBuff.append(getPath(f));
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
            cmdLineBuff.append(quote(s));
            cmdLineBuff.append(";");
        }

        cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
        cmdLineBuff.append(" ");

        cmdLineBuff.append("/property:");

        for (Object k : this.properties.keySet())
        {
            cmdLineBuff.append(quote(String.valueOf(k)));
            cmdLineBuff.append("=");
            String s = String.valueOf(this.properties.get(k));
            cmdLineBuff.append(quote(s));
            cmdLineBuff.append(";");
        }

        cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
        cmdLineBuff.append(" ");

        if (this.loggers != null && this.loggers.length != 0)
        {
            for (String s : this.loggers)
            {
                cmdLineBuff.append("/logger:");
                cmdLineBuff.append(quote(s));
                cmdLineBuff.append(" ");
            }
        }

        if (this.distributedLoggers != null
            && this.distributedLoggers.length != 0)
        {
            for (String s : this.distributedLoggers)
            {
                cmdLineBuff.append("/distributedlogger:");
                cmdLineBuff.append(quote(s));
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
            cmdLineBuff.append(quote(this.schema.getPath()));
            cmdLineBuff.append(" ");
        }

        if (this.ignoreProjectExtensions != null
            && this.ignoreProjectExtensions.length != 0)
        {
            cmdLineBuff.append("/ignoreprojectextensions:");

            for (String s : this.ignoreProjectExtensions)
            {
                cmdLineBuff.append(quote(s));
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
                cmdLineBuff.append(quote(s));
                cmdLineBuff.append(";");
            }

            cmdLineBuff.deleteCharAt(cmdLineBuff.length() - 1);
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.toolsVersion))
        {
            cmdLineBuff.append("/toolsversion:");
            cmdLineBuff.append(quote(this.toolsVersion));
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

        cmdLineBuff.append(getPath(this.buildFile));

        String clbs = cmdLineBuff.toString();

        return clbs;
    }

    /**
     * Finds the build file (either a sln, csproj, or vbproj file).
     * 
     * @return The build file.
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    File findBuildFile() throws MojoExecutionException
    {
        Collection slnFiles =
            FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
            {
                "sln"
            }, false);

        if (slnFiles != null && slnFiles.size() > 0)
        {
            return (File) slnFiles.iterator().next();
        }

        Collection projFiles =
            FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
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

    /**
     * Loads the build's target.
     */
    void loadTargets()
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

    /**
     * Loads the build's properties.
     */
    void loadProperties()
    {
        if (this.properties != null && this.properties.size() > 0)
        {
            return;
        }

        this.properties = new Properties();
        this.properties.put("Configuration", "Debug");
        this.properties.put("Platform", "Any CPU");

        if (super.mavenProject.isExecutionRoot() && isProject()
            && !isSolution())
        {
            this.properties.put("OutputPath", "bin\\Debug");
        }
    }

    /**
     * Loads a maven project's dependencies and adds them to the build's
     * ReferencePath property.
     * 
     * @param project The maven project.
     * @throws MojoExecutionException
     */
    @SuppressWarnings("unchecked")
    void loadProjectDependencies(MavenProject project)
        throws MojoExecutionException
    {
        List deps = project.getDependencies();

        if (deps == null)
        {
            return;
        }

        if (deps.size() == 0)
        {
            return;
        }

        if (this.referencePaths == null)
        {
            this.referencePaths = new ArrayList();
        }

        String localRepoBaseDirPath = super.localRepository.getBasedir();

        for (Object od : deps)
        {
            Dependency d = (Dependency) od;

            String dgid = d.getGroupId();
            String daid = d.getArtifactId();
            String dver = d.getVersion();

            String dgidSlash = dgid.replaceAll("\\.", "\\\\");

            String dp =
                String.format(
                    "%s\\%s\\%s\\%s",
                    localRepoBaseDirPath,
                    dgidSlash,
                    daid,
                    dver);

            File dpf = new File(dp);

            debug("loaded dependency path " + dpf.getPath());

            if (!this.referencePaths.contains(dpf))
            {
                this.referencePaths.add(dpf);
                copyDepSansVer(dpf, daid, dver);
            }
        }
    }

    /**
     * Creates a copy of the artifact dependency without the version number so
     * that msbuild can find the file that the developer may have originally
     * referenced.
     * 
     * @param dir The artifact directory.
     * @param artifactId The artifact ID.
     * @param version The artifact version.
     * @throws MojoExecutionException When an error occurs.
     */
    void copyDepSansVer(File dir, String artifactId, String version)
        throws MojoExecutionException
    {
        if (!dir.exists())
        {
            return;
        }

        copyDepSansVer(dir, artifactId, version, "dll");
        copyDepSansVer(dir, artifactId, version, "exe");
        copyDepSansVer(dir, artifactId, version, "pdb");
    }

    /**
     * Creates a copy of the artifact dependency without the version number so
     * that msbuild can find the file that the developer may have originally
     * referenced.
     * 
     * @param dir The artifact directory.
     * @param artifactId The artifact ID.
     * @param version The artifact version.
     * @param extension The artifact's file extension.
     * @throws MojoExecutionException When an error occurs.
     */
    void copyDepSansVer(
        File dir,
        String artifactId,
        String version,
        String extension) throws MojoExecutionException
    {
        String artVer = artifactId + "-" + version;
        File orig = new File(dir, artVer + "." + extension);
        File copy = new File(dir, artifactId + "." + extension);
        copyDepSansVer(orig, copy);
    }

    /**
     * Creates a copy of the artifact dependency without the version number so
     * that msbuild can find the file that the developer may have originally
     * referenced.
     * 
     * @param ver The versioned file.
     * @param nonVer The non-versioned file.
     * @throws MojoExecutionException When an error occurs.
     */
    void copyDepSansVer(File ver, File nonVer) throws MojoExecutionException
    {
        try
        {
            if (ver.exists() && !nonVer.exists())
            {
                FileUtils.copyFile(ver, nonVer);
            }
        }
        catch (IOException e)
        {
            String msg =
                String
                    .format(
                        "Error copying versioned file, %s, to non-versioned file, %s",
                        ver.getPath(),
                        nonVer.getPath());

            throw new MojoExecutionException(msg, e);
        }
    }

    /**
     * Loads this project's dependencies into the msbuild property
     * ReferencePath.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void loadMyDependencies() throws MojoExecutionException
    {
        loadProjectDependencies(this.mavenProject);
    }

    /**
     * Loads the collected dependencies into the msbuild property ReferencePath.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    void loadCollectedDependencies() throws MojoExecutionException
    {
        List projects = this.mavenProject.getCollectedProjects();

        if (projects == null)
        {
            return;
        }

        for (Object omp : projects)
        {
            MavenProject mp = (MavenProject) omp;
            loadProjectDependencies(mp);
        }
    }

    /**
     * Loads all the dependencies into the msbuild property ReferencePath.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void loadDependencies() throws MojoExecutionException
    {
        loadMyDependencies();

        if (this.mavenProject.getCollectedProjects() != null)
        {
            loadCollectedDependencies();
        }
    }

    /**
     * Loads the reference paths and build the property string.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void loadReferencePaths() throws MojoExecutionException
    {
        loadDependencies();

        String rp = "";

        if (this.properties != null)
        {
            if (this.properties.containsKey("ReferencePath"))
            {
                rp = this.properties.getProperty("ReferencePath");
            }
        }

        String[] rps = rp.split("\\,|\\;");

        StringBuilder rpsb = new StringBuilder();

        if (rps.length > 0)
        {
            for (String s : rps)
            {
                rpsb.append(s);

                if (s != rps[rps.length - 1])
                {
                    rpsb.append(";");
                }
            }
        }

        if (this.referencePaths != null)
        {
            for (Object of : this.referencePaths)
            {
                File f = (File) of;

                String fp = getPath(f, false);

                debug("added reference path " + fp);

                rpsb.append(fp);

                if (of != this.referencePaths
                    .get(this.referencePaths.size() - 1))
                {
                    rpsb.append(";");
                }
            }
        }

        if (rpsb.length() > 0)
        {
            String newRps = rpsb.toString();
            this.properties.put("ReferencePath", quote(newRps));
        }
    }

    /**
     * Load the build file to use.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void loadBuildFile() throws MojoExecutionException
    {
        if (this.buildFile == null)
        {
            this.buildFile = findBuildFile();
        }
    }

    @Override
    String getMojoName()
    {
        return "msbuild";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return isSolution() || isProject();
    }

    @Override
    File getDefaultCommand()
    {
        return new File("msbuild.exe");
    }
}