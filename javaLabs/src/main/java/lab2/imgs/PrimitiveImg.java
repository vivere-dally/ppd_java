package lab2.imgs;

public abstract class PrimitiveImg implements Img<long[][]> {
    protected final int rows, columns;
    protected long[][] img;

    public PrimitiveImg(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.img = new long[rows][columns];
    }

    @Override
    public void fillImg(Integer[] numbers) {
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                this.img[row][column] = numbers[row * this.columns + column];
            }
        }
    }

    @Override
    public abstract void applyKernel(long[][] kernel);

    public long[][] getImg() {
        return img;
    }
}
