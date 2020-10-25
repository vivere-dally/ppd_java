
package lab2.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class ParallelPrimitiveImg extends PrimitiveImg {
    private final int p, batchSize, batchSizeReminder;

    public ParallelPrimitiveImg(int rows, int columns, int p) {
        super(rows, columns);
        this.p = p;
        this.batchSize = (rows >= columns) ? (rows / p) : (columns / p);
        this.batchSizeReminder = (rows >= columns) ? ((rows % p == 0) ? 0 : 1) : ((columns % p == 0) ? 0 : 1);
    }

    private long rowMultiply(long[][] kernel, long[][] overlaidTopRows, long[][] overlaidBottomRows, int rowIndex, int columnIndex, int topLimit, int bottomLimit) {
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
                if (imgRow < topLimit) {
                    sum += overlaidTopRows[topLimit - imgRow][imgColumn] * kernel[kernelRow][kernelColumn];
                } else if (topLimit <= imgRow && imgRow < bottomLimit) {
                    sum += this.img[imgRow][imgColumn] * kernel[kernelRow][kernelColumn];
                } else {
                    sum += overlaidBottomRows[imgRow - bottomLimit][imgColumn] * kernel[kernelRow][kernelColumn];
                }
            }
        }

        return sum;
    }

    private void applyRowWise(long[][] kernel, long[][] overlaidTopRows, long[][] overlaidBottomRows, int batchStartIndex, int batchFinishIndex) {
        Deque<long[]> holder = new ArrayDeque<>(kernel.length);
        int topLimit = batchStartIndex + kernel.length / 2 - 1, bottomLimit = batchFinishIndex - kernel.length / 2 + 1;
        for (int row = batchStartIndex; row < batchFinishIndex; row++) {
            long[] top = new long[this.columns];
            for (int column = 0; column < this.columns; column++) {
                top[column] = this.rowMultiply(kernel, overlaidTopRows, overlaidBottomRows, row, column, topLimit, bottomLimit);
            }

            if (holder.size() == kernel.length) {
                this.img[row - kernel.length] = holder.getFirst();
                holder.removeFirst();
            }

            holder.addLast(top);
        }

        int rowsLeftIndex = kernel.length;
        while (!holder.isEmpty()) {
            this.img[batchFinishIndex - rowsLeftIndex] = holder.getFirst();
            holder.removeFirst();
            rowsLeftIndex--;
        }
    }

    @Override
    public void applyKernel(long[][] kernel) {
        Thread[] threads = new Thread[this.p];

        if (this.rows >= this.columns) {
            int batchSizePerThread = this.batchSize + this.batchSizeReminder;
            long[][][] overlaidTopRows = this.getOverlaidRows(kernel.length, kernel[0].length, 0);
            long[][][] overlaidBottomRows = this.getOverlaidRows(kernel.length, kernel[0].length, batchSizePerThread);
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
//                Thread thread = new Thread(() -> {
                int batchStart = batchSizePerThread * finalThreadIndex, batchFinish = batchSizePerThread * (finalThreadIndex + 1);
                this.applyRowWise(kernel, overlaidTopRows[finalThreadIndex], overlaidBottomRows[finalThreadIndex], batchStart, batchFinish);
//                });

//                thread.start();
//                threads[threadIndex] = thread;
            }
        } else {
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

    private long[][][] getOverlaidRows(int kernelRows, int kernelColumns, int offset) {
        long[][][] overlaidRows = new long[this.p][kernelRows / 2][kernelColumns];
        for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
//            for (int topRow = 0, row = kernelRows / 2 - 1; row >= 0; row--, topRow++) {
//                overlaidRows[threadIndex][topRow] = this.img[threadIndex * this.batchSize + offset + row];
//            }

            for (int row = 0; row < kernelRows / 2; row++) {
                overlaidRows[threadIndex][row] = this.img[threadIndex * row + offset];
            }
        }

        return overlaidRows;
    }
}
