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
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.AbstractArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;

/**
 * This class provides nvn-specific metadata about .NET artifacts.
 * 
 * @author akutz
 * 
 */
public class NvnArtifactMetadata extends AbstractArtifactMetadata
{
    /**
     * The artifact's AssemblyName.
     */
    private String assemblyName;

    /**
     * The metadata file.
     */
    private File file;

    protected NvnArtifactMetadata(Artifact artifact)
    {
        super(artifact);
    }

    /**
     * Gets the artifact's AssemblyName.
     * 
     * @return The artifact's AssemblyName.
     */
    public String getAssemblyName()
    {
        return this.assemblyName;
    }

    public static NvnArtifactMetadata instance(
        Artifact artifact,
        File nvnMetadataFile) throws IOException
    {
        NvnArtifactMetadata nmd = new NvnArtifactMetadata(artifact);
        nmd.assemblyName = FileUtils.readFileToString(nvnMetadataFile);
        nmd.file = nvnMetadataFile;
        return nmd;
    }

    public static NvnArtifactMetadata instance(
        Artifact artifact,
        String assemblyName) throws IOException
    {
        NvnArtifactMetadata nmd = new NvnArtifactMetadata(artifact);
        nmd.assemblyName = assemblyName;
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        nmd.file = new File(tmpdir, nmd.getFilename());
        FileUtils.writeStringToFile(nmd.file, assemblyName);
        return nmd;
    }

    @Override
    public String getBaseVersion()
    {
        return super.artifact.getBaseVersion();
    }

    @Override
    public Object getKey()
    {
        return "nvn " + artifact.getGroupId() + ":" + artifact.getArtifactId();
    }

    @Override
    public String getLocalFilename(ArtifactRepository repository)
    {
        return getFilename();
    }

    @Override
    public String getRemoteFilename()
    {
        return getFilename();
    }

    @Override
    public void merge(ArtifactMetadata metadata)
    {
        NvnArtifactMetadata m = (NvnArtifactMetadata) metadata;
        if (!m.file.equals(this.file))
        {
            throw new IllegalStateException(
                "Cannot add two different pieces of metadata for: " + getKey());
        }
    }

    @Override
    public void storeInLocalRepository(
        ArtifactRepository localRepository,
        ArtifactRepository remoteRepository)
        throws RepositoryMetadataStoreException
    {
        File destination =
            new File(
                localRepository.getBasedir(),
                localRepository.pathOfLocalRepositoryMetadata(
                    this,
                    remoteRepository));

        try
        {
            FileUtils.copyFile(file, destination);
        }
        catch (IOException e)
        {
            throw new RepositoryMetadataStoreException(
                "Error copying NVN metadata file to the local repository.",
                e);
        }
    }

    @Override
    public boolean storedInArtifactVersionDirectory()
    {
        return true;
    }

    public String getFilename()
    {
        return getArtifactId() + "-" + artifact.getVersion() + ".nvn";
    }
}
