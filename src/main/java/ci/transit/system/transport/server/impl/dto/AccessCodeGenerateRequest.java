package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cette classe represente les donnees de generation d'un code d'acces pour une rotation.
 *
 * @author Transit
 *
 */
public class AccessCodeGenerateRequest {

  @NotNull(message = "La rotation est obligatoire")
  public UUID rotationId;

  @Positive
  public Integer validityHours;
}
