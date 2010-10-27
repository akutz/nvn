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

package net.sf.nvn.commons.msbuild;

import org.apache.commons.lang.StringUtils;

/**
 * The MSBuild project types.
 * 
 * @author akutz
 * 
 */
public enum ProjectType
{
    /**
     * A library.
     */
    Library,
    
    /**
     * A windows application.
     */
    Application,

    /**
     * A command-line executable.
     */
    Exe,

    /**
     * A graphical executable.
     */
    WinExe,

    /**
     * A dynamic library.
     */
    DynamicLibrary,

    /**
     * A static library.
     */
    StaticLibrary,

    /**
     * A module
     */
    Module;

    /**
     * Parses a ProjectOutputType from an input string. This method is case
     * insensitive.
     * 
     * @param value The input string.
     * @return A ProjectOutputType enumeration.
     */
    public static ProjectType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }

        if (value.matches("(?i)library"))
        {
            return ProjectType.Library;
        }
        else if (value.matches("(?i)application"))
        {
            return ProjectType.Application;
        }
        else if (value.matches("(?i)dynamiclibrary"))
        {
            return ProjectType.DynamicLibrary;
        }
        else if (value.matches("(?i)staticlibrary"))
        {
            return ProjectType.StaticLibrary;
        }
        else if (value.matches("(?i)exe"))
        {
            return ProjectType.Exe;
        }
        else if (value.matches("(?i)winexe"))
        {
            return ProjectType.WinExe;
        }
        else if (value.matches("(?i)module"))
        {
            return ProjectType.Module;
        }

        return null;
    }

    /**
     * Gets the file extension associated with the output type.
     * 
     * @return The file extension associated with the output type.
     */
    public String getFileExtension()
    {
        switch (this)
        {
            case Application:
            case Exe :
            case WinExe :
            {
                return "exe";
            }
            case Module :
            case Library :
            case DynamicLibrary :
            {
                return "dll";
            }
            case StaticLibrary :
            {
                return "lib";
            }
            default :
            {
                return null;
            }
        }
    }
}
