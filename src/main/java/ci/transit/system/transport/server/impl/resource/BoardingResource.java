package ci.transit.system.transport.server.impl.resource;

import ci.transit.system.transport.server.impl.dto.BoardingResultDto;
import ci.transit.system.transport.server.impl.dto.BoardingVerifyRequest;
import ci.transit.system.transport.server.impl.service.BoardingService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/boarding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BoardingResource {

    @Inject
    BoardingService boardingService;

    @POST
    @Path("/verify")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER"})
    public BoardingResultDto verify(@Valid BoardingVerifyRequest request) {
        return boardingService.verify(request);
    }
}
