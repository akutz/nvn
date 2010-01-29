package net.sf.nvn.plugins.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.CollectionUtils;

/**
 * An abstract Mojo for nvn Mojos that call external programs.
 * 
 */
public abstract class AbstractExeMojo extends AbstractNvnMojo
{
    /**
     * The command used to start this process.
     * 
     * @parameter
     */
    File command;

    /**
     * This content of this parameter, if specified, will override all other of
     * this plug-in's parameters and execute this process with this string as
     * its sole command line argument(s).
     * 
     * @parameter
     */
    String commandLineArgs;

    /**
     * Set this parameter to true to specify that the msbuild process should
     * inherit the environment variables of the current process.
     * 
     * @parameter default-value="true"
     */
    boolean inheritEnvVars;

    /**
     * Environment variables to specify for the msbuild process.
     * 
     * @parameter
     */
    Properties envVars;

    /**
     * The environment variables to use for the executable process.
     */
    @SuppressWarnings("unchecked")
    Map envVarsToUseForProc;

    abstract String getArgs();

    final String buildCmdLineString()
    {
        String args =
            StringUtils.isEmpty(this.commandLineArgs) ? getArgs()
                : this.commandLineArgs;

        String cmd = String.format("%s %s", getPath(this.command), args);

        return cmd;
    }

    @Override
    final void nvnExecute() throws MojoExecutionException
    {
        exec();
    }

    final void exec(String cmd) throws MojoExecutionException
    {
        try
        {
            final Process p = Runtime.getRuntime().exec(cmd, getEnvVarArray());

            pipe(p);

            int exitCode = p.waitFor();

            if (exitCode != 0)
            {
                throw new MojoExecutionException(getMojoName()
                    + " exited with an unsuccessful error code: " + exitCode);
            }
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error running " + getMojoName()
                + ": ", e);
        }
    }
    
    final String[] getEnvVarArray()
    {
        String[] arr = new String[this.envVarsToUseForProc.size()];
        
        int x = 0;
        for (Object ok : this.envVarsToUseForProc.keySet())
        {
            Object ov = this.envVarsToUseForProc.get(ok);
            arr[x] = String.format("%s=%s", ok, ov);
            ++x;
        }
        
        return arr;
    }

    final void pipe(Process p)
    {
        pipe(p.getInputStream(), System.out);
        pipe(p.getErrorStream(), System.out);
    }

    final void pipe(final InputStream in, final OutputStream out)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    byte[] buff = new byte[1024];
                    int read;
                    while ((read = in.read(buff)) > 0)
                    {
                        out.write(buff, 0, read);
                    }
                }
                catch (IOException e)
                {
                    // Do nothing
                }
            }
        }).start();
    }

    final public void exec() throws MojoExecutionException
    {
        loadEnvVars();

        String cmd = buildCmdLineString();

        info(cmd);

        exec(cmd);
    }

    @SuppressWarnings("unchecked")
    void loadEnvVars()
    {
        if (this.inheritEnvVars && this.envVars != null)
        {
            this.envVarsToUseForProc =
                CollectionUtils.mergeMaps(this.envVars, System.getenv());
        }
        else if (this.inheritEnvVars && this.envVars == null)
        {
            this.envVarsToUseForProc = System.getenv();
        }
        else if (!this.inheritEnvVars && this.envVars != null)
        {
            this.envVarsToUseForProc = this.envVars;
        }
        else if (!this.inheritEnvVars && this.envVars == null)
        {
            this.envVarsToUseForProc = new HashMap();
        }
    }

    @Override
    boolean isInPath(File file)
    {
        if (this.envVarsToUseForProc == null)
        {
            debug("isInPath returning false because env vars is null");
            return false;
        }

        String fileName = file.getName();

        if (!this.envVarsToUseForProc.containsKey("Path"))
        {
            debug("isInPath returning false because \"Path\" env var does not exist");
            return false;
        }

        String path = String.valueOf(this.envVarsToUseForProc.get("Path"));
        String[] pathParts = path.split("\\;|\\,");

        for (String pp : pathParts)
        {
            File ep = new File(pp);
            String p = ep + "\\" + fileName;
            File epf = new File(p);

            if (epf.exists())
            {
                debug("isInPath returning true = " + p);
                return true;
            }
            else
            {
                debug("isInPath did not find = " + p);
            }
        }

        debug("isInPath returning false because file not found in path");
        return false;
    }
}
