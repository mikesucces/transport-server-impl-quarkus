package ci.transit.system.transport.server.impl.dto;

import java.time.Instant;

/**
 * Cette classe represente le format uniforme des erreurs de l'API.
 *
 * @author Transit
 *
 */
public class ErrorResponse {
  public String code;
  public String message;
  public Instant timestamp;

  public ErrorResponse() {
  }

  public ErrorResponse(String code, String message) {
    this.code = code;
    this.message = message;
    this.timestamp = Instant.now();
  }
}
