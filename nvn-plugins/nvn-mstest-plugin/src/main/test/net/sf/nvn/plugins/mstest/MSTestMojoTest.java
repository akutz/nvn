package net.sf.nvn.plugins.mstest;

import java.io.File;
import junit.framework.Assert;
import org.junit.Test;

/**
 * The test class for MSTestMojo.
 * 
 * @author akutz
 * 
 */
public class MSTestMojoTest
{
    private MSTestMojo loadMojo() throws Exception
    {
        MSTestMojo mojo = new MSTestMojo();
        mojo.baseDir = new File(".");
        mojo.mstest = new File("mstest.exe");
        mojo.timeout = new Long(300000);
        mojo.inheritEnvVars = true;
        mojo.testMetaDatas = new File[]
        {
            new File("MyTests.vsmdi")
        };
        mojo.loadTestMetaData();
        return mojo;
    }
    
    @Test
    public void buildCommandLineStringTest() throws Exception
    {
        MSTestMojo mojo = loadMojo();
        Assert.assertEquals("mstest.exe /testmetadata:MyTests.vsmdi ", mojo.buildCommandLineString());
    }
}
