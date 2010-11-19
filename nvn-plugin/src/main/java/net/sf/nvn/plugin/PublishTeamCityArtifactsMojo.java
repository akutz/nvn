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
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A MOJO for publishing a TeamCity artifact.
 * 
 * @author akutz
 * 
 * @goal publish-teamcity-artifacts
 * @phase install
 * @description A MOJO for publishing a TeamCity artifact.
 */
public class PublishTeamCityArtifactsMojo extends AbstractNvnMojo
{
    /**
     * An array of artifacts to publish.
     * 
     * @required
     * @parameter
     */
    File[] artifacts;

    /**
     * The target directories and/or archives to publish the artifacts to per
     * the <a href
     * ="http://confluence.jetbrains.net/display/TCD5/Build+Artifact">TeamCity
     * documentation</a>. This value may be null, but if it is defined it must
     * have an equal number of elements to that of the {@link #artifacts}
     * parameter.
     * 
     * @parameter
     */
    String[] targets;

    @Override
    void nvnExecute() throws MojoExecutionException
    {
        if (this.targets != null
            && this.targets.length != this.artifacts.length)
        {
            throw new MojoExecutionException(String.format(
                "targets.length (%s) != artifacts.length (%s)",
                this.targets.length,
                this.artifacts.length));
        }

        for (int x = 0; x < this.artifacts.length; ++x)
        {
            File f = this.artifacts[x];
            String t = ".";

            if (this.targets != null)
            {
                t = this.targets[x];
            }

            publishTeamCityArtifact(f, t);
        }
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        // Do nothing
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
        return "publishTeamCityArtifacts";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.artifacts != null;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }
}
