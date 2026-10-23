package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Cette classe represente une ligne de la table d'audit d'une table metier :
 * l'etat de l'enregistrement apres l'operation (avant pour une suppression), l'auteur et la date.
 *
 * @author Transit
 *
 */
public class AuditEntryDto {
  public int revision;
  public Instant timestamp;
  public String username;
  /** CREATION, MODIFICATION ou SUPPRESSION. */
  public String operation;
  public String entityId;
  public Map<String, Object> data;
}
