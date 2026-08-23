package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.PassengerDto;
import ci.transit.system.transport.server.impl.dto.PassengerRequest;
import ci.transit.system.transport.server.impl.service.PassengerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/passengers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PassengerResource {

    @Inject
    PassengerService passengerService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public List<PassengerDto> getAllPassengers(@QueryParam("status") String status) {
        return passengerService.findAll(status);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public PassengerDto getPassenger(@PathParam("id") UUID identifier) {
        return passengerService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createPassenger(@Valid PassengerRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(passengerService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public PassengerDto updatePassenger(@PathParam("id") UUID identifier, @Valid PassengerRequest request) {
        return passengerService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public PassengerDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return passengerService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deletePassenger(@PathParam("id") UUID identifier) {
        passengerService.delete(identifier);
        return Response.noContent().build();
    }
}
