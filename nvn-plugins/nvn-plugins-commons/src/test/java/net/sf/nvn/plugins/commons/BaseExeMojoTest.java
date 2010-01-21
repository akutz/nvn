package net.sf.nvn.plugins.commons;

import java.util.Map;
import junit.framework.Assert;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

/**
 * The test class for BaseExeMojo.
 * 
 * @author akutz
 * 
 */
public class BaseExeMojoTest
{
    private static class BaseExeMojoImpl extends BaseExeMojo
    {
        @Override
        public void execute() throws MojoExecutionException
        {
        }

        @Override
        public String buildCommandLineString()
        {
            return null;
        }

        @Override
        public String getExeDisplayName()
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void buildEnvVarsTest() throws Exception
    {
        BaseExeMojo mojo = new BaseExeMojoImpl();
        mojo.inheritEnvVars = true;
        Map ev = mojo.buildEnvVars();
        Assert.assertTrue(ev.size() > 0);

        mojo.inheritEnvVars = false;
        ev = mojo.buildEnvVars();
        Assert.assertEquals(0, ev.size());
    }
}
