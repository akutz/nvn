package net.sf.nvn.plugins.commons;

import java.io.File;
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
        public String getArgs(int execution)
        {
            return null;
        }

        @Override
        public String getMojoName()
        {
            return null;
        }

        @Override
        public void preExecute() throws MojoExecutionException
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

        @Override
        public File getDefaultCommand()
        {
            return new File("echo");
        }

        @Override
        public void postExecute(MojoExecutionException executionException)
            throws MojoExecutionException
        {
        }
    }

    @Test
    public void loadEnvVarsTest() throws Exception
    {
        AbstractExeMojo mojo = new BaseExeMojoImpl();
        mojo.inheritEnvVars = true;
        mojo.initProcEnvVars();
        Assert.assertTrue(mojo.procEnvVars.size() > 0);
        
        mojo.inheritEnvVars = false;
        mojo.initProcEnvVars();
        Assert.assertEquals(0, mojo.procEnvVars.size());
    }
}
