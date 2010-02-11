package net.sf.nvn.plugins.dotnet;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven plug-in for initializing the nvn build system.
 * 
 * @goal initialize
 * @phase initialize
 * @description A Maven plug-in for initializing the nvn build system.
 * 
 */
public class InitializeMojo extends AbstractNvnMojo
{
    @Override
    String getMojoName()
    {
        return "initialize";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @Override
    void nvnExecute() throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }
}