package net.sf.nvn.commons.dotnet.v35.msbuild;

import java.io.File;
import junit.framework.Assert;
import net.sf.nvn.commons.dotnet.DebugType;
import net.sf.nvn.commons.dotnet.ErrorReportType;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;
import org.junit.Test;

/**
 * The test class for ProjectUtils.
 * 
 * @author akutz
 * 
 */
public class MSBuildProjectTest
{
    @Test
    public void testInstance() throws Exception
    {
        File f = new File("src/test/resources/MyProject.csproj");

        MSBuildProject p = MSBuildProject.instance(f);
        Assert.assertEquals(f, p.getProjectFile());
        Assert.assertEquals(ProjectLanguageType.CSharp, p.getProjectLanguage());
        Assert.assertEquals("v3.5", p.getTargetFrameworkVersion());
        Assert.assertEquals("MyProject", p.getRootNamespace());
        Assert.assertEquals("MyProject.Library", p.getAssemblyName());
        Assert.assertEquals(2, p.getBuildConfigurations().size());

        BuildConfiguration bc0 = p.getBuildConfigurations().get("Debug");
        Assert.assertEquals("Debug", bc0.getName());
        Assert.assertEquals(DebugType.Full, bc0.getDebugType());
        Assert.assertEquals(false, bc0.isOptimize());
        Assert.assertEquals(true, bc0.isDebugSymbols());
        Assert.assertEquals(new File("bin\\Debug\\"), bc0.getOutputPath());
        Assert.assertEquals(new File("bin\\Debug\\MyProject.Library.XML"), bc0
            .getDocumentationFile());
        Assert.assertEquals(ErrorReportType.Prompt, bc0.getErrorReport());
        Assert.assertEquals(2, bc0.getDefinedConstants().length);
        Assert.assertEquals("DEBUG", bc0.getDefinedConstants()[0]);
        Assert.assertEquals("TRACE", bc0.getDefinedConstants()[1]);
        Assert.assertEquals(new Integer(4), bc0.getWarningLevel());

        BuildConfiguration bc1 = p.getBuildConfigurations().get("Release");
        Assert.assertEquals("Release", bc1.getName());
        Assert.assertEquals(true, bc1.isOptimize());
        Assert.assertEquals(false, bc1.isDebugSymbols());
        Assert.assertEquals(DebugType.PdbOnly, bc1.getDebugType());
        Assert.assertEquals(ErrorReportType.Prompt, bc1.getErrorReport());
        Assert.assertEquals(1, bc1.getDefinedConstants().length);
        Assert.assertEquals("TRACE", bc1.getDefinedConstants()[0]);
        Assert.assertEquals(new Integer(4), bc1.getWarningLevel());
        Assert.assertEquals(new File("bin\\Release\\"), bc1.getOutputPath());
        Assert.assertEquals(null, bc1.getDocumentationFile());
    }
}
