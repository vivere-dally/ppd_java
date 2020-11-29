package lab5;


import lab4.PolynomialGenerator;

import java.io.File;

@SuppressWarnings("DuplicatedCode")
public class PolynomialAdder {
    public void execute(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Need 6 arguments:" + System.lineSeparator() +
                    "> Number of polynomials." + System.lineSeparator() +
                    "> Maximum number of monomials." + System.lineSeparator() +
                    "> Maximum grade of a monomial." + System.lineSeparator() +
                    "> Number of threads." + System.lineSeparator() +
                    "> Number of producer threads." + System.lineSeparator() +
                    "> Path of data files.");
            throw new Exception("Bad args!");
        }

        final int numberOfPolynomials = Integer.parseInt(args[0]);
        final int maximumNumberOfMonomials = Integer.parseInt(args[1]);
        final int maximumGradeOfMonomials = Integer.parseInt(args[2]);
        final int numberOfThreads = Integer.parseInt(args[3]);
        final int numberOfProducerThreads = Integer.parseInt(args[4]);
        if (numberOfPolynomials < numberOfProducerThreads) {
            throw new Exception("The number of producer threads must be larger than the overall number of polynomials!" + System.lineSeparator());
        }

        final int numberOfConsumerThreads = numberOfThreads - numberOfProducerThreads;
        if (numberOfConsumerThreads <= 0) {
            throw new Exception("The number of consumer threads must be less than the overall number of threads!" + System.lineSeparator());
        }

        final String dir = args[5];
        String polynomialFileFormat = String.format("%d_%d_%d_%d", numberOfPolynomials, maximumNumberOfMonomials, maximumGradeOfMonomials, numberOfThreads);
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
            new PolynomialAdderSequential().execute(numberOfPolynomials, dir, polynomialFileFormat);
            new PolynomialAdderParallel().execute(numberOfProducerThreads, numberOfConsumerThreads, numberOfPolynomials, dir, polynomialFileFormat);
        }

        PolynomialAdderSequential polynomialAdderSequential = new PolynomialAdderSequential();
        PolynomialAdderParallel polynomialAdderParallel = new PolynomialAdderParallel();
        long t1 = System.nanoTime();
        Polynomial seq = polynomialAdderSequential.execute(numberOfPolynomials, dir, polynomialFileFormat);
        long t2 = System.nanoTime();
        Polynomial par = polynomialAdderParallel.execute(numberOfProducerThreads, numberOfConsumerThreads, numberOfPolynomials, dir, polynomialFileFormat);
        long t3 = System.nanoTime();
        if (!seq.equals(par)) {
            throw new Exception("RESULTS ARE NOT EQUAL!");
        }

        var dur1 = t2 - t1;
        var dur2 = t3 - t2;
        System.out.println("Threads," + numberOfThreads);
        System.out.println("Producer Threads," + numberOfProducerThreads);
        System.out.println("Sequential (ms)," + (dur1 / 1e6));
        System.out.println("Parallel (ms)," + (dur2 / 1e6));
        System.out.println("Number of polynomials," + numberOfPolynomials);
        System.out.println("Maximum number of Monomials," + maximumNumberOfMonomials);
        System.out.println("Maximum grade of Monomials," + maximumGradeOfMonomials);
    }
}
