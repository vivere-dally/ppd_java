import lab2.MatrixFilterExtended;
import lab4.Polynomial;
import lab5.PolynomialAdder;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
//            Lab 2
//            new MatrixFilterExtended(args);

            // Lab 4
//            PolynomialAdder polynomialAdder = new PolynomialAdder();
//            polynomialAdder.execute(args);

            // Lab 5
            PolynomialAdder polynomialAdder = new PolynomialAdder();
            polynomialAdder.execute(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
