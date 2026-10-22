package ci.transit.admin.dto;

public class RouteRequest {
  public String code;
  public String name;
  public String origin;
  public String destination;
  public Integer distanceKm;
  public String status;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Integer getDistanceKm() {
    return distanceKm;
  }

  public void setDistanceKm(Integer distanceKm) {
    this.distanceKm = distanceKm;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
