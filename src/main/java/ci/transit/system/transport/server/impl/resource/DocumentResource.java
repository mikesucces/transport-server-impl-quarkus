package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.DocumentDto;
import ci.transit.system.transport.server.impl.dto.DocumentRequest;
import ci.transit.system.transport.server.impl.service.DocumentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/vehicle-documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class DocumentResource {

    @Inject
    DocumentService documentService;

    /** Documents expires ou arrivant a echeance. */
    @GET
    @Path("/expiring")
    public List<DocumentDto> getExpiringDocuments(@QueryParam("days") @DefaultValue("30") int days) {
        return documentService.findExpiring(days);
    }

    @PUT
    @Path("/{id}")
    public DocumentDto updateDocument(@PathParam("id") UUID identifier,
                                      @Valid DocumentRequest request) {
        return documentService.update(identifier, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteDocument(@PathParam("id") UUID identifier) {
        documentService.delete(identifier);
        return Response.noContent().build();
    }
}
