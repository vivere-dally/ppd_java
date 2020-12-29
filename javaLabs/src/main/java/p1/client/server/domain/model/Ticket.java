package p1.client.server.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Vanzare (ID_spectacol, data_vanzare, nr_bilete_vandute, lista_locuri_vandute, suma)

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket implements Entity<Long> {
    private long id;
    private OffsetDateTime date;
    private List<Seat> seats;
    private Spectacle spectacle;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long value) {
        this.id = value;
    }

    public float sum() {
        return seats.size() * spectacle.getTicketPrice();
    }
}
