package net.sf.nvn.plugins.commons;

import junit.framework.Assert;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

/**
 * The test class for BaseExeMojo.
 * 
 * @author akutz
 * 
 */
public class AbstractExeMojoTest
{
    private static class BaseExeMojoImpl extends AbstractExeMojo
    {
        @Override
        public String getArgs()
        {
            return null;
        }

        @Override
        public String getMojoName()
        {
            return null;
        }

        @Override
        public void prepareForExecute() throws MojoExecutionException
        {
        }

        @Override
        public boolean shouldExecute() throws MojoExecutionException
        {
            return false;
        }

        @Override
        public boolean isProjectTypeValid()
        {
            return false;
        }
        
    }

    @Test
    public void loadEnvVarsTest() throws Exception
    {
        AbstractExeMojo mojo = new BaseExeMojoImpl();
        mojo.inheritEnvVars = true;
        mojo.loadEnvVars();
        Assert.assertTrue(mojo.procEnvVars.size() > 0);
        
        mojo.inheritEnvVars = false;
        mojo.loadEnvVars();
        Assert.assertEquals(0, mojo.procEnvVars.size());
    }
}
