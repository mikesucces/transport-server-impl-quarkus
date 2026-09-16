package ci.transit.system.transport.server.impl.resource;

import java.util.Map;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BoardingResourceTest {

    private String activeRotationId() {
        return given().when().get("/rotations/active")
            .then().extract().path("[0].identifier");
    }

    @Test
    @TestSecurity(user = "controller", roles = {"CONTROLLER", "OWNER"})
    void controllerCanGenerateCodeForRotation() {
        String rotationId = activeRotationId();

        given().contentType("application/json").body(Map.of("rotationId", rotationId))
            .when().post("/access-codes/generate")
            .then().statusCode(201)
            .body("code", matchesPattern("\\d{4}"))
            .body("rotationIdentifier", equalTo(rotationId));
    }

    @Test
    @TestSecurity(user = "driver", roles = "DRIVER")
    void driverCannotVerifyBoarding() {
        var body = Map.of(
            "rotationId", UUID.randomUUID().toString(),
            "code", "0000",
            "phone", "0708123456");

        given().contentType("application/json").body(body)
            .when().post("/boarding/verify")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "controller", roles = {"CONTROLLER", "OWNER"})
    void verifyingWithCorrectCodeCreatesAttendance() {
        String rotationId = activeRotationId();

        String code = given().contentType("application/json").body(Map.of("rotationId", rotationId))
            .when().post("/access-codes/generate")
            .then().statusCode(201).extract().path("code");

        var body = Map.of("rotationId", rotationId, "code", code, "phone", "0708123456");

        given().contentType("application/json").body(body)
            .when().post("/boarding/verify")
            .then().statusCode(200)
            .body("success", equalTo(true))
            .body("attendance.rotationIdentifier", equalTo(rotationId));
    }

    @Test
    @TestSecurity(user = "controller", roles = {"CONTROLLER", "OWNER"})
    void verifyingWithWrongCodeFailsWithoutAttendance() {
        String rotationId = activeRotationId();

        var body = Map.of("rotationId", rotationId, "code", "0000", "phone", "0708123456");

        given().contentType("application/json").body(body)
            .when().post("/boarding/verify")
            .then().statusCode(200)
            .body("success", equalTo(false))
            .body("attendance", nullValue());
    }
}
