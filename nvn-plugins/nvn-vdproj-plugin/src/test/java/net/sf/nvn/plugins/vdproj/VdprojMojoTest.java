package net.sf.nvn.plugins.vdproj;

import java.io.File;
import junit.framework.Assert;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * The test class for VdprojMojo.
 * 
 * @author akutz
 * 
 */
public class VdprojMojoTest
{
    private VdprojMojo loadMojo() throws Exception
    {
        VdprojMojo mojo = new VdprojMojo();
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setBasedir(new File("."));
        mojo.buildConfiguration = "Debug";
        mojo.command = new File("devenv.exe");
        mojo.vdProjFiles = new File[] { new File("MySetupProject.vdproj")};
        mojo.inheritEnvVars = true;
        return mojo;
    }

    @Test
    public void testBuildCmdLineString() throws Exception
    {
        VdprojMojo mojo = loadMojo();
        mojo.preExecute();
        Assert.assertEquals(
            ".\\devenv.exe /Build Debug /Project MySetupProject .\\MySetupProject.vdproj",
            mojo.buildCmdLineString(0));
    }
}
