package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.AccessCodeDto;
import ci.transit.system.transport.server.impl.dto.AccessCodeGenerateRequest;
import ci.transit.system.transport.server.impl.dto.AccessCodeRequest;
import ci.transit.system.transport.server.impl.service.AccessCodeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/access-codes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class AccessCodeResource {

    @Inject
    AccessCodeService accessCodeService;

    @GET
    public List<AccessCodeDto> getAllAccessCodes(@QueryParam("vehicleId") UUID vehicleIdentifier) {
        return accessCodeService.findAll(vehicleIdentifier);
    }

    @GET
    @Path("/{id}")
    public AccessCodeDto getAccessCode(@PathParam("id") UUID identifier) {
        return accessCodeService.findById(identifier);
    }

    @POST
    public Response createAccessCode(@Valid AccessCodeRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(accessCodeService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    public AccessCodeDto updateAccessCode(@PathParam("id") UUID identifier, @Valid AccessCodeRequest request) {
        return accessCodeService.update(identifier, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccessCode(@PathParam("id") UUID identifier) {
        accessCodeService.delete(identifier);
        return Response.noContent().build();
    }

    @POST
    @Path("/generate")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public Response generateAccessCode(@Valid AccessCodeGenerateRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(accessCodeService.generate(request)).build();
    }
}
