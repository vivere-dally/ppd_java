package lab4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("FieldCanBeLocal")
public class PolynomialAdder {
    private String polynomialFileFormat;
    private Polynomial sequentialPolynomial;
    private Polynomial parallelPolynomial;
    private Queue<Monomial> queue;
    private AtomicBoolean doneReading;

    private int numberOfPolynomials, maximumNumberOfMonomials, maximumGradeOfMonomials, numberOfThreads;
    private String dir;

    public PolynomialAdder() {
        init();
    }

    private void init() {
        sequentialPolynomial = new Polynomial();
        parallelPolynomial = new Polynomial();
        queue = new ArrayDeque<>();
        doneReading = new AtomicBoolean(false);
    }

    public void execute(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Need 5 arguments:" + System.lineSeparator() +
                    "> Number of polynomials." + System.lineSeparator() +
                    "> Maximum number of monomials." + System.lineSeparator() +
                    "> Maximum grade of a monomial." + System.lineSeparator() +
                    "> Number of threads." + System.lineSeparator() +
                    "> Path of data files.");
            throw new Exception("Bad args!");
        }

        numberOfPolynomials = Integer.parseInt(args[0]);
        maximumNumberOfMonomials = Integer.parseInt(args[1]);
        maximumGradeOfMonomials = Integer.parseInt(args[2]);
        numberOfThreads = Integer.parseInt(args[3]);
        dir = args[4];
        polynomialFileFormat = String.format("%d_%d_%d_%d", numberOfPolynomials, maximumNumberOfMonomials, maximumGradeOfMonomials, numberOfThreads);
        polynomialFileFormat = "%s\\Polynomial_" + polynomialFileFormat + "_%02d.csv";

        for (int polynomial = 1; polynomial <= numberOfPolynomials; polynomial++) {
            String polynomialFile = String.format(polynomialFileFormat, dir, polynomial);
            if (!new File(polynomialFile).exists()) {
                PolynomialGenerator.getInstance().generate(polynomialFile, maximumNumberOfMonomials, maximumGradeOfMonomials);
            }
        }

        // JIT optimization breaks the calculation.
        // Do it once and redo it after.
        {
            sequentialExecution();
            parallelExecution();
            init();
        }

        long t1 = System.nanoTime();
        sequentialExecution();
        long t2 = System.nanoTime();
        parallelExecution();
        long t3 = System.nanoTime();

        if (!sequentialPolynomial.equals(parallelPolynomial)) {
            throw new Exception("RESULTS ARE NOT EQUAL!");
        }

        var dur1 = t2 - t1;
        var dur2 = t3 - t2;
        System.out.println("Threads," + numberOfThreads);
        System.out.println("Sequential (ms)," + (dur1 / 1e6));
        System.out.println("Parallel (ms)," + (dur2 / 1e6));
        System.out.println("Number of polynomials," + numberOfPolynomials);
        System.out.println("Maximum number of Monomials," + maximumNumberOfMonomials);
        System.out.println("Maximum grade of Monomials," + maximumGradeOfMonomials);
    }

    private void sequentialExecution() throws IOException {
        for (int polynomial = 1; polynomial <= numberOfPolynomials; polynomial++) {
            String polynomialFile = String.format(polynomialFileFormat, dir, polynomial);
            String[] numbers = Files.readAllLines(Path.of(polynomialFile)).get(0).split(",");
            for (int i = 0; i < numbers.length; i += 2) {
                double coefficient = Double.parseDouble(numbers[i]);
                int power = Integer.parseInt(numbers[i + 1]);
                sequentialPolynomial.insertMonomial(new Monomial(power, coefficient));
            }
        }
    }

    private void parallelExecution() throws IOException, InterruptedException {
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    synchronized (queue) {
                        while (!doneReading.get() || !queue.isEmpty()) {
                            if (queue.isEmpty()) {
                                queue.wait();
                                if (!queue.isEmpty()) {
                                    parallelPolynomial.insertMonomial(queue.remove());
                                }
                            }
                            else {
                                parallelPolynomial.insertMonomial(queue.remove());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            threads[i].start();
        }

        for (int polynomial = 1; polynomial <= numberOfPolynomials; polynomial++) {
            String polynomialFile = String.format(polynomialFileFormat, dir, polynomial);
            String[] numbers = Files.readAllLines(Path.of(polynomialFile)).get(0).split(",");
            for (int i = 0; i < numbers.length; i += 2) {
                double coefficient = Double.parseDouble(numbers[i]);
                int power = Integer.parseInt(numbers[i + 1]);
                synchronized (queue) {
                    queue.add(new Monomial(power, coefficient));
                    queue.notifyAll();
                }
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
