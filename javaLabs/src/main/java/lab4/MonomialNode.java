package lab4;

public class MonomialNode {
    private final Monomial monomial;
    private MonomialNode next;

    public MonomialNode(Monomial monomial, MonomialNode next) {
        this.monomial = monomial;
        this.next = next;
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
}
