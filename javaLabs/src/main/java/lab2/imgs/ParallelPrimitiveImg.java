
package lab2.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class ParallelPrimitiveImg extends PrimitiveImg {
    private final int p;

    public ParallelPrimitiveImg(int rows, int columns, int p) {
        super(rows, columns);
        this.p = p;
    }

    private long rowMultiply(long[][] kernel, long[][] overlaidTopRows, long[][] overlaidBottomRows, int rowIndex, int columnIndex, int batchStartIndex, int batchFinishIndex) {
        long sum = 0;
        for (
                int row = rowIndex - kernel.length / 2, kernelRow = 0;
                row < kernel.length / 2 + this.rows && kernelRow < kernel.length;
                row++, kernelRow++
        ) {
            int imgRow = this.getPureIndex(row, this.rows);
            for (
                    int column = columnIndex - kernel[0].length / 2, kernelColumn = 0;
                    column < kernel[0].length / 2 + this.columns && kernelColumn < kernel[0].length;
                    column++, kernelColumn++
            ) {
                int imgColumn = this.getPureIndex(column, this.columns);
                long value;
                if (imgRow < batchStartIndex) {
                    value = overlaidTopRows[overlaidTopRows.length + imgRow - batchStartIndex][imgColumn];
                }
                else if (batchStartIndex <= imgRow && imgRow < batchFinishIndex) {
                    value = this.img[imgRow][imgColumn];
                }
                else {
                    value = overlaidBottomRows[imgRow - batchFinishIndex][imgColumn];
                }

                sum += value * kernel[kernelRow][kernelColumn];
            }
        }

        return sum;
    }

    private void applyRowWise(long[][] kernel, long[][] overlaidTopRows, long[][] overlaidBottomRows, int batchStartIndex, int batchFinishIndex) {
        Deque<long[]> holder = new ArrayDeque<>(kernel.length);
        for (int row = batchStartIndex; row < batchFinishIndex; row++) {
            long[] top = new long[this.columns];
            for (int column = 0; column < this.columns; column++) {
                top[column] = this.rowMultiply(kernel, overlaidTopRows, overlaidBottomRows, row, column, batchStartIndex, batchFinishIndex);
            }

            if (holder.size() == kernel.length) {
                this.img[row - kernel.length] = holder.getFirst();
                holder.removeFirst();
            }

            holder.addLast(top);
        }

        int rowsLeftIndex = batchFinishIndex - holder.size();
        while (!holder.isEmpty()) {
            this.img[rowsLeftIndex] = holder.getFirst();
            holder.removeFirst();
            rowsLeftIndex++;
        }
    }

    @Override
    public void applyKernel(long[][] kernel) {
        Thread[] threads = new Thread[this.p];
        int batchSize = (this.rows >= this.columns) ? (this.rows / this.p) : (this.columns / this.p);

        if (this.rows >= this.columns) {
            long[][][] overlaidTopRows = this.getTopOverlaidRows(kernel.length, batchSize);
            long[][][] overlaidBottomRows = this.getBottomOverlaidRows(kernel.length, batchSize);
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
                Thread thread = new Thread(() -> {
                    int batchStart = batchSize * finalThreadIndex, batchFinish = batchSize * (finalThreadIndex + 1);
                    if (finalThreadIndex == this.p - 1) {
                        batchFinish = this.rows;
                    }

                    this.applyRowWise(kernel, overlaidTopRows[finalThreadIndex], overlaidBottomRows[finalThreadIndex], batchStart, batchFinish);
                });

                thread.start();
                threads[threadIndex] = thread;
            }
        }
        else {
            //TODO
        }

        for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
            try {
                threads[threadIndex].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long[][][] getTopOverlaidRows(int kernelRows, int batchSize) {
        long[][][] overlaidRows = new long[this.p][kernelRows / 2][this.columns];
        for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
            for (int row = threadIndex * batchSize - 1, steps = kernelRows / 2 - 1; row >= 0 && steps >= 0; row--, steps--) {
                overlaidRows[threadIndex][steps] = this.img[row];
            }
        }

        return overlaidRows;
    }

    private long[][][] getBottomOverlaidRows(int kernelRows, int batchSize) {
        long[][][] overlaidRows = new long[this.p][kernelRows / 2][this.columns];
        for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
            for (int kernelRow = 0; kernelRow < kernelRows / 2; kernelRow++) {
                int imgIndex = (threadIndex + 1) * batchSize + kernelRow;
                if (0 <= imgIndex && imgIndex < this.rows) {
                    overlaidRows[threadIndex][kernelRow] = this.img[imgIndex];
                }
            }
        }

        return overlaidRows;
    }
}
