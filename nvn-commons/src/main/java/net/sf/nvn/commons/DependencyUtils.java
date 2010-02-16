package net.sf.nvn.commons;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A utility class for working with Dependencies.
 * 
 * @author akutz
 * 
 */
public class DependencyUtils
{
    /**
     * Copies the dependency file (and its associated pdb or XML documentation
     * files) to the original file name using the AssemblyName.
     * 
     * @param depFile The dependency file.
     * @param assemblyName The AssemblyName.
     * @throws MojoExecutionException When an error occurs.
     */
    public static boolean copyToAssemblyNamedFiles(
        File depFile,
        String assemblyName) throws MojoExecutionException
    {
        if (depFile == null)
        {
            return false;
        }
        
        if (StringUtils.isEmpty(assemblyName))
        {
            return false;
        }
        
        String filename = FilenameUtils.getBaseName(depFile.getName());
        String ext = FilenameUtils.getExtension(depFile.getName());
        File parent = depFile.getParentFile();

        File depFile2 =
            new File(parent, String.format("%s.%s", assemblyName, ext));

        if (!depFile2.exists())
        {
            try
            {
                FileUtils.copyFile(depFile, depFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    depFile,
                    depFile2), e);
            }
        }

        File pdbFile = new File(parent, filename + "-sources.pdb");
        File pdbFile2 = new File(parent, assemblyName + ".pdb");

        if (pdbFile.exists() && !pdbFile2.exists())
        {
            try
            {
                FileUtils.copyFile(pdbFile, pdbFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    pdbFile,
                    pdbFile2), e);
            }
        }

        File docFile = new File(parent, filename + "-dotnetdoc.xml");
        File docFile2 = new File(parent, assemblyName + ".xml");

        if (docFile.exists() && !docFile2.exists())
        {
            try
            {
                FileUtils.copyFile(docFile, docFile2);
            }
            catch (IOException e)
            {
                throw new MojoExecutionException(String.format(
                    "Error copying %s to %s",
                    docFile,
                    docFile2), e);
            }
        }
        
        return true;
    }
}
