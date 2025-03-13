package com.endpoints;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class) class UserEndpointTest {

    private static String jwtToken;
    private static Long createdUserId; // memorizzeremo l'ID dell'utente creato
    private static final String TEST_PASSWORD = "testPassword";
    private static String uniqueUsername;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/EasyTask-1.0-SNAPSHOT";
        RestAssured.port = 8080;
        uniqueUsername = "testUser_" + UUID.randomUUID();
        registerTestUser(uniqueUsername);
        jwtToken = obtainJwtToken(uniqueUsername);
    }


    static void registerTestUser(String username) {
        Map<String, String> registrationData = new HashMap<>();
        registrationData.put("username", username);
        registrationData.put("password", UserEndpointTest.TEST_PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(registrationData)
                .when()
                .post("/api/register")
                .then()
                .statusCode(201);
    }


    static String obtainJwtToken(String username) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", UserEndpointTest.TEST_PASSWORD);

        return given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private static void completeUserProfileForComment(String token) {
        String jsonBody = """
            {
                "age": 25,
                "sex": "MALE",
                "description": "Profilo completato ad hoc per testare i commenti",
                "qualifications": [
                    "BSc in Computer Science"
                ],
                "profession": "Developer"
            }
        """;

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/api/users/create")
                .then()
                .statusCode(201);
    }



    @Test
    @Order(1)
    void testCreateUserProfile() {
        // Costruiamo il JSON per creare il profilo utente
        String jsonBody = """
      {
            "age": 25,
            "sex": "MALE",
            "description": "A software developer with 5 years of experience.",
            "qualifications": [
                "BSc in Computer Science",
                "MSc in Artificial Intelligence"
            ],
            "profession": "Student"
        }
      """;
        // Chiamiamo POST /api/users/create
        Integer tempId = given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/api/users/create")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        UserEndpointTest.createdUserId = tempId.longValue();

        System.out.println("User created with ID = " + createdUserId);
    }


    @Test
    @Order(2)
    void testGetUserById() {
        given()
                .header("Authorization", jwtToken)
                .pathParam("id", createdUserId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(createdUserId.intValue()))
                .body("profession", equalTo("Student"));
    }


    @Test
    @Order(3)
    void testGetUserByUsername() {
        given()
                .header("Authorization", jwtToken)
                .pathParam("username", uniqueUsername)
                .when()
                .get("/api/users/username/{username}")
                .then()
                .statusCode(200)
                .body("description", equalTo("A software developer with 5 years of experience."));
    }


    @Test
    @Order(4)
    void testUpdateUser() {
        String jsonUpdate = """
        {
            "age": 30,
            "sex": "MALE",
            "description": "Updated description",
            "qualifications": ["PhD in Computer Science"],
            "profession": "Developer"
        }
        """;

        given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .pathParam("id", createdUserId)
                .body(jsonUpdate)
                .when()
                .put("/api/users/{id}")
                .then()
                .statusCode(200)
                .body("age", equalTo(30))
                .body("profession", equalTo("Developer"))
                .body("description", equalTo("Updated description"));
    }



    @Test
    @Order(5)
    void testDeleteUser() {
        given()
                .header("Authorization", jwtToken)
                .pathParam("id", createdUserId)
                .when()
                .delete("/api/users/{id}")
                .then()
                .statusCode(204);

        given()
                .header("Authorization", jwtToken)
                .pathParam("id", createdUserId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(6)
    void testMakeComment() {

        completeUserProfileForComment(jwtToken);

        String jsonCreate = """
                {
                    "name": "Freezable Personal Task",
                    "topic": "PROGRAMMING",
                    "totalTime": 2,
                    "timetable": "ALL_DAY",
                    "userId": 1,
                    "strategies": [
                       {
                          "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                          "tot":null,
                          "maxConsecSkipped":null
                      }
                    ],
                    "priority": 1,
                    "description": "A task that we want to freeze",
                    "resources": [],
                    "subtasks": [],
                    "sessions": [
                     {
                          "startDate": "2026-03-29T19:30:00",
                          "endDate": "2026-03-29T20:30:00"
                        },
                        {
                          "startDate": "2026-03-30T22:30:00",
                          "endDate": "2026-03-30T23:30:00"
                        }
                    ],
                    "userGuidance": "i'm not sure about it"
                }
                """;

        String commentJson = """
                {
                    "content": "Questo è il mio commento",
                    "authorId": 1,
                    "sharedId": 1
                }
                """;

        int id = given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonCreate)
                .when()
                .post("/api/shared")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(commentJson)
                .pathParam("sharedId", id)
                .when()
                .post("/api/users/{sharedId}/comments")
                .then()
                .log().all()
                .statusCode(201)
                // Verifichiamo che il campo "content" nel JSON di risposta corrisponda a quello inviato
                .body("content", equalTo("Questo è il mio commento"));
    }
}
