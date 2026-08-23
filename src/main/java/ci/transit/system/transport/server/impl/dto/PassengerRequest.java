package ci.transit.system.transport.server.impl.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cette classe represente les donnees de creation ou de modification d'un usager.
 *
 * @author Transit
 *
 */
public class PassengerRequest {

  @NotBlank(message = "Le nom est obligatoire")
  public String fullName;

  @NotBlank(message = "Le telephone est obligatoire")
  public String phone;

  public String status;
}
