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
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * <p>
 * A MOJO for running the JetBrains dotCover code coverage tool.
 * </p>
 * 
 * @author akutz
 * 
 * @goal dotcover
 * @phase test
 * @requiresDependencyResolution compile
 * @description A MOJO for running the JetBrains dotCover code coverage tool.
 */
public class DotCoverMojo extends AbstractExeMojo
{
    private static final Pattern ENDS_WITH_TEST_PATT = Pattern
        .compile("(?i)^(.*)\\.Test$");

    /**
     * The configuration file.
     * 
     * @parameter
     */
    File configFile;

    /**
     * All-in-one task. Performs coverage analysis, merges snapshots, generates
     * report and removes snapshots.
     * 
     * @parameter default-value="true"
     */
    boolean analyse;

    /**
     * Perform coverage analysis of the specified application.
     * 
     * @parameter
     */
    boolean cover;

    /**
     * Merge several coverage snapshots.
     * 
     * @parameter
     */
    boolean merge;

    /**
     * Create XML report by the specified snapshot.
     * 
     * @parameter
     */
    boolean report;

    /**
     * Obtain list of all snapshot files from coverage result descriptors.
     * 
     * @parameter
     */
    boolean list;

    /**
     * Delete all snapshot files specified in coverage result descriptors.
     * 
     * @parameter
     */
    boolean delete;

    @Override
    String getArgs(int execution)
    {
        StringBuilder buff = new StringBuilder();
        
        if (this.analyse)
        {
            buff.append("analyse");
            buff.append(" ");
        }
        else if (this.cover)
        {
            buff.append("cover");
            buff.append(" ");
        }
        else if (this.merge)
        {
            buff.append("merge");
            buff.append(" ");
        }
        else if (this.report)
        {
            buff.append("report");
            buff.append(" ");
        }
        else if (this.list)
        {
            buff.append("list");
            buff.append(" ");
        }
        else if (this.delete)
        {
            buff.append("delete");
            buff.append(" ");
        }
        
        buff.append(getPath(this.configFile));
        
        return null;
    }

    @Override
    File getCommand(int execution)
    {
        return new File("dotcover.exe");
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        if (this.configFile != null && !this.configFile.exists())
        {
            throw new MojoExecutionException("Config file does not exist: "
                + this.configFile);
        }

        // If there is no supplied configuration file and the analyse parameter
        // is set to true then create a configuration file.
        if (this.configFile == null && this.analyse)
        {
            String coverageTextPatt =
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
                    + "<AnalyseParams>\n"
                    + "    <Executable>%1$s</Executable>\n"
                    + "    <Arguments>/testcontainer:%2$s</Arguments>\n"
                    + "    <WorkingDir>%3$s</WorkingDir>\n"
                    + "    <Output>%4$s</Output>\n" + "    <Filters>\n"
                    + "        <IncludeFilters>\n"
                    + "            <FilterEntry>\n"
                    + "                <ModuleMask>%5$s.*</ModuleMask>\n"
                    + "                <ClassMask>*</ClassMask>\n"
                    + "                <FunctionMask>*</FunctionMask>\n"
                    + "            </FilterEntry>\n"
                    + "        </IncludeFilters>\n" + "    </Filters>\n"
                    + "</AnalyseParams>\n" + "";

            String mstestExePath = getMSTestExePath();
            String testContainer = getBinArtifact().getAbsolutePath();
            String workingDir = getBuildDir().getAbsolutePath();
            String outputFilePath = getOutputFilePath();
            String moduleMask = getModuleMask();

            String coverageText =
                String.format(
                    coverageTextPatt,
                    mstestExePath,
                    testContainer,
                    workingDir,
                    outputFilePath,
                    moduleMask);

            File cf = new File(getBuildDir(), "dotcover-config.xml");

            try
            {
                FileUtils.writeStringToFile(cf, coverageText);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(
                    "Error writing coverage text to " + cf);
            }

            this.configFile = cf;
        }
    }

    private String getModuleMask() throws MojoExecutionException
    {
        String an = getArtifactName();

        Matcher m = ENDS_WITH_TEST_PATT.matcher(an);

        if (!m.matches())
        {
            throw new MojoExecutionException("'" + an
                + "' does not end with \".Test\"");
        }

        String mm = m.group();

        return mm + ".*";
    }

    private String getOutputFilePath()
    {
        File f = new File(getBuildDir(), "dotcover-output.xml");
        String p = f.getAbsolutePath();
        return p;
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    String getMojoName()
    {
        return "dotcover";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        if (!existsDotCover10())
        {
            info("cannot find dotCover.exe");
            return false;
        }
        
        return true;
    }

    @Override
    boolean isProjectTypeValid()
    {
        // return super.mavenProject.getPackaging().equalsIgnoreCase("mstest");
        return true;
    }
}
