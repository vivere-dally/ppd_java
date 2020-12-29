package p1.client.server.utils;

import p1.client.server.controller.Controller;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Spectacle;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import static p1.client.server.utils.Constants.VALIDATION_FILE;
import static p1.client.server.utils.Constants.VALIDATION_THREAD_OFFSET_IN_MILLISECONDS;

public class ValidationRunnable implements Runnable {
    private final Controller controller;
    private final ReadWriteLock readWriteLock;
    private final int numberOfSeats, numberOfSpectacles;
    private final float[] spectaclePrices;

    private final MyFileWriter myFileWriter;

    private boolean running = true;


    public ValidationRunnable(Controller controller, ReadWriteLock readWriteLock, int numberOfSeats, int numberOfSpectacles, float[] spectaclePrices) {
        this.controller = controller;
        this.readWriteLock = readWriteLock;
        this.numberOfSeats = numberOfSeats;
        this.numberOfSpectacles = numberOfSpectacles;
        this.spectaclePrices = spectaclePrices;

        this.myFileWriter = new MyFileWriter(VALIDATION_FILE);
    }

    public void stop() {
        this.running = false;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (this.running) {
            List<String> messages = new ArrayList<>(numberOfSpectacles);
            try {
                Thread.sleep(VALIDATION_THREAD_OFFSET_IN_MILLISECONDS);
                this.readWriteLock.readLock().lock();
                int i = 0;
                for (Spectacle spectacle : this.controller.findAllSpectacles()) {
                    float sum = this.controller.sum(spectacle);
                    int numberOfReservedSeats = this.controller.numberOfReservedSeats(spectacle);
                    List<Seat> seats = this.controller.findAllSeats(spectacle);
                    boolean hasError = false;
                    if (Math.abs(sum - spectacle.getBalance()) > 1e-5 ||
                            numberOfReservedSeats + seats.size() != this.numberOfSeats) {
                        hasError = true;
                    }

                    messages.add(
                            String.format(
                                    "[%s] [%s] { spectacle [%s] balance [stored]=%5f [computed]=%5f [seatPrice]=%f } { seats [sold]=%d [left]=%d [total]=%d }",
                                    OffsetDateTime.now().toString(), hasError, spectacle.getId(), spectacle.getBalance(), sum, spectaclePrices[i++], numberOfReservedSeats, seats.spliterator().getExactSizeIfKnown(), numberOfSeats
                            )
                    );
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.readWriteLock.readLock().unlock();
            }

            myFileWriter.writeAppend(messages);
        }
    }
}
