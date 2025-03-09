package com.endpoints;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.*;


class GroupEndpointTest {

    private static String jwtToken;
    private static final String PASSWORD = "testPassword";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/EasyTask-1.0-SNAPSHOT";
        RestAssured.port = 8080;

        RestAssured.basePath = "/api/group";

        String uniqueUsername = "groupTestUser_" + UUID.randomUUID();
        registerTestUser(uniqueUsername);
        jwtToken = obtainJwtToken(uniqueUsername);
        completeUserProfile(jwtToken);
    }


    static void registerTestUser(String username) {
        Map<String, String> registrationData = new HashMap<>();
        registrationData.put("username", username);
        registrationData.put("password", PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(registrationData)
                .when()
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/register")  // Adatta l’endpoint se serve
                .then()
                .log().all()
                .statusCode(201);
    }

    static String obtainJwtToken(String username) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", PASSWORD);

        return given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("token");
    }


    static void completeUserProfile(String token) {
        String jsonBody = """
                {
                    "age": 25,
                    "sex": "MALE",
                    "description": "A software developer with 5 years of experience.",
                    "qualifications": [
                        "BSc in Computer Science"
                    ],
                    "profession": "Engineer"
                }
                """;

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/users/create")
                .then()
                .log().all()
                .statusCode(201);
    }


    @Test
    void testGetAllGroups() {
        given()
                .header("Authorization", jwtToken)
                .when()
                .get()
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(0));
    }


    @Test
    void testCreateGroup() {
        String jsonBody = """
                    {
                         "groupDTO":{
                       "name": "Test Group",
                       "topic": "FINANCE",
                       "totalTime": 4,
                       "timetable": "AFTERNOON_EVENING",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                             "tot":null,
                             "maxConsecSkipped":null
                         }
                       ],
                       "priority": 2,
                       "description": "This is a detailed task description",
                       "resources": [
                         {
                           "name": "Laptop",
                           "value": 3,
                           "type": "EQUIPMENT"
                         },
                         {
                           "name": "wires",
                           "type": "MONEY",
                           "money": 50
                         }
                       ],
                       "subtasks": [
                         {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         },
                         {
                           "name": "Subtask 2",
                           "description": "Complete the second part",
                           "totalTime": 30,
                           "level": 2,
                           "resources":[
                             {
                               "name": "wires",
                               "type": "MONEY",
                               "money": 50
                             }
                           ],
                           "subSessions":[
                             {
                                 "startDate": "2026-12-04T17:00:00",
                                 "endDate": "2026-12-04T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-07T14:00:00",
                           "endDate": "2026-12-07T16:00:00"
                         },
                         {
                           "startDate": "2026-12-04T17:00:00",
                           "endDate": "2026-12-04T19:00:00"
                         }
                       ],
                       "numUser":2
                     },
                     "chosenSubtaskDTO": {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         }
                     }
                """;

        int createdId =
                given()
                        .header("Authorization", jwtToken)
                        .contentType(ContentType.JSON)
                        .body(jsonBody)
                        .when()
                        .post()
                        .then()
                        .log().all()
                        .statusCode(201)
                        .contentType(ContentType.JSON)
                        .extract().path("id");

        given()
                .header("Authorization", jwtToken)
                .pathParam("id", createdId)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Test Group"));
    }

    @Test
    void testGetGroupById_NotFound() {
        long nonExistingId = 9999L;
        given()
                .header("Authorization", jwtToken)
                .pathParam("id", nonExistingId)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(500);
    }

    @Test
    void testUpdateGroup() {

        String jsonBody = """
                    {
                         "groupDTO":{
                       "name": "Test Group",
                       "topic": "FINANCE",
                       "totalTime": 4,
                       "timetable": "AFTERNOON_EVENING",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                             "tot":null,
                             "maxConsecSkipped":null
                         }
                       ],
                       "priority": 2,
                       "description": "This is a detailed task description",
                       "resources": [
                         {
                           "name": "Laptop",
                           "value": 3,
                           "type": "EQUIPMENT"
                         },
                         {
                           "name": "wires",
                           "type": "MONEY",
                           "money": 50
                         }
                       ],
                       "subtasks": [
                         {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         },
                         {
                           "name": "Subtask 2",
                           "description": "Complete the second part",
                           "totalTime": 30,
                           "level": 2,
                           "resources":[
                             {
                               "name": "wires",
                               "type": "MONEY",
                               "money": 50
                             }
                           ],
                           "subSessions":[
                             {
                                 "startDate": "2026-12-04T17:00:00",
                                 "endDate": "2026-12-04T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-07T14:00:00",
                           "endDate": "2026-12-07T16:00:00"
                         },
                         {
                           "startDate": "2026-12-04T17:00:00",
                           "endDate": "2026-12-04T19:00:00"
                         }
                       ],
                       "numUser":2
                     },
                     "chosenSubtaskDTO": {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         }
                     }
                """;
        int id =  given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().path("id");

        String jsonUpdate = """
               {
               "name": "Updated Group Task",
                       "topic": "ART",
                       "totalTime": 4,
                       "timetable": "ALL_DAY",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                             "tot":null,
                             "maxConsecSkipped":null
                         }
                       ],
                       "priority": 2,
                       "description": "This is a detailed task description",
                       "resources": [
                         {
                           "name": "Laptop",
                           "value": 3,
                           "type": "EQUIPMENT"
                         },
                         {
                           "name": "wires",
                           "type": "MONEY",
                           "money": 50
                         }
                       ],
                       "subtasks": [
                         {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-09T14:00:00",
                                 "endDate": "2026-12-09T16:00:00"
                             }
                           ]
                         },
                         {
                           "name": "Subtask 2",
                           "description": "Complete the second part",
                           "totalTime": 30,
                           "level": 2,
                           "resources":[
                             {
                               "name": "wires",
                               "type": "MONEY",
                               "money": 50
                             }
                           ],
                           "subSessions":[
                             {
                                 "startDate": "2026-12-14T17:00:00",
                                 "endDate": "2026-12-14T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-09T14:00:00",
                           "endDate": "2026-12-09T16:00:00"
                         },
                         {
                           "startDate": "2026-12-14T17:00:00",
                           "endDate": "2026-12-14T19:00:00"
                         }
                       ],
                       "numUser":2
               }
               """;
        given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonUpdate)
                .pathParam("groupId", id)
                .when()
                .put("/{groupId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Updated Group Task"))
                .body("priority", equalTo(2));

    }

    @Test
    void testDeletePersonal() {
        // Crea un task personale da eliminare con una sessione (60 minuti)
        String jsonCreate = """
                {
                         "groupDTO":{
                       "name": "Test Group",
                       "topic": "FINANCE",
                       "totalTime": 4,
                       "timetable": "AFTERNOON_EVENING",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                             "tot":null,
                             "maxConsecSkipped":null
                         }
                       ],
                       "priority": 2,
                       "description": "This is a detailed task description",
                       "resources": [
                         {
                           "name": "Laptop",
                           "value": 3,
                           "type": "EQUIPMENT"
                         },
                         {
                           "name": "wires",
                           "type": "MONEY",
                           "money": 50
                         }
                       ],
                       "subtasks": [
                         {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         },
                         {
                           "name": "Subtask 2",
                           "description": "Complete the second part",
                           "totalTime": 30,
                           "level": 2,
                           "resources":[
                             {
                               "name": "wires",
                               "type": "MONEY",
                               "money": 50
                             }
                           ],
                           "subSessions":[
                             {
                                 "startDate": "2026-12-04T17:00:00",
                                 "endDate": "2026-12-04T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-07T14:00:00",
                           "endDate": "2026-12-07T16:00:00"
                         },
                         {
                           "startDate": "2026-12-04T17:00:00",
                           "endDate": "2026-12-04T19:00:00"
                         }
                       ],
                       "numUser":2
                     },
                     "chosenSubtaskDTO": {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         }
                     }
        """;

        int id = given()
                .header("Authorization",  jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonCreate)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .path("id");


        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .delete("/{groupId}")
                .then()
                .statusCode(204);


        given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(500);
    }

    @Test
    void testFreezePersonal() {
        // 1) Crea un personal task
        String jsonCreate = """
          {
                         "groupDTO":{
                       "name": "Test Group",
                       "topic": "FINANCE",
                       "totalTime": 4,
                       "timetable": "AFTERNOON_EVENING",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS",
                             "tot":null,
                             "maxConsecSkipped":null
                         }
                       ],
                       "priority": 2,
                       "description": "This is a detailed task description",
                       "resources": [
                         {
                           "name": "Laptop",
                           "value": 3,
                           "type": "EQUIPMENT"
                         },
                         {
                           "name": "wires",
                           "type": "MONEY",
                           "money": 50
                         }
                       ],
                       "subtasks": [
                         {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         },
                         {
                           "name": "Subtask 2",
                           "description": "Complete the second part",
                           "totalTime": 30,
                           "level": 2,
                           "resources":[
                             {
                               "name": "wires",
                               "type": "MONEY",
                               "money": 50
                             }
                           ],
                           "subSessions":[
                             {
                                 "startDate": "2026-12-04T17:00:00",
                                 "endDate": "2026-12-04T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-07T14:00:00",
                           "endDate": "2026-12-07T16:00:00"
                         },
                         {
                           "startDate": "2026-12-04T17:00:00",
                           "endDate": "2026-12-04T19:00:00"
                         }
                       ],
                       "numUser":2
                     },
                     "chosenSubtaskDTO": {
                           "name": "Subtask 1",
                           "description": "Complete the first part",
                           "totalTime": 30,
                           "level": 1,
                           "resources":[
                             {
                               "name": "Laptop",
                               "value": 3,
                               "type": "EQUIPMENT"
                             }
                           ],
                           "subSessions": [
                             {
                                 "startDate": "2026-12-07T14:00:00",
                                 "endDate": "2026-12-07T16:00:00"
                             }
                           ]
                         }
                     }
        """;

        int id = given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonCreate)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .header("Authorization", jwtToken)
                .queryParam("groupId", id)
                .when()
                .put("/moveToCalendar")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization", jwtToken)
                .pathParam("personalId", id)
                .when()
                .put("/freeze/{personalId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization", jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("taskState", equalTo("FREEZED"));
    }




}


