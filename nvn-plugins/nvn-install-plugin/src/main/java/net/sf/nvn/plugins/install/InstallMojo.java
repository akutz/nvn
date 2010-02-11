package net.sf.nvn.plugins.install;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import net.sf.nvn.commons.dotnet.v35.msbuild.MSBuildProject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.project.validation.ModelValidator;
import org.codehaus.plexus.digest.Digester;
import org.codehaus.plexus.digest.DigesterException;

/**
 * A Maven plug-in for installing nvn artifacts. 99.999% of this plug-in is
 * ripped from the <a
 * href="http://maven.apache.org/plugins/maven-install-plugin/">Maven Install
 * Plugin</a>.
 * 
 * @author akutz
 * 
 * @goal install
 * @phase install
 * @description A Maven plug-in for installing nvn artifacts.
 */
public class InstallMojo extends AbstractNvnMojo
{
    /**
     * The component used to validate the user-supplied artifact coordinates.
     * 
     * @component
     */
    ModelValidator modelValidator;

    /**
     * The artifact factory.
     * 
     * @component
     */
    ArtifactFactory artifactFactory;

    /**
     * The artifact installer.
     * 
     * @component
     */
    ArtifactInstaller installer;

    /**
     * Digester for MD5.
     * 
     * @component role-hint="md5"
     */
    Digester md5Digester;

    /**
     * Digester for SHA-1.
     * 
     * @component role-hint="sha1"
     */
    Digester sha1Digester;

    /**
     * Flag whether to create checksums (MD5, SHA-1) or not.
     * 
     * @parameter expression="${createChecksum}" default-value="true"
     */
    boolean createChecksum;

    /**
     * Whether to update the metadata to make the artifact a release version.
     * 
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    boolean updateReleaseInfo;

    @Override
    String getMojoName()
    {
        return "install";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    void install(MavenProject project, MSBuildProject msbProject)
        throws MojoExecutionException
    {
        File artifactFile =
            new File(project.getBasedir(), msbProject.getBuildArtifact(
                getActiveBuildConfigurationName()).getPath());

        String gid = project.getGroupId();
        String aid = project.getArtifactId();
        String pkg = project.getPackaging();
        String ver = project.getVersion();

        Artifact artifact =
            this.artifactFactory.createArtifactWithClassifier(
                gid,
                aid,
                ver,
                pkg,
                null);

        ArtifactMetadata pomMetadata =
            new ProjectArtifactMetadata(artifact, project.getFile());
        artifact.addMetadata(pomMetadata);

        try
        {
            this.installer.install(
                artifactFile,
                artifact,
                super.localRepository);
            installChecksums(artifact);
            copyLessVer(artifact);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(String.format(
                "Error installing primary build artifact: %s",
                artifactFile), e);
        }

        File docFile =
            new File(project.getBasedir(), msbProject
                .getBuildDocumentationArtifact(
                    getActiveBuildConfigurationName())
                .getPath());

        if (docFile != null && docFile.exists())
        {
            artifact =
                artifactFactory.createArtifactWithClassifier(
                    gid,
                    aid,
                    ver,
                    "xml",
                    null);

            try
            {
                installer.install(docFile, artifact, localRepository);
                installChecksums(artifact);
                copyLessVer(artifact);
            }
            catch (Exception e)
            {
                throw new MojoExecutionException(String.format(
                    "Error installing documentation artifact: %s",
                    docFile), e);
            }
        }

        File pdbFile =
            new File(project.getBasedir(), msbProject.getBuildSymbolsArtifact(
                getActiveBuildConfigurationName()).getPath());

        if (pdbFile != null && pdbFile.exists())
        {
            artifact =
                artifactFactory.createArtifactWithClassifier(
                    gid,
                    aid,
                    ver,
                    "pdb",
                    null);

            try
            {
                installer.install(pdbFile, artifact, localRepository);
                installChecksums(artifact);
                copyLessVer(artifact);
            }
            catch (Exception e)
            {
                throw new MojoExecutionException(String.format(
                    "Error installing symbols artifact: %s",
                    docFile), e);
            }
        }
    }

    void copyLessVer(Artifact artifact) throws IOException
    {
        String path = localRepository.pathOf(artifact);
        File afile = new File(localRepository.getBasedir(), path);
        String name = afile.getName();
        name = name.replace("-" + artifact.getVersion(), "");
        File lessver = new File(afile.getParentFile(), name);
        FileUtils.copyFile(afile, lessver);

        String ext = FilenameUtils.getExtension(name);

        File dotnetfile =
            new File(afile.getParentFile(), String.format(
                "%s.%s",
                super.msbuildProject.getAssemblyName(),
                ext));
        FileUtils.copyFile(afile, dotnetfile);
    }

    @Override
    void nvnExecute() throws MojoExecutionException
    {
        install(this.mavenProject, this.msbuildProject);
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        // Do nothing
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    /**
     * Installs the checksums for the specified artifact (and its metadata
     * files) if this has been enabled in the plugin configuration. This method
     * creates checksums for files that have already been installed to the local
     * repo to account for on-the-fly generated/updated files. For example, in
     * Maven 2.0.4- the <code>ProjectArtifactMetadata</code> did not install the
     * original POM file (cf. MNG-2820). While the plugin currently requires
     * Maven 2.0.6, we continue to hash the installed POM for robustness with
     * regard to future changes like re-introducing some kind of POM filtering.
     * 
     * @param artifact The artifact for which to create checksums, must not be
     *        <code>null</code>.
     * @throws MojoExecutionException If the checksums could not be installed.
     */
    @SuppressWarnings("unchecked")
    protected void installChecksums(Artifact artifact)
        throws MojoExecutionException
    {
        if (!createChecksum)
        {
            return;
        }

        File artifactFile = getLocalRepoFile(artifact);
        installChecksums(artifactFile);

        Collection metadatas = artifact.getMetadataList();
        if (metadatas != null)
        {
            for (Iterator it = metadatas.iterator(); it.hasNext();)
            {
                ArtifactMetadata metadata = (ArtifactMetadata) it.next();
                File metadataFile = getLocalRepoFile(metadata);
                installChecksums(metadataFile);
            }
        }
    }

