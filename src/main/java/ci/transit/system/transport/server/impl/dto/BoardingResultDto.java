package ci.transit.system.transport.server.impl.dto;

/**
 * Cette classe represente le resultat d'une verification de code a l'embarquement.
 *
 * @author Transit
 *
 */
public class BoardingResultDto {
  public boolean success;
  public String message;
  public AttendanceDto attendance;
  public Long loginAttemptId;
}
