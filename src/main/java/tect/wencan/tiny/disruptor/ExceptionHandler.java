package tect.wencan.tiny.disruptor;

/**
 * @author fanze 2018/05/23.
 */
public interface ExceptionHandler<T> {
    void handleEventProcessException(Throwable t, long sequence, T event);

    void handleStartException(Throwable t);

    void handleShutdownException(Throwable t);
}
