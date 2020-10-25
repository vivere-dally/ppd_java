package lab2;

import lab2.imgs.ParallelPrimitiveImg;
import lab2.imgs.PrimitiveImg;
import lab2.imgs.SequentialPrimitiveImg;

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
    //    private final int N, M, n, m, p, paddingRow, paddingCol;
//    private final String dir;
    public MatrixFilterExtended(String[] args) {
        long[][] kernel = new long[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                kernel[i][j] = 1;
            }
        }

        Integer[] integers = new Integer[5 * 5];
        for (int i = 0; i < 25; i++) {
            integers[i] = i + 1;
        }

        PrimitiveImg seqImg = new SequentialPrimitiveImg(5, 5);
        seqImg.fillImg(integers);
        seqImg.applyKernel(kernel);
        var seq = seqImg.getImg();

        PrimitiveImg img = new ParallelPrimitiveImg(5, 5, 2);
        img.fillImg(integers);
        img.applyKernel(kernel);
        var par = img.getImg();
    }
}
