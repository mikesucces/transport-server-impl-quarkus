package ci.transit.system.transport.server.impl.security;

import java.util.Set;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * TEMPORAIRE : attribue tous les roles aux requetes anonymes en dev, pour
 * pouvoir tester les endpoints @RolesAllowed sans token Keycloak.
 * N'est jamais actif en dehors du profil dev.
 *
 * @author Transit
 *
 */
@IfBuildProfile("dev")
@ApplicationScoped
public class DevAllRolesAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (!identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        QuarkusSecurityIdentity newIdentity = QuarkusSecurityIdentity.builder(identity)
            .addRoles(Set.of("OWNER", "MANAGER", "CONTROLLER", "DRIVER"))
            .build();
        return Uni.createFrom().item(newIdentity);
    }
}
