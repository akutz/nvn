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
    public static Process exec(
        String cmd,
        @SuppressWarnings("rawtypes") Map envVars) throws IOException
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
    public static Process exec(
        String cmd,
        @SuppressWarnings("rawtypes") Map envVars,
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
    public final static void pipe(final InputStream in, final OutputStream out)
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
    final static String[] getEnvVarArray(@SuppressWarnings("rawtypes") Map map)
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
