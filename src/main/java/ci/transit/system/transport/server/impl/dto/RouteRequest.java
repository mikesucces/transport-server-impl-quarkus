package ci.transit.system.transport.server.impl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Cette classe represente les donnees d'une ligne.
 *
 * @author Transit
 *
 */
public class RouteRequest {

  @NotBlank(message = "Le code est obligatoire")
  public String code;

  @NotBlank(message = "Le nom est obligatoire")
  public String name;

  @NotBlank(message = "L'origine est obligatoire")
  public String origin;

  @NotBlank(message = "La destination est obligatoire")
  public String destination;

  @PositiveOrZero
  public Integer distanceKm;

  public String status;
}
