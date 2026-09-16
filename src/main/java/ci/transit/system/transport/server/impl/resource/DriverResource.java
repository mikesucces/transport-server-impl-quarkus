package ci.transit.system.transport.server.impl.resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.DriverDto;
import ci.transit.system.transport.server.impl.dto.DriverRequest;
import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.service.DriverService;
import ci.transit.system.transport.server.impl.service.RotationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DriverResource {

    @Inject
    DriverService driverService;

    @Inject
    RotationService rotationService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<DriverDto> getAllDrivers(@QueryParam("status") String status) {
        return driverService.findAll(status);
    }

    @GET
    @Path("/available")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<DriverDto> getAvailableDrivers() {
        return driverService.findAvailable();
    }

    @GET
    @Path("/expiring-licenses")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<DriverDto> getExpiringLicenses(@QueryParam("days") @DefaultValue("30") int days) {
        return driverService.findWithExpiringLicense(days);
    }

    /** La masse salariale est une donnee financiere reservee au proprietaire. */
    @GET
    @Path("/payroll")
    @RolesAllowed("OWNER")
    public Map<String, BigDecimal> getPayroll() {
        return Map.of("monthlyPayroll", driverService.computeMonthlyPayroll());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public DriverDto getDriver(@PathParam("id") UUID identifier) {
        return driverService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createDriver(@Valid DriverRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(driverService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public DriverDto updateDriver(@PathParam("id") UUID identifier, @Valid DriverRequest request) {
        return driverService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public DriverDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return driverService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteDriver(@PathParam("id") UUID identifier) {
        driverService.delete(identifier);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/rotations")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RotationDto> getDriverRotations(@PathParam("id") UUID identifier) {
        return rotationService.findByDriver(identifier);
    }
}
