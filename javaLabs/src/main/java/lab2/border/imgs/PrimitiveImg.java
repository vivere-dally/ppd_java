package lab2.border.imgs;

public abstract class PrimitiveImg implements lab2.border.imgs.Img<long[][]> {
    protected final int rows, columns;
    protected int paddingRows, paddingColumns;
    protected long[][] img;

    public PrimitiveImg(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.paddingRows = 0;
        this.paddingColumns = 0;
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

    @SuppressWarnings("ManualArrayCopy")
    @Override
    public void doPadding(int paddingRows, int paddingColumns) {
        this.paddingRows = paddingRows;
        this.paddingColumns = paddingColumns;
        int paddedImgRows = this.rows + 2 * paddingRows, paddedImgColumns = this.columns + 2 * paddingColumns;
        long[][] paddedImg = new long[paddedImgRows][paddedImgColumns];

        // Copy the img
        for (int row = paddingRows; row < this.rows + paddingRows; row++) {
            for (int column = paddingColumns; column < this.columns + paddingColumns; column++) {
                paddedImg[row][column] = this.img[row - paddingRows][row - paddingColumns];
            }
        }

        // Do padding UP & DOWN
        for (int row = 0; row < paddingRows; row++) {
            for (int column = 0; column < this.columns; column++) {
                paddedImg[paddingRows - row - 1][column] = this.img[row][column];
                paddedImg[this.rows + row - 1][column] = this.img[this.rows - row - 1][column];
            }
        }

        // Do padding LEFT & RIGHT
        for (int column = 0; column < paddingColumns; column++) {
            for (int row = 0; row < this.rows; row++) {
                paddedImg[row + paddingRows][paddingColumns - column - 1] = this.img[row][column];
                paddedImg[row + paddingRows][this.columns + column - 1] = this.img[row][this.columns - column - 1];
            }
        }

        // Do padding UP-LEFT corner
        for (int row = 0; row < paddingRows; row++) {
            for (int column = 0; column < paddingColumns; column++) {
                paddedImg[row][column] = this.img[paddingRows - row - 1][paddingColumns - column - 1];
            }
        }

        // Do padding UP-RIGHT corner
        for (int row = 0; row < paddingRows; row++) {
            for (int column = this.columns - 1; column > this.columns - paddingColumns - 1; column--) {
                paddedImg[row][paddingColumns + column] = this.img[paddingRows - row - 1][column];
            }
        }

        // Do padding DOWN-LEFT corner
        for (int row = this.rows - 1; row > this.rows - paddingRows - 1; row--) {
            for (int column = 0; column < paddingColumns; column++) {
                paddedImg[paddingRows + row][column] = this.img[row][paddingColumns - column - 1];
            }
        }

        // Do padding DOWN-RIGHT corner
        for (int row = this.rows - 1; row > this.rows - paddingRows - 1; row--) {
            for (int column = this.columns - 1; column > this.columns - paddingColumns - 1; column--) {
                paddedImg[paddingRows + row][paddingColumns + column] = this.img[row][column];
            }
        }

        // Overwrite
        this.img = paddedImg;
    }

    @Override
    public abstract void applyKernel(long[][] kernel);

    public long[][] getImg() {
        return img;
    }
}
