package p1.client.server.utils.networking.protocol.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import p1.client.server.domain.model.Seat;
import p1.client.server.utils.networking.protocol.response.Response;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAvailableSeats implements Notification, Response {
    private List<Seat> seats;
}
