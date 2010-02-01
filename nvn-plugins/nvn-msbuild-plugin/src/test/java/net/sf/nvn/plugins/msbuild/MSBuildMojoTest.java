package net.sf.nvn.plugins.msbuild;

import java.io.File;
import java.util.ArrayList;
import junit.framework.Assert;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class MSBuildMojoTest
{
    private static String LOCAL_REPO_PATH =
        "file://c:\\Documents and Settings\\akutz\\.m2\\\\repository";

    private MSBuildMojo loadMojo() throws Exception
    {
        ArtifactRepository localRepo =
            new DefaultArtifactRepository(
                "localRepo",
                LOCAL_REPO_PATH,
                new DefaultRepositoryLayout());

        MSBuildMojo mojo = new MSBuildMojo();
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setBasedir(new File("."));
        mojo.command = new File("msbuild.exe");
        mojo.buildFile = new File("MySolution.sln");
        mojo.inheritEnvVars = false;
        mojo.localRepository = localRepo;

        return mojo;
    }

    @Test
    public void testBuildCmdLineString() throws Exception
    {
        MSBuildMojo mojo = loadMojo();
        mojo.prepareForExecute();
        String cls = mojo.buildCmdLineString();
        Assert
            .assertEquals(
                ".\\msbuild.exe /target:Build /property:Platform=\"Any CPU\";Configuration=Debug /nodeReuse:false .\\MySolution.sln",
                cls);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBuildCmdLineString2() throws Exception
    {
        MSBuildMojo mojo = loadMojo();

        Dependency d = new Dependency();
        d.setGroupId("com.vmware");
        d.setArtifactId("viplugins");
        d.setVersion("4.0.0.1");
        d.setType("dotnet:dll");

        mojo.mavenProject.setDependencies(new ArrayList());
        mojo.mavenProject.getDependencies().add(d);

        mojo.prepareForExecute();

        String cls = mojo.buildCmdLineString();
        Assert
            .assertEquals(
                ".\\msbuild.exe /target:Build /property:Platform=\"Any CPU\";Configuration=Debug;ReferencePath=\"c:\\Documents and Settings\\akutz\\.m2\\\\repository\\com\\vmware\\viplugins\\4.0.0.1\" /nodeReuse:false .\\MySolution.sln",
                cls);
    }
}
