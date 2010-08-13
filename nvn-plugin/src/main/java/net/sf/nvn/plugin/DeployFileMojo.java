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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * <p>
 * A MOJO for deploying .NET artifacts.
 * </p>
 * <p>
 * This MOJO was straight-stolen from the Maven Deploy Plug-in:
 * </p>
 * <ul>
 * <li><a href="http://bit.ly/aXDscR">AbstractDeployMojo</a></li>
 * <li><a href="http://bit.ly/dqGb60">DeployFileMojo</a></li>
 * </ul>
 * 
 * @author akutz
 * 
 * @goal deploy-file
 * @requiresProject false
 * @description A MOJO for deploying .NET artifacts.
 */
public class DeployFileMojo extends AbstractMojo
{
    /**
     * @parameter expression="${assemblyName}"
     */
    String assemblyName;

    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * Component used to create an artifact.
     * 
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * Component used to create a repository.
     * 
     * @component
     */
    ArtifactRepositoryFactory repositoryFactory;

    /**
     * Map that contains the layouts.
     * 
     * @component role=
     *            "org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    @SuppressWarnings("rawtypes")
    private Map repositoryLayouts;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Flag whether Maven is currently in online/offline mode.
     * 
     * @parameter default-value="${settings.offline}"
     * @readonly
     */
    private boolean offline;

    /**
     * Parameter used to update the metadata to make the artifact as release.
     * 
     * @parameter expression="${updateReleaseInfo}" default-value="false"
     */
    protected boolean updateReleaseInfo;

    /* Setters and Getters */

    public ArtifactDeployer getDeployer()
    {
        return deployer;
    }

