package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.DocumentDto;
import ci.transit.system.transport.server.impl.dto.DocumentRequest;
import ci.transit.system.transport.server.impl.dto.MaintenanceDto;
import ci.transit.system.transport.server.impl.dto.MaintenanceRequest;
import ci.transit.system.transport.server.impl.dto.VehicleDetailDto;
import ci.transit.system.transport.server.impl.dto.VehicleDto;
import ci.transit.system.transport.server.impl.dto.VehicleRequest;
import ci.transit.system.transport.server.impl.service.DocumentService;
import ci.transit.system.transport.server.impl.service.MaintenanceService;
import ci.transit.system.transport.server.impl.service.VehicleService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/vehicles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleResource {

    @Inject
    VehicleService vehicleService;

    @Inject
    DocumentService documentService;

    @Inject
    MaintenanceService maintenanceService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER", "DRIVER"})
    public List<VehicleDto> getAllVehicles(@QueryParam("status") String status) {
        return vehicleService.findAll(status);
    }

    @GET
    @Path("/available")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<VehicleDto> getAvailableVehicles() {
        return vehicleService.findAvailable();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER", "DRIVER"})
    public VehicleDetailDto getVehicle(@PathParam("id") UUID identifier) {
        return vehicleService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createVehicle(@Valid VehicleRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(vehicleService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public VehicleDto updateVehicle(@PathParam("id") UUID identifier, @Valid VehicleRequest request) {
        return vehicleService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public VehicleDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return vehicleService.updateStatus(identifier, body.get("status"));
    }

    @PATCH
    @Path("/{id}/mileage")
    @RolesAllowed({"OWNER", "MANAGER", "DRIVER"})
    public VehicleDto updateMileage(@PathParam("id") UUID identifier, Map<String, Integer> body) {
        return vehicleService.updateMileage(identifier, body.get("mileageKm"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteVehicle(@PathParam("id") UUID identifier) {
        vehicleService.delete(identifier);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/documents")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<DocumentDto> getDocuments(@PathParam("id") UUID identifier) {
        return documentService.findByVehicle(identifier);
    }

    @POST
    @Path("/{id}/documents")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createDocument(@PathParam("id") UUID identifier, @Valid DocumentRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(documentService.create(identifier, request)).build();
    }

    @GET
    @Path("/{id}/maintenances")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<MaintenanceDto> getMaintenances(@PathParam("id") UUID identifier) {
        return maintenanceService.findByVehicle(identifier);
    }

    @POST
    @Path("/{id}/maintenances")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createMaintenance(@PathParam("id") UUID identifier,
                                      @Valid MaintenanceRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(maintenanceService.create(identifier, request)).build();
    }
}
