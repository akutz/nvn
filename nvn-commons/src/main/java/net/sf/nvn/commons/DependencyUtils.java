package net.sf.nvn.commons;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A utility class for working with Dependencies.
 * 
 * @author akutz
 * 
 */
public class DependencyUtils
{
    /**
     * Copies the dependency file (and its associated pdb or XML documentation
     * files) to the original file name using the AssemblyName.
     * 
     * @param depFile The dependency file.
     * @param assemblyName The AssemblyName.
     * @throws MojoExecutionException When an error occurs.
     */
    public static boolean copyToAssemblyNamedFiles(
        File depFile,
        String assemblyName) throws MojoExecutionException
    {
        if (depFile == null)
        {
            return false;
        }

        if (StringUtils.isEmpty(assemblyName))
        {
            return false;
        }

        String filename = FilenameUtils.getBaseName(depFile.getName());
        String ext = FilenameUtils.getExtension(depFile.getName());
        File parent = depFile.getParentFile();

        File depFile2 =
            new File(parent, String.format("%s.%s", assemblyName, ext));

        if (!depFile2.exists())
        {
            try
            {
                FileUtils.copyFile(depFile, depFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    depFile,
                    depFile2), e);
            }
        }

        File pdbFile = new File(parent, filename + "-sources.pdb");
        File pdbFile2 = new File(parent, assemblyName + ".pdb");

        if (pdbFile.exists() && !pdbFile2.exists())
        {
            try
            {
                FileUtils.copyFile(pdbFile, pdbFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    pdbFile,
                    pdbFile2), e);
            }
        }

        File docFile = new File(parent, filename + "-dotnetdoc.xml");
        File docFile2 = new File(parent, assemblyName + ".xml");

        if (docFile.exists() && !docFile2.exists())
        {
            try
            {
                FileUtils.copyFile(docFile, docFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    docFile,
                    docFile2), e);
            }
        }

        return true;
    }

    /**
     * Gets the dependency's artifact file.
     * 
     * @param dependency The dependency.
     * @return The dependency's artifact file.
     */
    public static File getArtifactFile(
        ArtifactFactory factory,
        ArtifactRepository localRepository,
        Dependency dependency)
    {
        if (factory == null || localRepository == null || dependency == null)
        {
            return null;
        }

        Artifact artifact =
            factory.createDependencyArtifact(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                VersionRange.createFromVersion(dependency.getVersion()),
                dependency.getType(),
                dependency.getClassifier(),
                dependency.getScope());

        String path = localRepository.pathOf(artifact);

        File f = new File(localRepository.getBasedir(), path);

        return f;
    }

    /**
     * Gets the AssemblyName from the dependency. This method should only be
     * called for dependencies of type "dll" or "exe".
     * 
     * @param dependency The dependency.
     * @return The AssemblyName.
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    public static String getAssemblyName(
        ArtifactFactory factory,
        ArtifactRepository localRepository,
        List remoteRepositories,
        ArtifactResolver resolver,
        Dependency dependency) throws MojoExecutionException
    {
        Artifact nvnMetadataArtifact =
            factory.createArtifact(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion(),
                Artifact.SCOPE_COMPILE,
                "nvn");

        try
        {
            resolver.resolve(
                nvnMetadataArtifact,
                remoteRepositories,
                localRepository);
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoExecutionException(
                "Error resolving nvn metadata artifact",
                e);
        }
        catch (ArtifactNotFoundException e)
        {
            throw new MojoExecutionException(
                "Error finding nvn metadata artifact",
                e);
        }

        String assemblyName;
        String nvnFilePath = localRepository.pathOf(nvnMetadataArtifact);
        File nvnFile = new File(localRepository.getBasedir(), nvnFilePath);

        try
        {
            assemblyName = FileUtils.readFileToString(nvnFile);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(String.format(
                "Error loading nvn metadata artifact %s",
                nvnFile), e);
        }

        return assemblyName;
    }
}
