package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees de creation ou de modification d'un code d'acces.
 *
 * @author Transit
 *
 */
public class AccessCodeRequest {

  @NotNull(message = "Le car est obligatoire")
  public UUID vehicleIdentifier;

  @NotBlank(message = "Le code est obligatoire")
  public String code;

  @NotNull(message = "La date de debut de validite est obligatoire")
  public Instant validFrom;

  @NotNull(message = "La date de fin de validite est obligatoire")
  public Instant validUntil;
}
