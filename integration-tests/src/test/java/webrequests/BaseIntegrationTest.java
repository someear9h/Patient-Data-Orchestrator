package webrequests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import static io.restassured.RestAssured.given;

public class BaseIntegrationTest {

    @BeforeAll
    static void commonSetUp() {
        // The Gateway URL
        RestAssured.baseURI = "http://localhost:4004";
    }

    /**
     * Helper method to authenticate and return a valid JWT token.
     */
    protected String authenticateAndGetToken() {
        String loginPayload = """
              {
                "email": "testuser@test.com",
                "password": "password123"
              }
            """;

        return given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }
}
