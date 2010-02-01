package net.sf.nvn.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 * A utility class for manipulating processes.
 * 
 * @author akutz
 * 
 */
public final class ProcessUtils
{
    /**
     * Executes a command line string exactly as is.
     * 
     * @param cmd The command line string to execute.
     * @return A reference to the process that was created.
     * @throws IOException When an error occurs.
     */
    public static Process exec(String cmd) throws IOException
    {
        return exec(cmd, null, true);
    }

    /**
     * Executes a command line string exactly as is.
     * 
     * @param cmd The command line string to execute.
     * @param envVars The environment variables to use for the process's
     *        environment. If a null value is given then the process is executed
     *        with the environment of the parent process. An empty map should be
     *        specified if a blank environment is desired.
     * @return A reference to the process that was created.
     * @throws IOException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    public static Process exec(String cmd, Map envVars) throws IOException
    {
        return exec(cmd, envVars, true);
    }

    /**
     * Executes a command line string exactly as is.
     * 
     * @param cmd The command line string to execute.
     * @param copyStdOutAndStdErrToParentProcess Specify true to copy the stdout
     *        and stderr streams of the process to the parent process's stdout
     *        and stderr streams.
     * @return A reference to the process that was created.
     * @throws IOException When an error occurs.
     */
    public static Process exec(
        String cmd,
        boolean copyStdOutAndStdErrToParentProcess) throws IOException
    {
        return exec(cmd, null, copyStdOutAndStdErrToParentProcess);
    }

    /**
     * Executes a command line string exactly as is.
     * 
     * @param cmd The command line string to execute.
     * @param envVars The environment variables to use for the process's
     *        environment. If a null value is given then the process is executed
     *        with the environment of the parent process. An empty map should be
     *        specified if a blank environment is desired.
     * @param copyStdOutAndStdErrToParentProcess Specify true to copy the stdout
     *        and stderr streams of the process to the parent process's stdout
     *        and stderr streams.
     * @return A reference to the process that was created.
     * @throws IOException When an error occurs.
     */
    @SuppressWarnings("unchecked")
    public static Process exec(
        String cmd,
        Map envVars,
        boolean copyStdOutAndStdErrToParentProcess) throws IOException
    {
        String[] envVarsArr = getEnvVarArray(envVars);

        Process p = Runtime.getRuntime().exec(cmd, envVarsArr);

        if (copyStdOutAndStdErrToParentProcess)
        {
            pipe(p);
        }

        return p;
    }

    /**
     * Gets the stdout stream from a process as a string.
     * 
     * @param p The process.
     * @return The stdout stream from a process as a string.
     * @throws IOException When an error occurs.
     */
    public static String getStdOut(Process p) throws IOException
    {
        return IOUtils.toString(p.getInputStream());
    }

    /**
     * Gets the stderr stream from a process as a string.
     * 
     * @param p The process.
     * @return The stderr stream from a process as a string.
     * @throws IOException When an error occurs.
     */
    public static String getStdErr(Process p) throws IOException
    {
        return IOUtils.toString(p.getErrorStream());
    }

    /**
     * Pipes the process's stdout and stderr to the stdout stream of the parent
     * process.
     * 
     * @param p The process to pipe.
     */
    final static void pipe(Process p)
    {
        pipe(p.getInputStream(), System.out);
        pipe(p.getErrorStream(), System.err);
    }

    /**
     * Pipes the input stream to the given output stream.
     * 
     * @param in The input stream.
     * @param out The output stream.
     */
    final static void pipe(final InputStream in, final OutputStream out)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    IOUtils.copy(in, out);
                }
                catch (IOException e)
                {
                    // Do nothing
                }
            }
        }).start();
    }

    /**
     * Gets the environment variable map as an array of strings with the format
     * "KEY=VALUE".
     * 
     * @param map The map to transform into an array of strings.
     * 
     * @return The environment variable map as an array of strings with the
     *         format "KEY=VALUE".
     */
    @SuppressWarnings("unchecked")
    final static String[] getEnvVarArray(Map map)
    {
        if (map == null)
        {
            return null;
        }

        String[] arr = new String[map.size()];

        int x = 0;
        for (Object k : map.keySet())
        {
            Object v = map.get(k);
            arr[x] = String.format("%s=%s", k, v);
            ++x;
        }

        return arr;
    }
}