    public void setDeployer(ArtifactDeployer deployer)
    {
        this.deployer = deployer;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public void setLocalRepository(ArtifactRepository localRepository)
    {
        this.localRepository = localRepository;
    }

    void failIfOffline() throws MojoFailureException
    {
        if (offline)
        {
            throw new MojoFailureException(
                "Cannot deploy artifacts when Maven is in offline mode");
        }
    }

    ArtifactRepositoryLayout getLayout(String id) throws MojoExecutionException
    {
        ArtifactRepositoryLayout layout =
            (ArtifactRepositoryLayout) repositoryLayouts.get(id);

        if (layout == null)
        {
            throw new MojoExecutionException("Invalid repository layout: " + id);
        }

        return layout;
    }

    /**
     * GroupId of the artifact to be deployed. Retrieved from POM file if
     * specified.
     * 
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * ArtifactId of the artifact to be deployed. Retrieved from POM file if
     * specified.
     * 
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * Version of the artifact to be deployed. Retrieved from POM file if
     * specified.
     * 
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * Type of the artifact to be deployed. Retrieved from POM file if
     * specified.
     * 
     * @parameter expression="${packaging}"
     */
    private String packaging;

    /**
     * Description passed to a generated POM file (in case of generatePom=true)
     * 
     * @parameter expression="${generatePom.description}"
     */
    private String description;

    /**
     * File to be deployed.
     * 
     * @parameter expression="${file}"
     * @required
     */
    private File file;

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of
     * settings.xml In most cases, this parameter will be required for
     * authentication.
     * 
     * @parameter expression="${repositoryId}" default-value="remote-repository"
     * @required
     */
    private String repositoryId;

    /**
     * The type of remote repository layout to deploy to. Try <i>legacy</i> for
     * a Maven 1.x-style repository layout.
     * 
     * @parameter expression="${repositoryLayout}" default-value="default"
     * @required
     */
    private String repositoryLayout;

    /**
     * URL where the artifact will be deployed. <br/>
     * ie ( file://C:\m2-repo or scp://host.com/path/to/repo )
     * 
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    /**
     * Location of an existing POM file to be deployed alongside the main
     * artifact, given by the ${file} parameter.
     * 
     * @parameter expression="${pomFile}"
     */
    private File pomFile;

    /**
     * Upload a POM for this artifact. Will generate a default POM if none is
     * supplied with the pomFile argument.
     * 
     * @parameter expression="${generatePom}" default-value="true"
     */
    private boolean generatePom;

    /**
     * Add classifier to the artifact
     * 
     * @parameter expression="${classifier}";
     */
    private String classifier;

    /**
     * Whether to deploy snapshots with a unique version or not.
     * 
     * @parameter expression="${uniqueVersion}" default-value="true"
     */
    private boolean uniqueVersion;

    /**
     * The component used to validate the user-supplied artifact coordinates.
     * 
     * @component
     */
    private ModelValidator modelValidator;

    void initProperties() throws MojoExecutionException
    {
        // Process the supplied POM (if there is one)
        if (pomFile != null)
        {
            generatePom = false;

            Model model = readModel(pomFile);

            processModel(model);
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        failIfOffline();

        initProperties();

        validateArtifactInformation();

        if (!file.exists())
        {
            throw new MojoExecutionException(file.getPath() + " not found.");
        }

        ArtifactRepositoryLayout layout = getLayout(repositoryLayout);

        ArtifactRepository deploymentRepository =
            repositoryFactory.createDeploymentArtifactRepository(
                repositoryId,
                url,
                layout,
                uniqueVersion);

        String protocol = deploymentRepository.getProtocol();

        if (StringUtils.isEmpty(protocol))
        {
            throw new MojoExecutionException("No transfer protocol found.");
        }

        // Create the artifact
        Artifact artifact =
            artifactFactory.createArtifactWithClassifier(
                groupId,
                artifactId,
                version,
                packaging,
                classifier);

        if (StringUtils.isEmpty(this.assemblyName))
        {
            this.assemblyName = FilenameUtils.getBaseName(this.file.getName());
        }

        NvnArtifactMetadata nmd;

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
                "Cannot deploy artifact from the local repository: " + file);
        }

        // Upload the POM if requested, generating one if need be
        if (!"pom".equals(packaging))
        {
            if (pomFile != null)
            {
                ArtifactMetadata metadata =
                    new ProjectArtifactMetadata(artifact, pomFile);
                artifact.addMetadata(metadata);
            }
            else if (generatePom)
            {
                ArtifactMetadata metadata =
                    new ProjectArtifactMetadata(artifact, generatePomFile());
                artifact.addMetadata(metadata);
            }
        }

        if (updateReleaseInfo)
        {
            artifact.setRelease(true);
        }

        try
        {
            getDeployer().deploy(
                file,
                artifact,
                deploymentRepository,
                getLocalRepository());
        }
        catch (ArtifactDeploymentException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
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
    private File getLocalRepoFile(Artifact artifact)
    {
        String path = getLocalRepository().pathOf(artifact);
        return new File(getLocalRepository().getBasedir(), path);
    }

    /**
     * Process the supplied pomFile to get groupId, artifactId, version, and
     * packaging
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
     * Extract the model from the specified POM file.
     * 
     * @param pomFile The path of the POM file to parse, must not be
     *        <code>null</code>.
     * @return The model from the POM file, never <code>null</code>.
     * @throws MojoExecutionException If the file doesn't exist of cannot be
     *         read.
     */
    Model readModel(File pomFile) throws MojoExecutionException
    {
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader(pomFile);
            return new MavenXpp3Reader().read(reader);
        }
        catch (FileNotFoundException e)
        {
            throw new MojoExecutionException("POM not found " + pomFile, e);
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

    private File generatePomFile() throws MojoExecutionException
    {
        Model model = generateModel();

        Writer fw = null;
        try
        {
            File tempFile = File.createTempFile("mvndeploy", ".pom");
            tempFile.deleteOnExit();

            fw = WriterFactory.newXmlWriter(tempFile);
            new MavenXpp3Writer().write(fw, model);

            return tempFile;
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(
                "Error writing temporary pom file: " + e.getMessage(),
                e);
        }
        finally
        {
            IOUtil.close(fw);
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

        model.setDescription(description);

        return model;
    }

    void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    void setVersion(String version)
    {
        this.version = version;
    }

    void setPackaging(String packaging)
    {
        this.packaging = packaging;
    }

    void setPomFile(File pomFile)
    {
        this.pomFile = pomFile;
    }

    String getGroupId()
    {
        return groupId;
    }

    String getArtifactId()
    {
        return artifactId;
    }

    String getVersion()
    {
        return version;
    }

    String getPackaging()
    {
        return packaging;
    }

    File getFile()
    {
        return file;
    }

    String getClassifier()
    {
        return classifier;
    }

    void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }
}
