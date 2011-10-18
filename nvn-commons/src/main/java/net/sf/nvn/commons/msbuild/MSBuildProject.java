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

package net.sf.nvn.commons.msbuild;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.sf.nvn.commons.msbuild.xsd.ItemDefinitionGroupType;
import net.sf.nvn.commons.msbuild.xsd.ItemGroupType;
import net.sf.nvn.commons.msbuild.xsd.LinkItem;
import net.sf.nvn.commons.msbuild.xsd.Project;
import net.sf.nvn.commons.msbuild.xsd.ProjectReference;
import net.sf.nvn.commons.msbuild.xsd.PropertyGroupType;
import net.sf.nvn.commons.msbuild.xsd.SimpleItemType;
import net.sf.nvn.commons.msbuild.xsd.StringPropertyType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 * An MSBuild project.
 * 
 * @author akutz
 * 
 */
public class MSBuildProject
{
    /**
     * The pattern to match a property group's configuration attribute value.
     */
    private static Pattern CONDITION_PATT =
        Pattern
            .compile("^\\s?'\\$\\(Configuration\\)\\|\\$\\(Platform\\)'\\s?==\\s?'(.*)\\|(.*)'\\s?$");

    @Override
    public String toString()
    {
        return this.file.toString();
    }

    /**
     * Reads a MSBuild project file and returns its object representation.
     * 
     * @param projectFile A MSBuild project file.
     * @return A MSBuild Project object.
     * @throws IOException When an error occurs.
     * @throws JAXBException When an error occurs.
     */
    public static MSBuildProject instance(File projectFile)
        throws IOException,
        JAXBException
    {
        MSBuildProject msbp = new MSBuildProject();
        msbp.projectLanguage = ProjectLanguageType.parse(projectFile);

        // If the project language was unrecognized then throw an IOException.
        if (msbp.projectLanguage == null)
        {
            throw new IOException(String.format(
                "Error reading file '%s'. Unrecognized extension.",
                projectFile));
        }

        // Deserialize the project file.
        JAXBContext jc = JAXBContext.newInstance(Project.class);
        Unmarshaller um = jc.createUnmarshaller();
        msbp.project = (Project) um.unmarshal(projectFile);

        // Retain a reference to the project file.
        msbp.file = projectFile;

        switch (msbp.projectLanguage)
        {
            case CSharp :
            case VisualBasic :
            {
                processCSharpOrVBProject(msbp);
                break;
            }
            case CPP :
            {
                processCppProject(msbp);
                break;
            }
        }

        return msbp;
    }

    private static void processCSharpOrVBProject(MSBuildProject msbp)
    {
        List<Object> tags =
            msbp.project.getProjectLevelTagExceptTargetOrImportType();

        for (Object tag : tags)
        {
            if (tag instanceof PropertyGroupType)
            {
                PropertyGroupType pg = (PropertyGroupType) tag;
                processCSharpOrVBProjectPropertyGroup(msbp, pg);
            }
            else if (tag instanceof ItemGroupType)
            {
                ItemGroupType ig = (ItemGroupType) tag;
                loadProjectReferences(ig, msbp);
            }
        }
    }

    private static void processCSharpOrVBProjectPropertyGroup(
        MSBuildProject msbp,
        PropertyGroupType pg)
    {
        if (StringUtils.isEmpty(pg.getCondition()))
        {
            processCSharpOrVBProjectGlobalProps(pg, msbp);
        }
        else
        {
            ProjectInfo pi = msbp.getProjectInfo(pg.getCondition());

            for (JAXBElement<?> jel : pg.getProperty())
            {
                String jelName = jel.getName().getLocalPart();
                String jelValu;

                if (jel.getValue() instanceof StringPropertyType)
                {
                    jelValu = ((StringPropertyType) jel.getValue()).getValue();
                }
                else if (jel.getValue() instanceof String)
                {
                    jelValu = (String) jel.getValue();
                }
                else
                {
                    jelValu = "";
                }

                if (jelName.equals("OutputPath"))
                {
                    pi.setOutputDirPath(jelValu);
                }
                else if (jelName.equals("DocumentationFile"))
                {
                    pi.setDocFilePath(jelValu);
                }
            }
        }
    }

