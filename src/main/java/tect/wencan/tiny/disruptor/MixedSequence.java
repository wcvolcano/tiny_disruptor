package tect.wencan.tiny.disruptor;

import java.util.Arrays;

import tect.wencan.tiny.disruptor.utils.SequenceUtil;

/**
 * @author fanze 2018/05/18.
 */
public class MixedSequence extends Sequence {

    private Sequence[] sequences;

    public MixedSequence(Sequence[] sequences) {
        this.sequences = sequences;
    }

    public void add(Sequence[] addSequences) {
        int oldSize = sequences.length;
        this.sequences = Arrays.copyOf(sequences, sequences.length + addSequences.length);
        for (int i = oldSize, size = this.sequences.length; i < size; ++i) {
            this.sequences[i] = addSequences[i - oldSize];
        }
    }

    @Override
    public long get() {
        return SequenceUtil.getMinimum(sequences);
    }

    @Override
    public boolean compareAndSet(long expect, long newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long incrementAndGet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long incrementAndGet(long add) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(long value) {
        throw new UnsupportedOperationException();
    }
}
