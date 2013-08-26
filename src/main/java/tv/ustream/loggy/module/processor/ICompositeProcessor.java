package tv.ustream.loggy.module.processor;

/**
 * @author bandesz
 */
public interface ICompositeProcessor extends IProcessor
{

    public void addProcessor(IProcessor processor);

}
