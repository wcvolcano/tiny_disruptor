package tect.wencan.tiny.disruptor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import tect.wencan.tiny.disruptor.utils.EventFactory;
import tect.wencan.tiny.disruptor.utils.EventTranslatorWithOneArg;

/**
 * @author fanze 2018/05/22.
 */
public class Disruptor<T> {
    private final SingleProducer producer;
    private final RingBuffer<T> ringBuffer;
    private final ConsumerRepo<T> consumerRepo = new ConsumerRepo<>();
    private final ThreadFactory threadFactory;
    private AtomicBoolean stated = new AtomicBoolean(false);
    private final ExceptionHandler<T> exceptionHandler;

    public Disruptor(int size, EventFactory<T> eventFactory,
                     ThreadFactory threadFactory, ExceptionHandler<T> exceptionHandler) {
        this.ringBuffer = new RingBuffer<>(size, eventFactory);
        this.producer = new SingleProducer(size, new Sequence[] {});
        this.threadFactory = threadFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public Disruptor(int size, EventFactory<T> eventFactory) {
        this.ringBuffer = new RingBuffer<>(size, eventFactory);
        this.producer = new SingleProducer(size, new Sequence[] {});
        final AtomicLong threadCounter = new AtomicLong(0);
        this.threadFactory = r ->
            new Thread(r, "default-disruptor-thread-" + threadCounter.incrementAndGet());
        this.exceptionHandler = new ExceptionHandler<T>() {
            private final Logger LOGGER = Logger.getLogger("defaultExceptionHandler");

            @Override
            public void handleEventProcessException(Throwable t, long sequence, T event) {
                LOGGER.log(Level.SEVERE, String.format("error when eventProcess,sequence=%d,event=%s,errMsg=%s",
                    sequence, event.toString(), t.getMessage()));
            }

            @Override
            public void handleStartException(Throwable t) {
                LOGGER.log(Level.SEVERE, "error when start,errMsg=" + t.getMessage());
            }

            @Override
            public void handleShutdownException(Throwable t) {
                LOGGER.log(Level.SEVERE, "error when shutdown,errMsg=" + t.getMessage());

            }
        };
    }

    @SafeVarargs
    public final void handleWith(EventHandler<T>... eventHandlers) {
        if (eventHandlers.length == 0) {
            return;
        }
        Sequence[] sequences = new Sequence[eventHandlers.length];
        SequenceBarrier sequenceBarrier = producer.newSequenceBarrier(new Sequence[0]);

        for (int i=0, len=eventHandlers.length; i<len; ++i) {
            EventHandler<T> handler = eventHandlers[i];
            EventProcessor<T> processor = new EventProcessor<>(handler, sequenceBarrier, ringBuffer, threadFactory);
            consumerRepo.add(processor);
            sequences[i] = processor.getSequence();
        }

        producer.addLeafSequence(sequences);
    }

    public <OneArg> void publish(EventTranslatorWithOneArg<T, OneArg> translator, OneArg oneArg) {
        if (!stated.get()) {
            throw new RuntimeException("has not stated yet");
        }
        long sequence = producer.next();
        try {
            translator.translate(sequence, ringBuffer.get(sequence), oneArg);
        } finally {
            producer.publish(sequence);
        }
    }

    public void start() {
        if (stated.compareAndSet(false, true)) {
            consumerRepo.start();
        } else {
            throw new RuntimeException("can only stat once");
        }
    }

    public void halt() {
        consumerRepo.halt();
    }

    public void shutdown() {
        try {
            shutdown(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            exceptionHandler.handleShutdownException(e);
        }
    }

    public void shutdown(long await, TimeUnit timeUnit) throws TimeoutException {
        final long timeout = System.currentTimeMillis() + timeUnit.toMillis(await);

        while (!producer.hasAllEventProcessed()) {
            if (await > 0 && System.currentTimeMillis() > timeout) {
                throw new TimeoutException();
            }
            //busy spin
        }
        halt();
    }



}
