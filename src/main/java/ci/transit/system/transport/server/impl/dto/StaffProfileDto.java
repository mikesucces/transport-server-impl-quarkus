package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe represente un profil personnel expose par l'API.
 *
 * @author Transit
 *
 */
public class StaffProfileDto {
  public UUID identifier;
  public UUID keycloakSub;
  public String username;
  public String fullName;
  public String phone;
  public String email;
  public String staffType;
  public String matricule;
  public boolean active;
  public Instant firstSeenAt;
  public Instant lastLoginAt;
}
