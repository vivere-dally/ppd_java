package p1.client.server.utils.networking.protocol.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import p1.client.server.domain.model.Ticket;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseReserveTicket implements Response {
    private Ticket ticket;
}
