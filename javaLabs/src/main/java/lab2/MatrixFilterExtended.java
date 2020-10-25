package lab2;

import lab2.imgs.ParallelPrimitiveImg;
import lab2.imgs.PrimitiveImg;
import lab2.imgs.SequentialPrimitiveImg;

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
    private final int N, M, n, m, p;
//    private final String dir;

    public MatrixFilterExtended(String[] args) {
        this.N = 1000;
        this.M = 1000;
        this.m = 5;
        this.n = 5;
        this.p = 4;

        long[][] kernel = new long[this.m][this.n];
        for (int i = 0; i < this.m; i++) {
            for (int j = 0; j < this.n; j++) {
                kernel[i][j] = 1;
            }
        }

        Integer[] integers = new Integer[this.M * this.N];
        for (int i = 0; i < this.M * this.N; i++) {
            integers[i] = i + 1;
        }

        PrimitiveImg seqImg = new SequentialPrimitiveImg(this.M, this.N);
        seqImg.fillImg(integers);
        seqImg.applyKernel(kernel);
        var seq = seqImg.getImg();

        PrimitiveImg img = new ParallelPrimitiveImg(this.M, this.N, this.p);
        img.fillImg(integers);
        img.applyKernel(kernel);
        var par = img.getImg();

        if (Arrays.deepEquals(seq, par)) {
            System.out.println("Yeeee");
        }
        else {
            for (int i = 0; i < this.M; i++) {
                for (int j = 0; j < this.N; j++) {
                    if (seq[i][j] != par[i][j]) {
                        System.out.println(i + " " + j + " " + seq[i][j] + " " + par[i][j] + System.lineSeparator());
                    }
                }
            }
        }
    }
}
