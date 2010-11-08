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
import net.sf.nvn.commons.Version;
import net.sf.nvn.commons.msbuild.MSBuildProject;
import net.sf.nvn.commons.msbuild.ProjectLanguageType;
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
import org.apache.maven.wagon.PathUtils;

/**
 * The base class for all nvn MOJOs.
 * 
 * @author akutz
 * 
 */
public abstract class AbstractNvnMojo extends AbstractMojo
{
    /**
     * The NVN property prefix for NVN properties that are set in the Maven
     * project's properties collection.
     */
    private final static String NPK_PREFIX = "nvn.";

    /**
     * The property key for the MSBuildProject object.
     */
    protected final static String NPK_PROJECT = "project";

    /**
     * The property key for the build configuration name (Debug, Release, etc.)
     */
    protected final static String NPK_CONFIG = "config";

    /**
     * The property key for the build platform type (AnyCPU, x64, Win32, etc.)
     */
    protected final static String NPK_PLATFORM = "platform";

    /**
     * The property key for the standard version.
     */
    protected final static String NPK_VERSION = "version";

    /**
     * The property key for the standard version's MAJOR component.
     */
    protected final static String NPK_VERSION_1 = "version.1";

    /**
     * The property key for the standard version's MAJOR and MINOR components.
     */
    protected final static String NPK_VERSION_2 = "version.2";

    /**
     * The property key for the standard version's MAJOR, MINOR, and BUILD
     * components.
     */
    protected final static String NPK_VERSION_3 = "version.3";

    /**
     * The property key for the project's build directory. Unlike the default
     * Maven property 'project.build.directory', this property value is relative
     * to the project's base directory.
     */
    protected final static String NPK_BUILD_DIR = "build.directory";

    /**
     * The property key for the name of the project's artifact. The value does
     * not include any type of file extension or path, it is simply the name of
     * the artifact. For example, if an MSBuild project produces an artifact at
     * '\bin\Debug\HelloWorld.exe' then the name of the artifact is
     * 'HelloWorld'.
     */
    protected final static String NPK_ARTIFACT_NAME = "artifact.name";

    /**
     * The property key for the project's binary artifact.
     */
    protected final static String NPK_ARTIFACT_BIN = "artifact.bin";

    /**
     * The property key for the project's symbols artifact.
     */
    protected final static String NPK_ARTIFACT_PDB = "artifact.pdb";

    /**
     * The property key for the project's documentation artifact.
     */
    protected final static String NPK_ARTIFACT_DOC = "artifact.doc";

    /**
     * The property key for the project's import library artifact. This may be
     * the same as the binary artifact if the project is a C++ StaticLibrary
     * project.
     */
    protected final static String NPK_ARTIFACT_LIB = "artifact.lib";

    /**
     * The property key for the project's type library artifact.
     */
    protected final static String NPK_ARTIFACT_TLB = "artifact.tlb";

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
    @SuppressWarnings("rawtypes")
    List reactorProjects;

    /**
     * Skip this plug-in. Skip beats forceProjectType and
     * ignoreExecutionRequirements.
     * 
     * @parameter default-value="false"
     */
    boolean skip;

    /**
     * <p>
     * Setting this parameter to true enables NVN to integrate with my favorite
     * build server, <a href="http://www.jetbrains.com/teamcity/">JetBrains
     * TeamCity</a>. TeamCity integration means different things for different
     * MOJOs:
     * </p>
     * <ul>
     * <li>For many MOJOs, TeamCity integration enables the emission of <a
     * href="http://bit.ly/aLP8hl">TeamCity messages</a> where appropriate. For
     * example, the mstest mojo emits messages that indicate the location of the
     * resulting report file.</li>
     * <li>For the AssemblyInfo MOJO the TeamCity build number is added to the
     * AssemblyInfoInformationalVersion attribute.</li>
     * </ul>
     * 
     * <p>
     * The default value is true for all MOJOs, but there are instances where it
     * may be desirable to override this value. For instance, the msbuild,
     * light, and bootstrap MOJOs all emit messages that instruct TeamCity to
     * attach artifacts to the running build configuration. This can result in
     * the use of additional disk space on the build server and may need to be
     * disabled if lack of storage is a concern.
     * </p>
     * 
     * @parameter default-value="true"
     */
    boolean enableTeamCityIntegration;

