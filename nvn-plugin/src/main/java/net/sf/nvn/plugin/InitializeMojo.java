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
import net.sf.nvn.commons.Version;
import net.sf.nvn.commons.msbuild.MSBuildProject;
import net.sf.nvn.commons.msbuild.ProjectLanguageType;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * A MOJO for initializing the nvn build system.
 * 
 * @author akutz
 * 
 * @goal initialize
 * @phase initialize
 * @description A MOJO for initializing the nvn build system.
 * 
 */
public class InitializeMojo extends AbstractNvnMojo
{
    /**
     * <p>
     * The MSBuild project file to use.
     * </p>
     * 
     * @parameter
     */
    File msbuildProjectFile;

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
     * @parameter default-value="AnyCPU"
     */
    String buildPlatformDebug;

    /**
     * The default build platform for <strong>Release</strong> builds.
     * 
     * @parameter default-value="AnyCPU"
     */
    String buildPlatformRelease;

    /**
     * The default version for a .NET project if one is not specified.
     * 
     * @parameter default-value="0.0.0.0"
     */
    String defaultVersion;

    /**
     * <p>
     * Setting this parameter to true causes this mojo to read the BUILD_NUMBER
     * environment variable set by TeamCity and replace the number of
     * significant digits in the project's version that the BUILD_NUMBER is
     * composed of from right to left.
     * </p>
     * <p>
     * For example, if the project's version is "4.0.0-SNAPSHOT" and the value
     * of BUILD_NUMBER is "42" then the NVN version will be set to "4.0.0.42".
     * </p>
     * <p>
     * If though the BUILD_NUMBER value is "0.42" then the NVN version will be
     * set to "4.0.0.42".
     * </p>
     * 
     * @parameter default-value="true"
     */
    boolean enableTeamCityBuildNumber;

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
        initNvnVersion();
        initMSBuildProject();
        initBuildConfig();
        initBuildPlatform();
        initBuildDirectory();
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
     * Initializes the Maven project's build directory with the MSBuild
     * project's build directory.
     */
    void initBuildDirectory()
    {
        File relBuildDir =
            getMSBuildProject().getBuildDir(
                getBuildConfig(),
                getBuildPlatform());
        initNvnProp(NPK_BUILD_DIR, relBuildDir);
        info("Initialized build directory (relative): " + relBuildDir);

        File baseDir = super.mavenProject.getBasedir();
        File absBuildDir = new File(baseDir, relBuildDir.toString());
        super.mavenProject.getBuild().setDirectory(absBuildDir.toString());
        info("Initialized build directory (absolute): " + absBuildDir);
    }

    /**
     * Initializes this project's version.
     */
    void initVersion()
    {
        debug("initializing version");

        String myVersion = super.mavenProject.getVersion();

        if (myVersion.equals(MavenProject.EMPTY_PROJECT_VERSION))
        {
            super.mavenProject.setVersion(this.defaultVersion);
        }

        info("initialized version: " + myVersion);
    }

