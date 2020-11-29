package lab5;

import lab4.Monomial;

import java.util.concurrent.locks.ReentrantLock;

public class MonomialNode {
    private final Monomial monomial;
    private MonomialNode next;
    private final ReentrantLock lock;

    public MonomialNode(Monomial monomial, MonomialNode next) {
        this.monomial = monomial;
        this.next = next;
        this.lock = new ReentrantLock();
    }

    public Monomial getMonomial() {
        return monomial;
    }

    public MonomialNode getNext() {
        return next;
    }

    public void setNext(MonomialNode next) {
        this.next = next;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
