package net.sf.nvn.plugins.mstest;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import static org.apache.commons.exec.util.StringUtils.quoteArgument;

/**
 * A Maven plug-in for testing .NET solutions and/or projects with MSTest.
 * 
 * @goal mstest
 * @phase test
 * @description A Maven plug-in for testing .NET solutions and/or projects with
 *              MSTest.
 */
public class MSTestMojo extends AbstractExeMojo
{
    /**
     * The path to the mstest executable.
     * 
     * @parameter expression="${mstest.mstest}" default-value="mstest.exe"
     */
    File mstest;

    /**
     * An assembly that contains tests.
     * 
     * @parameter expression="${mstest.testContainer}"
     */
    File testContainer;

    /**
     * One or more files with the extension "vsmdi" that contains test metadata.
     * 
     * @parameter expression="${mstest.testMetaDatas}"
     */
    File[] testMetaDatas;

    /**
     * A list of tests, as specified in the test metadata file, to be run.
     * 
     * @parameter expression="${mstest.testLists}"
     */
    String[] testLists;

    /**
     * A list of individual tests to run.
     * 
     * @parameter expression="${mstest.tests}"
     */
    String[] tests;

    /**
     * Run tests within the MSTest.exe process. This choice improves test run
     * speed but increases risk to the MSTest.exe process.
     * 
     * @parameter expression="${mstest.noIsolation}" default-value="false"
     */
    boolean noIsolation;

    /**
     * A run configuration file.
     * 
     * @parameter expression="${mstest.runConfig}"
     */
    File runConfig;

    /**
     * Save the test run results to this file.
     * 
     * @parameter expression="${mstest.resultsFile}"
     */
    File resultsFile;

    /**
     * Instructs MSTest to run only a single test whose name matches one of the
     * tests you supply in the tests parameter.
     * 
     * @parameter expression="${mstest.unique}"
     */
    boolean unique;

    /**
     * Display additional test case properties, if they exist.
     * 
     * @parameter expression="${mstest.details}"
     */
    String[] details;

    /**
     * Display no startup banner and copyright message.
     * 
     * @parameter expression="${mstest.noLogo}" default-value="false"
     */
    boolean noLogo;

    /**
     * The name of a Team Foundation Server to publish the test results to.
     * 
     * @parameter expression="${mstest.publish}"
     */
    String publish;

    /**
     * Specify the results file name to be published. If no results file name is
     * specified, use the file produced by the current run.
     * 
     * @parameter expression="${mstest.publishResultsFile}"
     */
    File publishResultsFile;

    /**
     * Publish test results using this build ID.
     * 
     * @parameter expression="${mstest.publishBuild}"
     */
    String publishBuild;

    /**
     * Specify the flavor of the build against which test results should be
     * published.
     * 
     * @parameter expression="${mstest.flavor}"
     */
    String flavor;

    /**
     * Specify the platform of the build against which test results should be
     * published.
     * 
     * @parameter expression="${mstest.platform}"
     */
    String platform;

    /**
     * Specify the name of the team project to which the build belongs.
     * 
     * @parameter expression="${mstest.teamProject}"
     */
    String teamProject;

    @Override
    public void prepareForExecute() throws MojoExecutionException
    {
        loadTestMetaData();
    }

    @Override
    public boolean shouldExecute()
    {
        if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
        {
            return true;
        }

        if (this.testContainer != null)
        {
            return true;
        }

        getLog().info("nvn-" + getMojoName() + ": no tests found");
        return false;
    }

    @Override
    public String buildCommandLineString()
    {
        if (StringUtils.isNotEmpty(this.commandLineArgs))
        {
            String cmd = this.mstest.getName() + " " + this.commandLineArgs;
            return cmd;
        }

        StringBuilder cmdLineBuff = new StringBuilder();

        cmdLineBuff.append(quoteArgument(this.mstest.getName()));
        cmdLineBuff.append(" ");

        if (this.testContainer != null)
        {
            cmdLineBuff.append("/testcontainer:");
            cmdLineBuff.append(quoteArgument(this.testContainer.getName()));
            cmdLineBuff.append(" ");
        }

        if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
        {
            for (File f : this.testMetaDatas)
            {
                cmdLineBuff.append("/testmetadata:");
                cmdLineBuff.append(quoteArgument(f.getName()));
                cmdLineBuff.append(" ");
            }
        }

        if (this.testLists != null && this.testLists.length > 0)
        {
            for (String s : this.testLists)
            {
                cmdLineBuff.append("/testlist:");
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(" ");
            }
        }

        if (this.noIsolation)
        {
            cmdLineBuff.append("/noisolation");
            cmdLineBuff.append(" ");
        }

        if (this.tests != null && this.tests.length > 0)
        {
            for (String s : this.tests)
            {
                cmdLineBuff.append("/test:");
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(" ");
            }
        }

        if (this.runConfig != null)
        {
            cmdLineBuff.append("/runconfig:");
            cmdLineBuff.append(quoteArgument(this.runConfig.getName()));
            cmdLineBuff.append(" ");
        }

        if (this.resultsFile != null)
        {
            cmdLineBuff.append("/resultsfile:");
            cmdLineBuff.append(quoteArgument(this.resultsFile.getName()));
            cmdLineBuff.append(" ");
        }

        if (this.unique)
        {
            cmdLineBuff.append("/unique");
            cmdLineBuff.append(" ");
        }

        if (this.details != null && this.details.length > 0)
        {
            for (String s : this.details)
            {
                cmdLineBuff.append("/detail:");
                cmdLineBuff.append(quoteArgument(s));
                cmdLineBuff.append(" ");
            }
        }

        if (this.noLogo)
        {
            cmdLineBuff.append("/nologo");
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.publish))
        {
            cmdLineBuff.append("/publish:");
            cmdLineBuff.append(quoteArgument(this.publish));
            cmdLineBuff.append(" ");
        }

        if (this.publishResultsFile != null)
        {
            cmdLineBuff.append("/publishresultsfile:");
            cmdLineBuff
                .append(quoteArgument(this.publishResultsFile.getName()));
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.publishBuild))
        {
            cmdLineBuff.append("/publishbuild:");
            cmdLineBuff.append(quoteArgument(this.publishBuild));
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.teamProject))
        {
            cmdLineBuff.append("/teamproject:");
            cmdLineBuff.append(quoteArgument(this.teamProject));
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.platform))
        {
            cmdLineBuff.append("/platform:");
            cmdLineBuff.append(quoteArgument(this.platform));
            cmdLineBuff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.flavor))
        {
            cmdLineBuff.append("/flavor:");
            cmdLineBuff.append(quoteArgument(this.flavor));
            cmdLineBuff.append(" ");
        }

        String clbs = cmdLineBuff.toString();

        return clbs;
    }

    @Override
    public String getMojoName()
    {
        return "mstest";
    }

    public void loadTestMetaData()
    {
        if (this.testMetaDatas != null && this.testMetaDatas.length > 0)
        {
            return;
        }

        this.testMetaDatas = findVsmdi();
    }

    @SuppressWarnings("unchecked")
    public File[] findVsmdi()
    {
        Collection vsmdiFiles =
            FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
            {
                "vsmdi"
            }, false);

        if (vsmdiFiles == null || vsmdiFiles.size() == 0)
        {
            return null;
        }

        File[] files = new File[vsmdiFiles.size()];

        int x = 0;

        for (Object f : vsmdiFiles)
        {
            files[x] = (File) f;
        }

        return files;
    }

    @Override
    public boolean isProjectTypeValid()
    {
        return isSolution() || isProject();
    }
}