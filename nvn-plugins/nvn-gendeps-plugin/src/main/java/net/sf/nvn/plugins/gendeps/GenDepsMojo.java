package net.sf.nvn.plugins.gendeps;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.sf.nvn.commons.DependencyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Maven plug-in for generating the original .NET dependency files from the
 * Mavenized versions.
 * 
 * @author akutz
 * 
 * @goal generate-dependencies
 * @requiresProject false
 * @description A Maven plug-in for generating the original .NET dependency
 *              files from the Mavenized versions.
 */
public class GenDepsMojo extends AbstractMojo
{
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File localRepoDir = new File(this.localRepository.getBasedir());

        Collection nvnFiles = FileUtils.listFiles(localRepoDir, new String[]
        {
            "nvn"
        }, true);

        if (nvnFiles == null)
        {
            return;
        }

        for (Object onf : nvnFiles)
        {
            File nvnFile = (File) onf;
            File basedir = nvnFile.getParentFile();
            File artifactFile = getArtifactFile(basedir);
            String assemblyName = getAssemblyName(nvnFile);
            if (DependencyUtils.copyToAssemblyNamedFiles(
                artifactFile,
                assemblyName))
            {
                getLog().info("Processed: " + assemblyName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    File getArtifactFile(File dir)
    {
        Collection files = FileUtils.listFiles(dir, new String[]
        {
            "dll", "exe"
        }, false);

        if (files == null)
        {
            return null;
        }

        if (files.size() == 0)
        {
            return null;
        }

        return (File) files.iterator().next();
    }

    String getAssemblyName(File nvnFile) throws MojoExecutionException
    {
        try
        {
            return FileUtils.readFileToString(nvnFile);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(String.format(
                "Error getting assembly name from %s",
                nvnFile), e);
        }
    }
}