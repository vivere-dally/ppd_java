package lab2.imgs;

public interface Img<T> {
    void fillImg(Integer[] numbers);

    void applyKernel(T kernel);

    /**
     * Returns the actual index needed for the kernel application operation.
     * If the index goes out of bounds, the function make it point to a valid index within the matrix.
     * If index is (-1,-1) it will point to (1,1). If the index is greater than the M or N, then M or N will act as a median point.
     *
     * @param index the index that may be out of bounds
     * @param limit point of reference
     * @return the actual index
     */
    default int getPureIndex(int index, int limit) {
        int pureIndex = Math.abs(index);
        if (pureIndex >= limit) {
            int offset = (1 + pureIndex - limit) * 2;
            pureIndex -= offset;
        }

        return pureIndex;
    }
}
