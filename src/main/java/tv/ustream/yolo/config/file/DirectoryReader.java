package tv.ustream.yolo.config.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author bandesz
 */
public class DirectoryReader extends Reader
{

    public DirectoryReader(final File file, final long watchInterval, final IConfigFileListener listener)
            throws IOException
    {
        super(file, watchInterval, listener);
    }

    @Override
    protected void readConfig()
    {
        String[] extensions = {"json"};
        Collection<File> files = FileUtils.listFiles(getFile(), extensions, false);
        for (File fileItem : FileUtils.convertFileCollectionToFileArray(files))
        {
            readFile(fileItem, true);
        }
    }

    @Override
    protected FileAlterationObserver createObserver()
    {
        return new FileAlterationObserver(
                getFile(),
                FileFilterUtils.trueFileFilter()
        );
    }

}
