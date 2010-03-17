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
