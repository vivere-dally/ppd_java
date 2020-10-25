package lab2.imgs;

public interface Img<T> {
    void fillImg(Integer[] numbers);

    void applyKernel(T kernel);

    default int getPureIndex(int index, int limit) {
        int pureIndex = Math.abs(index);
        if (pureIndex >= limit) {
            int offset = (1 + pureIndex - limit) * 2;
            pureIndex -= offset;
        }

        return pureIndex;
    }
}
