import lab2.MatrixFilterExtended;
import lab4.PolynomialAdder;

public class Main {
    public static void main(String[] args) {
        try {
//            Lab 2
//            new MatrixFilterExtended(args);

            // Lab 4
            PolynomialAdder polynomialAdder = new PolynomialAdder();
            polynomialAdder.execute(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
