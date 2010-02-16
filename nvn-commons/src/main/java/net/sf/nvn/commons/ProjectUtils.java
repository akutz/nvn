package net.sf.nvn.commons;

import java.io.File;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

/**
 * A utility class for working with Maven projects.
 * 
 * @author akutz
 * 
 */
public class ProjectUtils
{
    /**
     * Reads a project file and builds a maven project.
     * 
     * @param builder The maven project builder.
     * @param localRepository The local repository.
     * @param profileManager The global profile manager.
     * @param projectFile The project file.
     * @param resolveDependencies Whether or not to resolve dependencies.
     * @return A maven project.
     * @throws MojoExecutionException When an error occurs.
     */
    public static MavenProject readProjectFile(
        MavenProjectBuilder builder,
        ArtifactRepository localRepository,
        ProfileManager profileManager,
        File projectFile,
        boolean resolveDependencies) throws MojoExecutionException
    {
        try
        {
            MavenProject project;

            if (resolveDependencies)
            {
                project =
                    builder.buildWithDependencies(
                        projectFile,
                        localRepository,
                        profileManager);
            }
            else
            {
                project =
                    builder.build(projectFile, localRepository, profileManager);
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
}
