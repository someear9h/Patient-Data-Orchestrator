package sideeffects;

import org.junit.jupiter.api.Test;
import webrequests.BaseIntegrationTest;

import java.util.concurrent.TimeUnit;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

public class AuditSideEffectTest extends BaseIntegrationTest {

    @Test
    public void shouldCreateAuditLogInElasticsearchWhenPatientIsCreated() {
        String token = authenticateAndGetToken();
        String uniqueEmail = "audit-test-" + System.currentTimeMillis() + "@test.com";

        String payload = String.format("""
            {
              "name": "Audit Tracer",
              "email": "%s",
              "address": "Kafka Street",
              "dateOfBirth": "1990-01-01",
              "registeredDate": "2024-11-28"
            }
        """, uniqueEmail);

        // ACT: Create a patient
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(payload)
                .post("/api/patients")
                .then()
                .statusCode(200);

        // ASSERT: Wait for Kafka to move the data to Elasticsearch
        // We give it 5 seconds to travel through the broker and the consumer
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    given()
                            .header("Authorization", "Bearer " + token)
                            .when()
                            .get("/api/audit")
                            .then()
                            .statusCode(200)
                            // Check if the latest log mentions our unique email
                            .body("content.details", hasItem(containsString(uniqueEmail)))
                            .body("content.action", hasItem("CREATE_PATIENT"));
                });
    }
}