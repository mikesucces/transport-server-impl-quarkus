package ci.transit.system.transport.server.impl.resource;

import ci.transit.system.transport.server.impl.dto.FleetAlertsDto;
import ci.transit.system.transport.server.impl.service.FleetService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/fleet")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class FleetResource {

    @Inject
    FleetService fleetService;

    /** Alimente le bloc « A surveiller » du tableau de bord. */
    @GET
    @Path("/alerts")
    public FleetAlertsDto getAlerts(@QueryParam("days") @DefaultValue("30") int days) {
        return fleetService.computeAlerts(days);
    }
}
