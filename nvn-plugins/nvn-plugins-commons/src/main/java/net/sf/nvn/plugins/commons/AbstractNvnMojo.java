package net.sf.nvn.plugins.commons;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public abstract class AbstractNvnMojo extends AbstractMojo
{
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

    @SuppressWarnings("unchecked")
    boolean isProject()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "csproj", "vbproj"
            }, false);

        return files != null && files.size() > 0;
    }

    public boolean isSolutionAndProject()
    {
        return isSolution() && isProject();
    }

    abstract void nvnExecute() throws MojoExecutionException;

    abstract void prepareForExecute() throws MojoExecutionException;

    abstract String getMojoName();

    abstract boolean shouldExecute() throws MojoExecutionException;

    abstract boolean isProjectTypeValid();

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

        prepareForExecute();

        if (!this.ignoreExecutionRequirements && !shouldExecute())
        {
            debug("execution requirements not met");
            return;
        }

        nvnExecute();
    }

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

        debug("getPath returned " + path);

        return path;
    }

    String getPath(File file)
    {
        return getPath(file, true);
    }

    String getFullPathFromPath(File file)
    {
        return null;
    }

    void debug(String message)
    {
        getLog().debug("NVN-" + getMojoName() + ": " + message);
    }

    void info(String message)
    {
        getLog().info("NVN-" + getMojoName() + ": " + message);
    }

    void error(String message)
    {
        getLog().error("NVN-" + getMojoName() + ": " + message);
    }
}
