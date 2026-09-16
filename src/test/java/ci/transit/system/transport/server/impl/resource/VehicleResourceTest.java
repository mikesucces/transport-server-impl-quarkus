package ci.transit.system.transport.server.impl.resource;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class VehicleResourceTest {

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void getAllVehiclesReturnsSeededFleet() {
        given().when().get("/vehicles")
            .then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(3))
            .body("plateNumber", hasItem("4521 CI 01"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void createVehicleThenRejectDuplicatePlate() {
        var body = Map.of("plateNumber", "9001 CI 09", "brand", "Toyota",
                          "model", "Hiace", "capacity", 18);

        given().contentType("application/json").body(body)
            .when().post("/vehicles")
            .then().statusCode(201);

        given().contentType("application/json").body(body)
            .when().post("/vehicles")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void mileageCannotDecrease() {
        String identifier = given().when().get("/vehicles")
            .then().extract().path("find { it.plateNumber == '4521 CI 01' }.identifier");

        given().contentType("application/json").body(Map.of("mileageKm", 1000))
            .when().patch("/vehicles/" + identifier + "/mileage")
            .then().statusCode(400);

        given().contentType("application/json").body(Map.of("mileageKm", 150000))
            .when().patch("/vehicles/" + identifier + "/mileage")
            .then().statusCode(200).body("mileageKm", equalTo(150000));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void payrollSumsFixedSalaries() {
        given().when().get("/drivers/payroll")
            .then().statusCode(200)
            .body("monthlyPayroll", comparesEqualTo(580000f));
    }

    @Test
    @TestSecurity(user = "ctrl", roles = "CONTROLLER")
    void controllerCannotReadPayroll() {
        given().when().get("/drivers/payroll").then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void fleetAlertsAggregateEverything() {
        given().when().get("/fleet/alerts")
            .then().statusCode(200)
            .body("vehiclesTotal", greaterThanOrEqualTo(3))
            .body("expiringDocuments.size()", greaterThanOrEqualTo(1));
    }
}
