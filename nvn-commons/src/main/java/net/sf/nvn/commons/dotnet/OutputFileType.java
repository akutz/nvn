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

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * The type of AssemblyInfo file to write.
 * 
 * @author akutz
 * 
 */
public enum OutputFileType
{
    /**
     * A C# (cs) file.
     */
    CSharp,

    /**
     * A VisualBasic (vb) file.
     */
    VisualBasic;

    /**
     * Parses an OutputFileType.
     * 
     * @param file The file to parse.
     * @return An OutputFileType.
     */
    public static OutputFileType parse(File file)
    {
        if (file == null)
        {
            return null;
        }

        return parse(file.getName());
    }

    /**
     * Parses an OutputFileType.
     * 
     * @param fileName The file name to parse.
     * @return An OutputFileType.
     */
    public static OutputFileType parse(String fileName)
    {
        if (StringUtils.isEmpty(fileName))
        {
            return null;
        }

        String fileExtension = FilenameUtils.getExtension(fileName);
        
        if (fileExtension.matches("(?i)cs"))
        {
            return OutputFileType.CSharp;
        }
        else if (fileExtension.matches("(?i)vb"))
        {
            return OutputFileType.VisualBasic;
        }

        return null;
    }
}
