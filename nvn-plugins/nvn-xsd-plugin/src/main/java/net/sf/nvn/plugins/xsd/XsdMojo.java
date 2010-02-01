package net.sf.nvn.plugins.xsd;

import static net.sf.nvn.commons.StringUtils.quote;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.nvn.commons.ProcessUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * A Maven plug-in for running the Microsoft xsd.exe utility.
 * 
 * @author akutz
 * 
 * @goal xsd
 * @phase generate-sources
 * @description A Maven plug-in for running the Microsoft xsd.exe utility.
 */
public class XsdMojo extends AbstractExeMojo
{
    /**
     * <p>
     * The input file to convert. You must specify the extension as one of the
     * following: .xdr, .xml, .xsd, .dll, or .exe.
     * </p>
     * <p>
     * If you specify an XDR schema file (.xdr extension), Xsd.exe converts the
     * XDR schema to an XSD schema. The output file has the same name as the XDR
     * schema, but with the .xsd extension.
     * </p>
     * <p>
     * If you specify an XML file (.xml extension), Xsd.exe infers a schema from
     * the data in the file and produces an XSD schema. The output file has the
     * same name as the XML file, but with the .xsd extension.
     * </p>
     * <p>
     * If you specify an XML schema file (.xsd extension), Xsd.exe generates
     * source code for runtime objects that correspond to the XML schema.
     * </p>
     * <p>
     * If you specify a runtime assembly file (.exe or .dll extension), Xsd.exe
     * generates schemas for one or more types in that assembly. You can use the
     * <strong>type</strong> parameter to specify the types for which to
     * generate schemas. The output schemas are named schema0.xsd, schema1.xsd,
     * and so on. Xsd.exe produces multiple schemas only if the given types
     * specify a namespace using the <strong>XMLRoot</strong> custom attribute.
     * </p>
     * 
     * @parameter
     */
    File inputFile;

    /**
     * This is similar to input file, but you can use a URL. If the URL is on a
     * remote system then this mojo will get the remote file and process it.
     * 
     * @parameter
     */
    URL inputUrl;

    /**
     * The user name to use when getting the URL specified by input URL. Basic
     * authentication only.
     * 
     * @parameter
     */
    String userName;

    /**
     * The password to use when getting the URL specified by input URL. Basic
     * authentication only.
     * 
     * @parameter
     */
    String password;

    /**
     * <p>
     * Specifies the directory for output files. This argument can appear only
     * once. The default is the current directory.
     * </p>
     * 
     * @parameter
     */
    File outputDirectory;

    /**
     * If you're generating a single file, such as a class file from an XSD,
     * then you can specify an output file to write to.
     * 
     * @parameter
     */
    File outputFile;

    /**
     * Generates classes that correspond to the specified schema. To read XML
     * data into the object, use the <a href="http://msdn.microsoft.com/en-us/library/system.xml.serialization.xmlserializer.deserialize(VS.71).aspx"
     * >System.XML.Serialization.XMLSerializer.Deserializer</a> method.
     * 
     * @parameter
     */
    boolean classes;

    /**
     * Generates a class derived from <a href=
     * "http://msdn.microsoft.com/en-us/library/system.data.dataset(VS.71).aspx"
     * >DataSet</a> that corresponds to the specified schema. To read XML data
     * into the derived class, use the <a href="http://msdn.microsoft.com/en-us/library/system.data.dataset.readxml(VS.71).aspx"
     * >System.Data.DataSet.ReadXml</a> method.
     * 
     * @parameter
     */
    boolean dataset;

    /**
     * Specifies the element in the schema to generate code for. By default all
     * elements are typed. You can specify this argument more than once.
     * 
     * @parameter
     */
    String[] elements;

    /**
     * Specifies the programming language to use. Choose from
     * <strong>CS</strong> (C#; default), <strong>VB</strong> (Visual Basic),
     * <strong>JS</strong> (JScript), or <strong>VJS</strong> (Visual J#). You
     * can also specify a fully qualified name for a class implementing <a href="http://msdn.microsoft.com/en-us/library/system.codedom.compiler.codedomprovider(VS.71).aspx"
     * >System.CodeDom.Compiler.CodeDomProvider</a>.
     * 
     * @parameter default-value="CS"
     */
    String language;

