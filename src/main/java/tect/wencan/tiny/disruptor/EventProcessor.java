package tect.wencan.tiny.disruptor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import tect.wencan.tiny.disruptor.exceptions.AlertException;

/**
 * @author fanze 2018/05/22.
 */
class EventProcessor<T> implements Runnable {
    private AtomicBoolean running = new AtomicBoolean(false);
    private final EventHandler<T> eventHandler;
    private final Sequence sequence = new Sequence(Sequence.INIT_VALUE);
    private final SequenceBarrier sequenceBarrier;
    private final RingBuffer<T> ringBuffer;
    private final ThreadFactory threadFactory;

    EventProcessor(EventHandler<T> eventHandler, SequenceBarrier sequenceBarrier,
                   RingBuffer<T> ringBuffer, ThreadFactory threadFactory) {
        this.eventHandler = eventHandler;
        this.sequenceBarrier = sequenceBarrier;
        this.ringBuffer = ringBuffer;
        this.threadFactory = threadFactory;
    }

    EventHandler<T> getEventHandler() {
        return eventHandler;
    }

    void start() {
        if (running.compareAndSet(false, true)) {
            Thread thread = threadFactory.newThread(this);
            thread.start();
        }
    }

    void halt() {
        sequenceBarrier.halt();
    }

    @Override
    public void run() {
        while (true) {
            long next = sequence.get() + 1L;
            long available;
            try {
                available = sequenceBarrier.waitFor(next);
            } catch (AlertException alertException) {
                running.set(false);
                break;
            }

            try {
                for (long i = next; i <= available; ++i) {
                    eventHandler.handle(i, ringBuffer.get(i));
                }
            } catch (Throwable t) {
                //ignore
            } finally {
                sequence.set(available);
            }
        }
    }

    public Sequence getSequence() {
        return sequence;
    }
}
