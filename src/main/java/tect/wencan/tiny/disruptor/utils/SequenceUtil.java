package tect.wencan.tiny.disruptor.utils;

import com.google.common.base.Preconditions;
import tect.wencan.tiny.disruptor.Sequence;

/**
 * @author fanze 2018/05/18.
 */
public class SequenceUtil {

    public static long getMinimum(Sequence[] sequences) {
        Preconditions.checkArgument(sequences != null && sequences.length > 0);
        Long min = Long.MAX_VALUE;
        for (Sequence sequence : sequences) {
            min = Math.min(min, sequence.get());
        }
        return min;
    }


}
