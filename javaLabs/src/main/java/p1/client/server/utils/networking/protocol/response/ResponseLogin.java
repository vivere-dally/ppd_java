package p1.client.server.utils.networking.protocol.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import p1.client.server.domain.model.Seat;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseLogin implements Response {
    private int id;
    private List<Seat> seats;
}