    private static void processCppProject(MSBuildProject msbp)
    {
        List<Object> tags =
            msbp.project.getProjectLevelTagExceptTargetOrImportType();

        for (Object tag : tags)
        {
            if (tag instanceof PropertyGroupType)
            {
                PropertyGroupType pg = (PropertyGroupType) tag;

                String label = pg.getLabel();

                if (StringUtils.isEmpty(label))
                {
                    processCppProjectAllConfigs(msbp, pg);
                }
                else if (label.equals("Globals"))
                {
                    processCppProjectGlobals(msbp, pg);
                }
                else if (label.equals("Configuration"))
                {
                    processCppProjectConfig(msbp, pg);
                }
            }
            else if (tag instanceof ItemDefinitionGroupType)
            {
                ItemDefinitionGroupType idg = (ItemDefinitionGroupType) tag;
                processCppProjectLibOutputFile(msbp, idg);
            }
        }
    }

    private static void processCppProjectGlobals(
        MSBuildProject msbp,
        PropertyGroupType pg)
    {
        for (JAXBElement<?> jel : pg.getProperty())
        {
            String jelName = jel.getName().getLocalPart();
            String jelValu = ((StringPropertyType) jel.getValue()).getValue();

            if (jelName.equals("RootNamespace"))
            {
                msbp.rootNamespace = jelValu;
            }
        }
    }

    private static void processCppProjectConfig(
        MSBuildProject msbp,
        PropertyGroupType pg)
    {
        ProjectInfo pi = msbp.getProjectInfo(pg.getCondition());

        for (JAXBElement<?> jel : pg.getProperty())
        {
            String jelName = jel.getName().getLocalPart();
            String jelValu = ((StringPropertyType) jel.getValue()).getValue();

            if (jelName.equals("ConfigurationType"))
            {
                pi.setType(ProjectType.parse(jelValu));
            }
        }
    }

    private static void processCppProjectAllConfigs(
        MSBuildProject msbp,
        PropertyGroupType pg)
    {
        for (JAXBElement<?> jel : pg.getProperty())
        {
            if (!(jel.getValue() instanceof StringPropertyType))
            {
                continue;
            }

            StringPropertyType spt = (StringPropertyType) jel.getValue();

            String jelName = jel.getName().getLocalPart();
            String jelValu = spt.getValue();
            String jelCond = spt.getCondition();

            if (jelName.equals("OutDir"))
            {
                msbp.getProjectInfo(jelCond).setOutputDirPath(jelValu);
            }
            else if (jelName.equals("TargetName"))
            {
                msbp.getProjectInfo(jelCond).setTargetName(jelValu);
            }
            else if (jelName.equals("TargetExtension"))
            {
                msbp.getProjectInfo(jelCond).setTargetExtension(jelValu);
            }
        }
    }

    private static void processCppProjectLibOutputFile(
        MSBuildProject msbp,
        ItemDefinitionGroupType idg)
    {
        ProjectInfo pi = msbp.getProjectInfo(idg.getCondition());

        List<JAXBElement<? extends SimpleItemType>> tags =
            idg.getItemOrLinkOrLib();

        for (JAXBElement<?> tag : tags)
        {
            String jelName = tag.getName().getLocalPart();

            if (jelName.equals("Link") || jelName.equals("Lib"))
            {
                LinkItem lib = (LinkItem) tag.getValue();

                List<JAXBElement<Object>> libChildTags =
                    lib.getAdditionalDependenciesOrOutputFileOrAssemblyDebug();

                for (JAXBElement<Object> libChildTag : libChildTags)
                {
                    if (libChildTag
                        .getName()
                        .getLocalPart()
                        .equals("OutputFile"))
                    {
                        Element el = (Element) libChildTag.getValue();
                        String elText = el.getTextContent();
                        pi.setOutputFilePath(elText);
                    }
                }
            }
        }
    }

    private ProjectInfo getProjectInfo(String condition)
    {
        String[] cap = parseConfigAndPlatform(condition);
        String cfg = cap[0];
        String pla = cap[1];
        return getProjectInfo(cfg, pla);
    }

