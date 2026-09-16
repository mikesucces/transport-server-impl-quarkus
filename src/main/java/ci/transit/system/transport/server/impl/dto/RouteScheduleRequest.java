package ci.transit.system.transport.server.impl.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cette classe represente les donnees d'un horaire de ligne.
 *
 * @author Transit
 *
 */
public class RouteScheduleRequest {

  @NotBlank(message = "Le jour de la semaine est obligatoire")
  public String dayOfWeek;

  @NotNull(message = "L'heure de depart est obligatoire")
  public LocalTime departureTime;
}
