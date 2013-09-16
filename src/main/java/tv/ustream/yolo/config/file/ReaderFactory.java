package tv.ustream.yolo.config.file;

import java.io.File;
import java.io.IOException;

/**
 * @author bandesz
 */
public final class ReaderFactory
{

    private ReaderFactory()
    {
    }

    public static Reader createReader(final File file, final long watchInterval, final IConfigFileListener listener)
            throws IOException
    {
        if (!file.exists())
        {
            throw new IOException("Config file or directory does not exist: " + file.getAbsolutePath());
        }

        if (file.isDirectory())
        {
            return new DirectoryReader(file, watchInterval, listener);
        }
        else
        {
            return new FileReader(file, watchInterval, listener);
        }
    }

}
