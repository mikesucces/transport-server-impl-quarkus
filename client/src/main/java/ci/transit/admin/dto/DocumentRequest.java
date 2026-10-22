package ci.transit.admin.dto;

import java.time.LocalDate;

public class DocumentRequest {
  public String docType;
  public String reference;
  public String issuer;
  public LocalDate issuedOn;
  public LocalDate expiresOn;

  public String getDocType() {
    return docType;
  }

  public void setDocType(String docType) {
    this.docType = docType;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public LocalDate getIssuedOn() {
    return issuedOn;
  }

  public void setIssuedOn(LocalDate issuedOn) {
    this.issuedOn = issuedOn;
  }

  public LocalDate getExpiresOn() {
    return expiresOn;
  }

  public void setExpiresOn(LocalDate expiresOn) {
    this.expiresOn = expiresOn;
  }
}
