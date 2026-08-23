package ci.transit.system.transport.server.impl.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente la vue complete d'un car :
 * caracteristiques, documents et entretiens.
 *
 * @author Transit
 *
 */
public class VehicleDetailDto {
  public VehicleDto vehicle;
  public List<DocumentDto> documents = new ArrayList<>();
  public List<MaintenanceDto> maintenances = new ArrayList<>();
}
