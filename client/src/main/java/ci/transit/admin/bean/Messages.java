package ci.transit.admin.bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.WebApplicationException;

/**
 * Raccourcis pour afficher les messages (succes / erreurs API) dans la vue.
 */
@ApplicationScoped
public class Messages {

  public void info(String summary) {
    add(FacesMessage.SEVERITY_INFO, summary, null);
  }

  public void error(String summary, Exception e) {
    String detail = e.getMessage();
    if (e instanceof WebApplicationException wae) {
      try {
        String body = wae.getResponse().readEntity(String.class);
        if (body != null && !body.isBlank()) {
          detail = body;
        }
      } catch (Exception ignored) {
        // on garde le message d'origine
      }
    }
    add(FacesMessage.SEVERITY_ERROR, summary, detail);
  }

  private void add(FacesMessage.Severity severity, String summary, String detail) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
  }
}
