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

package net.sf.nvn.commons;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * The Version class can be used to parse a standard version string into its
 * four components, MAJOR.MINOR.BUILD.REVISION.
 * 
 * @author akutz
 * 
 */
public class Version implements Serializable, Cloneable, Comparable<Version>
{
    /**
     * A serial version UID.
     */
    private static final long serialVersionUID = -4316270526722986552L;

    /**
     * A pattern to match the standard version format
     * MAJOR.MINOR.BUILD.REVISION.
     */
    private static Pattern STD_VERSION_PATT =
        Pattern
            .compile("^([^\\d]*?)(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\.(\\d+))?(.*)$");

    /**
     * Initialize a new Version object that is set to "0.0.0.0".
     */
    public Version()
    {
    }

    /**
     * Everything before the version in the string that was parsed.
     */
    private String prefix;

    /**
     * Everything after the version in the string that was parsed.
     */
    private String suffix;

    /**
     * The String that was parsed to create this version object.
     */
    private String rawVersion;

    /**
     * Gets the string that was parsed to create this Version object. This
     * string may not accurately reflect the current values of the Version's
     * components.
     * 
     * @return The string that was parsed to create this Version object. This
     *         string may not accurately reflect the current values of the
     *         Version's components.
     */
    public String toStringRaw()
    {
        return this.rawVersion;
    }

    /**
     * Gets everything before the version in the string that was parsed.
     * 
     * @return Everything before the version in the string that was parsed.
     */
    public String getPrefix()
    {
        return this.prefix;
    }

    /**
     * Gets everything after the version in the string that was parsed.
     * 
     * @return Everything after the version in the string that was parsed.
     */
    public String getSuffix()
    {
        return this.suffix;
    }

    /**
     * Parses a new Version object from a String.
     * 
     * @param toParse The String object to parse.
     * @return A new Version object.
     * @throws Exception When there is an error parsing the String.
     */
    public static Version parse(String toParse) throws Exception
    {
        Matcher m = STD_VERSION_PATT.matcher(toParse);

        if (!m.find())
        {
            throw new Exception(String.format(
                "Error parsing version from '%s'",
                toParse));
        }

        Version v = new Version();
        v.rawVersion = toParse;
        v.prefix = m.group(1);

        if (StringUtils.isNotEmpty(m.group(2)))
        {
            v.setMajor(m.group(2));
        }

        if (StringUtils.isNotEmpty(m.group(3)))
        {
            v.setMinor(m.group(3));
        }

        if (StringUtils.isNotEmpty(m.group(4)))
        {
            v.setBuild(m.group(4));
        }

        if (StringUtils.isNotEmpty(m.group(5)))
        {
            v.setRevision(m.group(5));
        }

        v.suffix = m.group(6);

        return v;
    }

    /**
     * The version's MAJOR component.
     */
    private String major = "0";

    /**
     * Gets the version's MAJOR component.
     * 
     * @return The version's MAJOR component.
     */
    public String getMajor()
    {
        return this.major;
    }

    /**
     * Sets the version's MAJOR component.
     * 
     * @param toSet The version's MAJOR component.
     * @throws IllegalArgumentException When a null or non-numeric value is
     *         given.
     */
    public void setMajor(String toSet) throws IllegalArgumentException
    {
        if (StringUtils.isEmpty(toSet))
        {
            throw new IllegalArgumentException("Argument is null");
        }

        if (!toSet.matches("\\d+"))
        {
            throw new IllegalArgumentException("Argument is not numeric");
        }

        if (this.numberOfComponents < 1)
        {
            this.numberOfComponents = 1;
        }

        this.major = toSet;
    }

    /**
     * Sets the version's MAJOR component.
     * 
     * @param toSet The version's MAJOR component.
     */
    public void setMajor(int toSet)
    {
        setMajor(String.valueOf(toSet));
    }

    /**
     * The version's MAJOR component as an integer.
     */
    private int getMajorAsInt()
    {
        return Integer.parseInt(this.major);
    }

    /**
     * The version's MINOR component.
     */
    private String minor = "0";

    /**
     * Gets the version's MINOR component.
     * 
     * @return The version's MINOR component.
     */
    public String getMinor()
    {
        return this.minor;
    }

    /**
     * Sets the version's MINOR component.
     * 
     * @param toSet The version's MINOR component.
     * @throws IllegalArgumentException When a null or non-numeric value is
     *         given.
     */
    public void setMinor(String toSet) throws IllegalArgumentException
    {
        if (StringUtils.isEmpty(toSet))
        {
            throw new IllegalArgumentException("Argument is null");
        }

        if (!toSet.matches("\\d+"))
        {
            throw new IllegalArgumentException("Argument is not numeric");
        }

        if (this.numberOfComponents < 2)
        {
            this.numberOfComponents = 2;
        }

        this.minor = toSet;
    }

