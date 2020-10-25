package lab2.border.imgs;

import lab0.Helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ManageData {
    private final String path;
    private final int rows, columns, lowerBound, upperBound;

    public ManageData(String path, int rows, int columns, int lowerBound, int upperBound) {
        this.path = path;
        this.rows = rows;
        this.columns = columns;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private void createFileIfNotExists() {
        File file = new File(this.path);
        if (!file.exists()) {
            Helper.randGenFile(this.path, this.rows * this.columns, this.lowerBound, this.upperBound);
        }
    }

    public long[][] loadImg() {
        var img = new long[this.rows][this.columns];
        try {
            String[] numbers = Files.readAllLines(Path.of(this.path)).get(0).split(",");
            for (int row = 0; row < this.rows; row++) {
                for (int col = 0; col < this.columns; col++) {
                    img[row][col] = Integer.parseInt(numbers[row * this.columns + col]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }
}
