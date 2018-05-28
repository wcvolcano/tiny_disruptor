package tect.wencan.tiny.disruptor.utils;

/**
 * @author fanze 2018/05/22.
 */
public interface EventTranslatorWithOneArg<T, OneArg> {
    void translate(long sequence, T event, OneArg arg);
}
