package sem2;

import java.util.Random;
import java.util.function.BinaryOperator;

public class Sem2 {
    //    static class SumThreads extends Thread {
//        private int start, finish;
//        private double[] x, y, z;
//        public SumThreads(double[] x, double[] y, double[] z, int start, int finish) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.start = start;
//            this.finish = finish;
//        }
//
//        @Override
//        public void run() {
//            for (int i = this.start; i < this.finish; i++) {
//                this.z[i] = this.x[i] + this.y[i];
//            }
//        }
//    }

    public Sem2() {
        Random random = new Random();
        int n = 1 << 20, p = 4;
        double[] x = new double[n], y = new double[n], z1 = new double[n], z2 = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = 2;
            y[i] = 3;
        }

        long t1 = System.nanoTime();
//        sequentialCalculation(x, y, z1, n);
        sequentialComplexCalculation(x, y, z1, n, (a, b) -> Math.sqrt(a * a * a + b * b * b));
        long t2 = System.nanoTime();
//        parallelCalculation(x, y, z2, n, p);
        parallelComplexCalculation(x, y, z2, n, p, (a, b) -> Math.sqrt(a * a * a + b * b * b));
        long t3 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            if (z1[i] != z2[i]) {
                System.out.println("Bad!");
                return;
            }
        }

        System.out.println((double) (t2 - t1) / 1e6);
        System.out.println((double) (t3 - t2) / 1e6);
    }

    public static void sequentialCalculation(double[] x, double[] y, double[] z, int n) {
        for (int i = 0; i < n; i++) {
            z[i] = x[i] + y[i];
        }
    }

    public static void sequentialComplexCalculation(double[] x, double[] y, double[] z, int n, BinaryOperator<Double> operator) {
        for (int i = 0; i < n; i++) {
//            z[i] = operator.apply(x[i], y[i]);
            z[i] = Math.sqrt(x[i] * x[i] * x[i] + y[i] * y[i] * y[i]);
        }
    }

    public static void parallelCalculation(double[] x, double[] y, double[] z, int n, int p) {
        Thread[] threads = new Thread[p];
        int numberOfOperations = (n % p == 0) ? n / p : (n / p) + 1;
        int prev = 0;
        for (int i = 0; i < p; i++) {
            int finalPrev = prev;
            int curr = prev + numberOfOperations;
            threads[i] = new Thread(() -> {
                for (int j = finalPrev; j < curr && j < n; j++) {
                    z[j] = x[j] + y[j];
                }
            });

            threads[i].start();
            prev = curr;
        }

        for (int i = 0; i < p; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parallelComplexCalculation(double[] x, double[] y, double[] z, int n, int p, BinaryOperator<Double> operator) {
        Thread[] threads = new Thread[p];
        int numberOfOperations = (n % p == 0) ? n / p : (n / p) + 1;
        int prev = 0;
        for (int i = 0; i < p; i++) {
            int finalPrev = prev;
            int curr = prev + numberOfOperations;
            threads[i] = new Thread(() -> {
                for (int j = finalPrev; j < curr && j < n; j++) {
//                    z[j] = operator.apply(x[j], y[j]);
                    z[j] = Math.sqrt(x[j] * x[j] * x[j] + y[j] * y[j] * y[j]);
                }
            });

            threads[i].start();
            prev = curr;
        }

        for (int i = 0; i < p; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
