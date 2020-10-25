
package lab2.border.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class ParallelPrimitiveImg extends PrimitiveImg {
    private final int p;

    public ParallelPrimitiveImg(int rows, int columns, int p) {
        super(rows, columns);
        this.p = p;
    }

    private void columnSplit(long[][] kernel, int batchStartIndex, int batchFinishIndex) {

    }

    private void rowSplit(long[][] kernel, int batchStartIndex, int batchFinishIndex) {
        int kernelRows = kernel.length, fullColumnSize = this.columns + 2 * this.paddingColumns;
        Deque<long[]> holder = new ArrayDeque<>(kernelRows);

        for (int row = batchStartIndex; row < this.rows && row <= this.paddingRows + batchFinishIndex; row++) {
            long[] top = new long[fullColumnSize];
            for (int paddingIndex = 0; paddingIndex < this.paddingColumns; paddingIndex++) {
                top[paddingIndex] = this.img[row][paddingIndex];
                top[this.columns + paddingIndex] = this.img[row][this.columns + paddingIndex];
            }

            for (
                    int column = 0, columnWithPadding = this.paddingColumns;
                    column < this.columns && columnWithPadding < this.columns + this.paddingColumns;
                    column++, columnWithPadding++
            ) {
//                top[columnWithPadding] = this.multiply(kernel, row, column);
            }
        }
    }


    @Override
    public void applyKernel(long[][] kernel) {
        Thread[] threads = new Thread[this.p];

        if (this.rows < this.columns) {
            int batchSizePerThread = this.columns / this.p + ((this.columns % this.p == 0) ? 0 : 1);
            long[][][] overlaidTopRows = this.getOverlaidTopRows();
            long[][][] overlaidBottomRows = this.getOverlaidBottomRows();
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
                Thread thread = new Thread(() -> {
                    int batchStart = batchSizePerThread * finalThreadIndex, batchFinish = batchSizePerThread * (finalThreadIndex + 1);
                    this.columnSplit(kernel, batchStart, batchFinish);
                });

                thread.start();
                threads[threadIndex] = thread;
            }
        } else {
            int batchSizePerThread = this.rows / this.p + ((this.rows % this.p == 0) ? 0 : 1);
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
                Thread thread = new Thread(() -> {
                    int batchStart = batchSizePerThread * finalThreadIndex, batchFinish = batchSizePerThread * (finalThreadIndex + 1);
                    this.rowSplit(kernel, batchStart, batchFinish);
                });

                thread.start();
                threads[threadIndex] = thread;
            }
        }

        for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
            try {
                threads[threadIndex].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long[][][] getOverlaidTopRows() {
        int batchSizePerThread = this.rows / this.p,
                batchSizeReminder = (this.rows % this.p == 0) ? 0 : 1,
                fullColumnSize = this.columns + 2 * this.paddingColumns;
        long[][][] overlaidRows = new long[this.p - 1][this.paddingRows][fullColumnSize];
        for (int threadIndex = 1; threadIndex < this.p; threadIndex++) {
            for (int row = this.paddingRows - 1; row >= 0; row--) {
                overlaidRows[threadIndex][row] = this.img[threadIndex * batchSizePerThread - batchSizeReminder - row];
            }
        }

        return overlaidRows;
    }

    @SuppressWarnings("ManualArrayCopy")
    private long[][][] getOverlaidBottomRows() {
        int batchSizePerThread = this.rows / this.p,
                batchSizeReminder = (this.rows % this.p == 0) ? 0 : 1,
                fullColumnSize = this.columns + 2 * this.paddingColumns;
        long[][][] overlaidRows = new long[this.p - 1][this.paddingRows][fullColumnSize];
        for (int threadIndex = 0; threadIndex < this.p - 1; threadIndex++) {
            for (int row = 0; row < this.paddingRows; row++) {
                overlaidRows[threadIndex][row] = this.img[(threadIndex + 1) * batchSizePerThread - batchSizeReminder + row];
            }
        }

        return overlaidRows;
    }
}
