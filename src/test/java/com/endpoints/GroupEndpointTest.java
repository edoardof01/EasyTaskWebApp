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

class GroupEndpointTest {

    private static String jwtToken;
    private static String secondUserJwtToken;

    private static final String PASSWORD = "testPassword";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/EasyTask-1.0-SNAPSHOT";
        RestAssured.port = 8080;

        RestAssured.basePath = "/api/group";

        String uniqueUsername1 = "groupTestUser_" + UUID.randomUUID();
        jwtToken = createAndSetupUser(uniqueUsername1);

        String uniqueUsername2 = "groupTestUser2_" + UUID.randomUUID();
        secondUserJwtToken = createAndSetupUser(uniqueUsername2);

    }


    static String createAndSetupUser(String username) {
        registerTestUser(username);
        String token = obtainJwtToken(username);
        completeUserProfile(token);
        return token;
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
    void testDeleteGroup() {
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
    void testJoinGroup(){
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


        int subtaskId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));
    }


    @Test
    void testMoveToCalendar() {
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

        int subtaskId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        int userId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", userId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
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
                .body("taskState", equalTo("INPROGRESS"));
    }


    @Test
    void testCompleteSubtaskSession(){
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
                                 "startDate": "2026-12-04T14:00:00",
                                 "endDate": "2026-12-04T16:00:00"
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
                                 "startDate": "2026-12-07T17:00:00",
                                 "endDate": "2026-12-07T19:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2026-12-07T17:00:00",
                           "endDate": "2026-12-07T19:00:00"
                         },
                         {
                           "startDate": "2026-12-04T14:00:00",
                           "endDate": "2026-12-04T16:00:00"
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
                                 "startDate": "2026-12-04T14:00:00",
                                 "endDate": "2026-12-04T16:00:00"
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

        int subtaskId1 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[0].id");

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        int userId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", userId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id, "userId", userId,"subtaskId", subtaskId1)
                .queryParam("sessionId",3)
                .when()
                .put("/completeSubtaskSession/{userId}/{groupId}/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    void completeGroupBySessions(){
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

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        int userId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", userId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  secondUserJwtToken)
                .pathParams("groupId", id,"subtaskId", subtaskId2)
                .queryParam("sessionId",4)
                .when()
                .put("/completeSubtaskSession/2/{groupId}/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId",id)
                .when()
                .put("/completeBySessions/{groupId}")
                .then()
                .log().all()
                .statusCode(200);
    }


    @Test
    void testForceCompletion(){
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

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        int userId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", userId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId",id)
                .queryParam("userId", userId)
                .when()
                .put("/forceCompletion/{groupId}")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    void leaveGroupTest(){
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

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        given()
                .header("Authorization",  secondUserJwtToken)
                .pathParams("groupId", id, "userId", 2)
                .when()
                .put("/{groupId}/leave/{userId}")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    void testRemoveMemberFromGroup(){
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

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        int adminId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", adminId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization",  jwtToken)
                .pathParams("groupId", id, "adminId",adminId,"userId",2)
                .queryParam("substitute",true)
                .when()
                .delete("/{groupId}/remove/{adminId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);
    }


    @Test
    void handleLimitExceededTest(){
        String jsonCreate = """
                    {
                         "groupDTO":{
                       "name": "Test Group",
                       "topic": "FINANCE",
                       "totalTime": 4,
                       "timetable": "ALL_DAY",
                       "userId": 1,
                       "strategies": [
                          {
                             "strategy":"FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS",
                             "tot":0,
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
                                 "startDate": "2025-03-10T06:00:00",
                                 "endDate": "2025-03-10T07:00:00"
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
                                 "startDate": "2025-03-10T08:00:00",
                                 "endDate": "2025-03-10T09:00:00"
                             }
                           ]
                         }
                       ],
                       "sessions": [
                         {
                           "startDate": "2025-03-10T06:00:00",
                           "endDate": "2025-03-10T07:00:00"
                         },
                         {
                           "startDate": "2025-03-10T08:00:00",
                           "endDate": "2025-03-10T09:00:00"
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
                                 "startDate": "2025-03-10T06:00:00",
                                 "endDate": "2025-03-10T07:00:00"
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

        int subtaskId2 = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("subtasks[1].id");

        int sessionId =
                given()
                        .header("Authorization",  jwtToken)
                        .pathParam("id", id)
                        .when()
                        .get("/{id}")
                        .then()
                        .log().all()
                        .extract()
                        .path("sessions[0].id");

        given()
                .header("Authorization",  jwtToken)
                .pathParam("groupId", id)
                .when()
                .put("/toFeed/{groupId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization", secondUserJwtToken)
                .pathParams("groupId", id,"userId", 2, "subtaskId", subtaskId2)
                .when()
                .put("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("actualMembers",equalTo(2));

        int userId = given()
                .header("Authorization",  jwtToken)
                .pathParam("id", id)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .extract()
                .path("user.id");

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id, "userId", userId)
                .when()
                .put("/moveToCalendar/{groupId}/{userId}")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .header("Authorization", jwtToken)
                .pathParams("groupId", id)
                .queryParam("sessionId",sessionId)
                .when()
                .put("/handleLimitExceeded/{groupId}")
                .then()
                .log().all()
                .statusCode(200);
    }




}