    private static String[] parseConfigAndPlatform(String toParse)
    {
        String[] arr = new String[2];

        Matcher matt = CONDITION_PATT.matcher(toParse);
        matt.matches();

        arr[0] = matt.group(1);
        arr[1] = matt.group(2);

        return arr;
    }

    private static void loadProjectReferences(
        ItemGroupType itemGroup,
        MSBuildProject msbuildProject)
    {
        List<JAXBElement<? extends SimpleItemType>> tags =
            itemGroup.getItemOrLinkOrResourceCompile();

        for (JAXBElement<? extends SimpleItemType> tag : tags)
        {
            if (!(tag.getDeclaredType() == ProjectReference.class))
            {
                continue;
            }

            ProjectReference pr = (ProjectReference) tag.getValue();

            String name = "";

            for (JAXBElement<?> child : pr
                .getNameOrProjectOrReferenceOutputAssembly())
            {
                if (child.getName().getLocalPart().equals("Name"))
                {
                    Element el = (Element) child.getValue();
                    name = el.getTextContent();
                }
            }

            msbuildProject.projectReferences.put(pr.getInclude(), name);
        }
    }

    private static void processCSharpOrVBProjectGlobalProps(
        PropertyGroupType pg,
        MSBuildProject msbp)
    {
        for (JAXBElement<?> jel : pg.getProperty())
        {
            String jelName = jel.getName().getLocalPart();
            String jelValu = ((StringPropertyType) jel.getValue()).getValue();

            if (jelName.equals("OutputType"))
            {
                msbp.type = ProjectType.parse(jelValu);
            }
            else if (jelName.equals("RootNamespace"))
            {
                msbp.rootNamespace = jelValu;
            }
            else if (jelName.equals("AssemblyName"))
            {
                msbp.assemblyName = jelValu;
            }
            else if (jelName.equals("TargetFrameworkVersion"))
            {
                msbp.targetFrameworkVersion = jelValu;
            }
        }
    }

    /**
     * A map of the project's build artifacts. The map's key is the build
     * configuration (Debug, Release, etc.). The key returns a second map for
     * which the key is the platform type (AnyCPU, Win32, x64, etc.). The
     * ultimate value is the build artifacts object for that build configuration
     * / platform pair.
     */
    private Map<String, Map<String, ProjectInfo>> projectInfos =
        new HashMap<String, Map<String, ProjectInfo>>();

    /**
     * The deserialized project.
     */
    private Project project;

    /**
     * The project's language.
     */
    private ProjectLanguageType projectLanguage;

    /**
     * The underlying project file.
     */
    private File file;

    /**
     * The name of the final output assembly after the project is built.
     */
    private String assemblyName;

    /**
     * The project's type.
     */
    private ProjectType type;

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
     * The default build configuration name.
     */
    //private String defaultConfig;

    /**
     * The default build platform type.
     */
    //private String defaultPlatform;

    /**
     * The project's project references.
     */
    private Map<String, String> projectReferences =
        new HashMap<String, String>();

    /**
     * Gets the project's project references.
     * 
     * @return The project's project references.
     */
    public Map<String, String> getProjectReferences()
    {
        return this.projectReferences;
    }

