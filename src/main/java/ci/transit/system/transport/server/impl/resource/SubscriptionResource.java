package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.SubscriptionDetailDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionDto;
import ci.transit.system.transport.server.impl.dto.SubscriptionRenewRequest;
import ci.transit.system.transport.server.impl.dto.SubscriptionRequest;
import ci.transit.system.transport.server.impl.service.SubscriptionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionResource {

    @Inject
    SubscriptionService subscriptionService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public List<SubscriptionDto> getAllSubscriptions(@QueryParam("passengerId") UUID passengerId,
                                                       @QueryParam("status") String status) {
        return subscriptionService.findAll(passengerId, status);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public SubscriptionDetailDto getSubscription(@PathParam("id") UUID identifier) {
        return subscriptionService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public Response createSubscription(@Valid SubscriptionRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(subscriptionService.create(request)).build();
    }

    @POST
    @Path("/{id}/renew")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public SubscriptionDetailDto renewSubscription(@PathParam("id") UUID identifier,
                                                     @Valid SubscriptionRenewRequest request) {
        return subscriptionService.renew(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public SubscriptionDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return subscriptionService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteSubscription(@PathParam("id") UUID identifier) {
        subscriptionService.delete(identifier);
        return Response.noContent().build();
    }
}
