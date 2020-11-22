package lab4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PolynomialGenerator {
    private static PolynomialGenerator instance = null;

    private int[] getMonomialsPowers(int numberOfMonomials, int maximumGradeOfMonomials) {
        final List<Integer> powers = new ArrayList<>(maximumGradeOfMonomials);
        for (int i = 0; i < maximumGradeOfMonomials; i++) {
            powers.add(i, i);
        }
        Collections.shuffle(powers);
        return powers
                .stream()
                .limit(numberOfMonomials)
                .mapToInt(i -> i)
                .toArray();
    }

    public static PolynomialGenerator getInstance() {
        if (instance == null) {
            instance = new PolynomialGenerator();
        }

        return instance;
    }

    public void generate(String path, int maximumNumberOfMonomials, int maximumGradeOfMonomials) {
        Random random = new Random();
        try (FileWriter fileWriter = new FileWriter(path)) {
            int numberOfMonomials = random.nextInt(maximumNumberOfMonomials - 1) + 2;
            int[] powers = getMonomialsPowers(numberOfMonomials, maximumGradeOfMonomials);
            for (int i = 0; i < numberOfMonomials; i++) {
                int left = random.nextInt(Integer.MAX_VALUE / 2) * ((random.nextBoolean()) ? 1 : -1);
                int right = random.nextInt(Integer.MAX_VALUE / 2);
                fileWriter.write(String.format("%d.%d,%d", left, right, powers[i]));
                if (i != numberOfMonomials - 1) {
                    fileWriter.write(',');
                }
            }

            fileWriter.write(System.lineSeparator());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