    ProjectType getType()
    {
        return this.type;
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
     * Gets the project's language.
     * 
     * @return The project's language.
     */
    public ProjectLanguageType getProjectLanguage()
    {
        return this.projectLanguage;
    }

    /**
     * Gets the project file.
     * 
     * @return The project file.
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * Gets the root namespace.
     * 
     * @return The root namespace.
     */
    String getRootNamespace()
    {
        return this.rootNamespace;
    }

    /**
     * Gets the assembly name.
     * 
     * @return The assembly name.
     */
    String getAssemblyName()
    {
        return this.assemblyName;
    }

    /**
     * Gets the project's information object for the given configuration and
     * platform type.
     * 
     * @param config The configuration name (ex. Debug, Release).
     * @param platform The platform type (ex. AnyCPU, x86, Win32).
     * @return The project's information object for the given configuration and
     *         platform type.
     */
    private ProjectInfo getProjectInfo(String config, String platform)
    {
        if (StringUtils.isEmpty(config))
        {
            return null;
        }

        if (!this.projectInfos.containsKey(config))
        {
            this.projectInfos.put(config, new HashMap<String, ProjectInfo>());
        }

        Map<String, ProjectInfo> map = this.projectInfos.get(config);

        if (!map.containsKey(platform))
        {
            ProjectInfo pi = new ProjectInfo(this);
            pi.setConfig(config);
            pi.setPlatform(platform);
            map.put(platform, pi);
        }

        return map.get(platform);
    }

    /**
     * Gets the project's build directory for the default build configuration
     * and platform type.
     * 
     * @return The project's build directory for the default build configuration
     *         and platform type.
     */
    /*public File getBuildDir()
    {
        return getBuildDir(this.defaultConfig, this.defaultPlatform);
    }*/

    /**
     * Gets the project's build directory.
     * 
     * @param config The configuration name (ex. Debug, Release).
     * @param platform The platform type (ex. AnyCPU, x86, Win32).
     * @return The project's build directory.
     */
    public File getBuildDir(String config, String platform)
    {
        return getProjectInfo(config, platform).getDir();
    }

    /**
     * Gets the project's binary artifact.
     * 
     * @param config The configuration name (ex. Debug, Release).
     * @param platform The platform type (ex. AnyCPU, x86, Win32).
     * @return The project's binary artifact.
     */
    public File getBinArtifact(String config, String platform)
    {
        return getProjectInfo(config, platform).getBin();
    }

    /**
     * Gets the project's documentation artifact.
     * 
     * @param config The configuration name (ex. Debug, Release).
     * @param platform The platform type (ex. AnyCPU, x86, Win32).
     * @return The project's documentation artifact.
     */
    public File getDocArtifact(String config, String platform)
    {
        return getProjectInfo(config, platform).getDoc();
    }

    /**
     * Gets the project's symbols artifact.
     * 
     * @param config The configuration name (ex. Debug, Release).
     * @param platform The platform type (ex. AnyCPU, x86, Win32).
     * @return The project's symbols artifact.
     */
    public File getPdbArtifact(String config, String platform)
    {
        return getProjectInfo(config, platform).getPdb();
    }

    /**
     * Gets the name of the project's build artifact. The name does not include
     * a file extension. Thus, if the project produces an executable named
     * "HelloWorld.exe" this method will return the string "HelloWorld".
     * 
     * @return The name of the project's build artifact. The name does not
     *         include a file extension. Thus, if the project produces an
     *         executable named "HelloWorld.exe" this method will return the
     *         string "HelloWorld".
     */
    public String getArtifactName(String config, String platform)
    {
        return getProjectInfo(config, platform).getArtifactName();
    }

    private static class ProjectInfo implements Serializable
    {
        private final static Pattern TOKEN_PATT = Pattern
            .compile("\\$\\((.+?)\\)");

        private final Map<String, String> tokensAndReplacements =
            new HashMap<String, String>();

        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = 398870509494073351L;

        public ProjectInfo(MSBuildProject parent)
        {
            this.parent = parent;
            tokensAndReplacements.put("SolutionDir", ".\\\\");
            tokensAndReplacements.put("OutDir", "");
            tokensAndReplacements.put("ProjectDir", ".\\\\");
        }

        /**
         * The parent MSBuildProject object.
         */
        private MSBuildProject parent;

        /**
         * The project artifact's name.
         */
        private String artifactName;

        /**
         * The project's type.
         */
        private ProjectType type;

        /**
         * The build directory.
         */
        private File dir;

        /**
         * The binary artifact.
         */
        private File bin;

        /**
         * The documentation artifact.
         */
        private File doc;

        /**
         * The tokenized path to the base artifact directory.
         */
        private String outputDirPath;

        /**
         * The tokenized path to the documentation artifact.
         */
        private String docFilePath;

        /**
         * The target name (CPP only).
         */
        private String targetName;

        /**
         * The output file (CPP only).
         */
        private String outputFilePath;

        /**
         * The PDB (build symbols) artifact.
         */
        private File pdb;

        public File getBin()
        {
            if (this.bin != null)
            {
                return this.bin;
            }

            this.bin =
                new File(getDir(), String.format(
                    "%s.%s",
                    getArtifactName(),
                    getType().getFileExtension()));

            return this.bin;
        }

        public File getDoc()
        {
            if (StringUtils.isEmpty(this.docFilePath))
            {
                return null;
            }

            if (this.doc != null)
            {
                return this.doc;
            }

            this.doc = new File(replaceTokens(this.docFilePath));

            return this.doc;
        }

        public File getPdb()
        {
            if (this.pdb != null)
            {
                return this.pdb;
            }

            String fileName = String.format("%s.pdb", getArtifactName());

            this.pdb = new File(getDir(), fileName);

            return this.pdb;
        }

        public File getDir()
        {
            if (this.dir != null)
            {
                return this.dir;
            }

            this.dir = new File(replaceTokens(this.outputDirPath));

            return this.dir;
        }

        public void setType(ProjectType toSet)
        {
            this.type = toSet;
        }

        private ProjectType getType()
        {
            return this.type == null ? this.parent.getType() : this.type;
        }

        public void setConfig(String toSet)
        {
            this.tokensAndReplacements.put("Configuration", toSet);
        }

        public void setPlatform(String toSet)
        {
            this.tokensAndReplacements.put("Platform", toSet);
        }

        private String replaceTokens(String toReplace)
        {
            if (!this.tokensAndReplacements.containsKey("ProjectName"))
            {
                String bfn =
                    FilenameUtils.getBaseName(this.parent.getFile().toString());
                if (this.parent.getProjectLanguage() == ProjectLanguageType.CPP
                    && !getAssemNameOrRootNS().equals(bfn))
                {
                    this.tokensAndReplacements.put("ProjectName", bfn);
                }
                else
                {
                    this.tokensAndReplacements.put(
                        "ProjectName",
                        getAssemNameOrRootNS());
                }
            }

            if (!this.tokensAndReplacements.containsKey("TargetName"))
            {
                this.tokensAndReplacements.put(
                    "TargetName",
                    getAssemNameOrRootNS());
            }

            if (!this.tokensAndReplacements.containsKey("TargetExtension"))
            {
                this.tokensAndReplacements.put("TargetExtension", "."
                    + getType().getFileExtension());
            }

            StringBuffer sb = new StringBuffer();
            Matcher matt = TOKEN_PATT.matcher(toReplace);

            while (matt.find())
            {
                String t = matt.group(1);
                String r = this.tokensAndReplacements.get(t);
                matt.appendReplacement(sb, r);
            }

            matt.appendTail(sb);

            return sb.toString();
        }

        public String getArtifactName()
        {
            if (StringUtils.isNotEmpty(this.artifactName))
            {
                return this.artifactName;
            }

            if (StringUtils.isNotEmpty(this.targetName))
            {
                this.artifactName = replaceTokens(this.targetName);
            }
            else if (StringUtils.isNotEmpty(this.outputFilePath))
            {
                this.artifactName = replaceTokens(this.outputFilePath);
            }
            else
            {
                this.artifactName = getAssemNameOrRootNS();
            }

            return this.artifactName;
        }

        private String getAssemNameOrRootNS()
        {
            return StringUtils.isEmpty(this.parent.getAssemblyName())
                ? this.parent.getRootNamespace() : this.parent
                    .getAssemblyName();
        }

        public void setOutputDirPath(String toSet)
        {
            this.outputDirPath = toSet;
        }

        public void setDocFilePath(String toSet)
        {
            this.docFilePath = toSet;
        }

        public void setTargetName(String toSet)
        {
            this.targetName = toSet;
            this.tokensAndReplacements.put("TargetName", toSet);
        }

        public void setTargetExtension(String toSet)
        {
            this.tokensAndReplacements.put("TargetExtension", toSet);
        }

        public void setOutputFilePath(String toSet)
        {
            toSet = FilenameUtils.getBaseName(toSet);
            this.outputFilePath = toSet;
        }
    }
}
