package tv.ustream.yolo.module.reader;

import tv.ustream.yolo.module.IModule;

/**
 * @author bandesz
 */
public interface IReader extends IModule
{

    void start();

    void stop();

    void setReaderListener(IReaderListener listener);

}
