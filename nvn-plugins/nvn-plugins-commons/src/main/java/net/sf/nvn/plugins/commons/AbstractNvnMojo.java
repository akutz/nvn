package net.sf.nvn.plugins.commons;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    List<MavenProject> moduleProjects;

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
            && (isCSProject() || isVBProject() || isVdprojProject());
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
            && !(isCSProject() || isVBProject() || isVdprojProject());
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
    @SuppressWarnings("unchecked")
    boolean isCSProject()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "csproj"
            }, false);

        return files != null && files.size() > 0;
    }

    /**
     * Gets a flag indicating whether or not this project is a Visual Studio
     * VisualBasic.NET project.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         VisualBasic.NET project.
     */
    @SuppressWarnings("unchecked")
    boolean isVBProject()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "vbproj"
            }, false);

        return files != null && files.size() > 0;
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
            MavenProject project =
                this.builder.buildWithDependencies(
                    projectFile,
                    localRepository,
                    globalProfileManager);

            return project;
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoExecutionException(
                "Error resolving artifacts while loading parent",
                e);
        }
        catch (ArtifactNotFoundException e)
        {
            throw new MojoExecutionException(
                "Error finding artifacts while loading parent",
                e);
        }
        catch (ProjectBuildingException e)
        {
            throw new MojoExecutionException(
                "Error building project while loading parent",
                e);
        }
    }

    @Override
    final public void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            info("skipping execution");
            return;
        }

        if (!this.ignoreProjectType && !isProjectTypeValid())
        {
            return;
        }

        try
        {
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
    void execute(MavenProject project, String... goals)
        throws MojoExecutionException
    {
        File projectPomFile = new File(project.getBasedir(), "pom.xml");

        Invoker invoker = new DefaultInvoker();
        invoker.setErrorHandler(new PrintStreamHandler(System.err, true));
        invoker.setOutputHandler(new PrintStreamHandler(System.out, true));

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
            MavenProject mp = readProjectFile(modulePomFile, true);
            list.add(mp);
        }

        this.moduleProjects = list;

        return this.moduleProjects;
    }
}