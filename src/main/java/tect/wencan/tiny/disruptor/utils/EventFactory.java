package tect.wencan.tiny.disruptor.utils;

/**
 * @author fanze 2018/05/18.
 */
public interface EventFactory<T> {
    T newInstance();
}
