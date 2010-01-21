package net.sf.nvn.plugins.msbuild;

import java.io.File;
import junit.framework.Assert;
import org.junit.Test;

public class MSBuildMojoTest
{
    private MSBuildMojo loadMojo() throws Exception
    {
        MSBuildMojo mojo = new MSBuildMojo();
        mojo.baseDir = new File(".");
        mojo.msbuild = new File("msbuild.exe");
        mojo.buildFile = new File("MySolution.sln");
        mojo.timeout = new Long(300000);
        mojo.inheritEnvVars = true;
        mojo.loadBuildFile();
        mojo.loadProperties();
        mojo.loadTargets();
        return mojo;
    }

    @Test
    public void testBuildCommandLineString() throws Exception
    {
        MSBuildMojo mojo = loadMojo();
        String cls = mojo.buildCommandLineString();
        Assert
            .assertEquals(
                "msbuild.exe /target:Build /property:Platform=\"Any CPU\";Configuration=Debug /nodeReuse:false MySolution.sln",
                cls);
    }
}
