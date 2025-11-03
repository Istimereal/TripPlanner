package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.daos.GuideDAO;
import app.entities.Guide;
import app.service.Populator;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuideControllerTest {
    private static EntityManagerFactory emfTest;
    private static Javalin app;
    private static GuideDAO guideDAO;
    private static String adminToken;

    private static Guide g1, g2;
    List<Guide> guideList = new ArrayList<>();

    @BeforeAll
    static void testSetup() {

        emfTest = HibernateConfig.getEntityManagerFactoryForTest();
        guideDAO = GuideDAO.getInstance(emfTest);

        app = ApplicationConfig.startServer(7079, emfTest);

        RestAssured.baseURI = "http://localhost:7079/api/v1";
    }

    @BeforeEach
    public void deleteAndInitializeDB() {

        try (EntityManager em = emfTest.createEntityManager()) {

            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE guide RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE role_user RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE role RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();

            Populator populator = new Populator(emfTest);
            populator.createUsersAndRolesTest();
            populator.poppulateDBTest();

            guideList = guideDAO.getAllGuides();
            g1 = guideList.get(0);
            g2 = guideList.get(1);

            // 🔹 Login for at få token (OBLIGATORISK før GET /guides)
            String loginJson = """
                {
                  "username": "admin",
                  "password": "admin123"
                }
                """;

            adminToken = given()
                    .contentType("application/json")
                    .body(loginJson)
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("token");

            System.out.println("Admin token hentet til test: " + adminToken);
        }
        catch (Exception e) {
            throw new RuntimeException("failed to TRUNCATE table and populate", e);
        }
    }


    @AfterAll
    static void stopServer() {
        if (app != null) app.stop();
    }

    @Test
    @DisplayName("Create guide succes")
    void createGuide() {

        // Arrange
        String json = """
                        {
                        "name": "jonas",
                        "email": "jonas@guide.dk",
                        "phoneNumber": 12345678,
                        "experienceInYears": 5
                } """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(json)
                .when()
                .post("/guides")
                .then()
                .statusCode(201)
                .body("id", equalTo(3))
                .body("name", equalTo("jonas"))
                .body("email", equalTo("jonas@guide.dk"))
                .body("phoneNumber", equalTo(12345678))
                .body("experienceInYears", equalTo(5));
    }

    @Test
    @DisplayName("Create guide failed")
    void createGuideFail() {

        // Arrange missing end } in json format
        String json = """
                        {
                        "name": "jonas",
                        "email": "jonas@guide.dk",
                        "phoneNumber": 12345678,
                        "experienceInYears": 5
                 """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(json)
                .when()
                .post("/guides")
                .then()
                .statusCode(500)
                .body("status", equalTo(500))
                .body("msg", equalTo("There was an unexpected problem with the server"));
    }

    @Test
    @DisplayName("get all Guides succes")
    void geAlltGuides() {
        //arrange done in @beforeAll
        //act done in @BeforeAll

        //Assert

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .get("/guides")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].id", equalTo(1))
                .body("[0].name", equalTo("tim"))
                .body("[0].email", equalTo("tim@guide.dk"))
                .body("[0].phoneNumber", equalTo(12345678))
                .body("[0].experienceInYears", equalTo(4))
                .body("[1].id", equalTo(2))
                .body("[1].name", equalTo("max"))
                .body("[1].email", equalTo("max@guide.dk"))
                .body("[1].phoneNumber", equalTo(34567812))
                .body("[1].experienceInYears", equalTo(2));
    }

    @Test
    void getGuideById() {

        //assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .get("/guides/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("tim"))
                .body("email", equalTo("tim@guide.dk"))
                .body("phoneNumber", equalTo(12345678))
                .body("experienceInYears", equalTo(4));
    }

    @Test
    @DisplayName("Update guide succes")
    void updateGuide() {
//Arrange
        String createJson = """
               {
            "name":"jonas",
                "email":"jonas@guide.dk",
                "phoneNumber":12345678,
                "experienceInYears":4
                }
        """;

        int idRecieved = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(createJson)
                .when()
                .post("/guides")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String jsonUpdate = """
                        {
                         "phoneNumber": 44345678,
                        "experienceInYears": 5
                } """;

        //Act and assert
        given()
        .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(jsonUpdate)
                .when()
                .put("/guides/" + idRecieved)
                .then()
                .statusCode(200)
                .body("id", equalTo(idRecieved))
                .body("name", equalTo("jonas"))
                .body("email", equalTo("jonas@guide.dk"))
                .body("phoneNumber", equalTo(44345678))
                .body("experienceInYears", equalTo(5));
    }

    @Test
    void deleteGuide() {

        //Arrange in BeforeAll

        //Act and Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .delete("/guides/2")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("msg", equalTo("Guide deleted"));
    }
}