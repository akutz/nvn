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
 * A MOJO for generating the original .NET dependency files from the Mavenized
 * versions.
 * 
 * @author akutz
 * 
 * @goal generate-dependencies
 * @requiresProject false
 * @description A MOJO for generating the original .NET dependency files from
 *              the Mavenized versions.
 */
public class GenerateDependenciesMojo extends AbstractMojo
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

    @SuppressWarnings("rawtypes")
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

    @SuppressWarnings("rawtypes")
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

    @SuppressWarnings("rawtypes")
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
