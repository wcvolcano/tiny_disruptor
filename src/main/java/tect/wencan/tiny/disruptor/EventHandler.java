package tect.wencan.tiny.disruptor;

/**
 * @author fanze 2018/05/22.
 */
public interface EventHandler<T> {
    void handle(long sequence, T event);
}
