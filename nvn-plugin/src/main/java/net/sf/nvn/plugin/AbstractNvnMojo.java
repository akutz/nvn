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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.sf.nvn.commons.ProjectUtils;
import net.sf.nvn.commons.dotnet.PlatformType;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;
import net.sf.nvn.commons.dotnet.v35.msbuild.BuildConfiguration;
import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;

/**
 * The base class for all nvn MOJOs.
 * 
 * @author akutz
 * 
 */
public abstract class AbstractNvnMojo extends AbstractMojo
{
    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @component
     */
    ArtifactResolver resolver;

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
     * is not met. {@link #skip} beats {@link #ignoreProjectType}.
     * 
     * @parameter default-value="false"
     */
    boolean ignoreProjectType;

    /**
     * Force the execution of this plug-in even if its execution requirements
     * are not met. {@link #skip} beats {@link #ignoreExecutionRequirements}.
     * 
     * @parameter default-value="false"
     */
    boolean ignoreExecutionRequirements;

    /**
     * The nvn execute method. nvn MOJOs override this method instead of the
     * normal MOJO {@link AbstractMojo#execute()} method.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    abstract void nvnExecute() throws MojoExecutionException;

    /**
     * This method is invoked after {@link #isProjectTypeValid()} but before
     * {@link #shouldExecute()}.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    abstract void preExecute() throws MojoExecutionException;

    /**
     * This method is invoked after the {@link #preExecute()},
     * {@link #shoulExecute()}, and {@link #nvnExecute()} methods regardless
     * whether any of them threw an exception.
     * 
     * @param executionException If the {@link #preExecute()},
     *        {@link #shoulExecute()}, or {@link #nvnExecute()} methods threw an
     *        exception it will be passed to the {@link #postExecute()} method
     *        via this parameter. If the methods did not throw an exception this
     *        parameter will be null.
     * 
     * @throws MojoExecutionException
     */
    abstract void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException;

    /**
     * Gets this MOJO's name. Will be used for logging.
     * 
     * @return This MOJO's name.
     */
    abstract String getMojoName();

    /**
     * Gets a flag indicating whether or not this MOJO should execute. This
     * method is invoked after {@link #isProjectTypeValid()} and
     * {@link #prepareForExecute()}.
     * 
     * @return A flag indicating whether or not this MOJO should execute.
     * @throws MojoExecutionException When an error occurs.
     */
    abstract boolean shouldExecute() throws MojoExecutionException;

    /**
     * Returns a flag indicating whether or not this MOJO is valid for this
     * project type. This method is invoked before {@link #prepareForExecute()}
     * and {@link #shoulExecute()}.
     * 
     * @return A flag indicating whether or not this MOJO is valid for this
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
        return ProjectUtils.readProjectFile(
            this.builder,
            this.localRepository,
            this.mavenProject
                .getProjectBuilderConfiguration()
                .getGlobalProfileManager(),
            projectFile,
            resolveDependencies);
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
     * Gets the active build configuration.
     * 
     * @return The active build configuration.
     */
    BuildConfiguration getBuildConfig()
    {
        if (!(isCSProject() || isVBProject()))
        {
            return null;
        }

        return getMSBuildProject().getBuildConfigurations().get(
            getBuildConfigName());
    }

    /**
     * Gets the name of the active build configuration. A null value is returned
     * if this property is not set.
     * 
     * @return The name of the active build configuration.
     */
    String getBuildConfigName()
    {
        return getProperty("build.config.name");
    }

    void setBuildConfigName(String toSet)
    {
        setProperty("build.config.name", toSet);
    }

    /**
     * Gets the build platform.
     * 
     * @return The build platform.
     */
    PlatformType getBuildPlatform()
    {
        return getProperty("build.platform");
    }

    /**
     * Sets the build platform.
     * 
     * @param toSet The build platform.
     */
    void setBuildPlatform(String toSet)
    {
        setBuildPlatform(PlatformType.parse(toSet));
    }

    /**
     * Sets the build platform.
     * 
     * @param toSet The build platform.
     */
    void setBuildPlatform(PlatformType toSet)
    {
        setProperty("build.platform", toSet);
    }

    /**
     * Gets a value indicating whether or not this Maven project has an
     * associated MSBuild project.
     * 
     * @return A value indicating whether or not this Maven project has an
     *         associated MSBuild project.
     */
    boolean isMSBuildProject()
    {
        return getMSBuildProject() != null;
    }

    /**
     * Gets the MSBuild project associated with this Maven project. This field
     * will be null if not MSBuild project file is found.
     * 
     * @return The MSBuild project associated with this Maven project. This
     *         field will be null if not MSBuild project file is found.
     */
    MSBuildProject getMSBuildProject()
    {
        return getProperty("msbuild.project");
    }

    /**
     * Sets the MSBuild project associated with this Maven project.
     * 
     * @param toSet The MSBuild project associated with this Maven project.
     */
    void setMSBuildProject(MSBuildProject toSet)
    {
        setProperty("msbuild.project", toSet);
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
        return isMSBuildProject()
            && getMSBuildProject().getProjectLanguage() == ProjectLanguageType.CSharp;
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
        return isMSBuildProject()
            && getMSBuildProject().getProjectLanguage() == ProjectLanguageType.VisualBasic;
    }

    /**
     * Sets a property on the current Maven project's properties collection.
     * 
     * @param name The property's name.
     * @param value The property's value.
     */
    void setProperty(String name, Object value)
    {
        nvnPropertiesRWL.writeLock().lock();

        name = "nvn.properties." + name;
        getNvnProperties().put(name, value);

        nvnPropertiesRWL.writeLock().unlock();
    }

    /**
     * Gets a property from the current Maven project's properties collection.
     * 
     * @param <T> The property's type.
     * @param name The property's name.
     * @return The property's value.
     */
    @SuppressWarnings("unchecked")
    <T> T getProperty(String name)
    {
        T value = null;

        nvnPropertiesRWL.readLock().lock();

        Properties nvnProps = getNvnProperties();

        name = "nvn.properties." + name;

        if (nvnProps.containsKey(name))
        {
            value = (T) nvnProps.get(name);
        }

        nvnPropertiesRWL.readLock().unlock();

        return value;
    }

    /**
     * Gets the properties map for the current Maven project.
     * 
     * @return The properties map for the current Maven project.
     */
    private Properties getNvnProperties()
    {
        Map<String, Properties> projProps = nvnStorage.get();

        String key =
            String.format(
                "%s-%s",
                this.mavenProject.getGroupId(),
                this.mavenProject.getArtifactId());

        if (!projProps.containsKey(key))
        {
            nvnStorage.get().put(key, new Properties());
        }

        Properties props = nvnStorage.get().get(key);

        return props;
    }

    /**
     * The read/write lock for {@link #getNvnProperties()} and
     * {@link #nvnStorage}.
     */
    private static final ReadWriteLock nvnPropertiesRWL =
        new ReentrantReadWriteLock();

    /**
     * A ThreadLocal map that is indexed by a Maven project's groupId and
     * artifactId in the format '%s-%s'. This map provides a properties
     * collection specific to nvn for each Maven project in the reactor list.
     */
    private static final NvnStorage nvnStorage = new NvnStorage();

    /**
     * A ThreadLocal class for storing nvn properties per Maven project.
     * 
     * @author akutz
     * 
     */
    private static final class NvnStorage extends
        ThreadLocal<Map<String, Properties>>
    {
        @Override
        protected Map<String, Properties> initialValue()
        {
            return new HashMap<String, Properties>();
        }
    }
}
