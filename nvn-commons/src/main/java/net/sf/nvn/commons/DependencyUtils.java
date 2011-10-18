/*******************************************************************************
 * Copyright (c) 2010, Schley Andrew Kutz
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Schley Andrew Kutz nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
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

        File pdbFile = new File(parent, filename + "-sources.pdb");
        File pdbFile2 = new File(parent, assemblyName + ".pdb");

        if (pdbFile.exists())
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

        if (docFile.exists())
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
    public static String getAssemblyName(
        ArtifactFactory factory,
        ArtifactRepository localRepository,
        @SuppressWarnings("rawtypes") List remoteRepositories,
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
