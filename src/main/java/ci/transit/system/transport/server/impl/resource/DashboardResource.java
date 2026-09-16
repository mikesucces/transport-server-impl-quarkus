package ci.transit.system.transport.server.impl.resource;

import ci.transit.system.transport.server.impl.dto.DashboardDto;
import ci.transit.system.transport.server.impl.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class DashboardResource {

    @Inject
    DashboardService dashboardService;

    @GET
    public DashboardDto getDashboard(@QueryParam("days") @DefaultValue("30") int days) {
        return dashboardService.computeDashboard(days);
    }
}
