package tect.wencan.tiny.disruptor;

import java.util.concurrent.locks.LockSupport;

import com.google.common.base.Preconditions;

/**
 * @author fanze 2018/05/18.
 * 单线程的生产者，分配编号和publish的时候不存在竞争
 */
public class SingleProducer {
    /**
     * 叶子消费者的缓存
     */
    private long cachedLeaf = Sequence.INIT_VALUE;
    /**
     * 表示已经生产出去以供消费的序列号
     */
    private volatile long published = Sequence.INIT_VALUE;

    /**
     * 叶子消费者
     */
    private final MixedSequence leafTrack;
    /**
     * cursor.get()表示已经分配出去的位置。
     */
    private final Sequence cursor = new Sequence(Sequence.INIT_VALUE);
    private final int bufferSize;

    public SingleProducer(int bufferSize, Sequence[] leafSequence) {
        this.bufferSize = bufferSize;
        if (leafSequence == null || leafSequence.length == 0) {
            leafSequence = new Sequence[] {cursor};
        }
        this.leafTrack = new MixedSequence(leafSequence);
    }

    public long next() {
        return next(1);
    }

    public long next(int capacity) {
        Preconditions.checkArgument(capacity >= 1);
        long next = cursor.get() + capacity;
        long wrap = next - bufferSize;

        //不能将消费者还没有消费的内容覆盖
        if (wrap > cachedLeaf) {
            while (wrap > (cachedLeaf = leafTrack.get())) {
                LockSupport.parkNanos(1L);
            }
        }

        cursor.set(next);
        return next;
    }

    public void addLeafSequence(Sequence[] sequences) {
        this.leafTrack.add(sequences);
    }

    public void publish(long sequence) {
        published = sequence;
    }

    public long getAvailable(long sequence) {
        return published;
    }

    public SequenceBarrier newSequenceBarrier(Sequence[] sequences) {
        MixedSequence mixedSequence = (sequences == null || sequences.length == 0) ?
            new MixedSequence(new Sequence[] {cursor})
            : new MixedSequence(sequences);
        return new SequenceBarrier(this, mixedSequence);
    }

    public boolean hasAllEventProcessed() {
        return leafTrack.get() >= published;
    }
}