    /**
     * <p>
     * This parameter allows the level of integration with TeamCity to be
     * fine-tuned. When set to false, the MOJOs that would otherwise emit a
     * message indicating that there is an artifact to be published to TeamCity
     * will not do so.
     * </p>
     * <p>
     * This parameter has no effect when {@link enableTeamCityIntegration} is
     * set to false.
     * </p>
     * 
     * @parameter default-value="true"
     */
    boolean publishTeamCityArtifacts;

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
     * {@link #shouldExecute()}, and {@link #nvnExecute()} methods regardless
     * whether any of them threw an exception.
     * 
     * @param executionException If the {@link #preExecute()},
     *        {@link #shouldExecute()}, or {@link #nvnExecute()} methods threw
     *        an exception it will be passed to the
     *        {@link #postExecute(MojoExecutionException)} method via this
     *        parameter. If the methods did not throw an exception this
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
     * {@link #preExecute()}.
     * 
     * @return A flag indicating whether or not this MOJO should execute.
     * @throws MojoExecutionException When an error occurs.
     */
    abstract boolean shouldExecute() throws MojoExecutionException;

    /**
     * Returns a flag indicating whether or not this MOJO is valid for this
     * project type. This method is invoked before {@link #preExecute()} and
     * {@link #shouldExecute()}.
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
            info("project type not valid");
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
     * Initializes an NVN property in the Maven project's properties collection.
     * 
     * @param key The property's key.
     * @param value The property's value.
     */
    protected void initNvnProp(String key, Object value)
    {
        nvnPropertiesRWL.writeLock().lock();

        key = NPK_PREFIX + key;
        getNvnProperties().put(key, value);
        this.mavenProject.getProperties().put(key, value.toString());

        nvnPropertiesRWL.writeLock().unlock();
    }

    /**
     * Gets the name of the active build configuration. A null value is returned
     * if this property is not set.
     * 
     * @return The name of the active build configuration.
     */
    String getBuildConfig()
    {
        return getNvnProp(NPK_CONFIG);
    }

    /**
     * Gets the build platform.
     * 
     * @return The build platform.
     */
    String getBuildPlatform()
    {
        return getNvnProp(NPK_PLATFORM);
    }

    /**
     * Gets the artifact name.
     * 
     * @return The artifact name.
     */
    String getArtifactName()
    {
        return getNvnProp(NPK_ARTIFACT_NAME);
    }

    /**
     * The build directory.
     */
    private File buildDir;

    /**
     * Gets the build directory.
     * 
     * @return The build directory.
     */
    File getBuildDir()
    {
        if (this.buildDir != null)
        {
            return this.buildDir;
        }

        this.buildDir = new File(this.mavenProject.getBuild().getDirectory());
        return this.buildDir;
    }

    /**
     * Gets the binary artifact.
     * 
     * @return The binary artifact.
     */
    File getBinArtifact()
    {
        return getNvnProp(NPK_ARTIFACT_BIN);
    }

    /**
     * Gets the project's documentation artifact using the current configuration
     * name and platform type.
     * 
     * @return The project's documentation artifact using the current
     *         configuration name and platform type.
     */
    File getDocArtifact()
    {
        return getNvnProp(NPK_ARTIFACT_DOC);
    }

    /**
     * Gets the project's symbols artifact using the current configuration name
     * and platform type.
     * 
     * @return The project's symbols artifact using the current configuration
     *         name and platform type.
     */
    File getPdbArtifact()
    {
        return getNvnProp(NPK_ARTIFACT_PDB);
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
        if (getMSBuildProject() == null)
        {
            debug("msbuild project is null");
        }

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
        return getNvnProp(NPK_PROJECT);
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
     * Gets a flag indicating whether or not this project is a Visual Studio C++
     * project.
     * 
     * @return A flag indicating whether or not this project is a Visual Studio
     *         C++ project.
     */
    boolean isCppProject()
    {
        return isMSBuildProject()
            && getMSBuildProject().getProjectLanguage() == ProjectLanguageType.CPP;
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
     * Gets a property from the current Maven project's properties collection.
     * 
     * @param <T> The property's type.
     * @param name The property's key.
     * @return The property's value.
     */
    @SuppressWarnings("unchecked")
    <T> T getNvnProp(String key)
    {
        key = NPK_PREFIX + key;

        T value = null;

        nvnPropertiesRWL.readLock().lock();

        Properties nvnProps = getNvnProperties();

        if (nvnProps.containsKey(key))
        {
            value = (T) nvnProps.get(key);
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

    protected Version getNvnVersion()
    {
        return getNvnProp(NPK_VERSION);
    }

    /**
     * Publishes an artifact to TeamCity by emitting a TeamCity message with the
     * relative path to the artifact.
     * 
     * @param file The absolute path to the artifact.
     */
    void publishTeamCityArtifact(File file)
    {
        if (!this.enableTeamCityIntegration || !this.publishTeamCityArtifacts)
        {
            return;
        }

        if (file == null)
        {
            return;
        }

        if (!file.exists())
        {
            debug("not publishing non-existent artifact to teacmcity: %s", file);
        }

        File bd = null;
        MavenProject mp = this.mavenProject;

        while (bd == null)
        {
            if (mp.isExecutionRoot())
            {
                bd = mp.getBasedir();
            }
            else
            {
                mp = mp.getParent();
            }
        }

        String relpath = PathUtils.toRelative(bd, file.toString());
        info("##teamcity[publishArtifacts '%s => .']", relpath);
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
