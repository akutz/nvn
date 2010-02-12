package net.sf.nvn.plugins.vdproj;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * A Maven plug-in for building VisualStudio setup projects.
 * 
 * @author akutz
 * 
 * @goal package
 * @phase package
 * @description A Maven plug-in for building VisualStudio setup projects.
 */
public class VdprojMojo extends AbstractExeMojo
{
    private static Pattern PRODUCT_NAME_PATT =
        Pattern.compile("(^.*\"ProductName\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern PRODUCT_VERSION_PATT =
        Pattern.compile("(^.*\"ProductVersion\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern MANUFACTURER_PATT =
        Pattern.compile("(^.*\"Manufacturer\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern HELP_URL_PATT =
        Pattern.compile("(^.*\"ARPHELPLINK\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern TITLE_PATT =
        Pattern.compile("(^.*\"Title\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern CONTACT_PATT =
        Pattern.compile("(^.*\"ARPCONTACT\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern COMMENTS_PATT =
        Pattern.compile("(^.*\"ARPCOMMENTS\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern ABOUT_URL_PATT =
        Pattern.compile("(^.*\"ARPURLINFOABOUT\" = \"8:)[^\\\"]*(\"$)");
    private static Pattern PROJECT_NAME_PATT =
        Pattern.compile("(?m)^.*\"ProjectName\" = \"8:([^\\\"]*)\"$");
    private static Pattern OUTPUT_FILE_PATT =
        Pattern
            .compile("(?i)(^.*\"OutputFilename\" = \"8:.*\\\\)[^\\\\]*\\.msi(\"$)");
    private static Pattern VER_PATT = Pattern.compile("(?:\\d|\\.)+");

    /**
     * Setting this parameter to true causes the Vdproj plug-in to create a
     * temporary copy of the vdproj files it discovers and inject them with
     * values from the Maven object model such as: the version, the company's
     * name, the product name, etc.
     * 
     * @parameter default-value="true"
     */
    boolean injection;

    /**
     * The vdproj file(s) to build.
     * 
     */
    File[] vdProjFiles;

    private File[] backupVdProjFiles;

    /**
     * The names of the setup project(s).
     */
    String[] projectNames;

    @Override
    String getArgs(int execution)
    {
        StringBuilder cmdLineBuff = new StringBuilder();

        cmdLineBuff.append(getPath(this.vdProjFiles[execution]));
        cmdLineBuff.append(" ");

        cmdLineBuff.append("/Build");
        cmdLineBuff.append(" ");
        cmdLineBuff.append(quote(getActiveBuildConfigurationName()));
        cmdLineBuff.append(" ");

        cmdLineBuff.append("/Project");
        cmdLineBuff.append(" ");
        cmdLineBuff.append(quote(this.projectNames[execution]));

        String clbs = cmdLineBuff.toString();
        return clbs;
    }

    @Override
    String getMojoName()
    {
        return "vdproj";
    }

    @Override
    void preExecute() throws MojoExecutionException
    {
        initVdprojFiles();
        initProjectNames();
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return this.vdProjFiles != null && this.vdProjFiles.length > 0;
    }

    /**
     * Initializes the vdProjFile field.
     */
    void initVdprojFiles() throws MojoExecutionException
    {
        if (this.vdProjFiles == null)
        {
            this.vdProjFiles = findVdprojFiles();
        }

        this.projectNames = new String[this.vdProjFiles.length];

        injectValues();
    }

    void initProjectNames() throws MojoExecutionException
    {
        if (this.injection || this.vdProjFiles == null)
        {
            return;
        }

        for (int x = 0; x < this.vdProjFiles.length; ++x)
        {
            File f = this.vdProjFiles[x];

            debug("processing %s for project name", f);

            String fc;

            try
            {
                fc = FileUtils.readFileToString(f);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error reading content from %s",
                    f), e);
            }

            Matcher m = PROJECT_NAME_PATT.matcher(fc);
            m.find();

            this.projectNames[x] = m.group(1);
        }
    }

    @SuppressWarnings("unchecked")
    void injectValues() throws MojoExecutionException
    {
        if (!this.injection || this.vdProjFiles.length == 0)
        {
            return;
        }

        this.backupVdProjFiles = new File[this.vdProjFiles.length];

        for (int x = 0; x < this.vdProjFiles.length; ++x)
        {
            File originalLoc = this.vdProjFiles[x];
            File backupLoc =
                new File(originalLoc.getParentFile(), originalLoc.getName()
                    + ".backup");
            this.backupVdProjFiles[x] = backupLoc;

            try
            {
                FileUtils.moveFile(originalLoc, backupLoc);
                debug("moved %s to %s", originalLoc, backupLoc);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error moving %s to %s",
                    this.vdProjFiles[x],
                    backupLoc), e);
            }

            try
            {
                FileUtils.copyFile(backupLoc, originalLoc);
                debug("copied %s to %s", backupLoc, originalLoc);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    backupLoc,
                    originalLoc), e);
            }
        }

        String projVer = super.mavenProject.getVersion();
        String safeVer = super.mavenProject.getVersion();
        
        if (StringUtils.isNotEmpty(projVer))
        {
            Matcher m = VER_PATT.matcher(safeVer);
            if (m.find())
            {
                safeVer = m.group();
            }
        }
        
        debug("injecting project version: %s", projVer);
        debug("injecting safe version: %s", safeVer);

        String projName =
            super.mavenProject.hasParent() ? super.mavenProject
                .getParent()
                .getName() : super.mavenProject.getName();
        debug("injecting project name: %s", projName);

        String url =
            super.mavenProject.hasParent() ? super.mavenProject
                .getParent()
                .getUrl() : super.mavenProject.getUrl();
        debug("injecting project url: %s", url);

        String orgName =
            super.mavenProject.getOrganization() == null ? null
                : super.mavenProject.getOrganization().getName();
        debug("injecting organization name: %s", orgName);

        String devName = null;
        if (super.mavenProject.getDevelopers() != null)
        {
            if (super.mavenProject.getDevelopers().size() > 0)
            {
                Developer d =
                    (Developer) super.mavenProject.getDevelopers().get(0);
                devName = d.getName();
            }
        }
        debug("injecting developer name: %s", devName);

        String[] injValues = new String[]
        {
            projName, safeVer, orgName, url, projName, devName, projName, url,
        };

        for (int x = 0; x < this.vdProjFiles.length; ++x)
        {
            File f = this.vdProjFiles[x];

            List lines;

            try
            {
                lines = FileUtils.readLines(f);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error reading lines from %s",
                    f), e);
            }

            List<String> newLines = new ArrayList<String>();

            for (Object ol : lines)
            {
                String l = (String) ol;

                Matcher m0 = PRODUCT_NAME_PATT.matcher(l);
                Matcher m1 = PRODUCT_VERSION_PATT.matcher(l);
                Matcher m2 = MANUFACTURER_PATT.matcher(l);
                Matcher m3 = HELP_URL_PATT.matcher(l);
                Matcher m4 = TITLE_PATT.matcher(l);
                Matcher m5 = CONTACT_PATT.matcher(l);
                Matcher m6 = COMMENTS_PATT.matcher(l);
                Matcher m7 = ABOUT_URL_PATT.matcher(l);

                Matcher[] marr = new Matcher[]
                {
                    m0, m1, m2, m3, m4, m5, m6, m7
                };

                boolean matched = false;

                for (int y = 0; y < marr.length; ++y)
                {
                    Matcher m = marr[y];

                    if (m.matches())
                    {
                        debug("matched pattern: %s", m.pattern().pattern());

                        String iv = injValues[y];
                        String nl;

                        if (StringUtils.isEmpty(iv))
                        {
                            nl = l;
                        }
                        else
                        {
                            nl =
                                m.replaceAll(String.format(
                                    "$1%s$2",
                                    injValues[y]));
                        }

                        newLines.add(nl);
                        matched = true;

                        break;
                    }
                }

                if (matched)
                {
                    continue;
                }

                Matcher projnamem = PROJECT_NAME_PATT.matcher(l);
                Matcher ofilem = OUTPUT_FILE_PATT.matcher(l);

                if (projnamem.matches())
                {
                    this.projectNames[x] = projnamem.group(1);
                }
                else if (ofilem.matches() && StringUtils.isNotEmpty(projVer))
                {
                    String fileName =
                        String.format("%s-%s.msi", projName, projVer);
                    String nl =
                        String.format(
                            "%s%s%s",
                            ofilem.group(1),
                            fileName,
                            ofilem.group(2));
                    newLines.add(nl);
                }
                else
                {
                    newLines.add(l);
                }
            }

            try
            {
                FileUtils.writeLines(f, newLines);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error writing lines to %s",
                    f), e);
            }
        }
    }

    /**
     * Finds the vdproj file(s).
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    File[] findVdprojFiles()
    {
        debug("listing vdproj files in %s", super.mavenProject.getBasedir());

        Collection vdprojFiles =
            FileUtils.listFiles(super.mavenProject.getBasedir(), new String[]
            {
                "vdproj"
            }, false);

        if (vdprojFiles == null)
        {
            debug("did not discover any vdproj files");
            return new File[0];
        }

        List<File> files = new ArrayList<File>();

        int x = 0;
        for (Object of : vdprojFiles)
        {
            File f = (File) of;

            if (f.getName().endsWith(".nvn.vdproj"))
            {
                debug("not adding %s because it is a generated file", f);
            }
            else
            {
                files.add(f);
                debug("adding %s", of);
            }

            ++x;
        }

        return files.toArray(new File[0]);
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @Override
    File getDefaultCommand()
    {
        return new File("devenv.exe");
    }

    @Override
    void postExecute(MojoExecutionException executionException)
        throws MojoExecutionException
    {
        if (this.injection && this.vdProjFiles != null)
        {
            for (int x = 0; x < this.backupVdProjFiles.length; ++x)
            {
                File backupLoc = this.backupVdProjFiles[x];
                File originalLoc = this.vdProjFiles[x];

                if (!originalLoc.delete())
                {
                    throw new MojoExecutionException(String.format(
                        "Error deleting %s",
                        originalLoc));
                }

                try
                {
                    FileUtils.moveFile(backupLoc, originalLoc);
                    debug("moved %s to %s", backupLoc, originalLoc);
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException(String.format(
                        "Error moving %s to %s",
                        backupLoc,
                        originalLoc), e);
                }
            }
        }
    }
}