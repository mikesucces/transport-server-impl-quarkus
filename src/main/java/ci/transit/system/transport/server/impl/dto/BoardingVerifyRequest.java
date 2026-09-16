package ci.transit.system.transport.server.impl.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees de verification d'un code d'acces a l'embarquement.
 *
 * @author Transit
 *
 */
public class BoardingVerifyRequest {

  @NotNull(message = "La rotation est obligatoire")
  public UUID rotationId;

  @NotBlank(message = "Le code est obligatoire")
  public String code;

  @NotBlank(message = "Le telephone est obligatoire")
  public String phone;

  public UUID controllerId;
}
