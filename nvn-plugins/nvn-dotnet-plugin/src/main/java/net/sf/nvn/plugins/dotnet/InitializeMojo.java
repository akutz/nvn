package net.sf.nvn.plugins.dotnet;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Organization;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * A Maven plug-in for initializing the nvn build system.
 * 
 * @goal initialize
 * @phase initialize
 * @description A Maven plug-in for initializing the nvn build system.
 */
public class InitializeMojo extends AbstractNvnMojo
{
    /**
     * The default version for a .NET project.
     */
    private static String DEFAULT_VERSION = "0.0.0.0";

    /**
     * The location of this Visual Studio Project's parent Solution's pom file.
     * If this parameter is not set then this plug-in will attempt to
     * automatically discover the parent Solution (if one exists).
     * 
     * @parameter
     */
    File parent;

    /**
     * <p>
     * The location of this project's modules' pom files if this project is a
     * Visual Studio Solution. If the modules are not specified then this
     * plug-in will attempt to automatically discover them.
     * </p>
     * <p>
     * Defining <strong>any</strong> modules in this list turns off automatic
     * discovery.
     * </p>
     * 
     * @parameter
     */
    File[] modules;

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
        // Do nothing
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
        initParent();
        initModules();
        initVersion();
        initProperties();
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    /**
     * <p>
     * Initializes the parent project. The parent project will only be set only
     * be set if this project is not a Visual Studio Solution and exists beneath
     * a Visual Studio Solution in a folder hierarchy. Otherwise the parent
     * project will be null.
     * </p>
     * <p>
     * For example, the parent project will be set if this project is a CSharp
     * project in a folder hierarchy such as the following:
     * </p>
     * 
     * <pre>
     * MySolution
     * |
     * |-- MySolution.sln
     * |
     * |-- MyProject
     *     |
     *     |--MyProject.csproj
     * </pre>
     * 
     * <p>
     * However, the the parent project will be null if this project is a Visual
     * Studio Solution (which has no parent) or if this project exists in
     * flattened folder hierarchy with a Visual Studio Solution, such as the
     * following:
     * </p>
     * 
     * <pre>
     * MySolution
     * |
     * | -- MySolution.sln
     * |
     * | -- MyProject.csproj
     * </pre>
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    void initParent() throws MojoExecutionException
    {
        if (!(isCSProject() || isVBProject() || isVdprojProject()))
        {
            debug("not loading parent because this is not a Visual Studio Project");
            return;
        }

        if (isSolution())
        {
            debug("not loading parent because this is a Visual Studio Solution");
            return;
        }

        File parentPom;

        if (this.parent == null)
        {
            parentPom =
                new File(
                    super.mavenProject.getBasedir().getParentFile(),
                    "pom.xml");
        }
        else
        {
            parentPom = this.parent;
        }

        if (!parentPom.exists())
        {
            debug("not loading parent because \"%s\" does not exist", parentPom
                .getAbsolutePath());
            return;
        }

        MavenProject parentProject = readProjectFile(parentPom, true);

        super.mavenProject.setParent(parentProject);
    }

    /**
     * Initializes this project's version.
     */
    void initVersion()
    {
        if (super.mavenProject.getParent() != null)
        {
            super.mavenProject.setVersion(super.mavenProject
                .getParent()
                .getVersion());
        }

        if (super.mavenProject.getVersion().equals(
            MavenProject.EMPTY_PROJECT_VERSION))
        {
            super.mavenProject.setVersion(DEFAULT_VERSION);
        }
    }

