package net.sf.nvn.plugins.assemblyinfo;

import java.io.File;
import junit.framework.Assert;
import org.apache.commons.io.FilenameUtils;
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
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        mojo.setGuid("2a585612-ae72-458e-b877-554c0d51a142");
        
        File tmpfile = File.createTempFile("foo", "txt");
        File tmpdir = new File(FilenameUtils.getFullPath(tmpfile.getPath()));
        tmpfile.delete();
        
        mojo.setBasedir(tmpdir);
        mojo.execute();
        mojo.setOutputFile(new File("Properties/AssemblyInfo.vb"));
        mojo.execute();
    }
    
    @Test
    public void testGetOutputFileType() throws Exception
    {
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();
        Assert.assertEquals("cs", mojo.getOutputFileType());

        mojo.setOutputFile(new File("Properties/AssemblyInfo.Vb"));
        Assert.assertEquals("vb", mojo.getOutputFileType());
    }

    @Test
    public void testParseSafeVersion()
    {
        Assert.assertEquals("0.1.0", AssemblyInfoMojo
            .parseSafeVersion("0.1.0-SNAPSHOT"));

        Assert.assertEquals("0.1.0.989", AssemblyInfoMojo
            .parseSafeVersion("0.1.0.989-SNAPSHOT-1.0"));
    }

    @Test
    public void testCreateCSharpAssembyInfoText()
    {
        AssemblyInfoMojo mojo = new AssemblyInfoMojo();

        mojo.setGuid("2a585612-ae72-458e-b877-554c0d51a142");
        String text = mojo.createCSharpAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n",
                text);

        mojo.setName("MojoTest");
        text = mojo.createCSharpAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n",
                text);

        mojo.setCompany("SourceForge");
        text = mojo.createCSharpAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : AssemblyProduct(\"SourceForge MojoTest\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n",
                text);

        mojo.setDescription("Pants on the ground!");
        text = mojo.createCSharpAssemblyInfoText();
        Assert
            .assertEquals(
                "using System.Reflection;\r\n"
                    + "using System.Runtime.InteropServices;\r\n\r\n"
                    + "[assembly : AssemblyTitle(\"MojoTest\")]\r\n"
                    + "[assembly : AssemblyProduct(\"SourceForge MojoTest\")]\r\n"
                    + "[assembly : AssemblyDescription(\"Pants on the ground!\")]\r\n"
                    + "[assembly : Guid(\"2a585612-ae72-458e-b877-554c0d51a142\")]\r\n",
                text);

        mojo.setVersion("0.0.1-SNAPSHOT");
        text = mojo.createCSharpAssemblyInfoText();
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
                    + "[assembly : AssemblyInformationalVersionAttribute(\"0.0.1-SNAPSHOT\")]",
                text);
    }
}
