package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.LoginAttemptDto;
import ci.transit.system.transport.server.impl.dto.LoginAttemptRequest;
import ci.transit.system.transport.server.impl.service.LoginAttemptService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/login-attempts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class LoginAttemptResource {

    @Inject
    LoginAttemptService loginAttemptService;

    @GET
    public List<LoginAttemptDto> getAllLoginAttempts(@QueryParam("passengerId") UUID passengerIdentifier) {
        return loginAttemptService.findAll(passengerIdentifier);
    }

    @GET
    @Path("/{id}")
    public LoginAttemptDto getLoginAttempt(@PathParam("id") Long identifier) {
        return loginAttemptService.findById(identifier);
    }

    @POST
    public Response createLoginAttempt(@Valid LoginAttemptRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(loginAttemptService.create(request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteLoginAttempt(@PathParam("id") Long identifier) {
        loginAttemptService.delete(identifier);
        return Response.noContent().build();
    }
}
