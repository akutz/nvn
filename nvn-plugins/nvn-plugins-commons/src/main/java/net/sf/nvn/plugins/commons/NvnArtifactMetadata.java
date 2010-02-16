package net.sf.nvn.plugins.commons;

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
            new File(localRepository.getBasedir(), localRepository
                .pathOfLocalRepositoryMetadata(this, remoteRepository));

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

    private String getFilename()
    {
        return getArtifactId() + "-" + artifact.getVersion() + ".nvn";
    }
}