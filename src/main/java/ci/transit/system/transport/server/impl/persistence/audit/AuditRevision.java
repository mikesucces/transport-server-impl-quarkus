package ci.transit.system.transport.server.impl.persistence.audit;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Revision d'audit : une ligne par transaction ayant modifie des donnees.
 * Porte la date (revtstmp, heritee) et l'auteur de la modification.
 * Chaque table audite possede sa table <code>xxx_aud</code> qui renvoie ici par la colonne rev.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevision extends DefaultRevisionEntity {

    private static final long serialVersionUID = 1L;

    /** Identifiant de l'utilisateur (nom d'utilisateur du jeton), ou "system". */
    @Column(name = "username", length = 120)
    private String username;
}
