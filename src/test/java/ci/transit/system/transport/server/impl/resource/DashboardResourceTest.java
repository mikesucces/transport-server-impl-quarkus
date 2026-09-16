package ci.transit.system.transport.server.impl.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class DashboardResourceTest {

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void dashboardAggregatesFleetSubscriptionsAndRevenue() {
        given().when().get("/dashboard")
            .then().statusCode(200)
            .body("fleetAlerts.vehiclesTotal", greaterThanOrEqualTo(3))
            .body("activeSubscriptionsCount", greaterThanOrEqualTo(1))
            .body("monthlyRevenue", greaterThanOrEqualTo(15000f));
    }

    @Test
    @TestSecurity(user = "owner", roles = "OWNER")
    void expiringSubscriptionsIncludesSeededSubscription() {
        given().when().get("/dashboard?days=30")
            .then().statusCode(200)
            .body("expiringSubscriptions.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(user = "driver", roles = "DRIVER")
    void driverCannotReadDashboard() {
        given().when().get("/dashboard").then().statusCode(403);
    }
}
