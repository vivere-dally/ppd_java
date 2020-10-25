package lab1;

import lab0.Helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class MatrixFilter {
    private int N, M, n, m, p, paddingRow, paddingCol;
    private String dir;
    private ArrayList<ArrayList<Long>> img, filter;

    public MatrixFilter(String[] args) {
        if (args.length < 6) {
            System.err.println("Need 6 arguments: N, M, n, m, p, Path.");
        }

        this.N = Integer.parseInt(args[0]);
        this.M = Integer.parseInt(args[1]);
        this.n = Integer.parseInt(args[2]);
        this.m = Integer.parseInt(args[3]);
        this.p = Integer.parseInt(args[4]);
        this.dir = args[5];
        this.createFilesIfNotExists();

        this.paddingRow = this.m / 2;
        this.paddingCol = this.n / 2;
        this.padding();

        double t1 = (double) System.nanoTime() / 1000000;
        var seq = this.applyFilterSeq();
        double t2 = (double) System.nanoTime() / 1000000;
        var par = this.applyFilterPar();
        double t3 = (double) System.nanoTime() / 1000000;

        if (!seq.equals(par)) {
            System.err.println("Different results");
        }

        var diff1 = t2 - t1;
        var diff2 = t3 - t2;

        System.out.println("Threads," + p);
        System.out.println("Sequential (ms)," + diff1);
        System.out.println("Parallel (ms)," + diff2);
        System.out.println("Specification,OJDK 11");
    }

    private void createFilesIfNotExists() {
        String matrixFileName = "matrix_N-" + this.N + "_M-" + this.M + "_n-" + this.n + "_m-" + this.m + "_p-" + this.p + ".csv";
        File matrixFile = new File(this.dir, matrixFileName);
        if (!matrixFile.exists()) {
            Helper.randGenFile(matrixFile.getAbsolutePath(), N * M, Integer.MAX_VALUE / 2, Integer.MAX_VALUE);
        }

        this.img = loadImg(matrixFile.getAbsolutePath(), this.M, this.N);

        String filterFileName = "filter_N-" + this.N + "_M-" + this.M + "_n-" + this.n + "_m-" + this.m + "_p-" + this.p + ".csv";
        File filterFile = new File(this.dir, filterFileName);
        if (!filterFile.exists()) {
            Helper.randGenFile(matrixFile.getAbsolutePath(), n * m, Integer.MAX_VALUE / 4, Integer.MAX_VALUE / 2);
        }

        this.filter = loadImg(filterFile.getAbsolutePath(), this.m, this.n);
    }

    private ArrayList<ArrayList<Long>> loadImg(String path, int M, int N) {
        ArrayList<ArrayList<Long>> img = new ArrayList<>();
        try {
            String[] numbers = Files.readAllLines(Path.of(path)).get(0).split(",");
            for (int row = 0; row < M; row++) {
                img.add(new ArrayList<>());
                for (int col = 0; col < N; col++) {
                    img.get(row).add(Long.parseLong(numbers[row * N + col]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    ArrayList<Long> newArrayList(int size) {
        ArrayList<Long> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(0L);
        }

        return result;
    }

    void padding() {
        // Padding UP & DOWN
        for (int i = 0; i < this.paddingRow; i++) {
            ArrayList<Long> paddingUp = newArrayList(this.N), paddingDown = newArrayList(this.N);
            for (int j = 0; j < this.N; j++) {
                paddingUp.set(j, this.img.get(i * 2).get(j));
                paddingDown.set(j, this.img.get(this.img.size() - i * 2 - 1).get(j));
            }

            this.img.add(0, paddingUp);
            this.img.add(this.img.size() - 1, paddingDown);
        }

        // Padding LEFT & RIGHT
        for (int i = 0; i < this.paddingCol; i++) {
            ArrayList<Long> paddingLeft = newArrayList(this.M), paddingRight = newArrayList(this.M);
            for (int j = this.paddingRow; j < this.M + this.paddingRow; j++) {
                paddingLeft.set(j - this.paddingRow, this.img.get(j).get(i * 2));
                paddingRight.set(j - this.paddingRow, this.img.get(j).get(this.img.get(j).size() - i * 2 - 1));
            }

            for (int j = 0; j < this.M + this.paddingRow * 2; j++) {
                if (j < this.paddingRow || this.M + this.paddingRow <= j) {
                    this.img.get(j).add(0, 0L);
                    this.img.get(j).add(this.img.get(j).size() - 1, 0L);
                }
                else {
                    this.img.get(j).add(0, paddingLeft.get(j - this.paddingRow));
                    this.img.get(j).add(this.img.get(j).size() - 1, paddingRight.get(j - this.paddingRow));
                }
            }
        }

        // Padding UP LEFT
        for (int row = 0; row < this.paddingRow; row++) {
            for (int col = 0; col < this.paddingCol; col++) {
                this.img.get(row).set(col, this.img.get(this.paddingRow * 2 - row - 1).get(this.paddingCol * 2 - col - 1));
            }
        }

        // Padding UP RIGHT
        for (int row = 0; row < this.paddingRow; row++) {
            for (int col = this.N + 2 * this.paddingCol - 1, i = 0; this.N + this.paddingCol - 1 < col; col--, i += 2) {
                this.img.get(row).set(col, this.img.get(this.paddingRow * 2 - row - 1).get(col - this.paddingCol - 1 + i));
            }
        }

        // Padding DOWN LEFT
        for (int row = this.M + 2 * this.paddingRow - 1, i = 0; this.M + this.paddingRow - 1 < row; row--, i += 2) {
            for (int col = 0; col < this.paddingCol; col++) {
                this.img.get(row).set(col, this.img.get(row - this.paddingRow - 1 + i).get(this.paddingCol * 2 - col - 1));
            }
        }

        // Padding DOWN RIGHT
        for (int row = this.M + 2 * this.paddingRow - 1, i = 0; this.M + this.paddingRow - 1 < row; row--, i += 2) {
            for (int col = this.N + 2 * this.paddingCol - 1, j = 0; this.N + this.paddingCol - 1 < col; col--, j += 2) {
                this.img.get(row).set(col, this.img.get(row - this.paddingRow - 1 + i).get(col - this.paddingCol - 1 + j));
            }
        }
    }

    private Long multiply(int startingRow, int startingCol) {
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        for (int row = startingRow; row < startingRow + this.filter.size(); row++) {
            result.add(new ArrayList<>());
            for (int col = startingCol; col < startingCol + this.filter.get(0).size(); col++) {
                result.get(row - startingRow).add(0L);
                for (int k = 0; k < filter.size(); k++) {
                    result.get(row - startingRow).set(col - startingCol, img.get(row).get(k) * filter.get(k).get(col - startingCol));
                }
            }
        }

        return result.get(result.size() / 2).get(result.get(0).size() / 2);
    }

    private ArrayList<ArrayList<Long>> applyFilterSeq() {
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        for (int row = this.paddingRow; row < this.M + this.paddingRow; row++) {
            result.add(new ArrayList<>());
            for (int col = this.paddingCol; col < this.N + this.paddingCol; col++) {
                result.get(row - this.paddingRow).add(this.multiply(row - this.paddingRow, col - this.paddingCol));
            }
        }

        return result;
    }

    private ArrayList<ArrayList<Long>> applyFilterPar() {
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ArrayList<Long>> result = new ArrayList<>(this.M);
        for (int i = 0; i < this.M; i++) {
            result.add(newArrayList(this.N));
        }

        // Row separation
        if (this.M >= this.N) {
            int batchSizePerThread = this.M / this.p + ((this.M % this.p == 0) ? 0 : 1);
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
                Thread thread = new Thread(() -> {
                    int batchStart = batchSizePerThread * finalThreadIndex, batchFinish = batchSizePerThread * (finalThreadIndex + 1);
                    for (int row = this.paddingRow + batchStart; row < this.M + this.paddingRow && row <= this.paddingRow + batchFinish; row++) {
                        for (int col = this.paddingCol; col < this.N + this.paddingCol; col++) {
                            result.get(row - paddingRow).set(col - paddingCol, multiply(row - paddingRow, col - paddingCol));
                        }
                    }
                });

                thread.start();
                threads.add(thread);
            }
        }
        // Column separation
        else {
            int batchSizePerThread = this.N / this.p + ((this.N % this.p == 0) ? 0 : 1);
            for (int threadIndex = 0; threadIndex < this.p; threadIndex++) {
                int finalThreadIndex = threadIndex;
                Thread thread = new Thread(() -> {
                    int batchStart = batchSizePerThread * finalThreadIndex, batchFinish = batchSizePerThread * (finalThreadIndex + 1);
                    for (int row = this.paddingRow; row < this.M + this.paddingRow; row++) {
                        for (int col = this.paddingCol + batchStart; col < this.N + this.paddingCol && col <= this.paddingCol + batchFinish; col++) {
                            result.get(row - paddingRow).set(col - paddingCol, multiply(row - paddingRow, col - paddingCol));
                        }
                    }
                });

                thread.start();
                threads.add(thread);
            }
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
