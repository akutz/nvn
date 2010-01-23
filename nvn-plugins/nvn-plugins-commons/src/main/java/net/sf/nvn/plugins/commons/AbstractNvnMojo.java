package net.sf.nvn.plugins.commons;

import static org.apache.commons.exec.util.StringUtils.quoteArgument;
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
    public boolean isSolution()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "sln"
            }, false);

        return files != null && files.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public boolean isVdprojProject()
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "vdproj"
            }, false);

        return files != null && files.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public boolean isProject()
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

    @SuppressWarnings("unchecked")
    public boolean isModularProject()
    {
        File parentDir = this.mavenProject.getBasedir().getParentFile();

        if (parentDir == null)
        {
            return false;
        }

        Collection files = FileUtils.listFiles(parentDir, new String[]
        {
            "sln"
        }, false);

        return files != null && files.size() > 0;
    }

    abstract public void nvnExecute() throws MojoExecutionException;

    abstract public void prepareForExecute() throws MojoExecutionException;

    abstract public String getMojoName();

    abstract public boolean shouldExecute() throws MojoExecutionException;

    abstract public boolean isProjectTypeValid();

    @Override
    final public void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            getLog().info("nvn-" + getMojoName() + ": skipping execution");
            return;
        }

        if (!this.ignoreProjectType && !isProjectTypeValid())
        {
            return;
        }

        prepareForExecute();

        if (!this.ignoreExecutionRequirements && !shouldExecute())
        {
            getLog().debug(
                "nvn-" + getMojoName() + ": execution requirements not met");
            return;
        }

        nvnExecute();
    }

    public String getPath(File file)
    {
        return getPath(file.getName());
    }

    public String getPath(String fileName)
    {
        return quoteArgument(mavenProject.getBasedir() + "\\" + fileName);
    }
}
