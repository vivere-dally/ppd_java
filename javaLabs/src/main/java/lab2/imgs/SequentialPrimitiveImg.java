package lab2.imgs;

import java.util.ArrayDeque;
import java.util.Deque;

public class SequentialPrimitiveImg extends PrimitiveImg {
    public SequentialPrimitiveImg(int rows, int columns) {
        super(rows, columns);
    }

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

    @Override
    public void applyKernel(long[][] kernel) {
        Deque<long[]> holder = new ArrayDeque<>(kernel.length);

        for (int row = 0; row < this.rows; row++) {
            long[] top = new long[this.columns];
            for (int column = 0; column < this.columns; column++) {
                top[column] = this.multiply(kernel, row, column);
            }

            if (holder.size() == kernel.length) {
                this.img[row - kernel.length] = holder.getFirst();
                holder.removeFirst();
            }

            holder.addLast(top);
        }

        int rowsLeftIndex = kernel.length;
        while (!holder.isEmpty()) {
            this.img[this.rows - rowsLeftIndex] = holder.getFirst();
            holder.removeFirst();
            rowsLeftIndex--;
        }
    }
}