    /**
     * Installs the checksums for the specified file (if it exists).
     * 
     * @param installedFile The path to the already installed file in the local
     *        repo for which to generate checksums, must not be
     *        <code>null</code>.
     * @throws MojoExecutionException If the checksums could not be installed.
     */
    private void installChecksums(File installedFile)
        throws MojoExecutionException
    {
        boolean signatureFile = installedFile.getName().endsWith(".asc");
        if (installedFile.isFile() && !signatureFile)
        {
            installChecksum(installedFile, installedFile, md5Digester, ".md5");
            installChecksum(installedFile, installedFile, sha1Digester, ".sha1");
        }
    }

    /**
     * Installs a checksum for the specified file.
     * 
     * @param originalFile The path to the file from which the checksum is
     *        generated, must not be <code>null</code>.
     * @param installedFile The base path from which the path to the checksum
     *        files is derived by appending the given file extension, must not
     *        be <code>null</code>.
     * @param digester The checksum algorithm to use, must not be
     *        <code>null</code>.
     * @param ext The file extension (including the leading dot) to use for the
     *        checksum file, must not be <code>null</code>.
     * @throws MojoExecutionException If the checksum could not be installed.
     */
    private void installChecksum(
        File originalFile,
        File installedFile,
        Digester digester,
        String ext) throws MojoExecutionException
    {
        String checksum;
        getLog().debug(
            "Calculating " + digester.getAlgorithm() + " checksum for "
                + originalFile);
        try
        {
            checksum = digester.calc(originalFile);
        }
        catch (DigesterException e)
        {
            throw new MojoExecutionException("Failed to calculate "
                + digester.getAlgorithm() + " checksum for " + originalFile, e);
        }

        File checksumFile = new File(installedFile.getAbsolutePath() + ext);
        getLog().debug("Installing checksum to " + checksumFile);
        try
        {
            checksumFile.getParentFile().mkdirs();
            FileUtils.writeStringToFile(checksumFile, checksum, "UTF-8");
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Failed to install checksum to "
                + checksumFile, e);
        }
    }

    /**
     * Gets the path of the specified artifact metadata within the local
     * repository. Note that the returned path need not exist (yet).
     * 
     * @param metadata The artifact metadata whose local repo path should be
     *        determined, must not be <code>null</code>.
     * @return The absolute path to the artifact metadata when installed, never
     *         <code>null</code>.
     */
    protected File getLocalRepoFile(ArtifactMetadata metadata)
    {
        String path =
            localRepository.pathOfLocalRepositoryMetadata(
                metadata,
                localRepository);
        return new File(localRepository.getBasedir(), path);
    }

    /**
     * Gets the path of the specified artifact within the local repository. Note
     * that the returned path need not exist (yet).
     * 
     * @param artifact The artifact whose local repo path should be determined,
     *        must not be <code>null</code>.
     * @return The absolute path to the artifact when installed, never
     *         <code>null</code>.
     */
    protected File getLocalRepoFile(Artifact artifact)
    {
        String path = localRepository.pathOf(artifact);
        return new File(localRepository.getBasedir(), path);
    }
}
