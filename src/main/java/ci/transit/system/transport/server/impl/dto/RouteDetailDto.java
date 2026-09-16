package ci.transit.system.transport.server.impl.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente la vue complete d'une ligne : ses
 * caracteristiques et ses horaires recurrents.
 *
 * @author Transit
 *
 */
public class RouteDetailDto {
  public RouteDto route;
  public List<RouteScheduleDto> schedules = new ArrayList<>();
}
