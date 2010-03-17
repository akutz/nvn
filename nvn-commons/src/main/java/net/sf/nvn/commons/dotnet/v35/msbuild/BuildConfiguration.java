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

package net.sf.nvn.commons.dotnet.v35.msbuild;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBElement;
import net.sf.nvn.commons.dotnet.DebugType;
import net.sf.nvn.commons.dotnet.ErrorReportType;
import net.sf.nvn.commons.dotnet.PlatformType;
import net.sf.nvn.commons.dotnet.v35.msbuild.xsd.PropertyGroupType;
import net.sf.nvn.commons.dotnet.v35.msbuild.xsd.StringPropertyType;

/**
 * An MSBuild build configuration.
 * 
 * @author akutz
 * 
 */
public class BuildConfiguration
{
    /**
     * The pattern to match a build configuration's condition statement.
     */
    private static Pattern CONDITION_PATT =
        Pattern
            .compile("^ '\\$\\(Configuration\\)\\|\\$\\(Platform\\)' == '(.*)\\|(.*)' $");

    /**
     * Do not allow this class to be instantiated with a constructor.
     */
    private BuildConfiguration()
    {
    }

    /**
     * The name of the build configuration.
     */
    private String name;

    /**
     * The operating system you are building for.
     */
    private PlatformType platform;

    /**
     * A boolean value that indicates whether symbols are generated by the
     * build. Setting this value to false disables generation of program
     * database (.pdb) symbol files.
     */
    private boolean debugSymbols;

    /**
     * Defines the level of debug information that you want generated.
     */
    private DebugType debugType;

    /**
     * A boolean value that when set to true, enables compiler optimizations.
     */
    private boolean optimize;

    /**
     * Specifies the path to the output directory, relative to the project
     * directory.
     */
    private File outputPath;

    /**
     * Defines conditional compiler constants. Symbol/value pairs are separated
     * by semicolons.
     */
    private String[] definedConstants;

    /**
     * Specifies how the compiler task should report internal compiler errors.
     */
    private ErrorReportType errorReport;

    /**
     * The warning level displayed during compilation.
     */
    private Integer warningLevel;

    /**
     * The name of the file that is generated as the XML documentation file.
     */
    private File documentationFile;

    /**
     * Gets the name of the build configuration.
     * 
     * @return The name of the build configuration.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Gets the operating system you are building for.
     * 
     * @return The operating system you are building for.
     */
    public PlatformType getPlatform()
    {
        return this.platform;
    }

    /**
     * Gets a boolean value that indicates whether symbols are generated by the
     * build. Setting this value to false disables generation of program
     * database (.pdb) symbol files.
     * 
     * @return A boolean value that indicates whether symbols are generated by
     *         the build. Setting this value to false disables generation of
     *         program database (.pdb) symbol files.
     */
    public boolean isDebugSymbols()
    {
        return this.debugSymbols;
    }

    /**
     * Gets the level of debug information that you want generated.
     * 
     * @return The level of debug information that you want generated.
     */
    public DebugType getDebugType()
    {
        return this.debugType;
    }

    /**
     * Gets a boolean value that when set to true, enables compiler
     * optimizations.
     * 
     * @return A boolean value that when set to true, enables compiler
     *         optimizations.
     */
    public boolean isOptimize()
    {
        return this.optimize;
    }

    /**
     * Gets the path to the output directory, relative to the project directory.
     * 
     * @return The path to the output directory, relative to the project
     *         directory.
     */
    public File getOutputPath()
    {
        return this.outputPath;
    }

    /**
     * Gets the conditional compiler constants. Symbol/value pairs are separated
     * by semicolons.
     * 
     * @return The conditional compiler constants. Symbol/value pairs are
     *         separated by semicolons.
     */
    public String[] getDefinedConstants()
    {
        return this.definedConstants;
    }

    /**
     * Gets how the compiler task should report internal compiler errors.
     * 
     * @return How the compiler task should report internal compiler errors.
     */
    public ErrorReportType getErrorReport()
    {
        return this.errorReport;
    }

    /**
     * Gets the warning level displayed during compilation.
     * 
     * @return The warning level displayed during compilation.
     */
    public Integer getWarningLevel()
    {
        return this.warningLevel;
    }

    /**
     * Gets the name of the file that is generated as the XML documentation
     * file.
     * 
     * @return The name of the file that is generated as the XML documentation
     *         file.
     */
    public File getDocumentationFile()
    {
        return this.documentationFile;
    }

    /**
     * Creates a new instance of a BuildConfiguration from its PropertyGroupType
     * definition.
     * 
     * @param propertyGroup The PropertyGroupType definition.
     * @return A new BuildConfiguration.
     */
    public static BuildConfiguration instance(PropertyGroupType propertyGroup)
    {
        String cond = propertyGroup.getCondition();
        Matcher condm = CONDITION_PATT.matcher(cond);
        condm.matches();
        String name = condm.group(1);
        String platform = condm.group(2);

        BuildConfiguration buildConfig = new BuildConfiguration();
        buildConfig.name = name;
        buildConfig.platform = PlatformType.parse(platform);

        for (JAXBElement<?> jel : propertyGroup.getProperty())
        {
            String jelName = jel.getName().getLocalPart();
            String jelValu = ((StringPropertyType) jel.getValue()).getValue();

            if (jelName.equals("DebugType"))
            {
                buildConfig.debugType = DebugType.parse(jelValu);
            }
            else if (jelName.equals("DebugSymbols"))
            {
                buildConfig.debugSymbols = Boolean.parseBoolean(jelValu);
            }
            else if (jelName.equals("Optimize"))
            {
                buildConfig.optimize = Boolean.parseBoolean(jelValu);
            }
            else if (jelName.equals("OutputPath"))
            {
                buildConfig.outputPath = new File(jelValu);
            }
            else if (jelName.equals("DocumentationFile"))
            {
                buildConfig.documentationFile = new File(jelValu);
            }
            else if (jelName.equals("DefineConstants"))
            {
                buildConfig.definedConstants = jelValu.split(";");
            }
            else if (jelName.equals("ErrorReport"))
            {
                buildConfig.errorReport = ErrorReportType.parse(jelValu);
            }
            else if (jelName.equals("WarningLevel"))
            {
                buildConfig.warningLevel = Integer.parseInt(jelValu);
            }
        }

        return buildConfig;
    }
}
