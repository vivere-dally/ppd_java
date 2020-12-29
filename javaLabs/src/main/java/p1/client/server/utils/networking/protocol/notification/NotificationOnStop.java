package p1.client.server.utils.networking.protocol.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import p1.client.server.utils.networking.protocol.response.Response;

@Data
@NoArgsConstructor
public class NotificationOnStop implements Notification, Response {
}
