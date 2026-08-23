package ci.transit.system.transport.server.impl.client;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.VehicleDetailDto;
import ci.transit.system.transport.server.impl.dto.VehicleDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Client REST permettant de consommer le referentiel des cars
 * depuis un autre module.
 *
 * @author Transit
 *
 */
@Path("/vehicles")
@RegisterRestClient(configKey = "transport-uri")
public interface VehicleClient {

    @GET
    List<VehicleDto> getAllVehicles(@QueryParam("status") String status);

    @GET
    @Path("/available")
    List<VehicleDto> getAvailableVehicles();

    @GET
    @Path("/{id}")
    VehicleDetailDto getVehicle(@PathParam("id") UUID identifier);
}
