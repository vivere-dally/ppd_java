package p1.client.server.utils.networking.service;

import p1.client.server.domain.model.Seat;

import java.util.List;

public interface Observer {
    void onStop();

    void availableSeats(List<Seat> seats);
}
