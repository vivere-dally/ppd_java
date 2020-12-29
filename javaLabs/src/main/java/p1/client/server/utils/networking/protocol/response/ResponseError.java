package p1.client.server.utils.networking.protocol.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseError implements Response {
    private Throwable cause;
}
