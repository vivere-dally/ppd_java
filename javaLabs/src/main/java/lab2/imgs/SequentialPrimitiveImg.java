package lab2.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class SequentialPrimitiveImg extends PrimitiveImg {
    public SequentialPrimitiveImg(int rows, int columns) {
        super(rows, columns);
    }

    /**
     * Multiplies a certain area of the original matrix with the kernel.
     *
     * @param kernel      kernel provided
     * @param rowIndex    row point of start
     * @param columnIndex column point of start
     * @return sum of all values from the matrix multiplied with the kernel.
     */
    private long multiply(long[][] kernel, int rowIndex, int columnIndex) {
        long sum = 0;
        for (
                int row = rowIndex - kernel.length / 2, kernelRow = 0;
                row < kernel.length / 2 + this.rows && kernelRow < kernel.length;
                row++, kernelRow++
        ) {
            int imgRow = getPureIndex(row, this.rows);
            for (
                    int column = columnIndex - kernel[0].length / 2, kernelColumn = 0;
                    column < kernel[0].length / 2 + this.columns && kernelColumn < kernel[0].length;
                    column++, kernelColumn++
            ) {
                int imgColumn = getPureIndex(column, this.columns);
                sum += this.img[imgRow][imgColumn] * kernel[kernelRow][kernelColumn];
            }
        }

        return sum;
    }

    /**
     * Applies a mxn matrix named kernel over the private MxN matrix.
     * The operation is done in-place without an additional matrix.
     *
     * @param kernel the kernel used
     */
    @Override
    public void applyKernel(long[][] kernel) {
        Deque<long[]> holder = new ArrayDeque<>(kernel.length);
        for (int row = 0; row < this.rows; row++) {
            long[] top = new long[this.columns];
            for (int column = 0; column < this.columns; column++) {
                top[column] = this.multiply(kernel, row, column);
            }

            if (holder.size() == kernel.length) {
                // The row - m row is not going to be used by any other operations so it is safe to overwrite.
                this.img[row - kernel.length] = holder.getFirst();
                holder.removeFirst();
            }

            // Save the row
            holder.addLast(top);
        }

        // Overwrite all left rows.
        int rowsLeftIndex = kernel.length;
        while (!holder.isEmpty()) {
            this.img[this.rows - rowsLeftIndex] = holder.getFirst();
            holder.removeFirst();
            rowsLeftIndex--;
        }
    }
}
