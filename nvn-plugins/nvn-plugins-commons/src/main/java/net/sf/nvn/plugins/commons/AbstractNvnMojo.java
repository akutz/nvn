package net.sf.nvn.plugins.commons;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import net.sf.nvn.commons.dotnet.PlatformType;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;
import net.sf.nvn.commons.dotnet.v35.msbuild.BuildConfiguration;
import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.codehaus.plexus.util.StringUtils;

/**
 * The base class for all nvn MOJOs.
 * 
 * @author akutz
 * 
 */
public abstract class AbstractNvnMojo extends AbstractMojo
{
    /**
     * The default version for a .NET project.
     */
    private static String DEFAULT_VERSION = "0.0.0.0";

    private static String ACTIVE_BUILD_CONFIG = "nvn.build.config";
    private static String DEFAULT_DEBUG_BUILD_CONFIG =
        "nvn.build.config.default.debug";
    private static String DEFAULT_RELEASE_BUILD_CONFIG =
        "nvn.build.config.default.release";
    private static String ACTIVE_BUILD_PLATFORM = "nvn.build.platform";
    private static String DEFAULT_DEBUG_BUILD_PLATFORM =
        "nvn.build.platform.default.debug";
    private static String DEFAULT_RELEASE_BUILD_PLATFORM =
        "nvn.build.platform.default.release";

    /**
     * <p>
     * The active build configuration.
     * </p>
     * 
     * <p>
     * The active build configuration is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.config</strong> is set then its
     * value is used.</li>
     * <li>Otherwise the project's version is examined to determine the active
     * build configuration.
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build configuration is set to the value of
     * {@link #defaultDebugBuildConfiguration}.</li>
     * <li>Otherwise the active build configuration is set to the value of
     * {@link #defaultReleaseBuildConfiguration}.</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.config}"
     */
    private String activeBuildConfiguration;

    /**
     * The default <strong>Debug</strong> build configuration name.
     * 
     * <p>
     * The default Debug build configuration is determined by the following
     * steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.config.default.debug</strong> is
     * set then its value is used.</li>
     * <li>Otherwise this parameter defaults to <strong>Debug</strong></li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.config.default.debug}"
     */
    private String defaultDebugBuildConfiguration;

    /**
     * The default <strong>Release</strong> build configuration name.
     * 
     * <p>
     * The default Release build configuration is determined by the following
     * steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.config.default.release</strong> is
     * set then its value is used.</li>
     * <li>Otherwise this parameter defaults to <strong>Release</strong></li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.config.default.release}"
     */
    private String defaultReleaseBuildConfiguration;

    /**
     * <p>
     * The active build platform.
     * </p>
     * 
     * <p>
     * The active build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.platform</strong> is set then its
     * value is used.</li>
     * <li>Otherwise the project's version is examined to determine the active
     * build platform.
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build platform is set to the value of
     * {@link #defaultDebugBuildPlatform}.</li>
     * <li>Otherwise the active build platform is set to the value of
     * {@link #defaultReleaseBuildPlatform}.</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.platform}"
     */
    private String activeBuildPlatform;

    /**
     * The default <strong>Debug</strong> build platform name.
     * 
     * <p>
     * The default Debug build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.platform.default.debug</strong> is
     * set then its value is used.</li>
     * <li>Otherwise this parameter defaults to <strong>Any CPU</strong></li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.platform.default.debug}"
     */
    private String defaultDebugBuildPlatform;

    /**
     * The default <strong>Release</strong> build platform name.
     * 
     * <p>
     * The default Release build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If this parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.platform.default.release</strong>
     * is set then its value is used.</li>
     * <li>Otherwise this parameter defaults to <strong>Any CPU</strong></li>
     * </ul>
     * 
     * @parameter expression="${nvn.build.platform.default.release}"
     */
    private String defaultReleaseBuildPlatform;

    /**
     * A MavenProjectHelper.
     * 
     * @component
     */
    MavenProjectHelper projectHelper;

    /**
     * The artifact factory.
     * 
     * @component
     */
    ArtifactFactory factory;

    /**
     * The MSBuild project associated with this project. This field will be null
     * if not MSBuild project file is found.
     */
    MSBuildProject msbuildProject;

    /**
     * Maven runtime information.
     * 
     * @component
     */
    RuntimeInformation runtimeInfo;

