/*******************************************************************************
 * Copyright (c) 2010, Schley Andrew Kutz
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Schley Andrew Kutz nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
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

package net.sf.nvn.commons.dotnet.v35.msbuild;

import java.io.File;
import org.testng.annotations.Test;
import junit.framework.Assert;
import net.sf.nvn.commons.dotnet.DebugType;
import net.sf.nvn.commons.dotnet.ErrorReportType;
import net.sf.nvn.commons.dotnet.PlatformType;
import net.sf.nvn.commons.dotnet.ProjectLanguageType;

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
        Assert.assertEquals(PlatformType.AnyCPU, bc0.getPlatform());
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
        Assert.assertEquals(new File("bin\\Debug\\/MyProject.Library.dll"), p
            .getBuildArtifact("Debug"));
        Assert.assertEquals(new File("bin\\Debug\\/MyProject.Library.pdb"), p
            .getBuildSymbolsArtifact("Debug"));
        Assert.assertEquals(new File("bin\\Debug\\MyProject.Library.XML"), p
            .getBuildDocumentationArtifact("Debug"));

        BuildConfiguration bc1 = p.getBuildConfigurations().get("Release");
        Assert.assertEquals("Release", bc1.getName());
        Assert.assertEquals(PlatformType.AnyCPU, bc1.getPlatform());
        Assert.assertEquals(true, bc1.isOptimize());
        Assert.assertEquals(false, bc1.isDebugSymbols());
        Assert.assertEquals(DebugType.PdbOnly, bc1.getDebugType());
        Assert.assertEquals(ErrorReportType.Prompt, bc1.getErrorReport());
        Assert.assertEquals(1, bc1.getDefinedConstants().length);
        Assert.assertEquals("TRACE", bc1.getDefinedConstants()[0]);
        Assert.assertEquals(new Integer(4), bc1.getWarningLevel());
        Assert.assertEquals(new File("bin\\Release\\"), bc1.getOutputPath());
        Assert.assertEquals(null, bc1.getDocumentationFile());
        Assert.assertEquals(new File("bin\\Release\\/MyProject.Library.dll"), p
            .getBuildArtifact("Release"));
        Assert.assertEquals(new File("bin\\Release\\/MyProject.Library.pdb"), p
            .getBuildSymbolsArtifact("Release"));
        Assert.assertEquals(null, p.getBuildDocumentationArtifact("Release"));
    }
}
