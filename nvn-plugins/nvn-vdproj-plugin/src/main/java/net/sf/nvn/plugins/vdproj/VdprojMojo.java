package net.sf.nvn.plugins.vdproj;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven plug-in for building VisualStudio setup projects.
 * 
 * @author akutz
 * 
 * @goal package
 * @phase package
 * @description A Maven plug-in for building VisualStudio setup projects.
 */
public class VdprojMojo extends AbstractExeMojo
{
    /**
     * The build configuration.
     * 
     * @parameter expression="${vdproj.buildConfiguration}"
     *            default-value="Debug"
     */
    String buildConfiguration;

    /**
     * The vdproj file(s) to build.
     * 
     * @parameter expression="${vdproj.vdprojFiles}"
     */
    File[] vdProjFiles;

    /**
     * The names of the setup project(s).
     */
    String[] projectNames;

    @Override
    String getArgs(int execution)
    {
        StringBuilder cmdLineBuff = new StringBuilder();

        cmdLineBuff.append("/Build");
        cmdLineBuff.append(" ");
        cmdLineBuff.append(quote(this.buildConfiguration));
        cmdLineBuff.append(" ");

        cmdLineBuff.append("/Project");
        cmdLineBuff.append(" ");
        cmdLineBuff.append(quote(this.projectNames[execution]));
        cmdLineBuff.append(" ");

        cmdLineBuff.append(getPath(this.vdProjFiles[execution]));

        String clbs = cmdLineBuff.toString();
        return clbs;
    }

    @Override
    String getMojoName()
    {
        return "vdproj";
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        initVdprojFiles();
        initProjectName();
    }

    /**
     * Initializes the projectName field.
     */
    void initProjectName()
    {
        this.projectNames = new String[this.vdProjFiles.length];

        for (int x = 0; x < this.vdProjFiles.length; ++x)
        {
            this.projectNames[x] =
                FilenameUtils.getBaseName(this.vdProjFiles[x].getName());
        }
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.vdProjFiles != null && this.vdProjFiles.length > 0;
    }

    /**
     * Initializes the vdProjFile field.
     */
    void initVdprojFiles()
    {
        if (this.vdProjFiles == null)
        {
            this.vdProjFiles = findVdprojFiles();
        }
    }

    /**
     * Finds the vdproj file(s).
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    File[] findVdprojFiles()
    {
        Collection vdprojFiles =
            FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
            {
                "vdproj"
            }, true);

        if (vdprojFiles == null)
        {
            return new File[0];
        }

        File[] files = new File[vdprojFiles.size()];

        int x = 0;
        for (Object of : vdprojFiles)
        {
            files[x] = (File) of;
            ++x;
        }

        return files;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @Override
    File getDefaultCommand()
    {
        return new File("devenv.exe");
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }
}