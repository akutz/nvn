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
        key = quote(key);
        Process p = ProcessUtils.exec("reg query " + key, false);
        int exitCode = p.waitFor();
        return exitCode == 0;
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
        key = quote(key);
        valueName = quote(valueName);
        String cmd = String.format("reg query %s /v %s", key, valueName);
        Process p = ProcessUtils.exec(cmd, false);
        int exitCode = p.waitFor();
        return exitCode == 0;
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
            throw new Exception(String.format(
                "Error from reg query: %s",
                exitCode));
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