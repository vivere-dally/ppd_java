package p1.client.server;

import java.util.concurrent.locks.ReentrantLock;

public class Application {
    private static class X {
        private final ReentrantLock lock = new ReentrantLock();

        public void doA() {
            try {
                lock.lock();
                Thread.sleep(5000);
                System.out.println("doA");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public synchronized void doB() {
            System.out.println("doB");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        double zzz = 1e-5;
        System.out.printf("%5f%n", zzz);

        X x = new X();
        Thread threadA = new Thread(x::doA);
        Thread threadB = new Thread(x::doB);
        threadA.start();
        Thread.sleep(3000);
        threadB.start();
        threadA.join();
        threadB.join();
    }
}
