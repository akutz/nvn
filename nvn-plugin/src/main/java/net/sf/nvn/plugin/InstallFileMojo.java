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

package net.sf.nvn.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.project.validation.ModelValidationResult;
import org.apache.maven.project.validation.ModelValidator;
import org.codehaus.plexus.digest.Digester;
import org.codehaus.plexus.digest.DigesterException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * <p>
 * A Maven plug-in for installing .NET artifacts.
 * </p>
 * <p>
 * This plug-in was straight-stolen from the Maven Install Plug-in:
 * </p>
 * <ul>
 * <li><a href="http://bit.ly/9YVlv5">AbstractInstallMojo</a></li>
 * <li><a href="http://bit.ly/cpy5Ia">InstallFileMojo</a></li>
 * </ul>
 * 
 * @author akutz
 * 
 * @goal install-file
 * @requiresProject false
 * @requiresDependencyResolution
 * @description A Maven plug-in for installing .NET artifacts.
 */
public class InstallFileMojo extends AbstractMojo
{

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactInstaller installer;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * Flag whether to create checksums (MD5, SHA-1) or not.
     * 
     * @parameter expression="${createChecksum}" default-value="false"
     * @since 2.2
     */
    protected boolean createChecksum;

    /**
     * Whether to update the metadata to make the artifact a release version.
     * 
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    protected boolean updateReleaseInfo;

    /**
     * Digester for MD5.
     * 
     * @component role-hint="md5"
     */
    protected Digester md5Digester;

    /**
     * Digester for SHA-1.
     * 
     * @component role-hint="sha1"
     */
    protected Digester sha1Digester;

    /**
     * GroupId of the artifact to be installed. Retrieved from POM file if one
     * is specified.
     * 
     * @parameter expression="${groupId}"
     */
    protected String groupId;

    /**
     * ArtifactId of the artifact to be installed. Retrieved from POM file if
     * one is specified.
     * 
     * @parameter expression="${artifactId}"
     */
    protected String artifactId;

    /**
     * Version of the artifact to be installed. Retrieved from POM file if one
     * is specified.
     * 
     * @parameter expression="${version}"
     */
    protected String version;

    /**
     * Packaging type of the artifact to be installed. Retrieved from POM file
     * if one is specified.
     * 
     * @parameter expression="${packaging}"
     */
    protected String packaging;

    /**
     * Classifier type of the artifact to be installed. For example, "sources"
     * or "javadoc". Defaults to none which means this is the project's main
     * artifact.
     * 
     * @parameter expression="${classifier}"
     * @since 2.2
     */
    protected String classifier;

    /**
     * The file to be installed in the local repository.
     * 
     * @parameter expression="${file}"
     * @required
     */
    private File file;

    /**
     * The name of the artifact's associated PDB file.
     * 
     * @parameter expression="${pdbFile}"
     */
    File pdbFile;

    /**
     * The name of the artifact's associated XML documentation file.
     * 
     * @parameter expression="${docFile}"
     */
    File docFile;

    /**
     * The artifact's .NET AssemblyName.
     * 
     * @parameter expression="${assemblyName}"
     */
    String assemblyName;

    /**
     * Location of an existing POM file to be installed alongside the main
     * artifact, given by the {@link #file} parameter.
     * 
     * @parameter expression="${pomFile}"
     * @since 2.1
     */
    private File pomFile;

    /**
     * Generate a minimal POM for the artifact if none is supplied via the
     * parameter {@link #pomFile}. Defaults to <code>true</code> if there is no
     * existing POM in the local repository yet.
     * 
     * @parameter expression="${generatePom}"
     * @since 2.1
     */
    private Boolean generatePom;

    /**
     * The type of remote repository layout to install to. Try
     * <code>legacy</code> for a Maven 1.x-style repository layout.
     * 
     * @parameter expression="${repositoryLayout}" default-value="default"
     * @required
     * @since 2.2
     */
    private String repositoryLayout;

