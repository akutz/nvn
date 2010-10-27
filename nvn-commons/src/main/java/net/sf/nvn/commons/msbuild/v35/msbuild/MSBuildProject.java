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

package net.sf.nvn.commons.dotnet.v35.msbuild;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;
import net.sf.nvn.commons.dotnet.ProjectOutputType;
import net.sf.nvn.commons.dotnet.v35.msbuild.xsd.Project;
import net.sf.nvn.commons.dotnet.v35.msbuild.xsd.PropertyGroupType;
import net.sf.nvn.commons.dotnet.v35.msbuild.xsd.StringPropertyType;
import org.apache.commons.lang.StringUtils;

/**
 * An MSBuild project.
 * 
 * @author akutz
 * 
 */
public class MSBuildProject
{
    /**
     * Do not allow this class to be constructed with a constructor.
     */
    private MSBuildProject()
    {
    }

    /**
     * Reads a .NET 3.5 MSBuild project file and returns its object
     * representation.
     * 
     * @param projectFile A .NET 3.5 MSBuild project file.
     * @return A 3.5 MSBuild Project object.
     * @throws IOException When an error occurs.
     * @throws JAXBException When an error occurs.
     */
    public static MSBuildProject instance(File projectFile)
        throws IOException,
        JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance(Project.class);
        Unmarshaller um = jc.createUnmarshaller();
        Project p = (Project) um.unmarshal(projectFile);

        MSBuildProject msbp = new MSBuildProject();
        msbp.projectFile = projectFile;
        msbp.projectLanguage = ProjectLanguageType.parse(projectFile);

        Map<String, BuildConfiguration> bcs =
            new HashMap<String, BuildConfiguration>();

        for (Object tag : p.getProjectLevelTagExceptTargetOrImportType())
        {
            if (!(tag instanceof PropertyGroupType))
            {
                continue;
            }

            PropertyGroupType pg = (PropertyGroupType) tag;

            if (StringUtils.isEmpty(pg.getCondition()))
            {
                loadGlobalConfig(pg, msbp);
            }
            else
            {
                BuildConfiguration bc = BuildConfiguration.instance(pg);
                bcs.put(bc.getName(), bc);
            }
        }

        msbp.buildConfigurations = bcs;
        return msbp;
    }

    private static void loadGlobalConfig(
        PropertyGroupType propertyGroup,
        MSBuildProject msbuildProject)
    {
        for (JAXBElement<?> jel : propertyGroup.getProperty())
        {
            String jelName = jel.getName().getLocalPart();
            String jelValu = ((StringPropertyType) jel.getValue()).getValue();

            if (jelName.equals("OutputType"))
            {
                msbuildProject.outputType = ProjectOutputType.parse(jelValu);
            }
            else if (jelName.equals("RootNamespace"))
            {
                msbuildProject.rootNamespace = jelValu;
            }
            else if (jelName.equals("AssemblyName"))
            {
                msbuildProject.assemblyName = jelValu;
            }
            else if (jelName.equals("TargetFrameworkVersion"))
            {
                msbuildProject.targetFrameworkVersion = jelValu;
            }
        }
    }

    /**
     * The project's language.
     */
    private ProjectLanguageType projectLanguage;

    /**
     * The file that this project was created from.
     */
    private File projectFile;

    /**
     * The name of the final output assembly after the project is built.
     */
    private String assemblyName;

    /**
     * Specifies the format of the output file.
     */
    private ProjectOutputType outputType;

    /**
     * The root namespace to use when you name an embedded resource. This
     * namespace is part of the embedded resource manifest name.
     */
    private String rootNamespace;

    /**
     * The version of the .NET Framework that is required to run the application
     * that you are building. Specifying this lets you reference certain
     * framework assemblies that you may not be able to reference otherwise.
     */
    private String targetFrameworkVersion;

    /**
     * The project's build configurations.
     */
    private Map<String, BuildConfiguration> buildConfigurations;

    /**
     * Gets the name of the final output assembly after the project is built.
     * 
     * @return The name of the final output assembly after the project is built.
     */
    public String getAssemblyName()
    {
        return this.assemblyName;
    }

    /**
     * Gets the format of the output file.
     * 
     * @return The format of the output file.
     */
    public ProjectOutputType getOutputType()
    {
        return this.outputType;
    }

    /**
     * Gets the root namespace to use when you name an embedded resource. This
     * namespace is part of the embedded resource manifest name.
     * 
     * @return The root namespace to use when you name an embedded resource.
     *         This namespace is part of the embedded resource manifest name.
     */
    public String getRootNamespace()
    {
        return this.rootNamespace;
    }

    /**
     * Gets the version of the .NET Framework that is required to run the
     * application that you are building. Specifying this lets you reference
     * certain framework assemblies that you may not be able to reference
     * otherwise.
     * 
     * @return The version of the .NET Framework that is required to run the
     *         application that you are building. Specifying this lets you
     *         reference certain framework assemblies that you may not be able
     *         to reference otherwise.
     */
    public String getTargetFrameworkVersion()
    {
        return this.targetFrameworkVersion;
    }

    /**
     * Gets the project's build configurations indexed by name.
     * 
     * @return The project's build configurations indexed by name.
     */
    public Map<String, BuildConfiguration> getBuildConfigurations()
    {
        return this.buildConfigurations;
    }

    /**
     * Gets the project's language.
     * 
     * @return The project's language.
     */
    public ProjectLanguageType getProjectLanguage()
    {
        return this.projectLanguage;
    }

    /**
     * Gets the file that this project was created from.
     * 
     * @return The file that this project was created from.
     */
    public File getProjectFile()
    {
        return this.projectFile;
    }

    /**
     * Gets the artifact created by this build.
     * 
     * @param buildConfigName The name of a build configuration.
     * @return The artifact created by this build.
     */
    public File getBuildArtifact(String buildConfigName)
    {
        BuildConfiguration bc = this.buildConfigurations.get(buildConfigName);

        String extension = "dll";

        if (this.outputType == ProjectOutputType.Exe
            || this.outputType == ProjectOutputType.WinExe)
        {
            extension = "exe";
        }

        File artifact =
            new File(bc.getOutputPath(), String.format(
                "%s.%s",
                this.assemblyName,
                extension));

        return artifact;
    }

    /**
     * Gets the documentation artifact created by this build. If no
     * documentation file was created then a null value is returned.
     * 
     * @param buildConfigName The name of a build configuration.
     * @return The documentation artifact created by this build.
     */
    public File getBuildDocumentationArtifact(String buildConfigName)
    {
        BuildConfiguration bc = this.buildConfigurations.get(buildConfigName);
        return bc.getDocumentationFile();
    }

    /**
     * Gets the build symbols artifact created by this build. If no symbols were
     * created then a null value is returned.
     * 
     * @param buildConfigName The name of a build configuration.
     * @return The build symbols artifact created by this build.
     */
    public File getBuildSymbolsArtifact(String buildConfigName)
    {
        BuildConfiguration bc = this.buildConfigurations.get(buildConfigName);

        File artifact =
            new File(bc.getOutputPath(), String.format(
                "%s.pdb",
                this.assemblyName));

        return artifact;
    }
}
