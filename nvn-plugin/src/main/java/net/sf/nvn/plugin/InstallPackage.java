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

package net.sf.nvn.plugin;

import java.io.File;
import java.io.Serializable;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * An install package.
 * 
 * @author akutz
 * 
 */
public class InstallPackage implements Serializable
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 8492597991547421295L;

    /**
     * The name of the installer.
     */
    String name;

    /**
     * The name of the installer file that will be written to the file system.
     */
    String fileName;

    /**
     * The install package's file.
     */
    File file;

    File[] fileParts;

    String installArgs;

    String uninstallArgs;

    String quietInstallArgs;

    String quietUninstallArgs;

    boolean supportsUninstall;

    Integer[] exitCodes;

    RegKey[] regKeys;

    RegValue[] regValues;

    RunningProcess[] runningProcesses;

    String prompt;

    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();

        buff.append("new InstallPackage {\r\n");

        buff.append("ResourceKeys=new[] {\r\n");

        for (File filePart : fileParts)
        {
            buff.append(String.format(
                "@\"%s\",\r\n",
                FilenameUtils.getBaseName(filePart.toString())));
        }

        buff.append("},\r\n");
        buff.append(String.format("Name=@\"%s\",\r\n", name));
        buff.append(String.format(
            "FileName=@\"%s\",\r\n",
            StringUtils.isNotEmpty(this.fileName) ? this.fileName : this.name));
        buff.append(String.format(
            "Extension=@\"%s\",\r\n",
            FilenameUtils.getExtension(file.toString())));
        buff.append(String.format(
            "SupportsUninstall=%s,\r\n",
            supportsUninstall ? "true" : "false"));

        if (StringUtils.isNotEmpty(installArgs))
        {
            buff.append(String.format("InstallArgs=@\"%s\",\r\n", installArgs));
        }

        if (StringUtils.isNotEmpty(uninstallArgs))
        {
            buff.append(String.format(
                "UninstallArgs=@\"%s\",\r\n",
                uninstallArgs));
        }

        if (StringUtils.isNotEmpty(quietInstallArgs))
        {
            buff.append(String.format(
                "QuietInstallArgs=@\"%s\",\r\n",
                quietInstallArgs));
        }

        if (StringUtils.isNotEmpty(quietUninstallArgs))
        {
            buff.append(String.format(
                "QuietUninstallArgs=@\"%s\",\r\n",
                quietUninstallArgs));
        }

        if (StringUtils.isNotEmpty(prompt))
        {
            buff.append(String.format("Prompt=@\"%s\",\r\n", prompt));
        }

        if (exitCodes == null)
        {
            buff.append("ExitCodes = new[] {0},\r\n");
        }
        else
        {
            buff.append("ExitCodes = new[]\r\n");
            buff.append("{\r\n");

            for (int x : this.exitCodes)
            {
                buff.append(String.format("%s, ", x));
            }

            buff.append("},\r\n");
        }

        if (regKeys == null)
        {
            buff.append("RegKeys = new RegKey[0],\r\n");
        }
        else
        {
            buff.append("RegKeys = new[]\r\n");
            buff.append("{\r\n");

            for (RegKey rk : regKeys)
            {
                buff.append(rk.toString());
            }

            buff.append("},\r\n");
        }

        if (regValues == null)
        {
            buff.append("RegValues = new RegValue[0],\r\n");
        }
        else
        {
            buff.append("RegValues = new[]\r\n");
            buff.append("{\r\n");

            for (RegValue rv : regValues)
            {
                buff.append(rv.toString());
            }

            buff.append("},\r\n");
        }

        if (runningProcesses == null)
        {
            buff.append("RunningProcesses = new RunningProcess[0],\r\n");
        }
        else
        {
            buff.append("RunningProcesses = new[]\r\n");
            buff.append("{\r\n");

            for (RunningProcess rp : runningProcesses)
            {
                buff.append(rp.toString());
            }

            buff.append("},\r\n");
        }

        buff.append("},\r\n");

        return buff.toString();
    }
}
