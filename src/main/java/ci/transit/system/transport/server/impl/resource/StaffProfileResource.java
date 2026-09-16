package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.StaffProfileDto;
import ci.transit.system.transport.server.impl.dto.StaffProfileRequest;
import ci.transit.system.transport.server.impl.service.StaffProfileService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/staff-profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffProfileResource {

    @Inject
    StaffProfileService staffProfileService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<StaffProfileDto> getAllStaffProfiles(@QueryParam("staffType") String staffType) {
        return staffProfileService.findAll(staffType);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public StaffProfileDto getStaffProfile(@PathParam("id") UUID identifier) {
        return staffProfileService.findById(identifier);
    }

    @POST
    @RolesAllowed("OWNER")
    public Response createStaffProfile(@Valid StaffProfileRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(staffProfileService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public StaffProfileDto updateStaffProfile(@PathParam("id") UUID identifier, @Valid StaffProfileRequest request) {
        return staffProfileService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/active")
    @RolesAllowed("OWNER")
    public StaffProfileDto updateActive(@PathParam("id") UUID identifier, Map<String, String> body) {
        return staffProfileService.updateActive(identifier, Boolean.parseBoolean(body.get("active")));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteStaffProfile(@PathParam("id") UUID identifier) {
        staffProfileService.delete(identifier);
        return Response.noContent().build();
    }
}
