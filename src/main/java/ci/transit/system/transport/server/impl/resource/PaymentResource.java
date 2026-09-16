package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.PaymentDto;
import ci.transit.system.transport.server.impl.service.PaymentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @GET
    public List<PaymentDto> getAllPayments(@QueryParam("subscriptionId") UUID subscriptionIdentifier) {
        return paymentService.findAll(subscriptionIdentifier);
    }

    @GET
    @Path("/{id}")
    public PaymentDto getPayment(@PathParam("id") UUID identifier) {
        return paymentService.findById(identifier);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deletePayment(@PathParam("id") UUID identifier) {
        paymentService.delete(identifier);
        return Response.noContent().build();
    }
}
