package ci.transit.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class SubscriptionRequest {
  public UUID passengerId;
  public String plan;
  public BigDecimal amount;
  public String method;

  public UUID getPassengerId() {
    return passengerId;
  }

  public void setPassengerId(UUID passengerId) {
    this.passengerId = passengerId;
  }

  public String getPlan() {
    return plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }
}