    /**
     * Sets the version's MINOR component.
     * 
     * @param toSet The version's MINOR component.
     */
    public void setMinor(int toSet)
    {
        setMinor(String.valueOf(toSet));
    }

    /**
     * The version's MINOR component as an integer.
     */
    private int getMinorAsInt()
    {
        return Integer.parseInt(this.minor);
    }

    /**
     * The version's BUILD component.
     */
    private String build = "0";

    /**
     * The version's BUILD component as an integer.
     */
    private int getBuildAsInt()
    {
        return Integer.parseInt(this.build);
    }

    /**
     * Gets the version's BUILD component.
     * 
     * @return The version's BUILD component.
     */
    public String getBuild()
    {
        return this.build;
    }

    /**
     * Sets the version's BUILD component.
     * 
     * @param toSet The version's BUILD component.
     * @throws IllegalArgumentException When a null or non-numeric value is
     *         given.
     */
    public void setBuild(String toSet) throws IllegalArgumentException
    {
        if (StringUtils.isEmpty(toSet))
        {
            throw new IllegalArgumentException("Argument is null");
        }

        if (!toSet.matches("\\d+"))
        {
            throw new IllegalArgumentException("Argument is not numeric");
        }

        if (this.numberOfComponents < 3)
        {
            this.numberOfComponents = 3;
        }

        this.build = toSet;
    }

    /**
     * Sets the version's BUILD component.
     * 
     * @param toSet The version's BUILD component.
     */
    public void setBuild(int toSet)
    {
        setBuild(String.valueOf(toSet));
    }

    /**
     * The version's REVISION component.
     */
    private String revision = "0";

    /**
     * The version's REVISION component as an integer.
     */
    private int getRevisionAsInt()
    {
        return Integer.parseInt(this.revision);
    }

    /**
     * Gets the version's REVISION component.
     * 
     * @return The version's REVISION component.
     */
    public String getRevision()
    {
        return this.revision;
    }

    /**
     * Sets the version's REVISION component.
     * 
     * @param toSet The version's REVISION component.
     * @throws IllegalArgumentException When a null or non-numeric value is
     *         given.
     */
    public void setRevision(String toSet) throws IllegalArgumentException
    {
        if (StringUtils.isEmpty(toSet))
        {
            throw new IllegalArgumentException("Argument is null");
        }

        if (!toSet.matches("\\d+"))
        {
            throw new IllegalArgumentException("Argument is not numeric");
        }

        if (this.numberOfComponents < 4)
        {
            this.numberOfComponents = 4;
        }

        this.revision = toSet;
    }

    /**
     * Sets the version's REVISION component.
     * 
     * @param toSet The version's REVISION component.
     */
    public void setRevision(int toSet)
    {
        setRevision(String.valueOf(toSet));
    }

    /**
     * The number of components that make up the version. The value will always
     * be between 1 (inclusive) and 4 (inclusive).
     */
    private int numberOfComponents;

    /**
     * Gets the number of components that make up the version. The value will
     * always be between 1 (inclusive) and 4 (inclusive).
     * 
     * @return The number of components that make up the version. The value will
     *         always be between 1 (inclusive) and 4 (inclusive).
     */
    public int getNumberOfComponents()
    {
        return this.numberOfComponents;
    }

    /**
     * Sets the number of components that make up the version.
     * 
     * @param toSet The number of components that make up the version. Values
     *        less than 1 are treated as 1. Values greater than 4 are treated as
     *        4.
     */
    public void setNumberOfComponents(int toSet)
    {
        if (toSet < 1)
        {
            toSet = 1;
        }
        else if (toSet > 4)
        {
            toSet = 4;
        }

        this.numberOfComponents = toSet;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Version v = new Version();

        v.rawVersion = this.rawVersion;
        v.prefix = this.prefix;
        v.suffix = this.suffix;

        v.numberOfComponents = this.numberOfComponents;

        v.major = this.major;
        v.minor = this.minor;
        v.build = this.build;
        v.revision = this.revision;

        return v;
    }

