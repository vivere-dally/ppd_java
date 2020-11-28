package lab5;

import lab4.Monomial;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("DuplicatedCode")
public class Polynomial {
    private int length;
    private MonomialNode root;
    private final ReentrantLock lock;

    public Polynomial() {
        root = null;
        length = 0;
        lock = new ReentrantLock(true);
    }

    public void insertMonomial(Monomial monomial) {
        lock.lock();
        MonomialNode previous = null, current = root, next;
        lock.unlock();
        for (; current != null; current = next) {
            try {
                current.lock();
                if (current.getMonomial().getPower() == monomial.getPower()) {
                    double coefficient = current.getMonomial().getCoefficient() + monomial.getCoefficient();
                    current.getMonomial().setCoefficient(coefficient);
                    return;
                }

                if (current.getMonomial().getPower() < monomial.getPower()) {
                    break;
                }

                previous = current;
            } finally {
                next = current.getNext();
                current.unlock();
            }
        }

        lock.lock();
        MonomialNode node = new MonomialNode(monomial, null);
        if (previous == null && current == null) {
            root = node;
        }
        else if (previous == null) {
            node.setNext(root);
            root = node;
        }
        else {
            previous.lock();
            node.setNext(current);
            previous.setNext(node);
            previous.unlock();
        }

        length++;
        lock.unlock();
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
