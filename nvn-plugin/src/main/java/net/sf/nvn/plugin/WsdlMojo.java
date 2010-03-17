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

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * <p>
 * A Maven plug-in for running the Microsoft wsdl.exe utility.
 * </p>
 * <p>
 * See <a
 * href="http://msdn.microsoft.com/en-us/library/7h3ystb6.aspx">wsdl.exe</a> for
 * a complete set of documentation on the tool that this plug-in drives.
 * </p>
 * 
 * @author akutz
 * 
 * @goal wsdl
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @description A Maven plug-in for running the Microsoft wsdl.exe utility.
 */
public class WsdlMojo extends AbstractExeMojo
{
    /**
     * <p>
     * The URL to a WSDL contract file (.wsdl), XSD schema file (.xsd), or
     * discovery document (.disco). Note that you cannot specify a URL to a
     * .discomap discovery document.
     * </p>
     * 
     * <p>
     * If you specify inputUrl then inputFile will be ignored.
     * </p>
     * 
     * @parameter
     */
    URL inputUrl;

    /**
     * <p>
     * The path to a local WSDL contract file (.wsdl), XSD schema file (.xsd),
     * or discovery document (.disco or .discomap).
     * </p>
     * <p>
     * Wsdl.exe does not retrieve includes and imports from the network when it
     * is given a local file. To enable Wsdl.exe to retrieve network resources
     * while processing a local file, pass a URL to the local file. For example,
     * the following file uses the network to retrieve necessary resources
     * <em>File:///E:/Customers/WSDLS/Accounts.wsdl</em>
     * </p>
     * <p>
     * If you specify inputUrl then inputFile will be ignored.
     * </p>
     * 
     * @parameter
     */
    File inputFile;

    /**
     * The default URL to use in the generated class file.
     * 
     * @parameter
     */
    URL defaultUrl;

    /**
     * <p>
     * Specifies the configuration key to use to read the default value for the
     * URL property when generating code.
     * </p>
     * <p>
     * When using the <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;appSettingsUrlKey&gt;</strong> element and contains a string.
     * </p>
     * 
     * @parameter
     */
    String appSettingsUrlKey;

    /**
     * <p>
     * Specifies the base URL to use when calculating the URL fragment.
     * </p>
     * <p>
     * The tool calculates the URL fragment by converting the relative URL from
     * the <em>baseurl</em> argument to the URL in the WSDL document. You must
     * specify the <strong>appSettingsUrlKey</strong> parameter with this
     * parameter. When using the <strong>parameters</strong> parameter, this
     * value is the <strong>&lt;appSettingBaseUrl&gt;</strong> element and
     * contains a string.
     * </p>
     * 
     * @parameter
     */
    String appSettingsBaseUrl;

    /**
     * Specifies the domain name to use when connecting to a server that
     * requires authentication. When using the <strong>parameters</strong>
     * parameter, this value is the <strong>&lt;domain&gt;</strong> element and
     * contains a string.
     * 
     * @parameter
     */
    String domain;

    /**
     * <p>
     * Specifies the language to use for the generated proxy class.
     * </p>
     * <p>
     * You can specify CS (C#; default), VB (Visual Basic), JS (JScript) or VJS
     * (Visual J#) as the language argument. You can also specify the
     * fully-qualified name of a class that implements the
     * <strong>System.CodeDom.Compiler.CodeDomProvider Class</strong>. When
     * using the <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;language&gt;</strong> element and contains a string.
     * </p>
     * 
     * @parameter
     */
    String language;

    /**
     * <p>
     * Specifies the namespace for the generated proxy or template.
     * </p>
     * <p>
     * The default namespace is the global namespace. When using the
     * <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;namespace&gt;</strong> element and contains a string. This
     * element must be in the parameters file.
     * </p>
     * 
     * @parameter
     */
    String namespace;

    /**
     * Suppresses the Microsoft startup banner display. When using the
     * <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;namespace&gt;</strong> element and contains either
     * <strong>true</strong> or <strong>false</strong>.
     * 
     * @parameter
     */
    boolean noLogo;

    /**
     * Generates explicit order identifiers on particle members.
     * 
     * @parameter
     */
    boolean order;

    /**
     * <p>
     * Specifies the file (or directory) in which to save the generated proxy
     * code.
     * </p>
     * <p>
     * You can also specify a directory in which to create this file. The tool
     * derives the default file name from the XML Web service name. The tool
     * saves generated datasets in different files. When using the
     * <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;out&gt;</strong> element and contains a string.
     * </p>
     * 
     * @parameter
     */
    File outputLocation;

    /**
     * <p>
     * Reads command-line options from the specified XML file.
     * </p>
     * <p>
     * Use this option to pass the Wsdl.exe tool a large number of options at
     * one time. Option elements are contained inside a
     * <strong>&lt;wsdlParameters
     * xmlns="http://microsoft.com/webReference/"&gt;</strong> element.
     * </p>
     * 
     * @parameter
     */
    File parameters;