    /**
     * Map that contains the repository layouts.
     * 
     * @component role=
     *            "org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    @SuppressWarnings("rawtypes")
    private Map repositoryLayouts;

    /**
     * The path for a specific local repository directory. If not specified the
     * local repository path configured in the Maven settings will be used.
     * 
     * @parameter expression="${localRepositoryPath}"
     * @since 2.2
     */
    private File localRepositoryPath;

    /**
     * The component used to validate the user-supplied artifact coordinates.
     * 
     * @component
     */
    private ModelValidator modelValidator;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @SuppressWarnings("deprecation")
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // ----------------------------------------------------------------------
        // Override the default localRepository variable
        // ----------------------------------------------------------------------
        if (localRepositoryPath != null)
        {
            try
            {
                ArtifactRepositoryLayout layout =
                    (ArtifactRepositoryLayout) repositoryLayouts
                        .get(repositoryLayout);
                getLog().debug("Layout: " + layout.getClass());

                localRepository =
                    new DefaultArtifactRepository(
                        localRepository.getId(),
                        localRepositoryPath.toURL().toString(),
                        layout);
            }
            catch (MalformedURLException e)
            {
                throw new MojoExecutionException("MalformedURLException: "
                    + e.getMessage(), e);
            }
        }

        if (pomFile != null)
        {
            processModel(readModel(pomFile));
        }

        validateArtifactInformation();

        Artifact artifact =
            artifactFactory.createArtifactWithClassifier(
                groupId,
                artifactId,
                version,
                packaging,
                classifier);
        
        NvnArtifactMetadata nmd;
        
        if (StringUtils.isEmpty(this.assemblyName))
        {
            this.assemblyName = FilenameUtils.getBaseName(this.file.getName());
        }