    @Override
    public boolean equals(Object toCompare)
    {
        // Compare pointers
        if (toCompare == this)
        {
            return true;
        }

        // Compare types
        if (!(toCompare instanceof Version))
        {
            return false;
        }

        return compareTo((Version) toCompare) == 0;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s.%s.%s.%s",
            this.major,
            this.minor,
            this.build,
            this.revision);
    }

    /**
     * Gets the version as a string including the prefix and suffix.
     * 
     * @return The version as a string including the prefix and suffix.
     */
    public String toStringWithPrefixAndSuffix()
    {
        return String.format(
            "%s%s.%s.%s.%s%s",
            this.prefix == null ? "" : this.prefix,
            this.major,
            this.minor,
            this.build,
            this.revision,
            this.suffix == null ? "" : this.suffix);
    }

    /**
     * Gets the version as a string using the specified number of components.
     * 
     * @param components The number of components. Values less than 1 will be
     *        treated as 1 and values greater than 4 will be treated as 4.
     * @return The version as a string using the specified number of components.
     */
    public String toString(int components)
    {
        StringBuilder buff = new StringBuilder();
        buff.append(this.major);

        if (components > 4)
        {
            components = 4;
        }

        switch (components)
        {
            case 2 :
            {
                buff.append(String.format(".%s", this.minor));
                break;
            }
            case 3 :
            {
                buff.append(String.format(".%s.%s", this.minor, this.build));
                break;
            }
            case 4 :
            {
                buff.append(String.format(
                    ".%s.%s.%s",
                    this.minor,
                    this.build,
                    this.revision));
                break;
            }
        }

        return buff.toString();
    }

    /**
     * Gets the version as a string including the prefix and suffix using the
     * specified number of components.
     * 
     * @param components The number of components. Values less than 1 will be
     *        treated as 1 and values greater than 4 will be treated as 4.
     * @return The version as a string including the prefix and suffix using the
     *         specified number of components.
     */
    public String toStringWithPrefixAndSuffix(int components)
    {
        StringBuilder buff = new StringBuilder();

        if (StringUtils.isNotEmpty(this.prefix))
        {
            buff.append(this.prefix);
        }

        buff.append(this.major);

        if (components > 4)
        {
            components = 4;
        }

        switch (components)
        {
            case 2 :
            {
                buff.append(String.format(".%s", this.minor));
                break;
            }
            case 3 :
            {
                buff.append(String.format(".%s.%s", this.minor, this.build));
                break;
            }
            case 4 :
            {
                buff.append(String.format(
                    ".%s.%s.%s",
                    this.minor,
                    this.build,
                    this.revision));
                break;
            }
        }

        if (StringUtils.isNotEmpty(this.suffix))
        {
            buff.append(this.suffix);
        }

        return buff.toString();
    }

    private int compareInts(int x, int y)
    {
        if (x == y)
        {
            return 0;
        }

        if (x < y)
        {
            return -1;
        }

        return 1;
    }

    @Override
    public int compareTo(Version toCompare)
    {
        int result = toString().compareTo(toCompare.toString());

        if (result == 0)
        {
            return result;
        }

        result = compareInts(getMajorAsInt(), toCompare.getMajorAsInt());

        if (result != 0)
        {
            return result;
        }

        result = compareInts(getMinorAsInt(), toCompare.getMinorAsInt());

        if (result != 0)
        {
            return result;
        }

        result = compareInts(getBuildAsInt(), toCompare.getBuildAsInt());

        if (result != 0)
        {
            return result;
        }

        result = compareInts(getRevisionAsInt(), toCompare.getRevisionAsInt());

        if (result != 0)
        {
            return result;
        }

        return result;
    }

    /**
     * Adds a whole integer (positive or negative) to a version component.
     * 
     * @param toAdd The whole integer (positive or negative) to add.
     * @param toAddTo The version component to add the integer to.
     * @return A string representing the sum that contains at least the same
     *         number of digits (padded at the left side) as the original
     *         version component.
     */
    private static String add(final int toAdd, final String toAddTo)
    {
        int l = toAddTo.length();
        int i = Integer.parseInt(toAddTo);
        i += toAdd;

        if (i < 0)
        {
            i = 0;
        }

        String f = String.format("%%0%sd", l);
        String s = String.format(f, i);
        return s;
    }

    /**
     * Adds a whole (positive or negative) integer to the MAJOR component of the
     * version. If the number to add is negative and results in a sum less than
     * 0, the sum is set to 0.
     * 
     * @param toAdd A whole (positive or negative) integer.
     */
    public void addMajor(int toAdd)
    {
        this.major = add(toAdd, this.major);
    }

    /**
     * Adds a whole (positive or negative) integer to the MINOR component of the
     * version. If the number to add is negative and results in a sum less than
     * 0, the sum is set to 0.
     * 
     * @param toAdd A whole (positive or negative) integer.
     */
    public void addMinor(int toAdd)
    {
        this.minor = add(toAdd, this.minor);
    }

    /**
     * Adds a whole (positive or negative) integer to the BUILD component of the
     * version. If the number to add is negative and results in a sum less than
     * 0, the sum is set to 0.
     * 
     * @param toAdd A whole (positive or negative) integer.
     */
    public void addBuild(int toAdd)
    {
        this.build = add(toAdd, this.build);
    }

    /**
     * Adds a whole (positive or negative) integer to the REVISION component of
     * the version. If the number to add is negative and results in a sum less
     * than 0, the sum is set to 0.
     * 
     * @param toAdd A whole (positive or negative) integer.
     */
    public void addRevision(int toAdd)
    {
        this.revision = add(toAdd, this.revision);
    }
}
