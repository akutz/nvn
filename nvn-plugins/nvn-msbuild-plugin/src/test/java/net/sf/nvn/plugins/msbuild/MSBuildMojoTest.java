package net.sf.nvn.plugins.msbuild;

import java.io.File;
import junit.framework.Assert;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
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
        mojo.preExecute();
        String cls = mojo.buildCmdLineString(0);
        Assert
            .assertEquals(
                ".\\msbuild.exe /target:Build /property:Platform=\"Any CPU\";Configuration=Debug;OutputPath=.\\bin\\Debug /nodeReuse:false .\\MySolution.sln",
                cls);
    }
}
