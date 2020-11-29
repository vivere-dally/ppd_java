package lab5;

import lab4.Monomial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final int CAPACITY = 20;

    public PolynomialAdderParallel() {
        queue = new ArrayDeque<>(CAPACITY);
        polynomial = new Polynomial();
        lock = new ReentrantLock();
        produce = lock.newCondition();
        consume = lock.newCondition();
    }

    public Polynomial execute(int numberOfProducers, int numberOfConsumers, int numberOfPolynomials, String path, String polynomialFileFormat) throws InterruptedException {
        Thread[] consumers = new Thread[numberOfConsumers], producers = new Thread[numberOfProducers];

        // Start consumers
        for (int i = 0; i < numberOfConsumers; i++) {
            consumers[i] = new Thread(this::consume);
            consumers[i].start();
        }

        // Start producers
        int batchSize = numberOfPolynomials / numberOfProducers;
        for (int i = 0; i < numberOfProducers; i++) {
            final int from = i * batchSize + 1;
            int to = (i + 1) * batchSize + 1;
            final int finalTo = Math.min(to, numberOfPolynomials + 1);
            producers[i] = new Thread(() -> produce(from, finalTo, path, polynomialFileFormat));
            producers[i].start();
        }

        // Join producers
        for (Thread thread : producers) {
            thread.join();
        }

        // Add Poison Pills
        for (int i = 0; i < numberOfConsumers; i++) {
            lock.lock();
            while (queue.size() == CAPACITY) {
                consume.await();
            }

            queue.add(new Monomial(true));
            produce.signalAll();
            lock.unlock();
        }

        // Join consumers
        for (Thread thread : consumers) {
            thread.join();
        }

        return polynomial;
    }

    private void consume() {
        try {
            while (true) {
                lock.lock();
                while (queue.isEmpty()) {
                    produce.await();
                }

                Monomial monomial = queue.remove();
                consume.signal();
                if (monomial.isPoisonPill()) {
                    lock.unlock();
                    break;
                }

                polynomial.insertMonomial(monomial);
                lock.unlock();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void produce(int from, int to, String path, String polynomialFileFormat) {
        try {
            for (int polynomial = from; polynomial < to; polynomial++) {
                String polynomialFile = String.format(polynomialFileFormat, path, polynomial);
                String[] numbers = Files.readAllLines(Path.of(polynomialFile)).get(0).split(",");
                for (int i = 0; i < numbers.length; i += 2) {
                    double coefficient = Double.parseDouble(numbers[i]);
                    int power = Integer.parseInt(numbers[i + 1]);
                    lock.lock();
                    while (queue.size() == CAPACITY) {
                        consume.await();
                    }

                    queue.add(new Monomial(power, coefficient));
                    produce.signal();
                    lock.unlock();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
