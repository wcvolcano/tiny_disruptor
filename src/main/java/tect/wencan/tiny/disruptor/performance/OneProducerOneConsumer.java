package tect.wencan.tiny.disruptor.performance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import tect.wencan.tiny.disruptor.Disruptor;
import tect.wencan.tiny.disruptor.EventHandler;

/**
 * @author fanze 2018/05/23.
 */
public class OneProducerOneConsumer {

    public static void main(String[] args) throws InterruptedException {
        rawDisruptorTest();
        tinyDisruptorTest();
        blockQueueTest();
    }

    static long iterations = 1000L*1000L*10L;
    static int bufferSize = 1024;
    static class LongEvent {
        private Long num;
    }

    private static void blockQueueTest() throws InterruptedException {
        BlockingQueue<LongEvent> queue = new LinkedBlockingDeque<>();

        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        LongEvent longEvent = queue.take();
                        //System.out.println("consume: " + longEvent.num);
                        if (longEvent.num == iterations - 1) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        long start = System.currentTimeMillis();
        consumer.start();

        for (int i = 0; i < iterations; ++i) {
            LongEvent longEvent = new LongEvent();
            longEvent.num = Long.valueOf(i);
            queue.add(longEvent);
        }

        consumer.join();

        long end = System.currentTimeMillis();
        System.out.println(end - start);

    }

    private static void tinyDisruptorTest() {

        Disruptor<LongEvent> disruptor = new Disruptor<>(bufferSize, LongEvent::new);
        disruptor.handleWith((EventHandler<LongEvent>)(sequence, event) -> {
            //System.out.println("consume: " + event.num);
        });

        disruptor.start();
        long start = System.currentTimeMillis();

        for (long i = 0; i < iterations; ++i) {
            disruptor.publish((sequence, event, aLong) -> event.num = aLong, i);
            //System.out.println("produce: " + i);
        }

        disruptor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println((end - start));
    }

    private static void rawDisruptorTest() {
        class LongEvent {
            private Long num;
        }
        com.lmax.disruptor.dsl.Disruptor<LongEvent> disruptor =
            new com.lmax.disruptor.dsl.Disruptor<>(LongEvent::new, bufferSize, (ThreadFactory)Thread::new,
                ProducerType.SINGLE, new BusySpinWaitStrategy());
        disruptor.handleEventsWith((com.lmax.disruptor.EventHandler<LongEvent>)(event, sequence, endOfBatch)
            -> {
            //System.out.println("consumer: " + event.num);
        });

        disruptor.start();
        long start = System.currentTimeMillis();

        for (long i = 0; i < iterations; ++i) {

            disruptor.publishEvent((event, sequence, arg0) -> event.num = arg0, i);
            //System.out.println("produce: " + i);
        }

        disruptor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println((end - start));
    }
}
