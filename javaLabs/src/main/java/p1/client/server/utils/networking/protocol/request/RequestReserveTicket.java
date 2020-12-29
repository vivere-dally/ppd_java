package p1.client.server.utils.networking.protocol.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import p1.client.server.domain.model.Seat;
import p1.client.server.domain.model.Spectacle;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestReserveTicket implements Request {
    private List<Seat> seats;
    private Spectacle spectacle;
}