        try
        {
           nmd = NvnArtifactMetadata.instance(artifact, this.assemblyName);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error creating NvnArtifactMetadata",
                e);
        }

        artifact.addMetadata(nmd);

        if (file.equals(getLocalRepoFile(artifact)))
        {
            throw new MojoFailureException(
                "Cannot install artifact. "
                    + "Artifact is already in the local repository.\n\nFile in question is: "
                    + file + "\n");
        }

        File generatedPomFile = null;

        if (!"pom".equals(packaging))
        {
            if (pomFile != null)
            {
                ArtifactMetadata pomMetadata =
                    new ProjectArtifactMetadata(artifact, pomFile);
                artifact.addMetadata(pomMetadata);
            }
            else
            {
                generatedPomFile = generatePomFile();
                ArtifactMetadata pomMetadata =
                    new ProjectArtifactMetadata(artifact, generatedPomFile);
                if (Boolean.TRUE.equals(generatePom)
                    || (generatePom == null && !getLocalRepoFile(pomMetadata)
                        .exists()))
                {
                    getLog().debug("Installing generated POM");
                    artifact.addMetadata(pomMetadata);
                }
                else if (generatePom == null)
                {
                    getLog()
                        .debug(
                            "Skipping installation of generated POM, already present in local repository");
                }
            }
        }

        if (updateReleaseInfo)
        {
            artifact.setRelease(true);
        }

        try
        {
            installer.install(file, artifact, localRepository);
            installChecksums(artifact);
        }
        catch (ArtifactInstallationException e)
        {
            throw new MojoExecutionException(
                "Error installing artifact '"
                    + artifact.getDependencyConflictId() + "': "
                    + e.getMessage(),
                e);
        }
        finally
        {
            if (generatedPomFile != null)
            {
                generatedPomFile.delete();
            }
        }

        if (pdbFile != null)
        {
            artifact =
                artifactFactory.createArtifactWithClassifier(
                    groupId,
                    artifactId,
                    version,
                    "pdb",
                    "sources");
            try
            {
                installer.install(pdbFile, artifact, localRepository);
                installChecksums(artifact);
            }
            catch (ArtifactInstallationException e)
            {
                throw new MojoExecutionException("Error installing sources "
                    + pdbFile + ": " + e.getMessage(), e);
            }
        }

        if (docFile != null)
        {
            artifact =
                artifactFactory.createArtifactWithClassifier(
                    groupId,
                    artifactId,
                    version,
                    "xml",
                    "dotnetdoc");
            try
            {
                installer.install(docFile, artifact, localRepository);
                installChecksums(artifact);
            }
            catch (ArtifactInstallationException e)
            {
                throw new MojoExecutionException("Error installing API docs "
                    + docFile + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Parses a POM.
     * 
     * @param pomFile The path of the POM file to parse, must not be
     *        <code>null</code>.
     * @return The model from the POM file, never <code>null</code>.
     * @throws MojoExecutionException If the POM could not be parsed.
     */
    private Model readModel(File pomFile) throws MojoExecutionException
    {
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader(pomFile);
            return new MavenXpp3Reader().read(reader);
        }
        catch (FileNotFoundException e)
        {
            throw new MojoExecutionException("File not found " + pomFile, e);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error reading POM " + pomFile, e);
        }
        catch (XmlPullParserException e)
        {
            throw new MojoExecutionException("Error parsing POM " + pomFile, e);
        }
        finally
        {
            IOUtil.close(reader);
        }
    }

    /**
     * Populates missing mojo parameters from the specified POM.
     * 
     * @param model The POM to extract missing artifact coordinates from, must
     *        not be <code>null</code>.
     */
    private void processModel(Model model)
    {
        Parent parent = model.getParent();

        if (this.groupId == null)
        {
            this.groupId = model.getGroupId();
            if (this.groupId == null && parent != null)
            {
                this.groupId = parent.getGroupId();
            }
        }
        if (this.artifactId == null)
        {
            this.artifactId = model.getArtifactId();
        }
        if (this.version == null)
        {
            this.version = model.getVersion();
            if (this.version == null && parent != null)
            {
                this.version = parent.getVersion();
            }
        }
        if (this.packaging == null)
        {
            this.packaging = model.getPackaging();
        }
    }

    /**
     * Validates the user-supplied artifact information.
     * 
     * @throws MojoExecutionException If any artifact coordinate is invalid.
     */
    private void validateArtifactInformation() throws MojoExecutionException
    {
        Model model = generateModel();

        ModelValidationResult result = modelValidator.validate(model);

        if (result.getMessageCount() > 0)
        {
            throw new MojoExecutionException(
                "The artifact information is incomplete or not valid:\n"
                    + result.render("  "));
        }
    }

    /**
     * Generates a minimal model from the user-supplied artifact information.
     * 
     * @return The generated model, never <code>null</code>.
     */
    private Model generateModel()
    {
        Model model = new Model();

        model.setModelVersion("4.0.0");

        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);

        model.setDescription("POM was created from install:install-file");

        return model;
    }

    /**
     * Generates a (temporary) POM file from the plugin configuration. It's the
     * responsibility of the caller to delete the generated file when no longer
     * needed.
     * 
     * @return The path to the generated POM file, never <code>null</code>.
     * @throws MojoExecutionException If the POM file could not be generated.
     */
    private File generatePomFile() throws MojoExecutionException
    {
        Model model = generateModel();

        Writer writer = null;
        try
        {
            File pomFile = File.createTempFile("mvninstall", ".pom");

            writer = WriterFactory.newXmlWriter(pomFile);
            new MavenXpp3Writer().write(writer, model);

            return pomFile;
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error writing temporary POM file: " + e.getMessage(),
                e);
        }
        finally
        {
            IOUtil.close(writer);
        }
    }

    /**
     * @return the localRepositoryPath
     */
    public File getLocalRepositoryPath()
    {
        return this.localRepositoryPath;
    }

    /**
     * @param theLocalRepositoryPath the localRepositoryPath to set
     */
    public void setLocalRepositoryPath(File theLocalRepositoryPath)
    {
        this.localRepositoryPath = theLocalRepositoryPath;
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
    @SuppressWarnings("rawtypes")
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
            FileUtils.fileWrite(
                checksumFile.getAbsolutePath(),
                "UTF-8",
                checksum);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Failed to install checksum to "
                + checksumFile, e);
        }
    }
}
