package lab4;

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
        return getPower() == monomial.getPower() &&
                Double.compare(monomial.getCoefficient(), getCoefficient()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPower(), getCoefficient());
    }
}
