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

import java.io.Serializable;
import org.codehaus.plexus.util.StringUtils;

public class RegValue extends InstallCheck implements Serializable
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -6926261494695851436L;

    /**
     * The registry key's path.
     */
    String path;
    
    boolean x64;

    /**
     * The registry key's value name.
     */
    String valueName;

    /**
     * The value to compare.
     */
    String value;

    /**
     * The value's type. Valid values include:
     * <ul>
     * <li><strong>string</strong></li>
     * <li><strong>long</strong></li>
     * </ul>
     */
    String typeName;

    /**
     * <p>
     * A comparison operator. Valid values for <strong>string</strong> types
     * include:
     * </p>
     * <ul>
     * <li><strong>==</strong></li>
     * <li><strong>!=</strong></li>
     * </ul>
     * <p>
     * Valid values for <strong>long</strong> types include:
     * </p>
     * <ul>
     * <li><strong>==</strong></li>
     * <li><strong>!=</strong></li>
     * <li><strong>&lt;</strong></li>
     * <li><strong>&gt;</strong></li>
     * <li><strong>&lt;=</strong></li>
     * <li><strong>&gt;=</strong></li>
     * </ul>
     */
    String comparison;

    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append("new RegValue {");
        buff.append(String.format("Path=@\"%s\", ", path));
        buff.append(String.format("X64=%s,", x64 ? "true" : "false"));
        buff.append(String.format("ValueName=@\"%s\", ", valueName));
        buff.append(String.format("Value=@\"%s\", ", value));
        buff.append(String.format("TypeName=@\"%s\", ", typeName));
        buff.append(String.format("Comparison=@\"%s\", ", comparison));
        buff.append(String.format(
            "ErrorMessage=@\"%s\", ",
            StringUtils.isEmpty(errorMessage) ? "" : errorMessage));
        buff.append(String.format("Inverse=%s, ", inverse ? "true" : "false"));
        buff.append("},\r\n");
        return buff.toString();
    }
}
