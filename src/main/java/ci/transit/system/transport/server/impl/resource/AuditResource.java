package ci.transit.system.transport.server.impl.resource;

import java.util.List;

import ci.transit.system.transport.server.impl.dto.AuditPageDto;
import ci.transit.system.transport.server.impl.dto.AuditTableDto;
import ci.transit.system.transport.server.impl.service.AuditService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Consultation du journal d'audit : reserve au proprietaire (OWNER).
 *
 * @author Transit
 *
 */
@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
public class AuditResource {

  @Inject
  AuditService auditService;

  /** Tables auditees avec le nombre de lignes d'audit de chacune. */
  @GET
  @Path("/tables")
  @RolesAllowed("OWNER")
  public List<AuditTableDto> tables() {
    return auditService.tables();
  }

  /** Historique d'une table, du plus recent au plus ancien, filtrable par auteur et par identifiant. */
  @GET
  @RolesAllowed("OWNER")
  public AuditPageDto find(@QueryParam("table") String table, @QueryParam("username") String username,
      @QueryParam("entityId") String entityId, @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("25") int size) {
    return auditService.find(table, username, entityId, page, size);
  }
}
