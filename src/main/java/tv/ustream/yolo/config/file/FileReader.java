package tv.ustream.yolo.config.file;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;

/**
 * @author bandesz
 */
public class FileReader extends Reader
{
    public FileReader(final File file, final long watchInterval, final IConfigFileListener listener) throws IOException
    {
        super(file, watchInterval, listener);
    }

    @Override
    protected void readConfig()
    {
        readFile(getFile(), true);
    }

    @Override
    protected final FileAlterationObserver createObserver()
    {
        String path = getFile().getAbsolutePath();

        String filename = path.substring(path.lastIndexOf('/') + 1);
        String directory = path.substring(0, path.lastIndexOf('/'));

        return new FileAlterationObserver(
                new File(directory),
                FileFilterUtils.nameFileFilter(filename)
        );
    }

}
