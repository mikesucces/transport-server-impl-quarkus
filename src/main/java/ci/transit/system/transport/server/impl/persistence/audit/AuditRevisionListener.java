package ci.transit.system.transport.server.impl.persistence.audit;

import org.hibernate.envers.RevisionListener;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * Renseigne l'auteur de chaque revision a partir de l'utilisateur authentifie de la requete en cours.
 *
 * @author Transit
 *
 */
public class AuditRevisionListener implements RevisionListener {

    static final String SYSTEM = "system";

    @Override
    public void newRevision(Object revisionEntity) {
        ((AuditRevision) revisionEntity).setUsername(currentUser());
    }

    private static String currentUser() {
        try {
            var container = Arc.container();
            if (container == null || !container.requestContext().isActive()) {
                return SYSTEM;
            }
            SecurityIdentity identity = container.instance(SecurityIdentity.class).get();
            if (identity == null || identity.isAnonymous() || identity.getPrincipal() == null) {
                return SYSTEM;
            }
            return identity.getPrincipal().getName();
        } catch (RuntimeException e) {
            return SYSTEM;
        }
    }
}
