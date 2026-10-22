package ci.transit.admin.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.AccessToken;

import ci.transit.admin.dto.DashboardDto;
import ci.transit.admin.dto.DocumentDto;
import ci.transit.admin.dto.DocumentRequest;
import ci.transit.admin.dto.DriverDto;
import ci.transit.admin.dto.MaintenanceDto;
import ci.transit.admin.dto.MaintenanceRequest;
import ci.transit.admin.dto.RentalDto;
import ci.transit.admin.dto.RentalRequest;
import ci.transit.admin.dto.RouteScheduleDto;
import ci.transit.admin.dto.RouteScheduleRequest;
import ci.transit.admin.dto.DriverRequest;
import ci.transit.admin.dto.PassengerDto;
import ci.transit.admin.dto.PassengerRequest;
import ci.transit.admin.dto.RotationDto;
import ci.transit.admin.dto.RotationRequest;
import ci.transit.admin.dto.RouteDto;
import ci.transit.admin.dto.RouteRequest;
import ci.transit.admin.dto.SubscriptionDto;
import ci.transit.admin.dto.SubscriptionRequest;
import ci.transit.admin.dto.VehicleDto;
import ci.transit.admin.dto.VehicleRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Client REST de l'API transport-server-impl-quarkus.
 */
@RegisterRestClient(configKey = "transport-api")
@AccessToken
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TransportApi {

  @GET
  @Path("/dashboard")
  DashboardDto dashboard(@QueryParam("days") int days);

  // ---------- Cars ----------
  @GET
  @Path("/vehicles")
  List<VehicleDto> vehicles(@QueryParam("status") String status);

  @POST
  @Path("/vehicles")
  VehicleDto createVehicle(VehicleRequest request);

  @PUT
  @Path("/vehicles/{id}")
  VehicleDto updateVehicle(@PathParam("id") UUID id, VehicleRequest request);

  @PATCH
  @Path("/vehicles/{id}/status")
  VehicleDto updateVehicleStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/vehicles/{id}")
  void deleteVehicle(@PathParam("id") UUID id);

  // ---------- Chauffeurs ----------
  @GET
  @Path("/drivers")
  List<DriverDto> drivers(@QueryParam("status") String status);

  @POST
  @Path("/drivers")
  DriverDto createDriver(DriverRequest request);

  @PUT
  @Path("/drivers/{id}")
  DriverDto updateDriver(@PathParam("id") UUID id, DriverRequest request);

  @PATCH
  @Path("/drivers/{id}/status")
  DriverDto updateDriverStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/drivers/{id}")
  void deleteDriver(@PathParam("id") UUID id);

  // ---------- Lignes ----------
  @GET
  @Path("/routes")
  List<RouteDto> routes(@QueryParam("status") String status);

  @POST
  @Path("/routes")
  RouteDto createRoute(RouteRequest request);

  @PUT
  @Path("/routes/{id}")
  RouteDto updateRoute(@PathParam("id") UUID id, RouteRequest request);

  @PATCH
  @Path("/routes/{id}/status")
  RouteDto updateRouteStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/routes/{id}")
  void deleteRoute(@PathParam("id") UUID id);

  // ---------- Usagers ----------
  @GET
  @Path("/passengers")
  List<PassengerDto> passengers(@QueryParam("status") String status);

  @POST
  @Path("/passengers")
  PassengerDto createPassenger(PassengerRequest request);

  @PUT
  @Path("/passengers/{id}")
  PassengerDto updatePassenger(@PathParam("id") UUID id, PassengerRequest request);

  @PATCH
  @Path("/passengers/{id}/status")
  PassengerDto updatePassengerStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/passengers/{id}")
  void deletePassenger(@PathParam("id") UUID id);

  // ---------- Abonnements ----------
  @GET
  @Path("/subscriptions")
  List<SubscriptionDto> subscriptions(@QueryParam("passengerId") UUID passengerId,
                                      @QueryParam("status") String status);

  @POST
  @Path("/subscriptions")
  SubscriptionDto createSubscription(SubscriptionRequest request);

  @PATCH
  @Path("/subscriptions/{id}/status")
  SubscriptionDto updateSubscriptionStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/subscriptions/{id}")
  void deleteSubscription(@PathParam("id") UUID id);

  // ---------- Documents et entretiens des cars ----------
  @GET
  @Path("/vehicles/{id}/documents")
  List<DocumentDto> vehicleDocuments(@PathParam("id") UUID vehicleId);

  @POST
  @Path("/vehicles/{id}/documents")
  DocumentDto createDocument(@PathParam("id") UUID vehicleId, DocumentRequest request);

  @DELETE
  @Path("/vehicle-documents/{id}")
  void deleteDocument(@PathParam("id") UUID id);

  @GET
  @Path("/vehicles/{id}/maintenances")
  List<MaintenanceDto> vehicleMaintenances(@PathParam("id") UUID vehicleId);

  @POST
  @Path("/vehicles/{id}/maintenances")
  MaintenanceDto createMaintenance(@PathParam("id") UUID vehicleId, MaintenanceRequest request);

  @DELETE
  @Path("/maintenances/{id}")
  void deleteMaintenance(@PathParam("id") UUID id);

  // ---------- Horaires de ligne ----------
  @GET
  @Path("/routes/{id}/schedules")
  List<RouteScheduleDto> routeSchedules(@PathParam("id") UUID routeId);

  @POST
  @Path("/routes/{id}/schedules")
  RouteScheduleDto createSchedule(@PathParam("id") UUID routeId, RouteScheduleRequest request);

  @DELETE
  @Path("/route-schedules/{id}")
  void deleteSchedule(@PathParam("id") UUID id);

  // ---------- Locations ----------
  @GET
  @Path("/rentals")
  List<RentalDto> rentals(@QueryParam("status") String status,
                          @QueryParam("passengerId") UUID passengerId,
                          @QueryParam("vehicleId") UUID vehicleId);

  @POST
  @Path("/rentals")
  RentalDto createRental(RentalRequest request);

  @PATCH
  @Path("/rentals/{id}/status")
  RentalDto updateRentalStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/rentals/{id}")
  void deleteRental(@PathParam("id") UUID id);

  // ---------- Rotations ----------
  @GET
  @Path("/rotations")
  List<RotationDto> rotations(@QueryParam("status") String status,
                              @QueryParam("driverId") UUID driverId,
                              @QueryParam("vehicleId") UUID vehicleId);

  @POST
  @Path("/rotations")
  RotationDto createRotation(RotationRequest request);

  @PATCH
  @Path("/rotations/{id}/status")
  RotationDto updateRotationStatus(@PathParam("id") UUID id, Map<String, String> body);

  @DELETE
  @Path("/rotations/{id}")
  void deleteRotation(@PathParam("id") UUID id);
}
