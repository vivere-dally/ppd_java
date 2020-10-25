package lab2.border.imgs;

public interface Img<T> {
    void fillImg(Integer[] numbers);

    void doPadding(int paddingRows, int paddingColumns);

    void applyKernel(T kernel);
}
