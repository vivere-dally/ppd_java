package lab4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Monomial {
    private final int power;
    private double coefficient;
    private final boolean isPoisonPill;

    public Monomial(int power, double coefficient) {
        this.power = power;
        this.coefficient = coefficient;
        this.isPoisonPill = false;
    }

    public Monomial(boolean isPoisonPill) {
        power = 0;
        this.isPoisonPill = isPoisonPill;
    }

    public boolean isPoisonPill() {
        return isPoisonPill;
    }

    public int getPower() {
        return power;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monomial monomial = (Monomial) o;
        BigDecimal aa = new BigDecimal(this.coefficient);
        aa = aa.setScale(3, RoundingMode.DOWN);
        BigDecimal bb = new BigDecimal(monomial.coefficient);
        bb = bb.setScale(3, RoundingMode.DOWN);
        return getPower() == monomial.getPower() && aa.equals(bb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPower(), getCoefficient());
    }
}
