package ci.transit.system.transport.server.impl.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class StaffProfileResourceTest {

    private Map<String, Object> newProfileBody() {
        UUID sub = UUID.randomUUID();
        return Map.of(
            "keycloakSub", sub.toString(),
            "username", "user." + sub.toString().substring(0, 8),
            "fullName", "Test Staff",
            "staffType", "MANAGER");
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void ownerCanCreateStaffProfile() {
        given().contentType("application/json").body(newProfileBody())
            .when().post("/staff-profiles")
            .then().statusCode(201)
            .body("staffType", equalTo("MANAGER"))
            .body("active", equalTo(true));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void duplicateKeycloakSubIsRejected() {
        var body = newProfileBody();

        given().contentType("application/json").body(body)
            .when().post("/staff-profiles")
            .then().statusCode(201);

        given().contentType("application/json").body(body)
            .when().post("/staff-profiles")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }

    @Test
    @TestSecurity(user = "manager", roles = "MANAGER")
    void managerCanReadButNotCreate() {
        given().when().get("/staff-profiles")
            .then().statusCode(200);

        given().contentType("application/json").body(newProfileBody())
            .when().post("/staff-profiles")
            .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void deletingProfileLinkedToDriverIsRejected() {
        String profileId = given().contentType("application/json").body(newProfileBody())
            .when().post("/staff-profiles")
            .then().statusCode(201).extract().path("identifier");

        String driverId = given().when().get("/drivers")
            .then().extract().path("[0].identifier");
        var driverBody = given().when().get("/drivers/" + driverId)
            .then().extract().jsonPath();

        // Reprend l'integralite du chauffeur existant pour ne pas violer les regles
        // de coherence remuneration/montant lors du PUT, en ajoutant juste staffProfileId.
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("fullName", driverBody.getString("fullName"));
        updateBody.put("phone", driverBody.getString("phone"));
        updateBody.put("matricule", driverBody.get("matricule"));
        updateBody.put("staffProfileId", profileId);
        updateBody.put("licenseNumber", driverBody.getString("licenseNumber"));
        updateBody.put("licenseCategory", driverBody.getString("licenseCategory"));
        updateBody.put("licenseExpiresOn", driverBody.getString("licenseExpiresOn"));
        updateBody.put("remunerationType", driverBody.getString("remunerationType"));
        updateBody.put("monthlySalary", driverBody.get("monthlySalary"));
        updateBody.put("tripRate", driverBody.get("tripRate"));
        updateBody.put("commissionRate", driverBody.get("commissionRate"));
        updateBody.put("status", driverBody.getString("status"));
        updateBody.put("hiredOn", driverBody.get("hiredOn"));
        updateBody.values().removeIf(v -> v == null);

        given().contentType("application/json").body(updateBody)
            .when().put("/drivers/" + driverId)
            .then().statusCode(200)
            .body("hasAccount", equalTo(true));

        given().when().delete("/staff-profiles/" + profileId)
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }
}
