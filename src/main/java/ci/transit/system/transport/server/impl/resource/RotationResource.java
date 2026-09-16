package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.AttendanceDto;
import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.dto.RotationRequest;
import ci.transit.system.transport.server.impl.service.AttendanceService;
import ci.transit.system.transport.server.impl.service.RotationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/rotations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RotationResource {

    @Inject
    RotationService rotationService;

    @Inject
    AttendanceService attendanceService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RotationDto> getAllRotations(@QueryParam("status") String status,
                                              @QueryParam("driverId") UUID driverId,
                                              @QueryParam("vehicleId") UUID vehicleId) {
        return rotationService.findAll(status, driverId, vehicleId);
    }

    @GET
    @Path("/active")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RotationDto> getActiveRotations() {
        return rotationService.findActive();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public RotationDto getRotation(@PathParam("id") UUID identifier) {
        return rotationService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createRotation(@Valid RotationRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(rotationService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public RotationDto updateRotation(@PathParam("id") UUID identifier, @Valid RotationRequest request) {
        return rotationService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public RotationDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return rotationService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteRotation(@PathParam("id") UUID identifier) {
        rotationService.delete(identifier);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/attendances")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<AttendanceDto> getRotationAttendances(@PathParam("id") UUID identifier) {
        return attendanceService.findByRotation(identifier);
    }
}
