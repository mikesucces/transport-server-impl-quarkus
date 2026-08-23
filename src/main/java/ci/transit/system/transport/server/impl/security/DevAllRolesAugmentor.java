package ci.transit.system.transport.server.impl.security;

import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Attribue tous les roles aux requetes anonymes quand le bypass est
 * explicitement active (variable d'env DEV_BYPASS_ROLES=true), pour
 * pouvoir tester les endpoints @RolesAllowed sans token Keycloak.
 * Desactive par defaut : aucun impact sur un deploiement reel qui ne
 * definit pas cette variable.
 *
 * @author Transit
 *
 */
@ApplicationScoped
public class DevAllRolesAugmentor implements SecurityIdentityAugmentor {

    @ConfigProperty(name = "transit.security.dev-bypass-roles", defaultValue = "false")
    boolean bypassEnabled;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (!bypassEnabled || !identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        QuarkusSecurityIdentity newIdentity = QuarkusSecurityIdentity.builder(identity)
            .addRoles(Set.of("OWNER", "MANAGER", "CONTROLLER", "DRIVER"))
            .build();
        return Uni.createFrom().item(newIdentity);
    }
}
