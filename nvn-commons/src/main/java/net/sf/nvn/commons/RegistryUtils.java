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

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for reading and writing the Windows registry.
 * 
 * @author akutz
 * 
 */
public final class RegistryUtils
{
    /**
     * Returns a flag indicating whether or not the specified registry key
     * exists.
     * 
     * @param key The registry key.
     * @return A flag indicating whether or not the specified registry key
     *         exists.
     * @throws IOException When an error occurs.
     * @throws InterruptedException When an error occurs.
     */
    public static boolean exists(String key)
        throws IOException,
        InterruptedException
    {
        String cmd = "reg query " + quote(key);
        Process p = ProcessUtils.exec(cmd, false);
        int exitCode = p.waitFor();

        if (exitCode != 0)
        {
            if (key.contains("SOFTWARE\\"))
            {
                key = key.replace("SOFTWARE\\", "SOFTWARE\\Wow6432Node\\");
                cmd = String.format("reg query %s", quote(key));
                p = ProcessUtils.exec(cmd, false);
                exitCode = p.waitFor();

                if (exitCode != 0)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a flag indicating whether or not the specified registry value
     * exists.
     * 
     * @param key The registry key.
     * @param valueName The value's name.
     * @return A flag indicating whether or not the specified registry key
     *         exists.
     * @throws IOException When an error occurs.
     * @throws InterruptedException When an error occurs.
     */
    public static boolean exists(String key, String valueName)
        throws IOException,
        InterruptedException
    {
        String cmd =
            String.format("reg query %s /v %s", quote(key), quote(valueName));
        Process p = ProcessUtils.exec(cmd, false);
        int exitCode = p.waitFor();

        if (exitCode != 0)
        {
            if (key.contains("SOFTWARE\\"))
            {
                key = key.replace("SOFTWARE\\", "SOFTWARE\\Wow6432Node\\");
                cmd =
                    String.format(
                        "reg query %s /v %s",
                        quote(key),
                        quote(valueName));
                p = ProcessUtils.exec(cmd, false);
                exitCode = p.waitFor();

                if (exitCode != 0)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Reads a registry value.
     * 
     * @param key The registry key.
     * @param valueName The value's name.
     * @return A registry value.
     * @throws IOException When an error occurs.
     * @throws InterruptedException When an error occurs.
     * @throws Exception When an error occurs.
     */
    public static String read(String key, String valueName)
        throws IOException,
        InterruptedException,
        Exception
    {
        String cmd =
            String.format("reg query %s /v %s", quote(key), quote(valueName));
        Process p = ProcessUtils.exec(cmd, false);
        int exitCode = p.waitFor();

        if (exitCode != 0)
        {
            if (key.contains("SOFTWARE\\"))
            {
                key = key.replace("SOFTWARE\\", "SOFTWARE\\Wow6432Node\\");
                cmd =
                    String.format(
                        "reg query %s /v %s",
                        quote(key),
                        quote(valueName));
                p = ProcessUtils.exec(cmd, false);
                exitCode = p.waitFor();

                if (exitCode != 0)
                {
                    throw new Exception(String.format(
                        "Error from reg query: %s",
                        exitCode));
                }
            }
            else
            {
                throw new Exception(String.format(
                    "Error from reg query: %s",
                    exitCode));
            }
        }

        String stdout = ProcessUtils.getStdOut(p);

        String spatt =
            String.format("\\s+%s\\s+REG_[^\\s]+\\s+([^\\s].+)", valueName);
        Pattern patt = Pattern.compile(spatt, Pattern.MULTILINE);
        Matcher m = patt.matcher(stdout);

        if (!m.find())
        {
            String msg =
                String.format("Error using @@%s@@ to match %s", spatt, stdout);
            throw new Exception(msg);
        }

        return m.group(1);
    }
}
