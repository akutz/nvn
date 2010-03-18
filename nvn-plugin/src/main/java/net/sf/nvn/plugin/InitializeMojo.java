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
import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * A MOJO for initializing the nvn build system.
 * 
 * @author akutz
 * @goal initialize
 * @phase initialize
 * @description A MOJO for initializing the nvn build system.
 * 
 */
public class InitializeMojo extends AbstractNvnMojo
{
    /**
     * <p>
     * The build configuration.
     * </p>
     * 
     * <p>
     * The build configuration is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build configuration is set to the value of
     * {@link #buildConfigDebug}.</li>
     * <li>Otherwise the active build configuration is set to the value of
     * {@link #buildConfigRelease}.</li>
     * </ul>
     * 
     * @parameter
     */
    String buildConfig;

    /**
     * The default build configuration for <strong>Debug</strong> builds.
     * 
     * @parameter default-value="Debug"
     */
    String buildConfigDebug;

    /**
     * The default build configuration for <strong>Release</strong> builds.
     * 
     * @parameter default-value="Release"
     */
    String buildConfigRelease;

    /**
     * <p>
     * The build platform.
     * </p>
     * 
     * <p>
     * The build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build platform is set to the value of {@link #buildPlatformDebug}.
     * </li>
     * <li>Otherwise the active build platform is set to the value of
     * {@link #buildPlatformRelease}.</li>
     * </ul>
     * 
     * @parameter
     */
    String buildPlatform;

    /**
     * The default build platform for <strong>Debug</strong> builds.
     * 
     * @parameter default-value="Any CPU"
     */
    String buildPlatformDebug;

    /**
     * The default build platform for <strong>Release</strong> builds.
     * 
     * @parameter default-value="Any CPU"
     */
    String buildPlatformRelease;

    /**
     * The default version for a .NET project if one is not specified.
     * 
     * @parameter default-value="0.0.0.0"
     */
    String defaultVersion;

    @Override
    String getMojoName()
    {
        return "initialize";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @Override
    void nvnExecute() throws MojoExecutionException
    {
        initVersion();
        initMSBuildProject();
        initBuildConfig();
        initBuildPlatform();
        initArtifacts();
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    /**
     * Initializes this project's version.
     */
    void initVersion()
    {
        String myVersion = super.mavenProject.getVersion();

        if (myVersion.equals(MavenProject.EMPTY_PROJECT_VERSION))
        {
            super.mavenProject.setVersion(this.defaultVersion);
        }
    }

    /**
     * Initializes this project's artifacts:
     * <ul>
     * <li>The assembly</li>
     * <li>The assembly's debug symbols</li>
     * <li>The assembly's XML documentation</li>
     * </ul>
     * 
     * @throws MojoExecutionException
     */
    void initArtifacts() throws MojoExecutionException
    {
        if (!isMSBuildProject())
        {
            debug("not initializing artifacts because msbuildProject is null");
            return;
        }

        File basedir = this.mavenProject.getBasedir();

        String bcn = getBuildConfigName();

        String filepath = getMSBuildProject().getBuildArtifact(bcn).getPath();
        File file = new File(basedir, filepath);

        Artifact artifact =
            this.factory.createBuildArtifact(
                this.mavenProject.getGroupId(),
                this.mavenProject.getArtifactId(),
                this.mavenProject.getVersion(),
                this.mavenProject.getPackaging());

        NvnArtifactMetadata nmd;

        try
        {
            String assemblyName = getMSBuildProject().getAssemblyName();
            nmd = NvnArtifactMetadata.instance(artifact, assemblyName);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error creating NvnArtifactMetadata",
                e);
        }

        artifact.addMetadata(nmd);
        artifact.setFile(file);
        debug("set artifact file %s", file);

        this.mavenProject.setArtifact(artifact);

        File pdbFile = getMSBuildProject().getBuildSymbolsArtifact(bcn);
        if (pdbFile != null)
        {
            filepath = pdbFile.getPath();
            pdbFile = new File(basedir, filepath);

            if (pdbFile.exists())
            {
                this.projectHelper.attachArtifact(
                    this.mavenProject,
                    "pdb",
                    "sources",
                    pdbFile);
            }
        }

        File docFile = getMSBuildProject().getBuildDocumentationArtifact(bcn);
        if (docFile != null)
        {
            filepath = docFile.getPath();
            docFile = new File(basedir, filepath);

            if (docFile.exists())
            {
                this.projectHelper.attachArtifact(
                    this.mavenProject,
                    "xml",
                    "dotnetdoc",
                    docFile);
            }
        }
    }

    /**
     * <p>
     * Initializes the build configuration.
     * </p>
     * 
     * <p>
     * The build configuration is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build configuration is set to the value of
     * {@link #buildConfigDebug}.</li>
     * <li>Otherwise the active build configuration is set to the value of
     * {@link #buildConfigRelease}.</li>
     * </ul>
     */
    void initBuildConfig()
    {
        if (StringUtils.isNotEmpty(this.buildConfig))
        {
            setBuildConfigName(this.buildConfig);
        }
        else if (this.mavenProject.getVersion().contains("-SNAPSHOT"))
        {
            setBuildConfigName(this.buildConfigDebug);
        }
        else
        {
            setBuildConfigName(this.buildConfigRelease);
        }

        debug("build configuration: %s", getBuildConfigName());
    }

    /**
     * <p>
     * Initializes the build platform.
     * </p>
     * 
     * <p>
     * The build platform is determined by the following steps:
     * </p>
     * 
     * <ul>
     * <li>If the version contains <strong><em>-SNAPSHOT</em></strong> then the
     * active build platform is set to the value of {@link #buildPlatformDebug}.
     * </li>
     * <li>Otherwise the active build platform is set to the value of
     * {@link #buildPlatformRelease}.</li>
     * </ul>
     * 
     */
    void initBuildPlatform()
    {
        if (StringUtils.isNotEmpty(this.buildPlatform))
        {
            setBuildPlatform(this.buildPlatform);
        }
        else if (super.mavenProject.getVersion().contains("-SNAPSHOT"))
        {
            setBuildPlatform(this.buildPlatformDebug);
        }
        else
        {
            setBuildPlatform(this.buildPlatformRelease);
        }

        debug("build platform:      %s", getBuildPlatform());
    }

    @SuppressWarnings("unchecked")
    void initMSBuildProject() throws MojoExecutionException
    {
        Collection files =
            FileUtils.listFiles(this.mavenProject.getBasedir(), new String[]
            {
                "csproj", "vbproj"
            }, false);

        if (files == null)
        {
            return;
        }

        if (files.size() == 0)
        {
            return;
        }

        File projectFile = (File) files.iterator().next();

        try
        {
            setMSBuildProject(MSBuildProject.instance(projectFile));
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(String.format(
                "Error reading MSBuild project from %s",
                projectFile), e);
        }
    }
}
