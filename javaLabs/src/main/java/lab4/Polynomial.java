package lab4;

import java.util.Objects;

public class Polynomial {
    private int length;
    private MonomialNode root;

    public Polynomial() {
        root = null;
        length = 0;
    }

    public synchronized void insertMonomial(Monomial monomial) {
        MonomialNode previous = null, current = root;
        for (; current != null; current = current.getNext()) {
            if (current.getMonomial().getPower() == monomial.getPower()) {
                double coefficient = current.getMonomial().getCoefficient() + monomial.getCoefficient();
                current.getMonomial().setCoefficient(coefficient);
                return;
            }

            if (current.getMonomial().getPower() < monomial.getPower()) {
                break;
            }

            previous = current;
        }

        MonomialNode node = new MonomialNode(monomial, null);
        if (previous == null && current == null) {
            root = node;
        }
        else if (previous == null) {
            node.setNext(root);
            root = node;
        }
        else {
            node.setNext(current);
            previous.setNext(node);
        }

        length++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polynomial that = (Polynomial) o;
        if (this.length != that.length) {
            return false;
        }

        MonomialNode thisCurrent, thatCurrent;
        for (
                thisCurrent = this.root, thatCurrent = that.root;
                thisCurrent != null && thatCurrent != null;
                thisCurrent = thisCurrent.getNext(), thatCurrent = thatCurrent.getNext()
        ) {
            if (!thisCurrent.getMonomial().equals(thatCurrent.getMonomial())) {
                return false;
            }
        }

        return thisCurrent == null && thatCurrent == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
