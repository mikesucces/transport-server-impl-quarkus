package ci.transit.system.transport.server.impl.resource;

import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.PaymentAccountDto;
import ci.transit.system.transport.server.impl.dto.PaymentAccountRequest;
import ci.transit.system.transport.server.impl.service.PaymentAccountService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payment-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentAccountResource {

    @Inject
    PaymentAccountService paymentAccountService;

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public PaymentAccountDto getPaymentAccount(@PathParam("id") UUID identifier) {
        return paymentAccountService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public Response createPaymentAccount(@Valid PaymentAccountRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(paymentAccountService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public PaymentAccountDto updatePaymentAccount(@PathParam("id") UUID identifier,
                                                   @Valid PaymentAccountRequest request) {
        return paymentAccountService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/active")
    @RolesAllowed({"OWNER", "MANAGER"})
    public PaymentAccountDto updateActive(@PathParam("id") UUID identifier, Map<String, String> body) {
        return paymentAccountService.updateActive(identifier, Boolean.parseBoolean(body.get("active")));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deletePaymentAccount(@PathParam("id") UUID identifier) {
        paymentAccountService.delete(identifier);
        return Response.noContent().build();
    }
}
