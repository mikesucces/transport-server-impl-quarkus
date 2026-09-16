package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.PassengerDto;
import ci.transit.system.transport.server.impl.dto.PassengerRequest;
import ci.transit.system.transport.server.impl.dto.PaymentAccountDto;
import ci.transit.system.transport.server.impl.dto.RentalDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionDto;
import ci.transit.system.transport.server.impl.service.PassengerService;
import ci.transit.system.transport.server.impl.service.PaymentAccountService;
import ci.transit.system.transport.server.impl.service.RentalService;
import ci.transit.system.transport.server.impl.service.SubscriptionService;
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

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    PaymentAccountService paymentAccountService;

    @Inject
    RentalService rentalService;

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

    @GET
    @Path("/{id}/subscriptions")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public List<SubscriptionDto> getPassengerSubscriptions(@PathParam("id") UUID identifier) {
        return subscriptionService.findByPassenger(identifier);
    }

    @GET
    @Path("/{id}/payment-accounts")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public List<PaymentAccountDto> getPassengerPaymentAccounts(@PathParam("id") UUID identifier) {
        return paymentAccountService.findByPassenger(identifier);
    }

    @GET
    @Path("/{id}/rentals")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RentalDto> getPassengerRentals(@PathParam("id") UUID identifier) {
        return rentalService.findByPassenger(identifier);
    }
}
