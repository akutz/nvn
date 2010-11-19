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

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * <p>
 * A MOJO for running the Microsoft Windows Installer XML (WiX) compiler,
 * candle.exe.
 * </p>
 * 
 * @author akutz
 * 
 * @goal candle
 * @phase package
 * @requiresDependencyResolution compile
 * @description A MOJO for running the Microsoft Windows Installer XML (WiX)
 *              compiler, candle.exe
 */
public class CandleMojo extends AbstractExeMojo
{
    /**
     * Settings this parameter to true causes a light pre-processor parameter of
     * DEBUG to be included when the version ends with "-SNAPSHOT".
     * 
     * @parameter default-value="true"
     */
    boolean enableDebugSymbolForSnapshotBuilds;
    
    /**
     * The source files to process.
     * 
     * @parameter
     */
    File[] sourceFiles;

    /**
     * The response file.
     * 
     * @parameter
     */
    File responseFile;

    /**
     * The platform defaults for package, components, etc. Valid values include
     * x86, x64, ia64.
     * 
     * @parameter default-value="x86"
     */
    String architecture;

    /**
     * A list of pre-processor parameter key/value pairs.
     * 
     * @parameter
     */
    @SuppressWarnings("rawtypes")
    Map preProcessorParmaeters;

    /**
     * A list of extension assemblies or "class, assembly".
     * 
     * @parameter
     */
    String[] extensions;

    /**
     * Enables FIPS compliant algorithms.
     * 
     * @parameter
     */
    boolean fips;

    /**
     * Directories to include in the search path.
     * 
     * @parameter
     */
    File[] includes;

    /**
     * The file to pre-process the source files to. If no file is specified then
     * stdout will be used.
     * 
     * @parameter
     */
    File preProcessFile;

    /**
     * Show pedantic messages.
     * 
     * @parameter
     */
    boolean pedantic;

    /**
     * Suppress marking files vital by default.
     * 
     * @parameter
     */
    boolean suppressVital;

    /**
     * Suppress schema validation of documents (performance boost).
     * 
     * @parameter
     */
    boolean suppressSchemaValidation;

    /**
     * The warning IDs to suppress.
     * 
     * @parameter
     */
    String[] suppressedWarningIds;

    /**
     * Shows source trace for errors, warnings, and verbose messages.
     * 
     * @parameter
     */
    boolean trace;

    /**
     * Enables verbose output.
     * 
     * @parameter
     */
    boolean verbose;

    /**
     * The warning IDs to treat as errors.
     * 
     * @parameter
     */
    String[] warningIdsAsErrors;

    /**
     * The output file.
     * 
     * @parameter
     */
    File outputFile;

    @Override
    String getArgs(int execution)
    {
        StringBuilder buff = new StringBuilder();

        if (this.outputFile != null)
        {
            buff.append("-out");
            buff.append(" ");
            buff.append(quote(this.outputFile.toString()));
            buff.append(" ");
        }

        if (this.preProcessorParmaeters != null
            && this.preProcessorParmaeters.size() > 0)
        {
            for (Object ok : this.preProcessorParmaeters.keySet())
            {
                String k = (String) ok;
                buff.append("-d");
                buff.append(quote(k));

                Object ov = this.preProcessorParmaeters.get(ok);
                String v = (String) ov;

                if (StringUtils.isNotEmpty(v))
                {
                    buff.append("=");
                    buff.append(quote(v));
                }

                buff.append(" ");
            }
        }

        if (this.extensions != null && this.extensions.length > 0)
        {
            for (String s : this.extensions)
            {
                buff.append("-ext");
                buff.append(" ");
                buff.append(quote(s));
                buff.append(" ");
            }
        }

        if (this.fips)
        {
            buff.append("-fips");
            buff.append(" ");
        }

        if (this.includes != null && this.includes.length > 0)
        {
            for (File f : this.includes)
            {
                buff.append("-I");
                buff.append(" ");
                buff.append(quote(f.toString()));
                buff.append(" ");
            }
        }

        if (this.preProcessFile != null)
        {
            buff.append("-p");
            buff.append(quote(this.preProcessFile.toString()));
            buff.append(" ");
        }

        if (this.suppressVital)
        {
            buff.append("-sfdvital");
            buff.append(" ");
        }

        if (this.suppressSchemaValidation)
        {
            buff.append("-ss");
            buff.append(" ");
        }

        if (this.suppressedWarningIds != null
            && this.suppressedWarningIds.length > 0)
        {
            for (String s : this.suppressedWarningIds)
            {
                buff.append("-sw");
                buff.append(quote(s));
                buff.append(" ");
            }
        }

        if (this.trace)
        {
            buff.append("-trace");
            buff.append(" ");
        }

        if (this.verbose)
        {
            buff.append("-v");
            buff.append(" ");
        }

        if (this.warningIdsAsErrors != null
            && this.warningIdsAsErrors.length > 0)
        {
            for (String s : this.warningIdsAsErrors)
            {
                buff.append("-wx");
                buff.append(quote(s));
                buff.append(" ");
            }
        }

        if (this.sourceFiles != null && this.sourceFiles.length > 0)
        {
            for (File f : this.sourceFiles)
            {
                buff.append(quote(f.toString()));
                buff.append(" ");
            }
        }

        if (this.responseFile != null)
        {
            buff.append(quote(this.responseFile.toString()));
            buff.append(" ");
        }

        return buff.toString();
    }

    @Override
    File getCommand(int execution)
    {
        return new File("candle.exe");
    }

    @SuppressWarnings("unchecked")
    @Override
    void preExecute() throws MojoExecutionException
    {
        if (this.sourceFiles == null
            || (this.sourceFiles != null && this.sourceFiles.length == 0))
        {
            File wixSetup =
                new File(super.mavenProject.getBasedir(), "Setup.wxs");

            if (wixSetup.exists())
            {
                this.sourceFiles = new File[]
                {
                    wixSetup
                };
            }
        }

        // If not output file was specified and there is
        // a single input file then use the input file to
        // create the name of the output file.
        if (this.outputFile == null && this.sourceFiles != null
            && this.sourceFiles.length == 1)
        {
            String path = this.sourceFiles[0].toString();
            String bn = FilenameUtils.getBaseName(path);
            String won = bn + ".wixobj";
            this.outputFile =
                new File(super.mavenProject.getBuild().getDirectory(), won);
            debug("WixObj File: " + this.outputFile);
        }

        if (this.enableDebugSymbolForSnapshotBuilds
            && super.mavenProject.getVersion().endsWith("-SNAPSHOT"))
        {
            if (this.preProcessorParmaeters == null)
            {
                this.preProcessorParmaeters = new HashMap<String, String>();
            }

            this.preProcessorParmaeters.put("DEBUG", "True");
            info("Set DEBUG pre-process param");
        }
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (this.outputFile != null)
        {
            super.mavenProject.getProperties().setProperty(
                "candle.output",
                this.outputFile.toString());
        }
    }

    @Override
    String getMojoName()
    {
        return "candle";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.sourceFiles != null && this.sourceFiles.length > 0;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }
}