    /**
     * Specifies the runtime namespace for the generated types.
     * 
     * @parameter default-value="Schemas"
     */
    String namespace;

    /**
     * Specifies the URI for the elements in the schema to generate code for.
     * This URI, if present, applies to all elements specified with the
     * <strong>element</strong> parameter.
     * 
     * @parameter
     */
    String uri;

    /**
     * Specifies the name of the type to create a schema for. You can specify
     * multiple type arguments. If typename does not specify a namespace,
     * Xsd.exe matches all types in the assembly with the specified type. If
     * typename specifies a namespace, only that type is matched. If typename
     * ends with an asterisk character (*), the tool matches all types that
     * start with the string preceding the *. If you omit the
     * <strong>type</strong> parameter, Xsd.exe generates schemas for all types
     * in the assembly.
     * 
     * @parameter
     */
    String[] types;

    /**
     * Attempts to correct the casing of generated classes so that the class
     * code syntax matches convention Microsoft syntax while the XSD matches XML
     * convention. For example, the property "foo" becomes "Foo" while still
     * mapping back to the XML property "foo". This feature is experimental.
     * 
     * @parameter default-value="false"
     */
    boolean correctCase;

    @Override
    String getArgs()
    {
        StringBuilder buff = new StringBuilder();

        buff.append(getPath(this.inputFile));
        buff.append(" ");

        if (this.outputDirectory != null)
        {
            buff.append("/outputdir:");
            buff.append(getPath(this.outputDirectory));
            buff.append(" ");
        }

        if (this.classes)
        {
            buff.append("/classes");
            buff.append(" ");
        }

        if (this.dataset)
        {
            buff.append("/dataset");
            buff.append(" ");
        }

        if (this.elements != null)
        {
            for (String el : this.elements)
            {
                buff.append("/element:");
                buff.append(quote(el));
                buff.append(" ");
            }
        }

        if (StringUtils.isNotEmpty(this.language))
        {
            buff.append("/language:");
            buff.append(this.language);
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.namespace))
        {
            buff.append("/namespace:");
            buff.append(this.namespace);
            buff.append(" ");
        }

        if (StringUtils.isNotEmpty(this.uri))
        {
            buff.append("/uri:");
            buff.append(this.uri);
            buff.append(" ");
        }

        if (this.types != null)
        {
            for (String t : this.types)
            {
                buff.append("/type:");
                buff.append(quote(t));
                buff.append(" ");
            }
        }