    /**
     * Initialize this project's properties.
     */
    @SuppressWarnings("unchecked")
    void initProperties()
    {
        if (super.mavenProject.getParent() == null)
        {
            debug("not initializing properties because parent project is null");
            return;
        }

        Properties parentProps = super.mavenProject.getParent().getProperties();
        Properties projctProps = super.mavenProject.getProperties();

        // Copy all of the parent's properties to this project's properties, but
        // don't overwrite any properties with the same key.
        for (Object ok : parentProps.keySet())
        {
            String k = (String) ok;

            if (!projctProps.containsKey(k))
            {
                String v = projctProps.getProperty(k);
                projctProps.setProperty(k, v);
            }

        }

        if (super.mavenProject.getParent().getOrganization() != null)
        {
            Organization parentOrg =
                super.mavenProject.getParent().getOrganization();

            if (super.mavenProject.getOrganization() == null)
            {
                super.mavenProject.setOrganization(parentOrg);
            }
            else
            {
                Organization projctOrg = super.mavenProject.getOrganization();

                if (StringUtils.isEmpty(projctOrg.getName()))
                {
                    projctOrg.setName(parentOrg.getName());
                }

                if (StringUtils.isEmpty(projctOrg.getUrl()))
                {
                    projctOrg.setUrl(parentOrg.getUrl());
                }
            }
        }

        if (StringUtils.isNotEmpty(super.mavenProject
            .getParent()
            .getInceptionYear()))
        {
            if (StringUtils.isEmpty(super.mavenProject.getInceptionYear()))
            {
                super.mavenProject.setInceptionYear(super.mavenProject
                    .getParent()
                    .getInceptionYear());
            }
        }

        if (super.mavenProject.getParent().getDevelopers() != null)
        {
            List parentDevs = super.mavenProject.getParent().getDevelopers();
            List projctDevs = super.mavenProject.getDevelopers();

            if (projctDevs == null)
            {
                super.mavenProject.setDevelopers(parentDevs);
            }
            else
            {
                List bothDevs = ListUtils.union(parentDevs, projctDevs);
                super.mavenProject.setDevelopers(bothDevs);
            }
        }
    }

    /**
     * <p>
     * Initializes the module projects.
     * </p>
     * <p>
     * The module projects will only be set if this project is a Visual Studio
     * Solution and exists above Visual Studio Projects in a folder hierarchy.
     * Otherwise the module projects will be null.
     * </p>
     * <p>
     * For example, the module projects will be set if this project is a Visual
     * Studio solution in a folder hierarchy such as the following:
     * </p>
     * 
     * <pre>
     * MySolution
     * |
     * |-- MySolution.sln
     * |
     * |-- MyProject
     *     |
     *     |--MyProject.csproj
     * </pre>
     * 
     * <p>
     * However, the module projects will be null if this project is a Visual
     * Studio Solution in flattened folder hierarchy with a Visual Studio
     * Solution, such as the following:
     * </p>
     * 
     * <pre>
     * MySolution
     * |
     * | -- MySolution.sln
     * |
     * | -- MyProject.csproj
     * </pre>
     * 
     * <p>
     * This module projects will also be null if this project is a Visual Studio
     * Solution with no contained Visual Studio Projects.
     * </p>
     * .
     * 
     * @throws MojoExecutionException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    void initModules() throws MojoExecutionException
    {
        if (!isSolution())
        {
            debug("not loading modules because this project is not a Visual Studio Solution");
            return;
        }

        Collection pomFiles;

        if (this.modules == null)
        {
            pomFiles =
                FileUtils.listFiles(
                    super.mavenProject.getBasedir(),
                    new NameFileFilter("pom.xml"),
                    TrueFileFilter.TRUE);
        }
        else
        {
            pomFiles = Arrays.asList(this.modules);
        }

        File myPom = new File(super.mavenProject.getBasedir() + "/pom.xml");

        for (Object opom : pomFiles)
        {
            File pom = (File) opom;

            debug("processing pom file \"%s\"", pom.getAbsolutePath());

            if (pom.equals(myPom))
            {
                debug(
                    "not adding \"%s\" because it is the current project's pom file",
                    myPom.getAbsolutePath());
            }
            else
            {
                String moduleName = pom.getParentFile().getName();
                super.mavenProject.getModules().add(moduleName);
                debug("added \"%s\"", moduleName);
            }
        }
    }
}