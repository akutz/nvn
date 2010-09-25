/*******************************************************************************
 * Copyright (c) 2010, Schley Andrew Kutz
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Schley Andrew Kutz nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
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

package net.sf.nvn.commons.dotnet;

import org.apache.commons.lang.StringUtils;

/**
 * The build's platform type.
 * 
 * @author akutz
 * 
 */
public enum PlatformType
{
    AnyCPU,

    x86,

    x64,
    
    Win32,

    Itanium;

    /**
     * Parses a PlatformType from an input string. This method is case
     * insensitive.
     * 
     * @param value The input string.
     * @return A PlatformType enumeration.
     */
    public static PlatformType parse(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }

        if (value.matches("(?i)any\\s?cpu"))
        {
            return PlatformType.AnyCPU;
        }
        else if (value.matches("(?i)x86"))
        {
            return PlatformType.x86;
        }
        else if (value.matches("(?i)x64"))
        {
            return PlatformType.x64;
        }
        else if (value.matches("(?i)win32"))
        {
            return PlatformType.Win32;
        }
        else if (value.matches("(?i)itanium"))
        {
            return PlatformType.Itanium;
        }

        return null;
    }
}
