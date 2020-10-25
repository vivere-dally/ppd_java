package lab2.border.imgs;


import java.util.ArrayList;

public abstract class DataTypeImg implements Img<ArrayList<ArrayList<Long>>> {
    private int rows, columns;
    private ArrayList<ArrayList<Long>> img;

    public DataTypeImg(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.img = initializeImg(rows, columns);
    }

    private ArrayList<ArrayList<Long>> initializeImg(int rows, int columns) {
        ArrayList<ArrayList<Long>> img = new ArrayList<>(rows);
        for (int row = 0; row < rows; row++) {
            img.add(new ArrayList<>(columns));
            for (int column = 0; column < columns; column++) {
                img.get(row).add(0L);
            }
        }

        return img;
    }

    @Override
    public void fillImg(Integer[] numbers) {
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                this.img.get(row).set(column, Long.valueOf(numbers[row * this.columns + column]));
            }
        }
    }

    @Override
    public void doPadding(int paddingRows, int paddingColumns) {
        int paddedImgRows = this.rows + 2 * paddingRows, paddedImgColumns = this.columns + 2 * paddingColumns;
        ArrayList<ArrayList<Long>> paddedImg = initializeImg(paddedImgRows, paddingColumns);

        // Copy the img
        for (int row = paddingRows; row < this.rows + paddingRows; row++) {
            for (int column = paddingColumns; column < this.columns + paddingColumns; column++) {
                paddedImg.get(row).set(column, this.img.get(row - paddingRows).get(row - paddingColumns));
            }
        }

        // Do padding UP & DOWN
        for (int row = 0; row < paddingRows; row++) {
            for (int column = 0; column < this.columns; column++) {
                paddedImg.get(paddingRows - row - 1).set(column, this.img.get(row).get(column));
                paddedImg.get(this.rows + row - 1).set(column, this.img.get(this.rows - row - 1).get(column));
            }
        }

        // Do padding LEFT & RIGHT
        for (int column = 0; column < paddingColumns; column++) {
            for (int row = 0; row < this.rows; row++) {
                paddedImg.get(row + paddingRows).set(paddingColumns - column - 1, this.img.get(row).get(column));
                paddedImg.get(row + paddingRows).set(this.columns + column - 1, this.img.get(row).get(this.columns - column - 1));
            }
        }

        // Do padding UP-LEFT corner
        for (int row = 0; row < paddingRows; row++) {
            for (int column = 0; column < paddingColumns; column++) {
                paddedImg.get(row).set(column, this.img.get(paddingRows - row - 1).get(paddingColumns - column - 1));
            }
        }

        // Do padding UP-RIGHT corner
        for (int row = 0; row < paddingRows; row++) {
            for (int column = this.columns - 1; column > this.columns - paddingColumns - 1; column--) {
                paddedImg.get(row).set(paddingColumns + column, this.img.get(paddingRows - row - 1).get(column));
            }
        }

        // Do padding DOWN-LEFT corner
        for (int row = this.rows - 1; row > this.rows - paddingRows - 1; row--) {
            for (int column = 0; column < paddingColumns; column++) {
                paddedImg.get(paddingRows + row).set(column, this.img.get(row).get(paddingColumns - column - 1));
            }
        }

        // Do padding DOWN-RIGHT corner
        for (int row = this.rows - 1; row > this.rows - paddingRows - 1; row--) {
            for (int column = this.columns - 1; column > this.columns - paddingColumns - 1; column--) {
                paddedImg.get(paddingRows + row).set(paddingColumns + column, this.img.get(row).get(column));
            }
        }

        // Overwrite
        this.img = paddedImg;
        this.rows = paddedImgRows;
        this.columns = paddedImgColumns;
    }

    @Override
    public abstract void applyKernel(ArrayList<ArrayList<Long>> kernel);

    public ArrayList<ArrayList<Long>> getImg() {
        return img;
    }
}
