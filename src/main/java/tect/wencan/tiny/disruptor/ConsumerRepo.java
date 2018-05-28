package tect.wencan.tiny.disruptor;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author fanze 2018/05/22.
 */
public class ConsumerRepo<T> {
    private AtomicBoolean started = new AtomicBoolean(false);
    private final Map<EventHandler<T>, EventProcessor<T>> processorMap = new IdentityHashMap<>();

    public void add(EventProcessor<T> processor) {
        processorMap.put(processor.getEventHandler(), processor);
    }

    public Collection<EventProcessor<T>> allEventProcessors() {
        return processorMap.values();
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            for (EventProcessor<T> processor : allEventProcessors()) {
                processor.start();
            }
        } else {
            throw new RuntimeException("consumer can only started once");
        }
    }

    public void halt() {
        for (EventProcessor<T> processor : allEventProcessors()) {
            processor.halt();
        }
    }
}
