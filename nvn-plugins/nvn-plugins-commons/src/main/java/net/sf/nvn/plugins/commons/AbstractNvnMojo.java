package net.sf.nvn.plugins.commons;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.nvn.commons.dotnet.PlatformType;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;
import net.sf.nvn.commons.dotnet.v35.msbuild.BuildConfiguration;
import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
import org.apache.commons.io.FileUtils;
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
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;

/**
 * The base class for all nvn MOJOs.
 * 
 * @author akutz
 * 
 */
public abstract class AbstractNvnMojo extends AbstractMojo
{
    /**
     * The property name for the active build configuration property.
     */
    static String ACTIVE_BUILD_CONFIGURATION_PROP_NAME =
        "net.sf.nvn.build.config.active";

    /**
     * The property name for the active build platform property.
     */
    static String ACTIVE_BUILD_PLATFORM_PROP_NAME =
        "net.sf.nvn.build.platform.active";

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
     * A list of the module projects.
     */
    private List<MavenProject> moduleProjects;

    /**
     * A map of the module MSBuild projects index by the maven project's name.
     */
    private Map<String, MSBuildProject> moduleMSBuildProjects;

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
     * Solution.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         Solution.
     */
    @SuppressWarnings("unchecked")
    boolean isSolution()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "sln"
            }, false);

        return files != null && files.size() > 0;
    }

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * Solution in a hierarchical structure.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         Solution in a hierarchical structure.
     */
    boolean isHierarchicalSolution()
    {
        return isSolution()
            && !(isCSProject() || isVBProject() || isVdprojProject());
    }

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * Solution in a flat structure.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         Solution in a flat structure.
     */
    boolean isFlatSolution()
    {
        return isSolution()
            && (isCSProject() || isVBProject() || isVdprojProject());
    }

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * Setup project.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         Setup project.
     */
    @SuppressWarnings("unchecked")
    boolean isVdprojProject()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "vdproj"
            }, false);

        return files != null && files.size() > 0;
    }

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
     * Gets the pom file for a given module name. If the name contains any
     * directory separator characters then it is considered to be a path and
     * used as is.
     * 
     * @param moduleName The module name.
     * @return The pom file.
     */
    File getPomFile(String moduleName)
    {
        if (moduleName.contains("pom.xml"))
        {
            return new File(moduleName);
        }

        return new File(moduleName, "pom.xml");
    }

    /**
     * Gets a list of the modules as maven projects from the maven project's
     * modules list
     * 
     * @return A list of maven projects.
     * @throws MojoExecutionException When an error occurs.
     */
    List<MavenProject> getModules() throws MojoExecutionException
    {
        if (moduleProjects != null)
        {
            return this.moduleProjects;
        }

        List<MavenProject> list = new ArrayList<MavenProject>();

        if (this.mavenProject.getModules().size() == 0)
        {
            debug("this project has no modules");
        }

        for (Object omoduleName : this.mavenProject.getModules())
        {
            String moduleName = (String) omoduleName;
            File modulePomFile = getPomFile(moduleName);
            MavenProject mp = readProjectFile(modulePomFile, false);
            list.add(mp);
        }

        this.moduleProjects = list;

        return this.moduleProjects;
    }

    /**
     * Gets a map of the modules as MSBuild projects indexed by the modules'
     * artifact IDs. This will only return CSharp and VisualBasic projects.
     * 
     * @return A list of the modules as MSBuild projects indexed by the modules'
     *         artifact IDs.
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    Map<String, MSBuildProject> getMSBuildModules()
        throws MojoExecutionException
    {
        if (this.moduleMSBuildProjects != null)
        {
            return this.moduleMSBuildProjects;
        }

        this.moduleMSBuildProjects = new HashMap<String, MSBuildProject>();

        List<MavenProject> mods = getModules();

        for (MavenProject mp : mods)
        {
            File pom = mp.getFile();
            File moddir = pom.getParentFile();

            debug("searching %s for msbuild project files", moddir);

            Collection files = FileUtils.listFiles(moddir, new String[]
            {
                "csproj", "vbproj"
            }, false);

            if (files == null)
            {
                continue;
            }

            if (files.size() == 0)
            {
                continue;
            }

            File projfile = (File) files.iterator().next();

            MSBuildProject msbp;

            try
            {
                msbp = MSBuildProject.instance(projfile);
            }
            catch (Exception e)
            {
                throw new MojoExecutionException(String.format(
                    "Error reading MSBuild project file %s",
                    projfile), e);
            }

            String key =
                String.format("%s-%s", mp.getArtifactId(), mp.getPackaging());

            this.moduleMSBuildProjects.put(key, msbp);
            debug("added %s to msbuild projects list", mp.getArtifactId());
        }

        return this.moduleMSBuildProjects;
    }

    /**
     * Gets a MSBuildProject from the maven project.
     * 
     * @param project The maven project to get the MSBuild project with.
     * @return A MSBuildProject.
     * @throws MojoExecutionException When an error occurs.
     */
    MSBuildProject getMSBuildModule(MavenProject project)
        throws MojoExecutionException
    {
        String key = getMSBuildModuleKey(project);
        return getMSBuildModules().get(key);
    }

    /**
     * Gets the key to access the moduleMSBuildProjects map.
     * 
     * @param project The maven project to get the key with.
     * @return The key.
     */
    String getMSBuildModuleKey(MavenProject project)
    {
        return String.format("%s-%s", project.getArtifactId(), project
            .getPackaging());
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
        if (!this.mavenProject.getProperties().containsKey(
            ACTIVE_BUILD_CONFIGURATION_PROP_NAME))
        {
            debug(
                "properties does not contain key %s",
                ACTIVE_BUILD_CONFIGURATION_PROP_NAME);
            return null;
        }

        String configName =
            this.mavenProject.getProperties().getProperty(
                ACTIVE_BUILD_CONFIGURATION_PROP_NAME);

        debug(
            "properties retrieved key %s=%s",
            ACTIVE_BUILD_CONFIGURATION_PROP_NAME,
            configName);

        return configName;
    }

    /**
     * Gets the active build platform.
     * 
     * @return The active build platform.
     */
    PlatformType getActiveBuildPlatform()
    {
        if (!this.mavenProject.getProperties().containsKey(
            ACTIVE_BUILD_PLATFORM_PROP_NAME))
        {
            debug(
                "properties does not contain key %s",
                ACTIVE_BUILD_PLATFORM_PROP_NAME);
            return null;
        }

        String platform =
            this.mavenProject.getProperties().getProperty(
                ACTIVE_BUILD_PLATFORM_PROP_NAME);

        debug(
            "properties retrieved key %s=%s",
            ACTIVE_BUILD_PLATFORM_PROP_NAME,
            platform);

        return PlatformType.parse(platform);
    }
}