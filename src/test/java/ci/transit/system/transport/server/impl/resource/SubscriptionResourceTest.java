package ci.transit.system.transport.server.impl.resource;

import java.util.Map;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SubscriptionResourceTest {

    private String activeRotationId() {
        return given().when().get("/rotations/active")
            .then().extract().path("[0].identifier");
    }

    /** Cree un usager frais (sans abonnement) pour isoler chaque test des seeds partages. */
    private String createFreshPassenger() {
        String phone = "07" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8);
        var body = Map.of("fullName", "Test Passenger", "phone", phone);
        return given().contentType("application/json").body(body)
            .when().post("/passengers")
            .then().statusCode(201).extract().path("identifier");
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void subscribingCreatesActiveSubscriptionAndPayment() {
        String passengerId = createFreshPassenger();

        var body = Map.of("passengerId", passengerId, "plan", "MENSUEL",
            "amount", 15000, "method", "ESPECES");

        given().contentType("application/json").body(body)
            .when().post("/subscriptions")
            .then().statusCode(201)
            .body("subscription.status", equalTo("ACTIVE"))
            .body("subscription.plan", equalTo("MENSUEL"))
            .body("payments.size()", equalTo(1))
            .body("payments[0].amount", comparesEqualTo(15000f));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void renewingExtendsEndsOn() {
        String passengerId = createFreshPassenger();

        var subscribeBody = Map.of("passengerId", passengerId, "plan", "HEBDOMADAIRE",
            "amount", 5000, "method", "ESPECES");

        var created = given().contentType("application/json").body(subscribeBody)
            .when().post("/subscriptions")
            .then().statusCode(201).extract();

        String subscriptionId = created.path("subscription.identifier");
        String firstEndsOn = created.path("subscription.endsOn");

        var renewBody = Map.of("amount", 5000, "method", "MOBILE_MONEY");

        given().contentType("application/json").body(renewBody)
            .when().post("/subscriptions/" + subscriptionId + "/renew")
            .then().statusCode(200)
            .body("subscription.endsOn", not(equalTo(firstEndsOn)))
            .body("payments.size()", equalTo(2));
    }

    @Test
    @TestSecurity(user = "controller", roles = {"CONTROLLER", "OWNER"})
    void boardingFailsWithoutSubscriptionThenSucceedsAfterSubscribing() {
        String rotationId = activeRotationId();
        String passengerId = createFreshPassenger();
        String phone = given().when().get("/passengers/" + passengerId)
            .then().extract().path("phone");

        String code = given().contentType("application/json").body(Map.of("rotationId", rotationId))
            .when().post("/access-codes/generate")
            .then().statusCode(201).extract().path("code");

        given().contentType("application/json")
            .body(Map.of("rotationId", rotationId, "code", code, "phone", phone))
            .when().post("/boarding/verify")
            .then().statusCode(200)
            .body("success", equalTo(false))
            .body("message", equalTo("Abonnement expire ou inexistant"));

        given().contentType("application/json")
            .body(Map.of("passengerId", passengerId, "plan", "MENSUEL", "amount", 15000, "method", "ESPECES"))
            .when().post("/subscriptions")
            .then().statusCode(201);

        given().contentType("application/json")
            .body(Map.of("rotationId", rotationId, "code", code, "phone", phone))
            .when().post("/boarding/verify")
            .then().statusCode(200)
            .body("success", equalTo(true));
    }
}
