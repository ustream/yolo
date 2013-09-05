package tv.ustream.yolo.util;

/**
 * @author bandesz
 */
public class NumberConverter
{

    private final static long KB_FACTOR = 1024;

    private final static long MB_FACTOR = KB_FACTOR * KB_FACTOR;

    private final static long GB_FACTOR = KB_FACTOR * MB_FACTOR;

    private final static long TB_FACTOR = KB_FACTOR * GB_FACTOR;

    public static Double convertByteValue(String value)
    {
        value = value.replace(" ", "").toUpperCase();

        switch (value.charAt(value.length() - 1))
        {
            case 'B':
                return Double.parseDouble(value.substring(0, value.length() - 1));
            case 'K':
                return Double.parseDouble(value.substring(0, value.length() - 1)) * KB_FACTOR;
            case 'M':
                return Double.parseDouble(value.substring(0, value.length() - 1)) * MB_FACTOR;
            case 'G':
                return Double.parseDouble(value.substring(0, value.length() - 1)) * GB_FACTOR;
            case 'T':
                return Double.parseDouble(value.substring(0, value.length() - 1)) * TB_FACTOR;
            default:
                return Double.parseDouble(value);
        }
    }

}
