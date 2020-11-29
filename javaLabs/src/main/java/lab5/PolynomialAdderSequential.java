package lab5;

import lab4.Monomial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("DuplicatedCode")
public class PolynomialAdderSequential {
    private final Polynomial polynomial;

    public PolynomialAdderSequential() {
        polynomial = new Polynomial();
    }

    public Polynomial execute(int numberOfPolynomials, String path, String polynomialFileFormat) {
        try {
            for (int polynomialIndex = 1; polynomialIndex <= numberOfPolynomials; polynomialIndex++) {
                String polynomialFile = String.format(polynomialFileFormat, path, polynomialIndex);
                String[] numbers = Files.readAllLines(Path.of(polynomialFile)).get(0).split(",");
                for (int i = 0; i < numbers.length; i += 2) {
                    double coefficient = Double.parseDouble(numbers[i]);
                    int power = Integer.parseInt(numbers[i + 1]);
                    polynomial.insertMonomial(new Monomial(power, coefficient));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return polynomial;
    }
}
