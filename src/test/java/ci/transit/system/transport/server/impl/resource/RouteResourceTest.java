package ci.transit.system.transport.server.impl.resource;

import java.time.Instant;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RouteResourceTest {

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void getAllRoutesReturnsSeededRoutes() {
        given().when().get("/routes")
            .then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(2))
            .body("code", hasItem("L01"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void routeDetailIncludesSeededSchedules() {
        String routeId = given().when().get("/routes")
            .then().extract().path("find { it.code == 'L01' }.identifier");

        given().when().get("/routes/" + routeId)
            .then().statusCode(200)
            .body("route.code", equalTo("L01"))
            .body("schedules.size()", greaterThanOrEqualTo(3));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void creatingRouteWithDuplicateCodeIsRejected() {
        var body = Map.of("code", "L01", "name", "Doublon",
            "origin", "A", "destination", "B");

        given().contentType("application/json").body(body)
            .when().post("/routes")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void attachingRotationToRouteShowsUpInRouteHistory() {
        String routeId = given().when().get("/routes")
            .then().extract().path("find { it.code == 'L02' }.identifier");
        String driverId = given().when().get("/drivers/available")
            .then().extract().path("[0].identifier");
        String vehicleId = given().when().get("/vehicles/available")
            .then().extract().path("[0].identifier");

        var body = Map.of(
            "driverId", driverId,
            "vehicleId", vehicleId,
            "routeId", routeId,
            "scheduledStart", Instant.now().toString());

        given().contentType("application/json").body(body)
            .when().post("/rotations")
            .then().statusCode(201)
            .body("routeIdentifier", equalTo(routeId))
            .body("routeCode", equalTo("L02"));

        given().when().get("/routes/" + routeId + "/rotations")
            .then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void deletingRouteUsedBySeededRotationIsRejected() {
        String routeId = given().when().get("/routes")
            .then().extract().path("find { it.code == 'L01' }.identifier");

        given().when().delete("/routes/" + routeId)
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }
}
