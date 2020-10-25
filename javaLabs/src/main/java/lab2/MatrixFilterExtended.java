package lab2;

import lab0.Helper;
import lab2.imgs.ParallelPrimitiveImg;
import lab2.imgs.SequentialPrimitiveImg;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/*
 * M=N=5, m=n=3
 * MxN =
 * {
 *   {  1,  2,  3,  14,  5 },
 *   {  6,  7,  8,  9, 10 },
 *   { 11, 12, 13, 14, 15 },
 *   { 16, 17, 18, 19, 20 },
 *   { 21, 22, 23, 24, 25 }
 * }
 *
 * mxn =
 * {
 *   { 1, 1, 1 },
 *   { 1, 1, 1 },
 *   { 1, 1, 1 },
 * }
 *
 * applyKernel ->
 * {
 *   {  45,  48,  57,  66,  69 },
 *   {  60,  63,  72,  81,  84 },
 *   { 105, 108, 117, 126, 129 },
 *   { 150, 153, 162, 171, 174 },
 *   { 165, 168, 177, 186, 189 },
 * }
 *
 *
 * */

public class MatrixFilterExtended {

    public MatrixFilterExtended(String[] args) {
        if (args.length < 6) {
            System.err.println("Need 6 arguments: N, M, n, m, p, Path." + System.lineSeparator() +
                    "NxM are the dimensions of the matrix." + System.lineSeparator() +
                    "nxm are the dimensions of the kernel." + System.lineSeparator() +
                    "p is the number of threads." + System.lineSeparator() +
                    "Path is the path where the data files are.");
            throw new ArgumentAccessException("Bad args!");
        }

        int N = Integer.parseInt(args[0]);
        int M = Integer.parseInt(args[1]);
        int m = Integer.parseInt(args[2]);
        int n = Integer.parseInt(args[3]);
        int p = Integer.parseInt(args[4]);
        String dir = args[5];

        // Handle img
        String imgPath = this.createFilesIfNotExists(N * M, dir, "img_N-" + N + "_M-" + M + "_p-" + p + ".csv");
        Integer[] imgNumbers = this.loadImg(N * M, imgPath);

        SequentialPrimitiveImg seqImg = new SequentialPrimitiveImg(M, N);
        seqImg.fillImg(imgNumbers);

        ParallelPrimitiveImg parImg = new ParallelPrimitiveImg(M, N, p);
        parImg.fillImg(imgNumbers);

        // Handle kernel
        String kernelPath = this.createFilesIfNotExists(N * M, dir, "img_n-" + n + "_m-" + m + "_p-" + p + ".csv");
        Integer[] kernelNumbers = this.loadImg(n * m, kernelPath);

        SequentialPrimitiveImg kernelImg = new SequentialPrimitiveImg(m, n);
        kernelImg.fillImg(kernelNumbers);

        var t1 = ((double) System.nanoTime()) / 1e6;
        seqImg.applyKernel(kernelImg.getImg());
        var t2 = ((double) System.nanoTime()) / 1e6;
        parImg.applyKernel(kernelImg.getImg());
        var t3 = ((double) System.nanoTime()) / 1e6;
        if (!Arrays.deepEquals(seqImg.getImg(), parImg.getImg())) {
            System.err.println("Results not equal!");
        }

        var dur1 = t2 - t1;
        var dur2 = t3 - t2;
        System.out.println("Threads," + p);
        System.out.println("Sequential (ms)," + dur1);
        System.out.println("Parallel (ms)," + dur2);
        System.out.println("Specification, OJDK 11");
        System.out.println("N," + N);
        System.out.println("M," + M);
        System.out.println("n," + n);
        System.out.println("m," + m);
    }

    private String createFilesIfNotExists(int size, String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            Helper.randGenFile(file.getAbsolutePath(), size, Integer.MAX_VALUE / 2, Integer.MAX_VALUE);
        }

        return file.getAbsolutePath();
    }

    private Integer[] loadImg(int size, String path) {
        var img = new Integer[size];
        try {
            String[] numbers = Files.readAllLines(Path.of(path)).get(0).split(",");
            for (int i = 0; i < size; i++) {
                img[i] = Integer.parseInt(numbers[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

//        long[][] kernel = new long[this.m][this.n];
//        for (int i = 0; i < this.m; i++) {
//            for (int j = 0; j < this.n; j++) {
//                kernel[i][j] = 1;
//            }
//        }
//
//        Integer[] integers = new Integer[this.M * this.N];
//        for (int i = 0; i < this.M * this.N; i++) {
//            integers[i] = i + 1;
//        }
//
//        PrimitiveImg seqImg = new SequentialPrimitiveImg(this.M, this.N);
//        seqImg.fillImg(integers);
//        seqImg.applyKernel(kernel);
//        var seq = seqImg.getImg();
//
//        PrimitiveImg img = new ParallelPrimitiveImg(this.M, this.N, this.p);
//        img.fillImg(integers);
//        img.applyKernel(kernel);
//        var par = img.getImg();
//
//        if (Arrays.deepEquals(seq, par)) {
//            System.out.println("Yeeee");
//        }
//        else {
//            for (int i = 0; i < this.M; i++) {
//                for (int j = 0; j < this.N; j++) {
//                    if (seq[i][j] != par[i][j]) {
//                        System.out.println(i + " " + j + " " + seq[i][j] + " " + par[i][j] + System.lineSeparator());
//                    }
//                }
//            }
//        }
}
