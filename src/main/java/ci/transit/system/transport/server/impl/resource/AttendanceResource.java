package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.AttendanceDto;
import ci.transit.system.transport.server.impl.service.AttendanceService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/attendances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AttendanceResource {

    @Inject
    AttendanceService attendanceService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<AttendanceDto> getAllAttendances(@QueryParam("rotationId") UUID rotationIdentifier) {
        return attendanceService.findAll(rotationIdentifier);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public AttendanceDto getAttendance(@PathParam("id") UUID identifier) {
        return attendanceService.findById(identifier);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteAttendance(@PathParam("id") UUID identifier) {
        attendanceService.delete(identifier);
        return Response.noContent().build();
    }
}
