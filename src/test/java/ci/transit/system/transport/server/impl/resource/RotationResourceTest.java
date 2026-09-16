package ci.transit.system.transport.server.impl.resource;

import java.time.Instant;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RotationResourceTest {

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void getAllRotationsReturnsSeededRotations() {
        given().when().get("/rotations")
            .then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(3))
            .body("status", hasItem("EN_COURS"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void cannotStartRotationForDriverAlreadyEnRotation() {
        String busyDriverId = given().when().get("/drivers")
            .then().extract().path("find { it.phone == '0708110001' }.identifier");
        String freeVehicleId = given().when().get("/vehicles/available")
            .then().extract().path("[0].identifier");

        var body = Map.of(
            "driverId", busyDriverId,
            "vehicleId", freeVehicleId,
            "status", "EN_COURS",
            "scheduledStart", Instant.now().toString());

        given().contentType("application/json").body(body)
            .when().post("/rotations")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void startingRotationPutsDriverAndVehicleEnService() {
        String driverId = given().when().get("/drivers")
            .then().extract().path("find { it.phone == '0544110002' }.identifier");
        String vehicleId = given().when().get("/vehicles")
            .then().extract().path("find { it.plateNumber == '7812 CI 02' }.identifier");

        var body = Map.of(
            "driverId", driverId,
            "vehicleId", vehicleId,
            "status", "EN_COURS",
            "scheduledStart", Instant.now().toString());

        String rotationId = given().contentType("application/json").body(body)
            .when().post("/rotations")
            .then().statusCode(201).body("status", equalTo("EN_COURS"))
            .extract().path("identifier");

        given().when().get("/drivers/" + driverId)
            .then().statusCode(200).body("status", equalTo("EN_ROTATION"));
        given().when().get("/vehicles/" + vehicleId)
            .then().statusCode(200).body("vehicle.status", equalTo("EN_SERVICE"));

        given().contentType("application/json").body(Map.of("status", "TERMINEE"))
            .when().patch("/rotations/" + rotationId + "/status")
            .then().statusCode(200).body("status", equalTo("TERMINEE"));

        given().when().get("/drivers/" + driverId)
            .then().statusCode(200).body("status", equalTo("DISPONIBLE"));
        given().when().get("/vehicles/" + vehicleId)
            .then().statusCode(200).body("vehicle.status", equalTo("DISPONIBLE"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void deletingActiveRotationIsRejected() {
        String activeRotationId = given().when().get("/rotations/active")
            .then().extract().path("[0].identifier");

        given().when().delete("/rotations/" + activeRotationId)
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }
}