    /**
     * Displays errors in a format similar to the error reporting format used by
     * language compilers. When using the <strong>parameters</strong> parameter,
     * this value is the <strong>&lt;parsablerrors&gt;</strong> element and is
     * either true or false.
     * 
     * @parameter
     */
    boolean parsableErrors;

    /**
     * Specifies the password to use when connecting to a server that requires
     * authentication. When using the <strong>parameters</strong> parameter,
     * this value is the <strong>&lt;password&gt;</strong> element and contains
     * a string.
     * 
     * @parameter
     */
    String password;

    /**
     * <p>
     * Specifies the protocol to implement.
     * </p>
     * <p>
     * You can specify SOAP (default), HttpGet, HttpPost, or a custom protocol
     * specified in the configuration file. When using the
     * <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;protocol&gt;</strong> element and contains a string.
     * </p>
     * 
     * @parameter
     */
    String protocol;

    /**
     * Specifies the URL of the proxy server to use for HTTP requests. The
     * default is to use the system proxy setting. When using the
     * <strong>parameters</strong> parameter, this value is the
     * <strong>&lt;proxy&gt;</strong> element and contains a string.
     * 
     * @parameter
     */
    URL proxy;

    /**
     * Specifies the domain to use when connecting to a proxy server that
     * requires authentication. When using the <strong>parameters</strong>
     * parameter, this value is the <strong>&lt;proxydomain&gt;</strong> element
     * and contains a string.
     * 
     * @parameter
     */
    String proxyDomain;

    /**
     * Specifies the password to use when connecting to a proxy server that
     * requires authentication. When using the <strong>parameters</strong>
     * parameter, this value is the <strong>&lt;proxypassword&gt;</strong>
     * element and contains a string.
     * 
     * @parameter
     */
    String proxyPassword;

    /**
     * Specifies the user name to use when connecting to a proxy server that
     * requires authentication. When using the <strong>parameters</strong>
     * parameter, this value is the <strong>&lt;proxyusername&gt;</strong>
     * element and contains a string.
     * 
     * @parameter
     */
    String proxyUserName;

    /**
     * <p>
     * Generates an abstract class for an XML Web service based on the
     * contracts.
     * </p>
     * The default is to generate client proxy classes. When using the
     * <strong>parameters</strong> parameter, this value is a
     * <strong>&lt;style&gt;</strong> element that contains "server".
     * 
     * @parameter
     */
    boolean server;

    /**
     * <p>
     * Generates interfaces for server implementation of an ASP.NET Web Service.
     * </p>
     * <p>
     * An interface is generated for each binding in the WSDL document(s). The
     * WSDL alone implements the WSDL contract (classes that implement the
     * interface should not include either of the following on the class
     * methods: Web Service attributes or Serialization attributes that change
     * the WSDL contract). When using the <strong>parameters</strong> parameter,
     * this value is a <strong>&lt;style&gt;</strong> element that contains
     * "serverInterface".
     * </p>
     * 
     * @parameter
     */
    boolean serverInterface;

    /**
     * <p>
     * Turns on the type sharing feature.
     * </p>
     * <p>
     * This feature creates one code file with a single type definition for
     * identical types shared between different services (the namespace, name,
     * and wire signature must be identical). Reference the services with
     * "http://" URLs as command-line parameters or create a discomap document
     * for local files. When using the <strong>parameters</strong> parameter,
     * this value is the <strong>&lt;sharestype&gt;</strong> element and is
     * either <strong>true</strong> or <strong>false</strong>.
     * </p>
     * 
     * @parameter
     */
    boolean shareTypes;

    /**
     * Specifies the user name to use when connecting to a server that requires
     * authentication. When using the <strong>parameters</strong> parameter,
     * this value is the <strong>&lt;username&gt;</strong> element and contains
     * a string.
     * 
     * @parameter
     */
    String userName;

    private File tempOutputFile;
    private File originalOutputLocation;

