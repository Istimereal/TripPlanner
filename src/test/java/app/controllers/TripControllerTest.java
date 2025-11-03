package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.enums.Category;
import app.service.Populator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.shaded.org.checkerframework.common.value.qual.ArrayLenRange;

import java.time.LocalDateTime;

import static app.enums.Category.BEACH;
import static app.enums.Category.LAKE;
import static org.hamcrest.Matchers.equalTo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

class TripControllerTest {
    private static EntityManagerFactory emfTest;
    private static Javalin app;
    private static String adminToken;

    @BeforeAll
    static void setupServer() {
        emfTest = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7082, emfTest);
        RestAssured.baseURI = "http://localhost:7082/api/v1";
    }


    @BeforeEach
    void resetDB() {
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE trip RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE guide RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE role_user RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE role RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();

            Populator pop = new Populator(emfTest);
            pop.createUsersAndRolesTest();
            pop.poppulateDBTest();

            String loginJson = """
                    {
                      "username": "admin",
                      "password": "admin123"
                    }
                    """;

            adminToken =
                    given()
                            .contentType("application/json")
                            .body(loginJson)
                            .when()
                            .post("/auth/login")
                            .then()
                            .statusCode(200)
                            .extract()
                            .path("token");

            System.out.println();
        }
    }

    @AfterAll
    static void stopServer() {
        if (app != null) app.stop();
    }

    @Test
    void getTrips() {
        //arrange done in @beforeAll
        //     Category category1 = LAKE;
        //     Category cat2 = BEACH;
        //arrange done in @BeforeAll

        //Act and assert  .log().body();
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/trips")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].id", equalTo(1))
                .body("[0].name", equalTo("ski"))
                .body("[0].startTime", equalTo("2025-11-10T08:30"))
                .body("[0].endTime", equalTo("2025-11-14T08:30"))
                .body("[0].locationCordinates", equalTo("10.103.23"))
                .body("[0].price", equalTo(4000.0F))
                .body("[0].category", equalTo("LAKE"))
                .body("[1].id", equalTo(2))
                .body("[1].name", equalTo("tenerife"))
                .body("[1].startTime", equalTo("2025-11-18T08:30"))
                .body("[1].endTime", equalTo("2025-11-24T08:30"))
                .body("[1].locationCordinates", equalTo("10.103.25"))
                .body("[1].price", equalTo(7000.0F))
                .body("[1].category", equalTo("BEACH"));
    }

    @Test
    @DisplayName("Only Id, getTripById with packinglist   (No category")
    void getTripById() {
        //arrange done in @beforeAll
        //Act and assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/trips/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("ski"))
                .body("startTime", equalTo("2025-11-10T08:30"))
                .body("endTime", equalTo("2025-11-14T08:30"))
                .body("locationCordinates", equalTo("10.103.23"))
                .body("price", equalTo(4000.0F))
                .body("category", equalTo("LAKE"))
                .body("packingList.items", notNullValue());
    }

    @Test
    void createTrip() {

        //Arrange
        String Json = """
                
                {
                "name": "Beach trip",
                 "startTime": "2025-11-01T09:00",
                 "endTime": "2025-11-01T17:00",
                 "locationCordinates": "57.048",
                 "price": 5000,
                 "category": "BEACH"
                 }
                """;

        //Arrange act
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Json)
                .when()
                .post("/trips")
                .then()
                .body("id", equalTo(3))
                .body("name", equalTo("Beach trip"))
                .body("startTime", equalTo("2025-11-01T09:00"))
                .body("endTime", equalTo("2025-11-01T17:00"))
                .body("locationCordinates", equalTo("57.048"))
                .body("price", equalTo(5000.0F))
                .body("category", equalTo("BEACH"));

    }

    @Test
    void updateTrip() {
        //Arrange
        String json = """
        { "price": 6000 }
        """;
//Act Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/trips/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("ski"))
                .body("startTime", equalTo("2025-11-10T08:30"))
                .body("endTime", equalTo("2025-11-14T08:30"))
                .body("locationCordinates", equalTo("10.103.23"))
                .body("price", equalTo(6000.0F))
                .body("category", equalTo("LAKE"));

    }

    @Test
    void deleteTrip() {

        //Arrange in BeforeEach

        // Act Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .contentType("application/json")
                .when()
                .delete("/trips/1")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("msg", equalTo("trip with id: " + 1 + " was deleted"));

    }

    @Test
    void totalPriceTripsByGuide() {
// Arrange in BeforeEach

        //Act Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .log().body()
                .statusCode(200)
                .body("'1'", equalTo(4000.0F))
                .body("'2'", equalTo(7000.0F));
    }

    @Test
    void getPackingWeight() {
        // Arrange in BeforeEach

        //Act Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .contentType("application/json")
                .when()
                .get("/trips/1/packing/weight")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("msg", equalTo("Weight in grams: 4300"));
    }

    @Test
    @DisplayName("Make a trip hve a guide SUCCES")
    void linkGuideToTrip() {
        //Arrange in before All
// act Assert
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .when()
                .put("/trips/1/guides/1")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("msg", equalTo("Guide have been added"));
    }
}