    /**
     * The current maven session.
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    MavenSession session;

    /**
     * The maven lifecycle executor.
     * 
     * @component
     */
    LifecycleExecutor lifecycle;

    /**
     * Used to build maven project files.
     * 
     * @component
     */
    MavenProjectBuilder builder;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject mavenProject;

    /**
     * The local maven repository.
     * 
     * @parameter expression="${localRepository}"
     */
    ArtifactRepository localRepository;

    /**
     * The reactor projects.
     * 
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unchecked")
    List reactorProjects;

    /**
     * Skip this plug-in. Skip beats forceProjectType and
     * ignoreExecutionRequirements.
     * 
     * @parameter default-value="false"
     */
    boolean skip;

    /**
     * Force the execution of this plug-in even if its project type requirement
     * is not met. Skip beats ignoreProjectType.
     * 
     * @parameter default-value="false"
     */
    boolean ignoreProjectType;

    /**
     * Force the execution of this plug-in even if its execution requirements
     * are not met. Skip beats ignoreExecutionRequirements.
     * 
     * @parameter default-value="false"
     */
    boolean ignoreExecutionRequirements;

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * CSharp project.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         CSharp project.
     */
    boolean isCSProject()
    {
        return this.msbuildProject != null
            && this.msbuildProject.getProjectLanguage() == ProjectLanguageType.CSharp;
    }

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * VisualBasic.NET project.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         VisualBasic.NET project.
     */
    boolean isVBProject()
    {
        return this.msbuildProject != null
            && this.msbuildProject.getProjectLanguage() == ProjectLanguageType.VisualBasic;
    }

    /**
     * The NVN execute method. NVN mojos override this method instead of the
     * normal MOJO execute() method.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    abstract void nvnExecute() throws MojoExecutionException;

    /**
     * This method is invoked after "isProjectTypeValid()" but before
     * "shouldExecute()".
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    abstract void preExecute() throws MojoExecutionException;

    /**
     * This method is invoked after the preExecute(), shouldExecute() and
     * nvnExecute() methods regardless whether any of them threw an exception.
     * 
     * @param executionException If the preExecute(), shouldExecute(), or
     *        nvnExecute() methods threw an exception it will be passed to the
     *        postExecute method via this parameter. If the methods did not
     *        throw an exception this parameter will be null.
     * 
     * @throws MojoExecutionException
     */
    abstract void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException;

    /**
     * Gets this mojo's name. Will be used for logging.
     * 
     * @return This mojo's name.
     */
    abstract String getMojoName();

    /**
     * Gets a flag indicating whether or not this mojo should execute. This
     * method is invoked after "isProjectTypeValid()" and "prepareForExecute()".
     * 
     * @return A flag indicating whether or not this mojo should execute.
     * @throws MojoExecutionException When an error occurs.
     */
    abstract boolean shouldExecute() throws MojoExecutionException;

    /**
     * Returns a flag indicating whether or not this mojo is valid for this
     * project type. This method is invoked before "prepareForExecute()" and
     * "shouldExecute()".
     * 
     * @return A flag indicating whether or not this mojo is valid for this
     *         project type.
     */
    abstract boolean isProjectTypeValid();

    /**
     * Reads a project file and builds a maven project.
     * 
     * @param projectFile The project file.
     * @param resolveDependencies Whether or not to resolve dependencies.
     * @return A maven project.
     * @throws MojoExecutionException When an error occurs.
     */
    MavenProject readProjectFile(File projectFile, boolean resolveDependencies)
        throws MojoExecutionException
    {
        ProjectBuilderConfiguration config =
            this.mavenProject.getProjectBuilderConfiguration();

        ProfileManager globalProfileManager = config.getGlobalProfileManager();

        try
        {
            MavenProject project;

            if (resolveDependencies)
            {
                project =
                    this.builder.buildWithDependencies(
                        projectFile,
                        localRepository,
                        globalProfileManager);
            }
            else
            {
                project =
                    this.builder.build(
                        projectFile,
                        localRepository,
                        globalProfileManager);
            }

            return project;
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoExecutionException(
                "Error resolving artifacts while reading project file: "
                    + projectFile.getAbsolutePath(),
                e);
        }
        catch (ArtifactNotFoundException e)
        {
            throw new MojoExecutionException(
                "Error finding artifacts while reading project file: "
                    + projectFile.getAbsolutePath(),
                e);
        }
        catch (ProjectBuildingException e)
        {
            throw new MojoExecutionException(
                "Error building project while reading project file: "
                    + projectFile.getAbsolutePath(),
                e);
        }
    }

