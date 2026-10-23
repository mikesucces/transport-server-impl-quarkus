package ci.transit.system.transport.server.impl.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe represente une page de l'historique d'audit d'une table.
 *
 * @author Transit
 *
 */
public class AuditPageDto {
  public String table;
  public long total;
  public int page;
  public int size;
  public List<AuditEntryDto> items = new ArrayList<>();
}
