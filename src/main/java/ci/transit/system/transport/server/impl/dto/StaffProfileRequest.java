package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees d'un profil personnel.
 *
 * @author Transit
 *
 */
public class StaffProfileRequest {

  @NotNull(message = "Le sub Keycloak est obligatoire")
  public UUID keycloakSub;

  @NotBlank(message = "Le nom d'utilisateur est obligatoire")
  public String username;

  @NotBlank(message = "Le nom est obligatoire")
  public String fullName;

  public String phone;
  public String email;

  @NotBlank(message = "Le type de personnel est obligatoire")
  public String staffType;

  public String matricule;
  public Boolean active;
}
