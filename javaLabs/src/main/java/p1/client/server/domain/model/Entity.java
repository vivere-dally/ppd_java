package p1.client.server.domain.model;

import java.io.Serializable;

public interface Entity<ID extends Serializable> extends Serializable {
    ID getId();

    void setId(ID id);
}

