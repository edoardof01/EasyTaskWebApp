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

 class PersonalEndpointTest {

    private static String jwtToken;
    private static final String PASSWORD = "testPassword";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/EasyTask-1.0-SNAPSHOT";
        RestAssured.port = 8080;

        RestAssured.basePath = "/api/personal";

        String uniqueUsername = "testUser_" + UUID.randomUUID();

        registerTestUser(uniqueUsername);

        jwtToken = obtainJwtToken(uniqueUsername);

        completeUserProfile(jwtToken);
    }

    static void completeUserProfile(String token) {
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

        given()
                .header("Authorization",  token)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/users/create")
                .then()
                .log().all()
                .statusCode(201);
    }


    static void registerTestUser(String username) {
        Map<String, String> registrationData = new HashMap<>();
        registrationData.put("username", username);
        registrationData.put("password", PersonalEndpointTest.PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(registrationData)
                .when()
                // Usa l'URL corretto per la registrazione
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/register")
                .then()
                .log().all()
                .statusCode(201);
    }

    static String obtainJwtToken(String username) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", PersonalEndpointTest.PASSWORD);

        return given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                // Imposta il basePath specifico per l'autenticazione
                .post("http://localhost:8080/EasyTask-1.0-SNAPSHOT/api/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    void testGetAllPersonalTasks() {
        given()
                .header("Authorization",  jwtToken)
                .when()
                .get()
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void testCreatePersonal() {
        String jsonBody = """
                {
                   "name": "Test Personal Task",
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
                         "name": "complete subtask",
                         "level":2,
                         "description": "This is a detailed subtask description",
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
                         "totalTime": 2,
                         "subSessions":[
                          {
                            "startDate": "2025-03-26T19:30:00",
                            "endDate": "2025-03-26T20:30:00"
                          },
                          {
                            "startDate": "2025-03-26T21:30:00",
                            "endDate": "2025-03-26T22:30:00"
                          }
                         ]
                     }
                   ],
                   "sessions": [
                     {
                       "startDate": "2025-03-26T19:30:00",
                       "endDate": "2025-03-26T20:30:00"
                     },
                     {
                       "startDate": "2025-03-26T21:30:00",
                       "endDate": "2025-03-26T22:30:00"
                     }
                   ]
                 }
        """;

        given()
                .header("Authorization",  jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test Personal Task"))
                .body("priority", equalTo(1));
    }

    @Test
    void testGetPersonalById_NotFound() {
        long nonExistingId = 9999;
        given()
                .header("Authorization",  jwtToken)
                .pathParam("id", nonExistingId)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(500);
    }

    @Test
    void testUpdatePersonal() {
        String jsonCreate = """
                {
                  "name": "Complete Personal Task",
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
                  "description": "This is a detailed task description",
                  "resources": [
                    {
                      "name": "Laptop",
                      "value": 1,
                      "type": "COMPETENCE"
                    },
                    {
                      "name": "wires",
                      "type": "MONEY",
                      "money": 50
                    }
                  ],
                  "subtasks": [
                    {
                        "name": "updated subtask",
                        "level":3,
                        "description": "This is a subtask description",
                        "resources": [{
                       "name": "Laptop",
                       "value": 1,
                       "type": "COMPETENCE"
                     },
                     {
                       "name": "wires",
                       "type": "MONEY",
                       "money": 50
                     }],
                        "totalTime": 2,
                        "subSessions":[
                             {
                      "startDate": "2025-06-26T19:30:00",
                      "endDate": "2025-06-26T20:30:00"
                    },
                    {
                      "startDate": "2025-06-26T21:30:00",
                      "endDate": "2025-06-26T22:30:00"
                    }
                        ]
                    }
                  ],
                  "sessions": [
                    {
                      "startDate": "2025-06-26T19:30:00",
                      "endDate": "2025-06-26T20:30:00"
                    },
                    {
                      "startDate": "2025-06-26T21:30:00",
                      "endDate": "2025-06-26T22:30:00"
                    }
                  ]
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

        String jsonUpdate = """
                {
                  "name": "Updated Personal Task",
                  "topic": "ART",
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
                        "name": "complete subtask",
                        "level":2,
                        "description": "This is a description",
                        "resources": [{
                       "name": "Laptop",
                       "value": 3,
                       "type": "EQUIPMENT"
                     },
                     {
                       "name": "wires",
                       "type": "MONEY",
                       "money": 50
                     }],
                        "totalTime": 2,
                        "subSessions":[
                             {
                      "startDate": "2025-05-26T19:30:00",
                      "endDate": "2025-05-26T20:30:00"
                    },
                    {
                      "startDate": "2025-05-26T22:30:00",
                      "endDate": "2025-05-26T23:30:00"
                    }
                        ]
                    }
                  ],
                  "sessions": [
                    {
                      "startDate": "2025-05-26T19:30:00",
                      "endDate": "2025-05-26T20:30:00"
                    },
                    {
                      "startDate": "2025-05-26T22:30:00",
                      "endDate": "2025-05-26T23:30:00"
                    }
                  ]
                }
                """;

        given()
                .header("Authorization", jwtToken)
                .contentType(ContentType.JSON)
                .body(jsonUpdate)
                .pathParam("personalId", id)
                .when()
                .put("/{personalId}")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Updated Personal Task"))
                .body("priority", equalTo(2));
    }

    @Test
    void testDeletePersonal() {
        // Crea un task personale da eliminare con una sessione (60 minuti)
        String jsonCreate = """
                {
                    "name": "Complete Personal Task",
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
                          "name": "complete subtask",
                          "level":2,
                          "description": "This is a detailed subtask description",
                          "resources": [{
                       "name": "Laptop",
                       "value": 3,
                       "type": "EQUIPMENT"
                     },
                     {
                       "name": "wires",
                       "type": "MONEY",
                       "money": 50
                     }],
                          "totalTime": 2,
                          "subSessions":[
                               {
                        "startDate": "2026-03-26T19:30:00",
                        "endDate": "2026-03-26T20:30:00"
                      },
                      {
                        "startDate": "2026-03-26T21:30:00",
                        "endDate": "2026-03-26T22:30:00"
                      }
                          ]
                      }
                    ],
                    "sessions": [
                      {
                        "startDate": "2026-03-26T19:30:00",
                        "endDate": "2026-03-26T20:30:00"
                      },
                      {
                        "startDate": "2026-03-26T21:30:00",
                        "endDate": "2026-03-26T22:30:00"
                      }
                    ]
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
                .pathParam("personalId", id)
                .when()
                .delete("/{personalId}")
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
            ]
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
                 .queryParam("personalId", id)
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


     @Test
     void testMoveToCalendar() {
         // 1) Crea un personal task
         String jsonCreate = """
            {
                "name": "Task to move in calendar",
                "topic": "ART",
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
                "priority": 2,
                "description": "We will move it to calendar",
                "resources": [],
                "subtasks": [],
                 "sessions": [
                    {
                      "startDate": "2026-03-28T19:30:00",
                      "endDate": "2026-03-28T20:30:00"
                    },
                    {
                      "startDate": "2026-03-28T22:30:00",
                      "endDate": "2026-03-28T23:30:00"
                    }
                ]
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
                 .queryParam("personalId", id)
                 .when()
                 .put("/moveToCalendar")
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
     void testCompleteSession() {

         String jsonCreate = """
            {
                "name": "Task to complete session",
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
                "description": "Test completeSession",
                "resources": [],
                "subtasks": [],
                "sessions": [
                  {
                    "startDate": "2025-04-26T19:30:00",
                    "endDate": "2025-04-26T20:30:00"
                  },
                  {
                    "startDate": "2025-04-27T19:30:00",
                    "endDate": "2025-04-27T20:30:00"
                  }
                ]
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
                 .queryParam("personalId", id)
                 .when()
                 .put("/moveToCalendar")
                 .then()
                 .log().all()
                 .statusCode(200);

         int sessionId = given()
                 .header("Authorization", jwtToken)
                 .pathParam("id", id)
                 .when()
                 .get("/{id}")
                 .then()
                 .log().all()
                 .statusCode(200)
                 .extract()
                 .path("sessions[1].id");



         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .queryParam("sessionId", sessionId)
                 .when()
                 .put("/completeSession/{personalId}/")
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
                 .body("sessions[1].state", equalTo("COMPLETED"));
     }


     @Test
     void testCompletePersonalBySessions() {
         String jsonCreate = """
            {
                "name": "Task for completeBySessions",
                "topic": "PROGRAMMING",
                "totalTime": 3,
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
                "description": "We will complete by sessions",
                "resources": [],
                "subtasks": [],
                "sessions": [
                  {
                    "startDate": "2025-03-26T19:30:00",
                    "endDate": "2025-03-26T20:30:00"
                  },
                  {
                    "startDate": "2025-03-26T20:30:00",
                    "endDate": "2025-03-26T21:30:00"
                  },
                  {
                    "startDate": "2025-03-27T20:30:00",
                    "endDate": "2025-03-27T21:30:00"
                  }

                ]
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
                 .queryParam("personalId", id)
                 .when()
                 .put("/moveToCalendar")
                 .then()
                 .log().all()
                 .statusCode(200);

         int sessionId = given()
                 .header("Authorization", jwtToken)
                 .pathParam("id", id)
                 .when()
                 .get("/{id}")
                 .then()
                 .log().all()
                 .statusCode(200)
                 .extract()
                 .path("sessions[1].id");

         int sessionId2 = given()
                 .header("Authorization", jwtToken)
                 .pathParam("id", id)
                 .when()
                 .get("/{id}")
                 .then()
                 .log().all()
                 .statusCode(200)
                 .extract()
                 .path("sessions[0].id");



         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .queryParam("sessionId", sessionId)
                 .when()
                 .put("/completeSession/{personalId}/")
                 .then()
                 .log().all()
                 .statusCode(200);

         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .queryParam("sessionId", sessionId2)
                 .when()
                 .put("/completeSession/{personalId}/")
                 .then()
                 .log().all()
                 .statusCode(200);



         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .when()
                 .put("/completeBySessions/{personalId}")
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
                 .body("taskState", equalTo("FINISHED"));
     }


     @Test
     void testForceCompletion() {
         // 1) Crea un personal task con alcune sessioni
         String jsonCreate = """
            {
                "name": "Task to Force Complete",
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
                "description": "We will force complete it",
                "resources": [],
                "subtasks": [],
                "sessions": [
                  {
                    "startDate": "2025-05-26T19:30:00",
                    "endDate": "2025-05-26T20:30:00"
                  },
                  {
                    "startDate": "2025-05-27T19:30:00",
                    "endDate": "2025-05-27T20:30:00"
                  }
                ]
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
                 .queryParam("personalId", id)
                 .when()
                 .put("/moveToCalendar")
                 .then()
                 .log().all()
                 .statusCode(200);

         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .when()
                 .put("/forceCompletion/{personalId}")
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
                 .body("taskState", equalTo("FINISHED"));
     }




     @Test
     void testHandleLimitExceeded() {
         String jsonCreate = """
            {
                "name": "Task to Force Complete",
                "topic": "PROGRAMMING",
                "totalTime": 2,
                "timetable": "ALL_DAY",
                "userId": 1,
                "strategies": [
                  {
                    "strategy":"FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS",
                    "tot": 0,
                    "maxConsecSkipped": null
                  }
                ],
                "priority": 1,
                "description": "We will force complete it",
                "resources": [],
                "subtasks": [],
                "sessions": [
                  {
                    "startDate": "2025-03-09T18:00:00",
                    "endDate": "2025-03-09T19:00:00"
                  },
                  {
                    "startDate": "2025-03-09T20:00:00",
                    "endDate": "2025-03-09T21:00:00"
                  }
                ]
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
                 .queryParam("personalId", id)
                 .when()
                 .put("/moveToCalendar")
                 .then()
                 .log().all()
                 .statusCode(200);

         int sessionId = given()
                 .header("Authorization", jwtToken)
                 .pathParam("id", id)
                 .when()
                 .get("/{id}")
                 .then()
                 .log().all()
                 .statusCode(200)
                 .extract()
                 .path("sessions[0].id");


         given()
                 .header("Authorization", jwtToken)
                 .pathParam("personalId", id)
                 .queryParam("sessionId", sessionId)
                 .when()
                 .put("/handleLimitExceeded/{personalId}/")
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