    @SuppressWarnings("unchecked")
    void initMSBuildProject() throws MojoExecutionException
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "csproj", "vbproj"
            }, false);

        if (files == null)
        {
            return;
        }

        if (files.size() == 0)
        {
            return;
        }

        File projectFile = (File) files.iterator().next();

        try
        {
            this.msbuildProject = MSBuildProject.instance(projectFile);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(String.format(
                "Error reading MSBuild project from %s",
                projectFile), e);
        }
    }

    @Override
    final public void execute() throws MojoExecutionException
    {
        debug("properties: %s", this.mavenProject.getProperties());

        if (this.skip)
        {
            info("skipping execution");
            return;
        }

        initMSBuildProject();
        initVersion();
        initActiveBuildConfiguration();
        initActiveBuildPlatform();
        initArtifacts();

        if (!this.ignoreProjectType && !isProjectTypeValid())
        {
            return;
        }

        try
        {
            String abc = getActiveBuildConfigurationName();
            PlatformType abp = getActiveBuildPlatform();
            debug("active build configuration: %s", abc);
            debug("active build platform:      %s", abp);

            preExecute();

            if (this.ignoreExecutionRequirements || shouldExecute())
            {
                nvnExecute();
            }
            else
            {
                debug("execution requirements not met");
            }

            postExecute(null);
        }
        catch (MojoExecutionException e)
        {
            postExecute(e);
            throw e;
        }
    }

    void initArtifacts() throws MojoExecutionException
    {
        // We don't need to initialize this twice.
        if (this.mavenProject.getArtifact().getFile() != null)
        {
            debug("not setting an artifact since once is already present");
            return;
        }

        File basedir = this.mavenProject.getBasedir();

        String bcn = getActiveBuildConfigurationName();

        String filePath = this.msbuildProject.getBuildArtifact(bcn).getPath();
        File file = new File(basedir, filePath);

        Artifact artifact =
            this.factory.createArtifactWithClassifier(
                this.mavenProject.getGroupId(),
                this.mavenProject.getArtifactId(),
                this.mavenProject.getVersion(),
                this.mavenProject.getPackaging(),
                null);
        
        artifact.setFile(file);
        debug("set artifact file %s", file);

        this.mavenProject.setArtifact(artifact);

        File pdbFile = this.msbuildProject.getBuildSymbolsArtifact(bcn);
        if (pdbFile != null)
        {
            this.projectHelper.attachArtifact(
                this.mavenProject,
                "pdb",
                pdbFile);
        }

        File docFile = this.msbuildProject.getBuildDocumentationArtifact(bcn);
        if (docFile != null)
        {
            this.projectHelper.attachArtifact(
                this.mavenProject,
                "xml",
                docFile);
        }
    }

    /**
     * Gets a file's path.
     * 
     * @param file A file.
     * @param quote Whether or not to quote the file path.
     * @return A file's path.
     */
    String getPath(File file, boolean quote)
    {
        String fp = file.getPath();

        debug("getPath(" + fp + ")");

        String path;

        if (fp.contains("\\") || fp.contains("/"))
        {
            debug("getPath - file path contains a \\ or /");

            if (fp.matches("^\\w\\:.*+"))
            {
                path = fp;
                debug("getPath detected a drive letter thus using file path as is");
            }
            else
            {
                path = this.mavenProject.getBasedir() + "\\" + fp;
                debug("getPath using basedir and file path as is");
            }
        }
        else if ((path = getFullPathFromPath(file)) != null)
        {
            debug("getPath file is in path via path environment variable");
        }
        else
        {
            path = this.mavenProject.getBasedir() + "\\" + fp;
            debug("getPath using basedir and file path as is");
        }

        if (quote)
        {
            path = quote(path);
        }

        debug("getPath returned \"%s\"", path);

        return path;
    }

    /**
     * Get a file's path.
     * 
     * @param file A file.
     * @return A file's path.
     */
    String getPath(File file)
    {
        return getPath(file, true);
    }

    /**
     * Get a file's full path if it is in the Path environment variable.
     * 
     * @param file A file.
     * @return A file's full path if it is in the Path environment variable.
     */
    String getFullPathFromPath(File file)
    {
        return null;
    }

    /**
     * Emits a debug message to the logger.
     * 
     * @param message The message to emit.
     */
    void debug(String message)
    {
        getLog().debug(String.format("NVN-%s: %s", getMojoName(), message));
    }

    /**
     * Emits a debug message to the logger.
     * 
     * @param messageFormat The message format string.
     * @param args Arguments to the message format string.
     */
    void debug(String messageFormat, Object... args)
    {
        debug(String.format(messageFormat, args));
    }

    /**
     * Emits an info message to the logger.
     * 
     * @param message The message to emit.
     */
    void info(String message)
    {
        getLog().info(String.format("NVN-%s: %s", getMojoName(), message));
    }

    /**
     * Emits an info message to the logger.
     * 
     * @param messageFormat The message format string.
     * @param args Arguments to the message format string.
     */
    void info(String messageFormat, Object... args)
    {
        info(String.format(messageFormat, args));
    }

    /**
     * Emits an error message to the logger.
     * 
     * @param message The message to emit.
     */
    void error(String message)
    {
        getLog().error(String.format("NVN-%s: %s", getMojoName(), message));
    }

    /**
     * Emits an error message to the logger.
     * 
     * @param messageFormat The message format string.
     * @param args Arguments to the message format string.
     */
    void error(String messageFormat, Object... args)
    {
        debug(String.format(messageFormat, args));
    }

    /**
     * Execute a given maven project using the specified goals.
     * 
     * @param project The maven project to execute.
     * @param goals The goals to execute.
     * @throws MojoExecutionException When an error occurs.
     */
    void execute(MavenProject project, List<String> goals)
        throws MojoExecutionException
    {
        execute(project, goals.toArray(new String[0]));
    }

    /**
     * Execute a given maven project using the specified goals.
     * 
     * @param project The maven project to execute.
     * @param goals The goals to execute.
     * @throws MojoExecutionException When an error occurs.
     */
    void execute(MavenProject project, String... goals)
        throws MojoExecutionException
    {
        File projectPomFile = new File(project.getBasedir(), "pom.xml");

        Invoker invoker = new DefaultInvoker();
        invoker.setErrorHandler(new PrintStreamHandler(System.err, true));
        invoker.setOutputHandler(new PrintStreamHandler(System.out, true));

        Properties reqProps = new Properties();
        reqProps.putAll(this.mavenProject.getProperties());

        InvocationRequest req = new DefaultInvocationRequest();
        req.setBaseDirectory(project.getBasedir());
        req.setOutputHandler(new PrintStreamHandler(System.out, true));
        req.setProperties(this.mavenProject.getProperties());
        req.setGoals(Arrays.asList(goals));
        req.setPomFile(projectPomFile);

        if (getLog().isDebugEnabled())
        {
            req.setDebug(true);
        }

        InvocationResult result;

        try
        {
            result = invoker.execute(req);
        }
        catch (MavenInvocationException e)
        {
            throw new MojoExecutionException("Error invoking "
                + project.getName(), e);
        }

        if (result.getExitCode() != 0)
        {
            throw new MojoExecutionException("Error invoking "
                + project.getName(), result.getExecutionException());
        }
    }

    /**
     * Gets the active build configuration.
     * 
     * @return The active build configuration.
     */
    BuildConfiguration getActiveBuildConfiguration()
    {
        if (!(isCSProject() || isVBProject()))
        {
            return null;
        }

        return this.msbuildProject.getBuildConfigurations().get(
            getActiveBuildConfigurationName());
    }

    /**
     * Gets the name of the active build configuration. A null value is returned
     * if this property is not set.
     * 
     * @return The name of the active build configuration.
     */
    String getActiveBuildConfigurationName()
    {
        return this.activeBuildConfiguration;
    }

    /**
     * Gets the active build platform.
     * 
     * @return The active build platform.
     */
    PlatformType getActiveBuildPlatform()
    {
        return PlatformType.parse(this.activeBuildPlatform);
    }

    /**
     * Initializes this project's version.
     */
    void initVersion()
    {
        String myVersion = this.mavenProject.getVersion();

        if (myVersion.equals(MavenProject.EMPTY_PROJECT_VERSION))
        {
            this.mavenProject.setVersion(DEFAULT_VERSION);
        }
    }

    /**
     * <p>
     * Initializes the active build configuration.
     * </p>
     * 
     * <p>
     * The active build configuration is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If the {@link #activeBuildConfiguration} parameter is set then its value
     * is used.</li>
     * <li>If the property <strong>nvn.build.config</strong> is set then its
     * value is used.</li>
     * <li>Otherwise the project's version is examined to determine the active
     * build configuration.
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build configuration is set to the value of
     * {@link #defaultDebugBuildConfiguration}.</li>
     * <li>Otherwise the active build configuration is set to the value of
     * {@link #defaultReleaseBuildConfiguration}.</li>
     * </ul>
     * </li>
     * </ul>
     */
    void initActiveBuildConfiguration()
    {
        if (StringUtils.isNotEmpty(this.activeBuildConfiguration))
        {
            return;
        }

        Properties props = this.mavenProject.getProperties();

        if (props.containsKey(ACTIVE_BUILD_CONFIG))
        {
            this.activeBuildConfiguration =
                props.getProperty(ACTIVE_BUILD_CONFIG);
            return;
        }

        if (StringUtils.isEmpty(this.defaultDebugBuildConfiguration))
        {
            if (props.containsKey(DEFAULT_DEBUG_BUILD_CONFIG))
            {
                this.defaultDebugBuildConfiguration =
                    props.getProperty(DEFAULT_DEBUG_BUILD_CONFIG);
            }
            else
            {
                this.defaultDebugBuildConfiguration = "Debug";
            }
        }

        if (StringUtils.isEmpty(this.defaultReleaseBuildConfiguration))
        {
            if (props.containsKey(DEFAULT_RELEASE_BUILD_CONFIG))
            {
                this.defaultReleaseBuildConfiguration =
                    props.getProperty(DEFAULT_RELEASE_BUILD_CONFIG);
            }
            else
            {
                this.defaultReleaseBuildConfiguration = "Release";
            }
        }

        if (this.mavenProject.getVersion().contains("-SNAPSHOT"))
        {
            this.activeBuildConfiguration = this.defaultDebugBuildConfiguration;
        }
        else
        {
            this.activeBuildConfiguration =
                this.defaultReleaseBuildConfiguration;
        }
    }

    /**
     * <p>
     * Initializes the active build platform.
     * </p>
     * 
     * <p>
     * The active build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>
     * If {@link #activeBuildPlatform} parameter is set then its value is used.</li>
     * <li>If the property <strong>nvn.build.platform</strong> is set then its
     * value is used.</li>
     * <li>Otherwise the project's version is examined to determine the active
     * build platform.
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build platform is set to the value of
     * {@link #defaultDebugBuildPlatform}.</li>
     * <li>Otherwise the active build platform is set to the value of
     * {@link #defaultReleaseBuildPlatform}.</li>
     * </ul>
     * </li>
     * </ul>
     * 
     */
    void initActiveBuildPlatform()
    {
        if (StringUtils.isNotEmpty(this.activeBuildPlatform))
        {
            return;
        }

        Properties props = this.mavenProject.getProperties();

        if (props.containsKey(ACTIVE_BUILD_PLATFORM))
        {
            this.activeBuildPlatform = props.getProperty(ACTIVE_BUILD_PLATFORM);
            return;
        }

        if (StringUtils.isEmpty(this.defaultDebugBuildPlatform))
        {
            if (props.containsKey(DEFAULT_DEBUG_BUILD_PLATFORM))
            {
                this.defaultDebugBuildPlatform =
                    props.getProperty(DEFAULT_DEBUG_BUILD_PLATFORM);
            }
            else
            {
                this.defaultDebugBuildPlatform = "Any CPU";
            }
        }

        if (StringUtils.isEmpty(this.defaultReleaseBuildPlatform))
        {
            if (props.containsKey(DEFAULT_RELEASE_BUILD_PLATFORM))
            {
                this.defaultReleaseBuildPlatform =
                    props.getProperty(DEFAULT_RELEASE_BUILD_PLATFORM);
            }
            else
            {
                this.defaultReleaseBuildPlatform = "Any CPU";
            }
        }

        if (this.mavenProject.getVersion().contains("-SNAPSHOT"))
        {
            this.activeBuildPlatform = this.defaultDebugBuildPlatform;
        }
        else
        {
            this.activeBuildPlatform = this.defaultReleaseBuildPlatform;
        }
    }
}