package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.RentalDetailDto;
import ci.transit.system.transport.server.impl.dto.RentalDto;
import ci.transit.system.transport.server.impl.dto.RentalPaymentDto;
import ci.transit.system.transport.server.impl.dto.RentalPaymentRequest;
import ci.transit.system.transport.server.impl.dto.RentalRequest;
import ci.transit.system.transport.server.impl.service.RentalPaymentService;
import ci.transit.system.transport.server.impl.service.RentalService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/rentals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class RentalResource {

    @Inject
    RentalService rentalService;

    @Inject
    RentalPaymentService rentalPaymentService;

    @GET
    public List<RentalDto> getAllRentals(@QueryParam("status") String status,
                                          @QueryParam("passengerId") UUID passengerId,
                                          @QueryParam("vehicleId") UUID vehicleId) {
        return rentalService.findAll(status, passengerId, vehicleId);
    }

    @GET
    @Path("/{id}")
    public RentalDetailDto getRental(@PathParam("id") UUID identifier) {
        return rentalService.findById(identifier);
    }

    @POST
    public Response createRental(@Valid RentalRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(rentalService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    public RentalDetailDto updateRental(@PathParam("id") UUID identifier, @Valid RentalRequest request) {
        return rentalService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    public RentalDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return rentalService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteRental(@PathParam("id") UUID identifier) {
        rentalService.delete(identifier);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/payments")
    public List<RentalPaymentDto> getPayments(@PathParam("id") UUID identifier) {
        return rentalPaymentService.findByRental(identifier);
    }

    @POST
    @Path("/{id}/payments")
    public Response createPayment(@PathParam("id") UUID identifier, @Valid RentalPaymentRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(rentalPaymentService.create(identifier, request)).build();
    }
}
