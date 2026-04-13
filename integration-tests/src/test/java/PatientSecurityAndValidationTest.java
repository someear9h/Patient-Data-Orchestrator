import org.junit.jupiter.api.Test;
import webrequests.BaseIntegrationTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class PatientSecurityAndValidationTest extends BaseIntegrationTest {

    // 1. Security Test: Invalid JWT
    @Test
    public void shouldReturnUnauthorizedWhenTokenIsInvalid() {
        given()
                .header("Authorization", "Bearer this_is_a_fake_token")
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401); // 401 Unauthorized
    }

    // 2. Validation Test: Missing Required Fields
    @Test
    public void shouldReturnBadRequestWhenEmailIsMissing() {
        String token = authenticateAndGetToken();

        // Missing the "email" field entirely
        String badPayload = """
            {
              "name": "John Doe",
              "address": "123 Street",
              "dateOfBirth": "1990-01-01"
            }
        """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(badPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(400); // 400 Bad Request
    }

    // 3. Logic Test: Duplicate Data
    @Test
    public void shouldReturnConflictWhenEmailAlreadyExists() {
        String token = authenticateAndGetToken();
        String email = "duplicate@test.com";

        String payload = String.format("""
            {
              "name": "Patient A",
              "email": "%s",
              "address": "Street A",
              "dateOfBirth": "1990-01-01",
              "registeredDate": "2024-11-28"
            }
        """, email);

        // First creation (Expected: 200 or 201)
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(payload)
                .post("/api/patients");

        // Second creation with SAME email
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(400)
                .body("message", equalTo("Email Address already exists"));
    }
}