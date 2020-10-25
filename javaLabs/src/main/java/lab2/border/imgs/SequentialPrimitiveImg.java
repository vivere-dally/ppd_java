package lab2.border.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class SequentialPrimitiveImg extends lab2.border.imgs.PrimitiveImg {
    public SequentialPrimitiveImg(int rows, int columns) {
        super(rows, columns);
    }

    private long multiply(long[][] kernel, int rowIndex, int columnIndex) {
        int kernelRows = kernel.length, kernelColumns = kernel[0].length;
        long sum = 0;
        for (int imgRow = rowIndex, kernelRow = 0; imgRow < kernelRows + rowIndex && kernelRow < kernelRows; imgRow++, kernelRow++) {
            for (int imgColumn = columnIndex, kernelColumn = 0; imgColumn < kernelColumns + columnIndex && kernelColumn < kernelColumns; imgColumn++, kernelColumn++) {
                sum += this.img[imgRow][imgColumn] * kernel[kernelRow][kernelColumn];
            }
        }

        return sum;
    }

    @Override
    public void applyKernel(long[][] kernel) {
        int kernelRows = kernel.length, fullColumnSize = this.columns + 2 * this.paddingColumns;
        Deque<long[]> holder = new ArrayDeque<>(kernelRows);

        for (int row = 0; row < this.rows; row++) {
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
                top[columnWithPadding] = this.multiply(kernel, row, column);
            }

            if (holder.size() == kernelRows) {
                this.img[row - kernelRows] = holder.getFirst();
                holder.removeFirst();
            }

            holder.addLast(top);
        }

        int rowsLeftIndex = 0;
        while (!holder.isEmpty()) {
            this.img[this.rows + rowsLeftIndex] = holder.getFirst();
            holder.removeFirst();
            rowsLeftIndex++;
        }
    }
}
