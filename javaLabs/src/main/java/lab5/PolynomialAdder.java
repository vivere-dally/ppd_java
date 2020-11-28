package lab5;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PolynomialAdder {
    private final Queue<Object> queue;
    private final AtomicBoolean doneReading;
    private final int numberOfThreads = 4, N = 100;

    public PolynomialAdder() {
        queue = new ArrayDeque<>();
        doneReading = new AtomicBoolean(false);
    }

    public void execute() throws InterruptedException {
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    synchronized (queue) {
                        while (!doneReading.get() || !queue.isEmpty()) {
                            if (queue.isEmpty()) {
                                queue.wait();
                                if (!queue.isEmpty()) {
                                    Object element = queue.remove();
                                    // Do stuff
                                }
                            }
                            else {
                                Object element = queue.remove();
                                // Do stuff
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < N; i++) {
            synchronized (queue) {
                queue.add(new Object());
                queue.notifyAll();
            }
        }

        doneReading.set(true);
        synchronized (queue) {
            queue.notifyAll();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
