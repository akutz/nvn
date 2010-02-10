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
}
