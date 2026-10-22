package ci.transit.admin;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * La racine du site n'a pas de page d'accueil : on redirige vers le tableau de bord
 * (sans cela le serveur repond 403 Forbidden sur http://localhost:8081/).
 */
@WebServlet(urlPatterns = "")
public class RootRedirectServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect(request.getContextPath() + "/index.xhtml");
  }
}
