package net.sf.nvn.commons;

import junit.framework.Assert;
import org.junit.Test;

/**
 * The test class for StringUtils.
 * 
 * @author akutz
 * 
 */
public class StringUtilsTest
{
    @Test
    public void quoteTest() throws Exception
    {
        Assert.assertEquals("f", StringUtils.quote("   f   "));

        Assert.assertEquals("\"f a\"", StringUtils.quote("   f a  "));

        Assert.assertEquals("\"Hello, mother. Hello, father.\"", StringUtils
            .quote("   Hello, mother. Hello, father.  "));

        Assert.assertEquals("'Hello, \"world\".'", StringUtils
            .quote("Hello, \"world\".   "));

        Assert.assertEquals("'1   ', '2', '  3'", StringUtils
            .quote("'1   ', '2', '  3'"));

        Assert
            .assertEquals("'1', '2', '3'", StringUtils.quote("'1', '2', '3'"));

        Assert.assertEquals("'1', '2', '3'", StringUtils
            .quote("   '1', '2', '3'"));

        Assert.assertEquals("'1', '2', '3'", StringUtils
            .quote("   '1', '2', '3'      "));
    }
}
