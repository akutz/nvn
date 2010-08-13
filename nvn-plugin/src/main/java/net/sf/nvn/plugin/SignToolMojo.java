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
import java.net.URL;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * <p>
 * A MOJO for running the Microsoft signing tool (signtool.exe). Currently only
 * the "sign" command is supported.
 * </p>
 * 
 * @author akutz
 * 
 * @goal signtool
 * @phase package
 * @requiresDependencyResolution compile
 * @description A MOJO for running the Microsoft signing tool (signtool.exe).
 *              Currently only the "sign" command is supported.
 */
public class SignToolMojo extends AbstractExeMojo
{
    /**
     * The files to sign.
     * 
     * @parameter
     */
    File[] files;

    /**
     * Select the best signing cert automatically. SignTool will find all valid
     * certs that satisfy all specified conditions and select the one that is
     * valid for the longest. If this option is not present, SignTool will
     * expect to find only one valid signing cert.
     * 
     * @parameter
     */
    boolean autoSelect;

    /**
     * Add an additional certificate to the signature block.
     * 
     * @parameter
     */
    File additionalCert;

    /**
     * Specify the Certificate Template Name (Microsoft extension) of the
     * signing cert.
     * 
     * @parameter
     */
    String certTemplateName;

    /**
     * Specify the signing cert in a file. If this file is a PFX with a
     * password, the password may be supplied with the <strong>password</strong>
     * parameter. If the file does not contain private keys, use the
     * <strong>cryptoServiceProvider</strong> and <strong>keyContainer</strong>
     * parameters to specify the CSP and container name of the private key.
     * 
     * @parameter
     */
    File certificate;

    /**
     * Specify the Issuer of the signing cert, or a substring.
     * 
     * @parameter
     */
    String issuerName;

    /**
     * Specify the Subject Name of the signing cert, or a substring.
     * 
     * @parameter
     */
    String subjectName;

    /**
     * Specify a password to use when opening the PFX file.
     * 
     * @parameter
     */
    String password;

    /**
     * Specify the Subject Name of a Root cert that the signing cert must chain
     * to.
     * 
     * @parameter
     */
    String rootCertSubjectName;

    /**
     * Specify the Store to open when searching for the cert. The default is the
     * "MY" Store.
     * 
     * @parameter
     */
    String certStoreName;

    /**
     * Open a Machine store instead of a User store.
     * 
     * @parameter
     */
    boolean useMachineStore;

    /**
     * Specify the SHA1 hash of the signing cert.
     * 
     * @parameter
     */
    String sha1Hash;

    /**
     * Specifies the file digest algorithm to use for creating file signatures.
     * (Default is SHA1)
     * 
     * @parameter
     */
    boolean fileDigestAlgorithm;

    /**
     * Specify the Enhanced Key Usage that must be present in the cert. The
     * parameter may be specified by OID or by string. The default usage is
     * "Code Signing" (1.3.6.1.5.5.7.3.3).
     * 
     * @parameter
     */
    String enhancedKeyUsage;

    /**
     * Specify usage of "Windows System Component Verification"
     * (1.3.6.1.4.1.311.10.3.6).
     * 
     * @parameter
     */
    boolean useWinSysCompVer;

    /**
     * Specify the CSP containing the Private Key Container.
     * 
     * @parameter
     */
    String cryptoServiceProvider;

    /**
     * Specify the Key Container Name of the Private Key.
     * 
     * @parameter
     */
    String keyContainer;

    /**
     * Provide a description of the signed content.
     * 
     * @parameter
     */
    String description;

    /**
     * Provide a URL with more information about the signed content.
     * 
     * @parameter
     */
    URL descriptionUrl;

    /**
     * Specify the timestamp server's URL. If this option is not present, the
     * signed file will not be timestamped. A warning is generated if
     * timestamping fails.
     * 
     * @parameter
     */
    URL timestampUrl;

    /**
     * Specifies the RFC 3161 timestamp server's URL. If this parameter or the
     * timestampUrl parameter is not specified, the signed file will not be
     * timestamped. A warning is generated if timestamping fails. This parameter
     * cannot be used with the timestampUrl parameter, if it is, the
     * timestampUrl parameter takes precedence.
     * 
     * @parameter
     */
    URL rfc3161TimestampUrl;

    /**
     * Used with the rfc3161TimestampUrl parameter to request a digest algorithm
     * used by the RFC 3161 timestamp server.
     * 
     * @parameter
     */
    String rfc3161Algorithm;

    /**
     * Generate page hashes for executable files if supported.
     * 
     * @parameter
     */
    boolean generatePageHashes;

    /**
     * Suppress page hashes for executable files if supported. The default is
     * determined by the SIGNTOOL_PAGE_HASHES environment variable and by the
     * wintrust.dll version. No output on success and minimal output on failure.
     * As always,
     * 
     * @parameter
     */
    boolean suppressPageHashes;

