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
import junit.framework.Assert;
import net.sf.nvn.commons.msbuild.MSBuildProject;
import net.sf.nvn.commons.msbuild.ProjectLanguageType;
import org.testng.annotations.Test;

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
        Assert.assertEquals(f, p.getFile());
        Assert.assertEquals(ProjectLanguageType.CSharp, p.getProjectLanguage());
        Assert.assertEquals("v3.5", p.getTargetFrameworkVersion());
        Assert.assertEquals(
            "MyProject.Library",
            p.getArtifactName("Debug", "AnyCPU"));

        Assert.assertEquals(2, p.getProjectReferences().size());
        Assert.assertTrue(p.getProjectReferences().containsKey(
            "..\\Common\\Foo.csproj"));
        Assert.assertTrue(p.getProjectReferences().containsKey("..\\."));
        Assert.assertEquals(
            "Foo",
            p.getProjectReferences().get("..\\Common\\Foo.csproj"));
        Assert
            .assertEquals("AlwaysTrue", p.getProjectReferences().get("..\\."));

        Assert.assertEquals(
            new File("bin\\Debug\\"),
            p.getBuildDir("Debug", "AnyCPU"));
        Assert.assertEquals(
            new File("bin\\Debug\\/MyProject.Library.dll"),
            p.getBinArtifact("Debug", "AnyCPU"));
        Assert.assertEquals(
            new File("bin\\Debug\\/MyProject.Library.pdb"),
            p.getPdbArtifact("Debug", "AnyCPU"));
        Assert.assertEquals(
            new File("bin\\Debug\\MyProject.Library.XML"),
            p.getDocArtifact("Debug", "AnyCPU"));

        Assert.assertEquals(
            new File("bin\\Release\\"),
            p.getBuildDir("Release", "AnyCPU"));
        Assert.assertEquals(
            new File("bin\\Release\\/MyProject.Library.dll"),
            p.getBinArtifact("Release", "AnyCPU"));
        Assert.assertEquals(
            new File("bin\\Release\\/MyProject.Library.pdb"),
            p.getPdbArtifact("Release", "AnyCPU"));
        Assert.assertNull(p.getDocArtifact("Release", "AnyCPU"));
    }

    @Test
    public void testInstance2() throws Exception
    {
        File f = new File("src/test/resources/MyProjectDll.vcxproj");

        MSBuildProject p = MSBuildProject.instance(f);
        Assert.assertEquals(f, p.getFile());
        Assert.assertEquals(ProjectLanguageType.CPP, p.getProjectLanguage());
        Assert.assertEquals(
            "PowerPathWrapper",
            p.getArtifactName("Debug", "Win32"));

        Assert.assertEquals(
            new File(".\\Debug\\"),
            p.getBuildDir("Debug", "Win32"));
        Assert.assertEquals(
            new File(".\\Release\\"),
            p.getBuildDir("Release", "Win32"));
    }

    @Test
    public void testInstance3() throws Exception
    {
        File f = new File("src/test/resources/MyProjectDll2.vcxproj");

        MSBuildProject p = MSBuildProject.instance(f);
        Assert.assertEquals(f, p.getFile());
        Assert.assertEquals(ProjectLanguageType.CPP, p.getProjectLanguage());
        Assert.assertEquals(
            "CE_EventTraceMessage",
            p.getArtifactName("Debug", "Win32"));

        Assert.assertEquals(
            new File("..\\..\\local_bin\\x86\\Debug\\"),
            p.getBuildDir("Debug", "Win32"));
        Assert.assertEquals(
            new File("..\\..\\local_bin\\x86\\Release\\"),
            p.getBuildDir("Release", "Win32"));
    }

    @Test
    public void testInstance4() throws Exception
    {
        File f = new File("src/test/resources/MyProjectLib.vcxproj");

        MSBuildProject p = MSBuildProject.instance(f);
        Assert.assertEquals(f, p.getFile());
        Assert.assertEquals(ProjectLanguageType.CPP, p.getProjectLanguage());
        Assert.assertEquals(
            "CE_MyProjectLib",
            p.getArtifactName("Debug", "Win32"));

        Assert.assertEquals(
            new File("..\\..\\local_bin\\x86\\Debug\\"),
            p.getBuildDir("Debug", "Win32"));
        Assert.assertEquals(
            new File("..\\..\\local_bin\\x86\\Release\\"),
            p.getBuildDir("Release", "Win32"));
    }

    @Test
    public void testInstance5() throws Exception
    {
        File f = new File("src/test/resources/MyProject2.csproj");

        MSBuildProject p = MSBuildProject.instance(f);
        Assert.assertEquals(f, p.getFile());
        Assert.assertEquals(ProjectLanguageType.CSharp, p.getProjectLanguage());
        Assert.assertEquals("v3.5", p.getTargetFrameworkVersion());
        Assert.assertEquals("SpmCommon", p.getArtifactName("Debug", "AnyCPU"));

        Assert.assertEquals(2, p.getProjectReferences().size());
        Assert.assertTrue(p.getProjectReferences().containsKey(
            "..\\..\\Common\\EMC.VSI.VSphere4.Features.SPO.Common.csproj"));
        Assert.assertTrue(p.getProjectReferences().containsKey(
            "..\\SpmClient\\SpmClient.csproj"));
        Assert.assertEquals(
            "EMC.VSI.VSphere4.Features.SPO.Common",
            p.getProjectReferences().get(
                "..\\..\\Common\\EMC.VSI.VSphere4.Features.SPO.Common.csproj"));
        Assert.assertEquals(
            "SpmClient",
            p.getProjectReferences().get("..\\SpmClient\\SpmClient.csproj"));
    }
}
