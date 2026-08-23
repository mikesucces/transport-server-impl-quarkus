package ci.transit.system.transport.server.impl;

/**
 * Cette classe represente le point d'entree de l'application.
 *
 * @author Transit
 *
 */
public class Application extends jakarta.ws.rs.core.Application {

  private final String timeZoneIdentifier = "UTC";

  public Application() {
    super();
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(timeZoneIdentifier));
  }

  public String getTimeZoneIdentifier() {
    return timeZoneIdentifier;
  }
}
