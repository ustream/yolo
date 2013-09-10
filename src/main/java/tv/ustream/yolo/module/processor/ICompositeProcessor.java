package tv.ustream.yolo.module.processor;

/**
 * @author bandesz
 */
public interface ICompositeProcessor extends IProcessor
{

    void addProcessor(IProcessor processor);

}
