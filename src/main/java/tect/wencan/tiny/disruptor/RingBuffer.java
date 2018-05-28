package tect.wencan.tiny.disruptor;

import tect.wencan.tiny.disruptor.utils.EventFactory;

/**
 * @author fanze 2018/05/18.
 * 环形数组
 */
public class RingBuffer<T> {
    /**
     * 2^n，环形数组的大小
     */
    private final int size;
    /**
     * 环形数组
     */
    private final T[] eventArray;
    /**
     * 2^n-1。用于计算sequence在数组中的下标位置
     */
    private final int indexMask;

    public RingBuffer(int size, EventFactory<T> factory) {
        if (size < 1 || factory == null) {
            throw new RuntimeException("size<1 || factory==null");
        }

        this.size = size;
        if (Integer.bitCount(size) != 1) {
            throw new RuntimeException("size should be 2^power");
        }

        this.indexMask = size - 1;

        //noinspection unchecked
        eventArray = (T[])new Object[size];
        for (int i = 0; i < size; ++i) {
            eventArray[i] = factory.newInstance();
        }

    }

    public T get(long sequence) {
        return eventArray[index(sequence)];
    }

    private int index(long sequence) {
        return (int)(sequence & indexMask);
    }

    public int getSize() {
        return size;
    }

}
