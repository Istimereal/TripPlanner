package app.security;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.service.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SecurityControllerTest {

    private static EntityManagerFactory emfTest;
    private static Javalin app;
    private static String adminToken;

    String adminJson = """
                {
                  "username": "admin",
                  "password": "admin123"
                }
                """;

    String userJson = """
                {
                  "username": "user1",
                  "password": "user123"
                }
                """;

    @BeforeAll
    static void setupAll() {
        emfTest = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7081, emfTest);
        RestAssured.baseURI = "http://localhost:7081/api/v1";
    }

    @BeforeEach
  public void deleteAndInitializeDB() {
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE role_user RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE role RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
        Populator populator = new Populator(emfTest);
        populator.poppulateDBTestSecurity();
    }

    @Test
    @DisplayName("Admin register account")
    void registerAdminAccount() {
        //Arrange done in beforeALl and string adminJson class attribute

        //Act and assert
        given()
                .contentType("application/json")
                .body(adminJson)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body("username", equalTo("admin"))
                .body("token", notNullValue());

    }
       @Test
       @DisplayName("Test admin login")
       public void adminLogin() {
        //Act
           given()
                   .contentType("application/json")
                   .body(adminJson)
                   .when()
                   .post("/auth/register");


//Assert
           adminToken = given()
                   .contentType("application/json")
                   .body(adminJson)
                   .when()
                   .post("/auth/login")
                   .then()
                   .statusCode(200)
                   .body("username", equalTo("admin"))
                   .body("token", notNullValue())
                   .extract()
                   .path("token");

       }

       @Test
       @DisplayName("Register user")
       public void registerUser() {


           given()
                   .contentType("application/json")
                   .body(userJson)
                   .when()
                   .post("/auth/register")
                   .then()
                   .statusCode(200)
                   .body("username", equalTo("user1"))
                   .body("token", notNullValue());
       }

    @Test
    @DisplayName("Login returns valid token")
    void userLogin() {
        Assertions.assertNotNull(adminToken);
    }

    @Test
    @DisplayName("User failed login")
    void userLoginFail() {
      //Arrange
        String loginJson = """
                { "username": "user1", "password": "wrongpass" }
                """;
        //Act
        given()
                .contentType("application/json")
                .body(userJson)
                .when()
                .post("/auth/register");

                //Assert
        given()
                .contentType("application/json")
                .body(loginJson)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(500)
                .body("msg", contains("login failed"));

    }

    @Test
    @DisplayName("Access protected endpoint with valid token")
    void testAccessProtectedEndpoint() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/protected/admin_demo/")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Access protected endpoint without token")
    void testAccessWithoutToken() {
        given()
                .when()
                .get("/protected/admin_demo/")
                .then()
                .statusCode(401);
    }

    @AfterAll
    static void stopServer() {
        if (app != null)
            app.stop();
    }
}
