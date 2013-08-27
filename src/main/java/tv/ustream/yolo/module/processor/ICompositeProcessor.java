package tv.ustream.yolo.module.processor;

/**
 * @author bandesz
 */
public interface ICompositeProcessor extends IProcessor
{

    public void addProcessor(IProcessor processor);

}
