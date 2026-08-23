package ci.transit.system.transport.server.impl.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe de base des entites : porte l'identifiant technique.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(name = "id")
    public UUID uuid;

    @PrePersist
    public void initializeEntity() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}
