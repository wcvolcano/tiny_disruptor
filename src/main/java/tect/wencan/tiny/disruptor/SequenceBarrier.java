package tect.wencan.tiny.disruptor;

import tect.wencan.tiny.disruptor.exceptions.AlertException;

/**
 * @author fanze 2018/05/21.
 */
public class SequenceBarrier {
    /**
     * mixedSequence
     */
    private final SingleProducer producer;
    private final Sequence waitFor;
    private volatile boolean alert = false;

    SequenceBarrier(SingleProducer producer,Sequence waitFor) {
        this.producer = producer;
        this.waitFor = waitFor;
    }

    /**
     * busy wait
     *
     * @param sequence
     * @return
     */
    public long waitFor(long sequence) {
        long maybeAvailable;
        while ((maybeAvailable = waitFor.get()) < sequence) {
            if (alert) {
                throw new AlertException();
            }
            //busy spin
        }
        return producer.getAvailable(maybeAvailable);
    }

    public void halt() {
        alert = true;
    }

}