        return buff.toString();
    }

    @Override
    String getMojoName()
    {
        return "xsd";
    }

    @Override
    boolean isProjectTypeValid()
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    void prepareForExecute() throws MojoExecutionException
    {
        if (this.inputUrl != null)
        {
            try
            {
                if (StringUtils.isNotEmpty(this.userName))
                {
                    if (this.password == null)
                    {
                        this.password = "";
                    }

                    Authenticator.setDefault(new Authenticator()
                    {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication()
                        {
                            return new PasswordAuthentication(
                                userName,
                                password.toCharArray());
                        }
                    });
                }

                String ext =
                    FilenameUtils.getExtension(this.inputUrl.getFile());
                List lines = IOUtils.readLines(this.inputUrl.openStream());
                String tmpFileName = "XsdMojo-" + UUID.randomUUID().toString();
                File tmpFile = File.createTempFile(tmpFileName, "." + ext);
                FileUtils.writeLines(tmpFile, lines);
                this.inputFile = tmpFile;
                info("retrieved " + this.inputUrl.toString() + " and saved as "
                    + tmpFile.getAbsolutePath());
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Error retrieving "
                    + this.inputUrl.toString(), e);
            }
        }
    }

    @Override
    boolean shouldExecute() throws MojoExecutionException
    {
        return true;
    }

    @Override
    boolean showExecOutput()
    {
        return false;
    }

    @Override
    void postExec(Process process) throws MojoExecutionException
    {
        if (process.exitValue() != 0)
        {
            return;
        }

        if (this.inputUrl != null)
        {
            this.inputFile.delete();
            debug("deleted " + this.inputFile.getAbsolutePath());
        }

        String stdout;

        try
        {
            stdout = ProcessUtils.getStdOut(process);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error getting xsd stdout", e);
        }

        Pattern filePatt = Pattern.compile("Writing file (.)(.+)\\1\\.");
        Matcher fileMatcher = filePatt.matcher(stdout);

        if (!fileMatcher.find())
        {
            throw new MojoExecutionException(
                "Error getting file name from xsd output");
        }

        File genOutFile = new File(fileMatcher.group(2));

        if (this.outputFile == null)
        {
            this.outputFile = genOutFile;
        }
        else
        {
            try
            {
                if (this.outputFile.exists())
                {
                    if (!FileUtils.contentEquals(genOutFile, this.outputFile))
                    {
                        this.outputFile.delete();
                    }
                }
                else
                {
                    FileUtils.moveFile(genOutFile, this.outputFile);
                    debug("moved " + genOutFile.getAbsolutePath() + " to "
                        + this.outputFile.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Error moving "
                    + genOutFile.getAbsolutePath() + " to "
                    + this.outputFile.getAbsolutePath(), e);
            }
        }

        if (this.correctCase)
        {
            StringWriter out = new StringWriter();
            try
            {
                doCorrectCase(this.outputFile, out);

                String corrected = out.toString();
                String original = FileUtils.readFileToString(this.outputFile);

                if (corrected.equals(original))
                {
                    debug("did not correct "
                        + this.outputFile.getAbsolutePath()
                        + " because corrected file was same as original file");
                }
                else
                {
                    FileUtils.writeStringToFile(this.outputFile, corrected);
                    info("corrected " + this.outputFile.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Error correcting case for "
                    + this.outputFile.getAbsolutePath(), e);
            }
        }
    }

    @Override
    File getDefaultCommand()
    {
        return new File("xsd.exe");
    }

    /**
     * A patter to match lines of code to ignore.
     */
    private static String IGNORE_LOC_PATT =

    // Ignore fields
        "(?:private [^\\s]*? [^;]*?;)|" +

        // Ignore the XmlIgnoreAttribute
            "XmlIgnoreAttribute|" +

            // Ignore the XmlElement Attribute
            "XmlElementAttribute";

    /**
     * A pattern for formatting XmlElementAttributes.
     */
    private static String XML_EL_ATTR_PATT = "%s[XmlElementAttribute(\"%s\")]";

    /**
     * A pattern for formatting XmlEnumAttributes.
     */
    private static String XML_ENUM_ATTR_PATT = "%s[XmlEnumAttribute(\"%s\")]";

    /**
     * A pattern for matching properties.
     */
    private static String PROP_PATT =
        "([\\t\\s]*?)public (?!enum)([^\\s]*?) ([^\\s]*?) \\{";

    /**
     * A pattern for matching enumerations.
     */
    private static String ENUM_PATT = "([\\t\\s]*?)public enum ([^\\s]*?) \\{";

    /**
     * A pattern for matching an enumeration's elements.
     */
    private static String ENUM_EL_PATT = "^([\\t\\s]*?)([^\\s\\,]*?),$";

    @SuppressWarnings("unchecked")
    void doCorrectCase(File fileToCorrect, Writer out) throws IOException
    {
        // Read all of the source code from the file, line by line into this
        // list.
        List linesOfCode = FileUtils.readLines(fileToCorrect);

        // Iterate over all of the lines of the file.
        for (int x = 0; x < linesOfCode.size(); ++x)
        {
            // Get the current line of code.
            String loc = (String) linesOfCode.get(x);

            if (loc.matches(IGNORE_LOC_PATT))
            {
                String locPX1 = (String) linesOfCode.get(x + 1);

                // Skip the line after this one if it is empty.
                if (locPX1.matches("^\\s*$"))
                {
                    ++x;
                }

                continue;
            }
            
            Pattern propPatt = Pattern.compile(PROP_PATT);
            Matcher propMatcher = propPatt.matcher(loc);
            
            Pattern enumPatt = Pattern.compile(ENUM_PATT);
            Matcher enumMatcher = enumPatt.matcher(loc);

            // Match properties
            if (propMatcher.matches())
            {
                String ws = propMatcher.group(1);
                String rtype = propMatcher.group(2);
                String pname = propMatcher.group(3);

                if (rtype.endsWith("Enum"))
                {
                    rtype = StringUtils.capitalizeFirstLetter(rtype);
                }

                String locMX1 = (String) linesOfCode.get(x - 1);

                // If the previous LOC was not an XmlArrayItemAttribute then
                // decorate the property with an XmlElementAttribute.
                if (!locMX1.contains("XmlArrayItemAttribute"))
                {
                    String elAttrLine =
                        String.format(XML_EL_ATTR_PATT, ws, pname);

                    println(out, elAttrLine);
                }

                // Build the new auto-property.
                String newProperty =
                    String.format(
                        "%spublic %s %s {{get; set;}}",
                        ws,
                        rtype,
                        StringUtils.capitalizeFirstLetter(pname));

                println(out, newProperty);

                // Skip the rest of the original property definition.
                x += 7;
            }

            // Match enumeration
            else if (enumMatcher.matches())
            {
                int y = x;

                String ws = enumMatcher.group(1);
                String ename = enumMatcher.group(2);

                // Build the new enum signature.
                String newEnumSig =
                    String.format("%spublic enum %s {{", ws, StringUtils
                        .capitalizeFirstLetter(ename));

                // Write the new enum signature.
                println(out, newEnumSig);
                ++y;

                // Write a line of white space.
                println(out, (String) linesOfCode.get(y));
                ++y;

                // Skip the empty remark.
                ++y;

                boolean skipNextEnumAttrWrite = false;

                while (true)
                {
                    String enumLoc = (String) linesOfCode.get(y);

                    if (enumLoc
                        .contains("System.Xml.Serialization.XmlEnumAttribute"))
                    {
                        println(out, enumLoc.replace(
                            "System.Xml.Serliazation",
                            ""));
                        ++y;
                        skipNextEnumAttrWrite = true;
                        continue;
                    }

                    Pattern enumElPatt = Pattern.compile(ENUM_EL_PATT);
                    Matcher enumElMatcher = enumElPatt.matcher(enumLoc);
                    enumElMatcher.matches();

                    ws = enumElMatcher.group(1);
                    String enumVal = enumElMatcher.group(2);

                    if (!skipNextEnumAttrWrite)
                    {
                        String newXmlEnumAttr =
                            String.format(XML_ENUM_ATTR_PATT, ws, enumVal);

                        println(out, newXmlEnumAttr);
                    }
                    else
                    {
                        skipNextEnumAttrWrite = false;
                    }

                    String newEnumLoc =
                        String.format("%s%s", ws, StringUtils
                            .capitalizeFirstLetter(enumVal));

                    println(out, newEnumLoc);
                    println(out);

                    // If the end is nigh then finish things up.
                    String locPY1 = (String) linesOfCode.get(y + 1);

                    if (locPY1.matches("^[\\s\\t]*}$"))
                    {
                        println(out, locPY1);
                        break;
                    }

                    y += 3;
                }

                x = y + 1;
            }
            else
            {
                if (loc.contains("System.Xml.Serialization."))
                {
                    loc = loc.replace("System.Xml.Serialization.", "");
                }

                if (loc.contains("System.SerializableAttribute()"))
                {
                    loc =
                        loc.replace(
                            "System.SerializableAttribute()",
                            "System.SerializableAttribute");
                }

                if (loc
                    .contains("System.Diagnostics.DebuggerStepThroughAttribute()"))
                {
                    loc =
                        loc
                            .replace(
                                "System.Diagnostics.DebuggerStepThroughAttribute()",
                                "System.Diagnostics.DebuggerStepThroughAttribute");
                }

                println(out, loc);
            }
        }

        out.flush();

        out.close();
    }

    private static void println(Writer out) throws IOException
    {
        println(out, "");
    }

    private static void println(Writer out, String toPrint) throws IOException
    {
        out.write(String.format("%s\r\n", toPrint));
    }
}