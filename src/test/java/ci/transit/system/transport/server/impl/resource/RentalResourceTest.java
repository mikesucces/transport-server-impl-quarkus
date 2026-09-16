package ci.transit.system.transport.server.impl.resource;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RentalResourceTest {

    private String createFreshPassenger() {
        String phone = "07" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8);
        var body = Map.of("fullName", "Test Renter", "phone", phone);
        return given().contentType("application/json").body(body)
            .when().post("/passengers")
            .then().statusCode(201).extract().path("identifier");
    }

    /** Vehicule frais et forcement DISPONIBLE, independant du pool partage par les autres tests. */
    private String createFreshVehicle() {
        String plate = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8) + " CI 99";
        var body = Map.of("plateNumber", plate, "brand", "Toyota", "model", "Hiace", "capacity", 18);
        return given().contentType("application/json").body(body)
            .when().post("/vehicles")
            .then().statusCode(201).extract().path("identifier");
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void startingRentalPutsVehicleEnServiceThenReleasesItOnCompletion() {
        String passengerId = createFreshPassenger();
        String vehicleId = createFreshVehicle();

        var body = Map.of(
            "passengerId", passengerId,
            "vehicleId", vehicleId,
            "status", "EN_COURS",
            "startDate", LocalDate.now().toString(),
            "endDate", LocalDate.now().plusDays(3).toString(),
            "totalAmount", 45000);

        String rentalId = given().contentType("application/json").body(body)
            .when().post("/rentals")
            .then().statusCode(201)
            .body("rental.status", equalTo("EN_COURS"))
            .extract().path("rental.identifier");

        given().when().get("/vehicles/" + vehicleId)
            .then().statusCode(200).body("vehicle.status", equalTo("EN_SERVICE"));

        given().contentType("application/json").body(Map.of("amount", 20000, "method", "ESPECES"))
            .when().post("/rentals/" + rentalId + "/payments")
            .then().statusCode(201).body("amount", equalTo(20000));

        given().contentType("application/json").body(Map.of("status", "TERMINEE"))
            .when().patch("/rentals/" + rentalId + "/status")
            .then().statusCode(200).body("status", equalTo("TERMINEE"));

        given().when().get("/vehicles/" + vehicleId)
            .then().statusCode(200).body("vehicle.status", equalTo("DISPONIBLE"));

        given().when().get("/rentals/" + rentalId)
            .then().statusCode(200).body("payments.size()", equalTo(1));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void cannotStartRentalOnUnavailableVehicle() {
        String passengerId = createFreshPassenger();
        String busyVehicleId = given().when().get("/vehicles")
            .then().extract().path("find { it.plateNumber == '4521 CI 01' }.identifier");

        var body = Map.of(
            "passengerId", passengerId,
            "vehicleId", busyVehicleId,
            "status", "EN_COURS",
            "startDate", LocalDate.now().toString(),
            "endDate", LocalDate.now().plusDays(2).toString(),
            "totalAmount", 30000);

        given().contentType("application/json").body(body)
            .when().post("/rentals")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void deletingActiveRentalIsRejected() {
        String passengerId = createFreshPassenger();
        String vehicleId = createFreshVehicle();

        var body = Map.of(
            "passengerId", passengerId,
            "vehicleId", vehicleId,
            "status", "EN_COURS",
            "startDate", LocalDate.now().toString(),
            "endDate", LocalDate.now().plusDays(1).toString(),
            "totalAmount", 15000);

        String rentalId = given().contentType("application/json").body(body)
            .when().post("/rentals")
            .then().statusCode(201).extract().path("rental.identifier");

        given().when().delete("/rentals/" + rentalId)
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "driver", roles = "DRIVER")
    void driverCannotAccessRentals() {
        given().when().get("/rentals").then().statusCode(403);
    }
}
