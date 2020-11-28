package lab5;

import lab4.Monomial;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PolynomialAdderParallel {
    private final Queue<Monomial> queue;
    private final Polynomial polynomial;
    private final ReentrantLock lock;
    private final Condition produce;
    private final Condition consume;
    private final Object poisonPill = new Object();
    private final int CAPACITY = 20;

    public PolynomialAdderParallel() {
        queue = new ArrayDeque<>(CAPACITY);
        polynomial = new Polynomial();
        lock = new ReentrantLock();
        produce = lock.newCondition();
        consume = lock.newCondition();
    }

    public Polynomial execute(int numberOfProducers, int numberOfConsumers, String path, String polynomialFileFormat) throws InterruptedException {
        Thread[] consumers = new Thread[numberOfConsumers], producers = new Thread[numberOfProducers];

        // Start consumers
        for (int i = 0; i < numberOfConsumers; i++) {
            consumers[i] = new Thread(() -> {
                try {
                    while (true) {
                        lock.lock();
                        if (queue.isEmpty()) {
                            produce.await();
                        }

                        Monomial monomial = queue.remove();
                        consume.signal();
                        if (monomial == poisonPill) {
                            break;
                        }

                        polynomial.insertMonomial(monomial);
                        lock.unlock();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });

            consumers[i].start();
        }

        // Start producers
        for (int i = 0; i < numberOfProducers; i++) {
            producers[i] = new Thread(() -> {

            });

            producers[i].start();
        }

        // Join producers
        for (Thread thread : producers) {
            thread.join();
        }

        // Join consumers
        for (Thread thread : consumers) {
            thread.join();
        }

        return polynomial;
    }
}
