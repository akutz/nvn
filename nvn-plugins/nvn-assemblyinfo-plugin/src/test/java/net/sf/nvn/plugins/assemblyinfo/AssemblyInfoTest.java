package net.sf.nvn.plugins.assemblyinfo;

import java.io.File;
import junit.framework.Assert;
import org.apache.maven.model.Organization;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * The test class for AssemblyInfo.
 * 
 * @author akutz
 * 
 */
public class AssemblyInfoTest
{
    @Test
    public void testExecute() throws Exception
    {
        String tmpdir = System.getProperty("java.io.tmpdir");

        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        mojo.guid = "2a585612-ae72-458e-b877-554c0d51a142";
        mojo.outputFile = new File(tmpdir + "/Properties/AssemblyInfo.cs");
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setBasedir(new File(tmpdir));
        
        mojo.execute();

        mojo.outputFile = new File(tmpdir + "/Properties/AssemblyInfo.vB");
        mojo.execute();
    }

    @Test
    public void testGetOutputFileType() throws Exception
    {
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        mojo.outputFile = new File("Properties/AssemblyInfo.cs");
        mojo.parseOutputFileType();
        Assert.assertEquals(OutputFileType.CSharp, mojo.outputFileType);

        mojo.outputFile = new File("Properties/AssemblyInfo.Vb");
        mojo.parseOutputFileType();
        Assert.assertEquals(OutputFileType.VisualBasic, mojo.outputFileType);
    }

    @Test
    public void testParseSafeVersion() throws Exception
    {
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setVersion("0.0.1-SNAPSHOT");
        mojo.parseSafeVersion();
        Assert.assertEquals("0.0.1", mojo.safeVersion);

        mojo.mavenProject.setVersion("0.1.0.989-SNAPSHOT-1.0");
        mojo.parseSafeVersion();
        Assert.assertEquals("0.1.0.989", mojo.safeVersion);
    }

    @Test
    public void testCreateAssemblyInfoTextCSharp() throws Exception
    {
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setOrganization(new Organization());
        mojo.outputFile = new File("Properties/AssemblyInfo.cs");
        mojo.guid = "2a585612-ae72-458e-b877-554c0d51a142";
        
        mojo.prepareForExecute();
        
        String text = mojo.createAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n"
                    + "[assembly : AssemblyVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyFileVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.0.0\")]\r\n",
                text);

        mojo.mavenProject.setName("MojoTest");
        text = mojo.createAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n"
                    + "[assembly : AssemblyVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyFileVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.0.0\")]\r\n",
                text);

        mojo.mavenProject.getOrganization().setName("SourceForge");
        text = mojo.createAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : AssemblyProduct(\"SourceForge MojoTest\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n"
                    + "[assembly : AssemblyVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyFileVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.0.0\")]\r\n",
                text);

        mojo.mavenProject.setDescription("Pants on the ground!");
        text = mojo.createAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : AssemblyProduct(\"SourceForge MojoTest\")]\r\n"
                    + "[assembly : AssemblyDescription(\"Pants on the ground!\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n"
                    + "[assembly : AssemblyVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyFileVersion(\"0.0.0.0\")]\r\n"
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.0.0\")]\r\n",
                text);

        mojo.mavenProject.setVersion("0.0.1-SNAPSHOT");
        mojo.parseSafeVersion();
        text = mojo.createAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : AssemblyProduct(\"SourceForge MojoTest\")]\r\n"
                    + "[assembly : AssemblyDescription(\"Pants on the ground!\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n"
                    + "[assembly : AssemblyVersion(\"0.0.1\")]\r\n"
                    + "[assembly : AssemblyFileVersion(\"0.0.1\")]\r\n"
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.1-SNAPSHOT\")]\r\n",
                text);
    }
}
