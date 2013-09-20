package tv.ustream.yolo.io;

import java.net.URI;

/**
 * @author bandesz
 */
public class TailerFile extends java.io.File
{

    private long previousLastModified = 0;

    private long lastModified = 0;

    private long previousLength = 0;

    private long length = 0;

    public static TailerFile create(final java.io.File file)
    {
        return new TailerFile(file.getAbsolutePath());
    }

    public TailerFile(final String pathname)
    {
        super(pathname);
        initPreviousValues();
    }

    public TailerFile(final String parent, final String child)
    {
        super(parent, child);
        initPreviousValues();
    }

    public TailerFile(final java.io.File parent, final String child)
    {
        super(parent, child);
        initPreviousValues();
    }

    public TailerFile(final URI uri)
    {
        super(uri);
        initPreviousValues();
    }

    private void initPreviousValues()
    {
        previousLength = length();
        previousLastModified = super.lastModified();
    }

    @Override
    public long lastModified()
    {
        lastModified = super.lastModified();
        if (lastModified == previousLastModified)
        {
            return lastModified;
        }
        else
        {
            length = length();
            if (length != previousLength)
            {
                previousLength = length;
                previousLastModified = lastModified;
                return lastModified;
            }
            else
            {
                return previousLastModified;
            }
        }
    }

}