    /**
     * No output on success and minimal output on failure. As always, SignTool
     * returns 0 on success, 1 on failure, and 2 on warning.
     * 
     * @parameter
     */
    boolean quiet;

    /**
     * Print verbose success and status messages. This may also provide slightly
     * more information on error.
     * 
     * @parameter
     */
    boolean verbose;

    @Override
    String getArgs(int execution)
    {
        StringBuilder buff = new StringBuilder();

        buff.append("sign");
        buff.append(" ");

        if (this.autoSelect)
        {
            buff.append("/a");
            buff.append(" ");
        }

        if (this.additionalCert != null)
        {
            buff.append("/ac");
            buff.append(" ");
            buff.append(quote(this.additionalCert.toString()));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.certTemplateName))
        {
            buff.append("/c");
            buff.append(" ");
            buff.append(quote(this.certTemplateName));
            buff.append(" ");
        }

        if (this.certificate != null)
        {
            buff.append("/f");
            buff.append(" ");
            buff.append(quote(this.certificate.toString()));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.issuerName))
        {
            buff.append("/i");
            buff.append(" ");
            buff.append(quote(this.issuerName));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.subjectName))
        {
            buff.append("/n");
            buff.append(" ");
            buff.append(quote(this.subjectName));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.password))
        {
            buff.append("/p");
            buff.append(" ");
            buff.append(quote(this.password));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.rootCertSubjectName))
        {
            buff.append("/r");
            buff.append(" ");
            buff.append(quote(this.rootCertSubjectName));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.certStoreName))
        {
            buff.append("/s");
            buff.append(" ");
            buff.append(quote(this.certStoreName));
            buff.append(" ");
        }

        if (this.useMachineStore)
        {
            buff.append("/sm");
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.sha1Hash))
        {
            buff.append("/sha1");
            buff.append(" ");
            buff.append(quote(this.sha1Hash));
            buff.append(" ");
        }

        if (this.fileDigestAlgorithm)
        {
            buff.append("/fd");
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.enhancedKeyUsage))
        {
            buff.append("/u");
            buff.append(" ");
            buff.append(quote(this.enhancedKeyUsage));
            buff.append(" ");
        }

        if (this.useWinSysCompVer)
        {
            buff.append("/uw");
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.cryptoServiceProvider))
        {
            buff.append("/csp");
            buff.append(" ");
            buff.append(quote(this.cryptoServiceProvider));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.keyContainer))
        {
            buff.append("/kc");
            buff.append(" ");
            buff.append(quote(this.keyContainer));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.description))
        {
            buff.append("/d");
            buff.append(" ");
            buff.append(quote(this.description));
            buff.append(" ");
        }

        if (this.descriptionUrl != null)
        {
            buff.append("/du");
            buff.append(" ");
            buff.append(quote(this.descriptionUrl.toString()));
            buff.append(" ");
        }

        if (this.timestampUrl != null)
        {
            buff.append("/t");
            buff.append(" ");
            buff.append(quote(this.timestampUrl.toString()));
            buff.append(" ");
        }

        if (this.rfc3161TimestampUrl != null)
        {
            buff.append("/tr");
            buff.append(" ");
            buff.append(quote(this.rfc3161TimestampUrl.toString()));
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.rfc3161Algorithm))
        {
            buff.append("/td");
            buff.append(" ");
            buff.append(quote(this.rfc3161Algorithm));
            buff.append(" ");
        }

        for (File f : this.files)
        {
            buff.append(quote(f.toString()));
            buff.append(" ");
        }

        if (this.generatePageHashes)
        {
            buff.append("/ph");
            buff.append(" ");
        }

        if (this.suppressPageHashes)
        {
            buff.append("/nph");
            buff.append(" ");
        }

        if (this.quiet)
        {
            buff.append("/q");
            buff.append(" ");
        }

        if (this.verbose)
        {
            buff.append("/v");
            buff.append(" ");
        }

        return buff.toString();
    }

    @Override
    File getDefaultCommand()
    {
        return new File("signtool.exe");
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        String lightOutPath =
            super.mavenProject.getProperties().getProperty("light.output");

        if (StringUtils.isNotEmpty(lightOutPath))
        {
            File lightOut = new File(lightOutPath);
            this.files = new File[]
            {
                lightOut
            };
            debug("Using light out file: " + lightOut);
        }
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
        return "signtool";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        if (this.files == null || this.files.length == 0)
        {
            debug("Not executing because files to sign are null or empty");
            return false;
        }

        if (this.certificate == null && !this.autoSelect)
        {
            debug("Not executing because certificate file is null and autoSelect is false");
            return false;
        }

        return true;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

}
