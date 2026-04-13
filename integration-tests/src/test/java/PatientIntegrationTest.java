import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import io.restassured.response.Response;
import webrequests.BaseIntegrationTest;

public class PatientIntegrationTest extends BaseIntegrationTest {
    @Test
    public void shouldReturnPatientsWithValidToken() {
        // use helper method from the parent class
        String token = authenticateAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());
    }

    @Test
    public void shouldCreateAndUpdateAndThenDeletePatient() {
        // 1. Get Token (Reusing login logic from base class)
        String token = authenticateAndGetToken();

        // 2. CREATE Patient
        String newPatientPayload = """
        {
          "name": "Integration Test Patient",
          "email": "test-integration@example.com",
          "address": "123 Test Street",
          "dateOfBirth": "1995-05-15",
          "registeredDate": "2024-11-28"
        }
        """;

        Response createResponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(newPatientPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract().response();

        String patientId = createResponse.jsonPath().getString("id");

        // 3. UPDATE Patient (Change the address)
        String updatePayload = """
        {
          "name": "Integration Test Patient",
          "email": "test-integration@example.com",
          "address": "456 Updated Blvd",
          "dateOfBirth": "1995-05-15"
        }
        """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(updatePayload)
                .when()
                .put("/api/patients/" + patientId)
                .then()
                .statusCode(200);

        // 4. DELETE Patient
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/patients/" + patientId)
                .then()
                .statusCode(204); // delete returns NO_CONTENT
    }
}
