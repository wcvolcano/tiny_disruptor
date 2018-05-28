package tect.wencan.tiny.disruptor;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicLong;

import sun.misc.Unsafe;

/**
 * @author fanze 2018/05/18.
 */
public class Sequence {
    public static final long INIT_VALUE = -1;
    private static final Unsafe UNSAFE;
    private static final long VALUE_OFFSET;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                @Override
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe)theUnsafe.get(null);
                }
            };

            UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
        try {

            VALUE_OFFSET = UNSAFE.objectFieldOffset
                (AtomicLong.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private long a0, a1, a2, a3, a4, a5, a6;
    private volatile long value;
    private long b0, b1, b2, b3, b4, b5, b6;

    public Sequence() {
    }

    public Sequence(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public boolean compareAndSet(long expect, long newValue) {
        return UNSAFE.compareAndSwapLong(this, VALUE_OFFSET, expect, newValue);
    }

    public long incrementAndGet() {
        return incrementAndGet(1);
    }

    public long incrementAndGet(long add) {
        return UNSAFE.getAndAddLong(this, VALUE_OFFSET, add) + 1L;

    }

    public void set(long value) {
        this.value = value;
    }
}
