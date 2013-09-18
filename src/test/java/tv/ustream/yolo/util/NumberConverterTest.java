package tv.ustream.yolo.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author bandesz
 */
public class NumberConverterTest
{

    private static final long KB_FACTOR = 1024;

    private static final long MB_FACTOR = KB_FACTOR * KB_FACTOR;

    private static final long GB_FACTOR = KB_FACTOR * MB_FACTOR;

    private static final long TB_FACTOR = KB_FACTOR * GB_FACTOR;

    @Test
    public void shouldReturnNullForNull()
    {
        Assert.assertNull(NumberConverter.convertByteValue(null));
    }

    @Test
    public void shouldLeaveNumberAsIs()
    {
        Assert.assertEquals(5D, NumberConverter.convertByteValue("5"));
        Assert.assertEquals(5.1D, NumberConverter.convertByteValue("5.1"));
    }

    @Test
    public void shouldLeaveByteValueAsIs()
    {
        assertConvertByteValue(5D, "5", "B");
        assertConvertByteValue(5.5D, "5.5", "B");
    }

    @Test
    public void shouldConvertKilobyteValue()
    {
        assertConvertByteValue(5D * KB_FACTOR, "5", "K");
        assertConvertByteValue(5.5D * KB_FACTOR, "5.5", "K");
    }

    @Test
    public void shouldConvertMegabyteValue()
    {
        assertConvertByteValue(5D * MB_FACTOR, "5", "M");
        assertConvertByteValue(5.5D * MB_FACTOR, "5.5", "M");
    }

    @Test
    public void shouldConvertGigabyteValue()
    {
        assertConvertByteValue(5D * GB_FACTOR, "5", "G");
        assertConvertByteValue(5.5D * GB_FACTOR, "5.5", "G");
    }

    @Test
    public void shouldConvertTerabyteValue()
    {
        assertConvertByteValue(5D * TB_FACTOR, "5", "T");
        assertConvertByteValue(5.5D * TB_FACTOR, "5.5", "T");
    }

    public void assertConvertByteValue(final Double expected, final String value, final String factor)
    {
        Assert.assertEquals(expected, NumberConverter.convertByteValue(value + factor.toLowerCase()));
        Assert.assertEquals(expected, NumberConverter.convertByteValue(value + factor.toUpperCase()));
        Assert.assertEquals(expected, NumberConverter.convertByteValue(value + " " + factor.toLowerCase()));
        Assert.assertEquals(expected, NumberConverter.convertByteValue(value + " " + factor.toUpperCase()));
    }

    @Test
    public void shouldReturNullWhenInvalidValue()
    {
        Assert.assertNull(NumberConverter.convertByteValue("not a number"));
    }

}
