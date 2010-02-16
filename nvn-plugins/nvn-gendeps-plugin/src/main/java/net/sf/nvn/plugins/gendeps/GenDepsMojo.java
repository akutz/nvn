package net.sf.nvn.plugins.gendeps;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.sf.nvn.commons.DependencyUtils;
import net.sf.nvn.commons.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

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

    /**
     * Used to build maven project files.
     * 
     * @component
     */
    MavenProjectBuilder builder;

    /**
     * The artifact factory.
     * 
     * @component
     */
    ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @component
     */
    ArtifactResolver resolver;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject mavenProject;

    /**
     * The pom file to generate the original .NET dependencies for.
     * 
     * @parameter expression="${pomFile}"
     */
    File pomFile;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (this.pomFile == null)
        {
            enumLocalRepo();
            return;
        }

        if (!this.pomFile.exists())
        {
            throw new MojoExecutionException(String.format(
                "The specified pom file does not exist: %s",
                this.pomFile));
        }

        MavenProject project =
            ProjectUtils.readProjectFile(
                builder,
                localRepository,
                this.mavenProject
                    .getProjectBuilderConfiguration()
                    .getGlobalProfileManager(),
                this.pomFile,
                true);

        Collection deps = project.getDependencies();

        if (deps == null)
        {
            getLog().info("Pom file has no detected dependencies");
            return;
        }

        for (Object od : deps)
        {
            Dependency d = (Dependency) od;

            File f =
                DependencyUtils.getArtifactFile(
                    this.factory,
                    this.localRepository,
                    d);

            String assemblyName =
                DependencyUtils.getAssemblyName(
                    this.factory,
                    this.localRepository,
                    this.mavenProject.getRemoteArtifactRepositories(),
                    this.resolver,
                    d);

            if (DependencyUtils.copyToAssemblyNamedFiles(f, assemblyName))
            {
                getLog().info("Processsed: " + assemblyName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void enumLocalRepo() throws MojoExecutionException
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