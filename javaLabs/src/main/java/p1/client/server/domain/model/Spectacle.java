package p1.client.server.domain.model;

//Spectacol (ID_spectacol, data_spectacol, titlu, pret_bilet, lista_locuri_vandute, sold)

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spectacle implements Entity<Long> {
    private long id;
    private String title;
    private OffsetDateTime date;
    private float ticketPrice;
    private float balance;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long value) {
        this.id = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spectacle spectacle = (Spectacle) o;
        return getId().equals(spectacle.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
