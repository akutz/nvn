package net.sf.nvn.plugin;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * <p>
 * A MOJO for running the Microsoft Windows Installer XML (WiX) linker utility,
 * light.exe.
 * </p>
 * 
 * @author akutz
 * 
 * @goal light
 * @phase package
 * @requiresDependencyResolution compile
 * @description A MOJO for running the Microsoft Windows Installer XML (WiX)
 *              linker utility, light.exe.
 */
public class LightMojo extends AbstractExeMojo
{
    /**
     * The source object files.
     * 
     * @parameter
     */
    File[] objectFiles;

    /**
     * The response file.
     * 
     * @parameter
     */
    File responseFile;

    /**
     * Identical rows will be treated as a warning.
     * 
     * @parameter
     */
    boolean allowIdenticalRows;

    /**
     * <p>
     * Allows unresolved references (setting this parameter to true will render
     * the output unusable).
     * </p>
     * <p>
     * <strong>Experimental</strong>
     * </p>
     * 
     * @parameter
     */
    boolean allowUnresolvedReferences;

    /**
     * The base path to locate all files (defaults to current directory).
     * 
     * @parameter default-value="${basedir}"
     */
    File basePath;

    /**
     * Binds files into a wixout. This parameter is only valid if the
     * {@link #wixout} parameter is set to true as well.
     * 
     * @parameter
     */
    boolean bindFilesIntoWixout;

    /**
     * A custom binder to use provided by an extension.
     * 
     * @parameter
     */
    String binder;

    /**
     * Localized string cultures to load from .wxl files and libraries. The
     * precedence of the culture is indicated by its order in the list with the
     * zeroth element having the greatest precedence.
     * 
     * @parameter
     */
    String[] cultures;

    /**
     * A list of pre-processor parameter key/value pairs.
     * 
     * @parameter
     */
    @SuppressWarnings("rawtypes")
    Map preProcessorParmaeters;

    /**
     * Drops unreal tables from the output image.
     * 
     * @parameter
     */
    boolean dropUnrealTables;

    /**
     * A list of extension assemblies or "class, assembly".
     * 
     * @parameter
     */
    String[] extensions;

    /**
     * The file to read localization strings from.
     * 
     * @parameter
     */
    File localizationFile;

    /**
     * Do not delete temporary files (useful for debugging).
     * 
     * @parameter
     */
    boolean noTidy;

    /**
     * The output file.
     * 
     * @parameter
     */
    File outputFile;

    /**
     * Show pedantic messages.
     * 
     * @parameter
     */
    boolean pedantic;

    /**
     * Suppress default admin sequence actions.
     * 
     * @parameter
     */
    boolean suppressAdmin;

    /**
     * Suppress default advanced sequence actions.
     * 
     * @parameter
     */
    boolean suppressAdvanced;

    /**
     * Suppress localization.
     * 
     * @parameter
     */
    boolean suppressLocalization;

    /**
     * Suppress processing the data in the MsiAssembly table.
     * 
     * @parameter
     */
    boolean suppressMsiAssembly;

    /**
     * Suppress schema validation of documents (performance boost).
     * 
     * @parameter
     */
    boolean suppressSchemaValidation;

    /**
     * Suppress tagging sectionId attributes on rows.
     * 
     * @parameter
     */
    boolean suppressTaggingSections;

    /**
     * Suppress default UI sequence actions.
     * 
     * @parameter
     */
    boolean suppressUI;

    /**
     * Suppress intermediate file version mismatch checking.
     * 
     * @parameter
     */
    boolean suppressFileVersionMismatch;

    /**
     * The warning IDs to suppress.
     * 
     * @parameter
     */
    String[] suppressedWarningIds;

    /**
     * The file to write the unreferenced symbols to.
     * 
     * @parameter
     */
    File unreferencedSymbolsFile;

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
     * Enables the WiX UI extension. This value is automatically set to true if
     * any of the object files include the text "WixUI".
     * 
     * @parameter
     */
    boolean wixUIExtension;

    /**
     * Enables the WiX .NET extension.
     * 
     * @parameter
     */
    boolean wixDotNetExtension;

    /**
     * Output wixout format instead of MSI format.
     * 
     * @parameter
     */
    boolean wixout;

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

                if (s.equals("WixUIExtension"))
                {
                    this.wixUIExtension = false;
                }

                if (s.equals("WiXNetFxExtension"))
                {
                    this.wixDotNetExtension = false;
                }
            }
        }

        if (this.wixUIExtension)
        {
            buff.append("-ext WixUIExtension");
            buff.append(" ");
        }

        if (this.wixDotNetExtension)
        {
            buff.append("-ext WiXNetFxExtension");
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

        if (this.objectFiles != null && this.objectFiles.length > 0)
        {
            for (File f : this.objectFiles)
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
    File getDefaultCommand()
    {
        return new File("light.exe");
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        String candleOutPath =
            super.mavenProject.getProperties().getProperty("candle.output");

        if (candleOutPath != null)
        {
            File candleOut = new File(candleOutPath);
            this.objectFiles = new File[]
            {
                candleOut
            };
            debug("Using candle out file: " + candleOut);
        }

        if (this.outputFile == null)
        {
            if (this.outputFile == null)
            {
                this.outputFile =
                    new File(new File(super.mavenProject
                        .getBuild()
                        .getDirectory()), String.format(
                        "%s Setup.msi",
                        super.mavenProject.getName()));
            }

            debug("Initialized output file: " + this.outputFile);
        }

        if (this.objectFiles != null)
        {
            for (File f : this.objectFiles)
            {
                try
                {
                    String s = FileUtils.readFileToString(f);

                    if (s.contains("WixUI"))
                    {
                        this.wixUIExtension = true;
                    }

                    if (s.contains("NETFRAMEWORK") || s.contains("WINDOWSSDK"))
                    {
                        this.wixDotNetExtension = true;
                    }

                    if (this.wixUIExtension && this.wixDotNetExtension)
                    {
                        break;
                    }
                }
                catch (IOException e)
                {
                    error("Error reading: " + f, e);
                    throw new MojoExecutionException("Error reading: " + f, e);
                }
            }
        }
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (this.outputFile != null && this.outputFile.exists())
        {
            super.mavenProject.getProperties().setProperty(
                "light.output",
                this.outputFile.toString());
            debug("set 'light.output' to '%s'", this.outputFile);
        }
    }

    @Override
    String getMojoName()
    {
        return "light";
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.objectFiles != null && this.objectFiles.length > 0;
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

}
