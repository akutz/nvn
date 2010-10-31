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

import org.testng.Assert;
import org.testng.annotations.Test;

public class VersionTest
{
    private final static String TEST_STRING_1 = "4-D4";
    private final static String TEST_STRING_2 = "4";
    private final static String TEST_STRING_3 = "4.0";
    private final static String TEST_STRING_4 = "4.0.0";
    private final static String TEST_STRING_5 = "4.0.0.0";
    private final static String TEST_STRING_6 = "4.0.0.0.9";
    private final static String TEST_STRING_7 = "4.0.1.0-SNAPSHOT";
    private final static String TEST_STRING_8 = "PM-3.0.1.0";
    private final static String TEST_STRING_9 = "PARSE_EXCEPTION";

    @Test
    public void testParse() throws Exception
    {
        Version v1 = testParseVersion4(TEST_STRING_1);
        Assert.assertEquals(v1.getSuffix(), "-D4");
        Assert.assertEquals(v1.getNumberOfComponents(), 1);

        Assert.assertEquals(testParseVersion4(TEST_STRING_2)
            .getNumberOfComponents(), 1);
        Assert.assertEquals(testParseVersion4(TEST_STRING_3)
            .getNumberOfComponents(), 2);
        Assert.assertEquals(testParseVersion4(TEST_STRING_4)
            .getNumberOfComponents(), 3);
        Assert.assertEquals(testParseVersion4(TEST_STRING_5)
            .getNumberOfComponents(), 4);

        Version v6 = testParseVersion4(TEST_STRING_6);
        Assert.assertEquals(v6.getSuffix(), ".9");
        Assert.assertEquals(v6.getNumberOfComponents(), 4);

        Assert.assertEquals(v1, v6);

        Version v7 = Version.parse(TEST_STRING_7);
        Assert.assertEquals(v7.toString(), "4.0.1.0");
        Assert.assertEquals(v7.getSuffix(), "-SNAPSHOT");
        Assert.assertTrue(v7.compareTo(v6) > 0);

        Version v8 = Version.parse(TEST_STRING_8);
        Assert.assertEquals(v8.toString(), "3.0.1.0");
        Assert.assertEquals(v8.getPrefix(), "PM-");
        Assert.assertTrue(v8.compareTo(v1) < 0);
        Assert.assertTrue(v8.compareTo(v7) < 0);

        try
        {
            Version.parse(TEST_STRING_9);
        }
        catch (Exception e)
        {
            Assert.assertNotNull(e);
        }
        
        v6.setBuild(2);
        Assert.assertTrue(v7.compareTo(v6) < 0);
        v6.setBuild(1);
        Assert.assertTrue(v7.compareTo(v6) == 0);
    }

    private Version testParseVersion4(String toParse) throws Exception
    {
        Version v = Version.parse(toParse);
        Assert.assertEquals(v.toString(), "4.0.0.0");
        Assert.assertEquals(v.toString(0), "4");
        Assert.assertEquals(v.toString(1), "4");
        Assert.assertEquals(v.toString(2), "4.0");
        Assert.assertEquals(v.toString(3), "4.0.0");
        Assert.assertEquals(v.toString(4), "4.0.0.0");
        Assert.assertEquals(v.toString(5), "4.0.0.0");
        Assert.assertEquals(v.toStringRaw(), toParse);
        Assert.assertEquals((Version) v.clone(), v);
        return v;
    }
}
