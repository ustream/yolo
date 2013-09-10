package tv.ustream.yolo.util;

/**
 * @author bandesz
 */
public final class NumberConverter
{

    private static final long KB_FACTOR = 1024;

    private static final long MB_FACTOR = KB_FACTOR * KB_FACTOR;

    private static final long GB_FACTOR = KB_FACTOR * MB_FACTOR;

    private static final long TB_FACTOR = KB_FACTOR * GB_FACTOR;

    private NumberConverter()
    {
    }

    public static Double convertByteValue(final String value)
    {
        String stripped = value.replace(" ", "").toUpperCase();

        switch (stripped.charAt(stripped.length() - 1))
        {
            case 'B':
                return Double.parseDouble(stripped.substring(0, stripped.length() - 1));
            case 'K':
                return Double.parseDouble(stripped.substring(0, stripped.length() - 1)) * KB_FACTOR;
            case 'M':
                return Double.parseDouble(stripped.substring(0, stripped.length() - 1)) * MB_FACTOR;
            case 'G':
                return Double.parseDouble(stripped.substring(0, stripped.length() - 1)) * GB_FACTOR;
            case 'T':
                return Double.parseDouble(stripped.substring(0, stripped.length() - 1)) * TB_FACTOR;
            default:
                return Double.parseDouble(value);
        }
    }

}