    @Override
    String getArgs(int execution)
    {
        StringBuffer args = new StringBuffer();

        if (StringUtils.isNotEmpty(this.appSettingsUrlKey))
        {
            args.append("/appsetingsurlkey:");
            args.append(quote(this.appSettingsUrlKey));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.appSettingsUrlKey))
        {
            args.append("/appsettingbaseurl:");
            args.append(quote(this.appSettingsUrlKey));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.language))
        {
            args.append("/language:");
            args.append(quote(this.language));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.namespace))
        {
            args.append("/namespace:");
            args.append(quote(this.namespace));
            args.append(" ");
        }

        if (this.noLogo)
        {
            args.append("/nologo ");
        }

        if (this.order)
        {
            args.append("/order ");
        }

        if (this.outputLocation != null)
        {
            args.append("/out:");
            args.append(getPath(this.outputLocation));
            args.append(" ");
        }

        if (this.parameters != null)
        {
            args.append("/parameters:");
            args.append(getPath(this.parameters));
            args.append(" ");
        }

        if (this.parsableErrors)
        {
            args.append("/parsableerrors ");
        }

        if (StringUtils.isNotEmpty(this.password))
        {
            args.append("/password:");
            args.append(quote(this.password));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.protocol))
        {
            args.append("/protocol:");
            args.append(quote(this.protocol));
            args.append(" ");
        }

        if (this.proxy != null)
        {
            args.append("/proxy:");
            args.append(quote(this.proxy.toString()));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.proxyDomain))
        {
            args.append("/proxydomain:");
            args.append(quote(this.proxyDomain));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.proxyPassword))
        {
            args.append("/proxypassword:");
            args.append(quote(this.proxyPassword));
            args.append(" ");
        }

        if (StringUtils.isNotEmpty(this.proxyUserName))
        {
            args.append("/proxyusername:");
            args.append(quote(this.proxyUserName));
            args.append(" ");
        }

        if (this.server)
        {
            args.append("/server ");
        }

        if (this.serverInterface)
        {
            args.append("/serverInterface ");
        }

        if (this.shareTypes)
        {
            args.append("/sharetypes ");
        }

        if (StringUtils.isNotEmpty(this.userName))
        {
            args.append("/username:");
            args.append(quote(this.userName));
            args.append(" ");
        }

        if (this.inputUrl != null)
        {
            args.append(quote(this.inputUrl.toString()));
        }
        else if (this.inputFile != null)
        {
            args.append(getPath(this.inputFile));
        }

        return args.toString();
    }

    @Override
    File getDefaultCommand()
    {
        return new File("wsdl.exe");
    }

    @Override
    String getMojoName()
    {
        return "wsdl";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (this.tempOutputFile != null)
        {
            if (this.defaultUrl != null)
            {
                String urlPatt =
                    "(?u)(.*\\s+/// <remarks\\/>.*\\s+public.*\\(\\)\\s\\{.*\\s+this.Url\\s=\\s\").*(\";.*\\s+\\}.*)";

                String tempContents;

                try
                {
                    tempContents =
                        FileUtils.readFileToString(this.tempOutputFile);
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException(String.format(
                        "Error reading the contents of %s",
                        this.tempOutputFile), e);
                }

                String tempContentsWithUrl =
                    tempContents.replaceAll(urlPatt, String.format(
                        "$1%s$2",
                        this.defaultUrl));

                try
                {
                    FileUtils.writeStringToFile(
                        this.tempOutputFile,
                        tempContentsWithUrl);
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException(String.format(
                        "Error writing contents with URL to %s",
                        this.tempOutputFile), e);
                }
            }

            boolean moveTempFile = false;

            if (this.originalOutputLocation.exists())
            {
                boolean equal;

                try
                {
                    equal =
                        FileUtils.contentEquals(
                            this.tempOutputFile,
                            this.originalOutputLocation);
                }
                catch (IOException e)
                {
                    String msg =
                        String.format(
                            "Error comparing content of %s and %s",
                            this.tempOutputFile,
                            this.originalOutputLocation);
                    throw new MojoExecutionException(msg, e);
                }

                if (equal)
                {
                    info(
                        "Not replacing %s because new content is same",
                        this.originalOutputLocation);
                }
                else
                {
                    moveTempFile = true;
                }
            }
            else
            {
                moveTempFile = true;
            }

            if (moveTempFile)
            {
                try
                {
                    if (this.originalOutputLocation.exists())
                    {
                        this.originalOutputLocation.delete();
                    }

                    FileUtils.moveFile(
                        this.tempOutputFile,
                        this.originalOutputLocation);
                }
                catch (IOException e)
                {
                    String msg =
                        String.format(
                            "Error moving %s to %s",
                            this.tempOutputFile,
                            this.originalOutputLocation);
                    throw new MojoExecutionException(msg, e);
                }
            }
            else
            {
                this.tempOutputFile.delete();
            }
        }
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        if (this.outputLocation != null)
        {
            if (this.outputLocation.isFile())
            {
                if (this.outputLocation.exists())
                {
                    try
                    {
                        this.tempOutputFile =
                            File.createTempFile(
                                UUID.randomUUID().toString(),
                                null);
                    }
                    catch (IOException e)
                    {
                        throw new MojoExecutionException(
                            "Error creating temp file",
                            e);
                    }

                    this.originalOutputLocation = this.outputLocation;
                    this.outputLocation = this.tempOutputFile;
                }
            }

            this.outputLocation.getParentFile().mkdirs();
            debug("making parent directories for %s", this.outputLocation);
        }
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.inputFile != null || this.inputUrl != null;
    }

    @Override
    boolean showExecOutput()
    {
        return false;
    }
}
