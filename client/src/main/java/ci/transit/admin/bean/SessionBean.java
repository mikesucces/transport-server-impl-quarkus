package ci.transit.admin.bean;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Utilisateur connecte, affiche dans l'en-tete de la page.
 */
@Named("currentUser")
@RequestScoped
public class SessionBean {

  /** Du role le plus eleve au plus limite. */
  private static final List<String> ROLE_ORDER = List.of("OWNER", "MANAGER", "CONTROLLER", "DRIVER");

  @Inject
  SecurityIdentity identity;

  public String getUsername() {
    return identity.isAnonymous() ? "" : identity.getPrincipal().getName();
  }

  public String getDisplayName() {
    String name = getUsername();
    return name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  public String getInitials() {
    String name = getUsername();
    return name.isEmpty() ? "?" : name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.FRENCH);
  }

  public Set<String> getRoles() {
    return identity.getRoles();
  }

  /** Role principal de l'utilisateur (le plus eleve parmi ceux de l'application). */
  public String getRole() {
    return ROLE_ORDER.stream().filter(identity.getRoles()::contains).findFirst().orElse("");
  }
}
