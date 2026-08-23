package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees d'enregistrement d'une tentative de connexion.
 *
 * @author Transit
 *
 */
public class LoginAttemptRequest {

  @NotBlank(message = "L'identifiant est obligatoire")
  public String identifier;

  public UUID passengerIdentifier;

  @NotNull(message = "Le resultat de la tentative est obligatoire")
  public Boolean success;

  public String ipAddress;
}
