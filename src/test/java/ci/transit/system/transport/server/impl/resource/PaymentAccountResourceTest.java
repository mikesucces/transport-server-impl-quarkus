package ci.transit.system.transport.server.impl.resource;

import java.util.Map;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PaymentAccountResourceTest {

    private String createFreshPassenger() {
        String phone = "07" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8);
        var body = Map.of("fullName", "Test Passenger", "phone", phone);
        return given().contentType("application/json").body(body)
            .when().post("/passengers")
            .then().statusCode(201).extract().path("identifier");
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void ownerCanCreatePaymentAccount() {
        String passengerId = createFreshPassenger();
        var body = Map.of("passengerId", passengerId, "method", "MOBILE_MONEY",
            "reference", "0700000000", "defaultAccount", true);

        given().contentType("application/json").body(body)
            .when().post("/payment-accounts")
            .then().statusCode(201)
            .body("defaultAccount", equalTo(true))
            .body("active", equalTo(true));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void settingNewDefaultClearsThePreviousOne() {
        String passengerId = createFreshPassenger();

        String firstId = given().contentType("application/json")
            .body(Map.of("passengerId", passengerId, "method", "ESPECES",
                "reference", "compte-1", "defaultAccount", true))
            .when().post("/payment-accounts")
            .then().statusCode(201).extract().path("identifier");

        given().contentType("application/json")
            .body(Map.of("passengerId", passengerId, "method", "MOBILE_MONEY",
                "reference", "compte-2", "defaultAccount", true))
            .when().post("/payment-accounts")
            .then().statusCode(201).body("defaultAccount", equalTo(true));

        given().when().get("/passengers/" + passengerId + "/payment-accounts")
            .then().statusCode(200)
            .body("find { it.identifier == '" + firstId + "' }.defaultAccount", equalTo(false));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void subscribingWithAnotherPassengersAccountIsRejected() {
        String ownerPassengerId = createFreshPassenger();
        String otherPassengerId = createFreshPassenger();

        String accountId = given().contentType("application/json")
            .body(Map.of("passengerId", otherPassengerId, "method", "ESPECES", "reference", "compte-autre"))
            .when().post("/payment-accounts")
            .then().statusCode(201).extract().path("identifier");

        var body = Map.of(
            "passengerId", ownerPassengerId,
            "plan", "MENSUEL",
            "amount", 15000,
            "method", "ESPECES",
            "paymentAccountId", accountId);

        given().contentType("application/json").body(body)
            .when().post("/subscriptions")
            .then().statusCode(400).body("code", equalTo("BAD_REQUEST"));
    }
}
