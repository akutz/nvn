package net.sf.nvn.plugins.commons;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
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
     * Skip this plug-in. Skip beats force.
     * 
     * @parameter default-value="false"
     */
    boolean skip;

    /**
     * Force the execution of this plug-in even if its requirements are not met.
     * Skip beats force.
     * 
     * default-value="false"
     */
    boolean force;

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

    @Override
    final public void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            getLog().info("nvn-" + getMojoName() + ": skipping execution");
            return;
        }
        
        prepareForExecute();

        if (!this.force && !shouldExecute())
        {
            getLog().info(
                "nvn-" + getMojoName() + ": execution requirements not met");
            return;
        }

        nvnExecute();
    }
}
