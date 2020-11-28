package lab5;

import lab4.Monomial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolynomialTest {
    private static Polynomial parallelPolynomial;
    private static Polynomial sequentialPolynomial;
    private static final Integer numberOfThreads = 4;

    private void insert(Polynomial polynomial, int from, int to) {
        for (int i = from; i < to; i++) {
            polynomial.insertMonomial(new Monomial(i, i));
        }
    }

    @BeforeEach
    void init() {
        parallelPolynomial = new Polynomial();
        sequentialPolynomial = new Polynomial();
    }

    @Test
    void insertMonomial1() throws InterruptedException {
        Thread t1 = new Thread(() -> parallelPolynomial.insertMonomial(new Monomial(1, 1)));
        t1.start();
        Thread t2 = new Thread(() -> parallelPolynomial.insertMonomial(new Monomial(1, 1)));
        t2.start();
        Thread t3 = new Thread(() -> parallelPolynomial.insertMonomial(new Monomial(1, 1)));
        t3.start();
        Thread t4 = new Thread(() -> parallelPolynomial.insertMonomial(new Monomial(1, 1)));
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        for (int i = 0; i < 4; i++) {
            sequentialPolynomial.insertMonomial(new Monomial(1, 1));
        }

        assert sequentialPolynomial.equals(parallelPolynomial);
    }

    @Test
    void insertMonomial2() throws InterruptedException {
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            final int from = i * numberOfThreads;
            final int to = (i + 1) * numberOfThreads;
            threads[i] = new Thread(() -> {
                insert(parallelPolynomial, from, to);
                insert(parallelPolynomial, from, to);
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        insert(sequentialPolynomial, 0, numberOfThreads * numberOfThreads);
        insert(sequentialPolynomial, 0, numberOfThreads * numberOfThreads);
        assert sequentialPolynomial.equals(parallelPolynomial);
    }
}