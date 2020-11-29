package lab5;

import lab4.Monomial;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("DuplicatedCode")
public class Polynomial {
    private final ReentrantLock lock;
    private MonomialNode root;

    public Polynomial() {
        root = null;
        lock = new ReentrantLock();
    }

    public void insertMonomial(Monomial monomial) {
        lock.lock();
        if (root == null || root.getMonomial().getPower() < monomial.getPower()) {
            root = new MonomialNode(monomial, root);
            lock.unlock();
        }
        else {
            MonomialNode current = root, temp;
            current.lock();
            lock.unlock();
            while (current.getNext() != null && monomial.getPower() <= current.getNext().getMonomial().getPower()) {
                temp = current.getNext();
                temp.lock();
                current.unlock();
                current = temp;
            }

            if (current.getMonomial().getPower() == monomial.getPower()) {
                current.getMonomial().setCoefficient(current.getMonomial().getCoefficient() + monomial.getCoefficient());
            }
            else {
                current.setNext(new MonomialNode(monomial, current.getNext()));
            }

            current.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polynomial that = (Polynomial) o;
        MonomialNode thisCurrent, thatCurrent;
        for (
                thisCurrent = this.root, thatCurrent = that.root;
                thisCurrent != null && thatCurrent != null;
                thisCurrent = thisCurrent.getNext(), thatCurrent = thatCurrent.getNext()
        ) {
            if (!thisCurrent.getMonomial().equals(thatCurrent.getMonomial())) {
                System.out.println("Not equal here " + thisCurrent.getMonomial().getPower() + " != " + thatCurrent.getMonomial().getPower() +
                        " || " + thisCurrent.getMonomial().getCoefficient() + " != " + thatCurrent.getMonomial().getCoefficient());
                return false;
            }
        }

        return thisCurrent == null && thatCurrent == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (MonomialNode node = root; node != null; node = node.getNext()) {
            out.append(node.getMonomial().getPower()).append(" > ");
        }

        return out.toString();
    }
}
