import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    public void shouldReturnOkWithValidToken() {
        // 1-> Arrange
        // 2-> act
        // 3 -> assert

        // arrange
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
             
                """;

        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath()
                .getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        // 1-> Arrange
        // 2-> act
        // 3 -> assert

        // arrange
        String loginPayload = """
                {
                    "email": "invalid_user@test.com",
                    "password": "wrongpassword"
                }
             
                """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

    }
}