    /**
     * Initializes the numeric version from the project's version.
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void initNvnVersion() throws MojoExecutionException
    {
        Version v;

        try
        {
            v = Version.parse(super.mavenProject.getVersion());
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error parsing version from "
                + super.mavenProject.getVersion());
        }

        if (super.enableTeamCityIntegration && this.enableTeamCityBuildNumber)
        {
            String envVarBuildNum = System.getenv("BUILD_NUMBER");

            if (StringUtils.isNotEmpty(envVarBuildNum))
            {
                Version buildNumVer;

                try
                {
                    buildNumVer = Version.parse(envVarBuildNum);
                }
                catch (Exception e)
                {
                    throw new MojoExecutionException(
                        "Error parsing version from " + envVarBuildNum);
                }

                switch (buildNumVer.getNumberOfComponents())
                {
                    case 1 :
                    {
                        v.setRevision(buildNumVer.getMajor());
                        break;
                    }
                    case 2 :
                    {
                        v.setBuild(buildNumVer.getMajor());
                        v.setRevision(buildNumVer.getMinor());
                        break;
                    }
                    case 3 :
                    {
                        v.setMinor(buildNumVer.getMajor());
                        v.setBuild(buildNumVer.getMinor());
                        v.setRevision(buildNumVer.getBuild());
                        break;
                    }
                    case 4 :
                    {
                        v.setMajor(buildNumVer.getMajor());
                        v.setMinor(buildNumVer.getMinor());
                        v.setBuild(buildNumVer.getBuild());
                        v.setRevision(buildNumVer.getRevision());
                        break;
                    }
                }
            }
        }

        initNvnProp(NPK_VERSION, v);
        initNvnProp(NPK_VERSION_1, v.toString(1));
        initNvnProp(NPK_VERSION_2, v.toString(2));
        initNvnProp(NPK_VERSION_3, v.toString(3));

        info("initialized nvn version: " + v);
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

        File bd = this.mavenProject.getBasedir();
        String bc = getBuildConfig();
        String bp = getBuildPlatform();

        String artName = getMSBuildProject().getArtifactName(bc, bp);
        initNvnProp(NPK_ARTIFACT_NAME, artName);
        info("artifact name: " + artName);

        // Create the binary artifact.
        Artifact artBin =
            this.factory.createBuildArtifact(
                this.mavenProject.getGroupId(),
                this.mavenProject.getArtifactId(),
                this.mavenProject.getVersion(),
                this.mavenProject.getPackaging());

        // Initialize the nvn artifact file.
        try
        {
            File artNvnFile = getNvnFile(artBin, artName);

            if (artNvnFile != null)
            {
                initNvnProp(NPK_ARTIFACT_NVN, artNvnFile);
                this.projectHelper.attachArtifact(
                    this.mavenProject,
                    "nvn",
                    artNvnFile);
                info("initialized nvn artifact: %s", artNvnFile);
            }
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error creating NvnNvnArtifact",
                e);
        }

        // Get the binary artifact's file.
        File artBinFile =
            new File(bd, getMSBuildProject().getBinArtifact(bc, bp).toString());
        artBin.setFile(artBinFile);
        this.mavenProject.setArtifact(artBin);
        initNvnProp(NPK_ARTIFACT_BIN, artBinFile);
        info("initialized bin artifact: %s", artBinFile);

        File artPdbFile =
            new File(bd, getMSBuildProject().getPdbArtifact(bc, bp).toString());

        if (artPdbFile != null)
        {
            initNvnProp(NPK_ARTIFACT_PDB, artPdbFile);
            info("initialized pdb artifact: %s", artPdbFile);
        }

        File artDocFile = getMSBuildProject().getDocArtifact(bc, bp);
        if (artDocFile != null)
        {
            artDocFile = new File(bd, artDocFile.toString());
            initNvnProp(NPK_ARTIFACT_DOC, artDocFile);
            info("initialized doc artifact: %s", artDocFile);
        }
    }
    
    static File getNvnFile(Artifact artBin, String assemblyName) throws IOException
    {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        String fileName = artBin.getArtifactId() + "-" + artBin.getVersion() + ".nvn";
        File file = new File(tmpdir, fileName);
        FileUtils.writeStringToFile(file, assemblyName);
        return file;
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
            initNvnProp(NPK_CONFIG, this.buildConfig);
        }
        else if (this.mavenProject.getVersion().endsWith("-SNAPSHOT"))
        {
            initNvnProp(NPK_CONFIG, this.buildConfigDebug);
        }
        else
        {
            initNvnProp(NPK_CONFIG, this.buildConfigRelease);
        }

        info("build configuration: %s", getBuildConfig());
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
    void initBuildPlatform() throws MojoExecutionException
    {
        if (StringUtils.isNotEmpty(this.buildPlatform))
        {
            initNvnProp(NPK_PLATFORM, this.buildPlatform);
        }
        else if (this.mavenProject.getVersion().endsWith("-SNAPSHOT"))
        {
            initNvnProp(NPK_PLATFORM, this.buildPlatformDebug);
        }
        else
        {
            initNvnProp(NPK_PLATFORM, this.buildPlatformRelease);
        }

        info("build platform:      %s", getBuildPlatform());
    }

    @SuppressWarnings("rawtypes")
    void initMSBuildProject() throws MojoExecutionException
    {
        if (this.msbuildProjectFile == null)
        {
            Collection files =
                FileUtils.listFiles(
                    this.mavenProject.getBasedir(),
                    new String[]
                    {
                        "csproj", "vbproj", "vcxproj"
                    },
                    false);

            if (files == null)
            {
                debug("project file list is null");
                return;
            }

            if (files.size() == 0)
            {
                debug("project file list is empty");
                return;
            }

            this.msbuildProjectFile = (File) files.iterator().next();
        }

        try
        {
            MSBuildProject msb =
                MSBuildProject.instance(this.msbuildProjectFile);

            if (msb == null)
            {
                info("project file could not be unmarshalled: "
                    + this.msbuildProjectFile);
            }
            else
            {
                initNvnProp(NPK_PROJECT, msb);
                info("initialized msproject");

                // If this is a C++ project then adjust the default build
                // platforms from 'AnyCPU' to 'Win32'.
                if (msb.getProjectLanguage() == ProjectLanguageType.CPP)
                {
                    if (this.buildPlatformDebug.equals("AnyCPU"))
                    {
                        this.buildPlatformDebug = "Win32";
                    }

                    if (this.buildPlatformRelease.equals("AnyCPU"))
                    {
                        this.buildPlatformRelease = "Win32";
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(String.format(
                "Error reading MSBuild project from %s",
                this.msbuildProjectFile), e);
        }
    }
}
