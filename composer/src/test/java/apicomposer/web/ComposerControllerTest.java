package apicomposer.web;

import apicomposer.main.Main;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(value = "test")
public class ComposerControllerTest {

    public static final String MOVIE_RATE_PATH = "/movies/1/rate";
    public static final String USERS_PROFILE_IDS_PATH = "/users/profile/by/.*";
    public static final String COMPOSED_MOVIES_RATE_PATH = "/composed/movies/1/rate";
    @Value("${movies.server.port}")
    private int MOVIES_SERVER_PORT;
    @Value("${shows.server.port}")
    private int SHOWS_SERVER_PORT;
    @Value("${users.server.port}")
    private int USERS_SERVER_PORT;

    @Autowired
    private TestRestTemplate restTemplate;
    private ClientAndServer moviesMockServer;
    private ClientAndServer usersMockServer;
    private ClientAndServer showsMockServer;

    @Test
    public void composedMovieRate() throws JSONException {
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        usersMockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);
        moviesMockServer.when(request().withPath(MOVIE_RATE_PATH))
                .respond(response().withBody(jsonMovieRatesBodyValid()));
        usersMockServer.when(request()
                        .withPath(USERS_PROFILE_IDS_PATH))
                .respond(response().withBody(jsonUsersProfile()));
        ResponseEntity<String> response = restTemplate.getForEntity(COMPOSED_MOVIES_RATE_PATH, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        JSONAssert.assertEquals(jsonExpectedMovieRates(), response.getBody(), true);
    }

    @Test
    public void composedShows() throws JSONException {
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        showsMockServer = ClientAndServer.startClientAndServer(SHOWS_SERVER_PORT);
        moviesMockServer.when(request().withPath("/movies/by/.*"))
                .respond(response().withBody(jsonMovies()));
        showsMockServer.when(request()
                        .withPath("/shows"))
                .respond(response().withBody(jsonShows()));
        ResponseEntity<String> response = restTemplate.getForEntity("/composed/shows", String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        JSONAssert.assertEquals(jsonExpectedShows(), response.getBody(), true);
    }

    @AfterEach
    public void stopMockServer() {
        if (usersMockServer != null) {
            usersMockServer.stop();
        }
        if (moviesMockServer != null) {
            moviesMockServer.stop();
        }
        if (showsMockServer != null) {
            showsMockServer.stop();
        }
    }

    private String jsonMovieRatesBodyValid() {
        return """
                [
                    {
                        "userId": 2,
                        "rateValue": 5,
                        "ratedInDate": "12-01-2025 12:46",
                        "comment": "Fantastic! The actors, the music, everything is fantastic!"
                    },
                    {
                        "userId": 1,
                        "rateValue": 5,
                        "ratedInDate": "02-01-2025 12:46",
                        "comment": "Great Movie"
                    }
                ]""";
    }

    private String jsonExpectedShows() {
        return """
                [
                    {
                        "movieId":123,
                        "name":"Crash Tea",
                        "duration":"1hr 45mins",
                        "genres":["Comedy"],
                        "shows":[{
                             "showId":1,
                             "playingTime":"Saturday 01/18 18:30",
                             "price":11.0,
                             "movieId":123
                            },
                            {
                             "showId":2,
                             "playingTime":"Saturday 01/18 22:30",
                              "price":10.0,
                               "movieId":123
                             }]
                    },
                    {
                        "movieId":11,
                        "name":"Small Fish",
                        "duration":"2hrs 05mins",
                        "genres":["Adventure","Drama"],
                        "shows":[{
                            "showId":3,
                            "playingTime":"Sunday 01/19 19:30",
                            "price":19.0,
                            "movieId":11
                         },
                         {
                            "showId":4,
                            "playingTime":"Sunday 01/19 23:30",
                            "price":19.5,"movieId":11
                         }]
                         }]""";
    }

    private String jsonExpectedMovieRates() {
        return """
                [
                    {
                        "userId":2,
                        "rateValue":5,
                        "ratedInDate":"12-01-2025 12:46",
                        "comment":"Fantastic! The actors, the music, everything is fantastic!",
                        "username":"nico"
                    },
                    {
                        "userId":1,
                        "rateValue":5,
                        "ratedInDate":"02-01-2025 12:46",
                        "comment":"Great Movie",
                        "username":"emolinari"
                    }
                ]""";
    }

    private String jsonUsersProfile() {
        return """
                [
                    {
                        "userId": 1,
                        "fullname": "Enrique Molinari",
                        "username": "emolinari",
                        "email": "enrique.molinari@gmail.com"
                    },
                    {
                        "userId": 2,
                        "fullname": "Nicolas Molimini",
                        "username": "nico",
                        "email": "nico@mymovies.com"
                    }
                ]""";
    }

    private String jsonShows() {
        return """
                [
                    {
                        "movieId": 123,
                        "shows": [
                            {
                                "showId": 1,
                                "playingTime": "Saturday 01/18 18:30",
                                "price": 11.0,
                                "movieId": 123
                            },
                            {
                                "showId": 2,
                                "playingTime": "Saturday 01/18 22:30",
                                "price": 10.0,
                                "movieId": 123
                            }
                        ]
                    },
                    {
                        "movieId": 11,
                        "shows": [
                            {
                                "showId": 3,
                                "playingTime": "Sunday 01/19 19:30",
                                "price": 19.0,
                                "movieId": 11
                            },
                            {
                                "showId": 4,
                                "playingTime": "Sunday 01/19 23:30",
                                "price": 19.5,
                                "movieId": 11
                            }
                        ]
                    }
                ]""";
    }

    private String jsonMovies() {
        return """
                [
                    {
                        "id": 123,
                        "name": "Crash Tea",
                        "duration": "1hr 45mins",
                        "genres": [
                            "Comedy"
                        ]
                    },
                    {
                        "id": 11,
                        "name": "Small Fish",
                        "duration": "2hrs 05mins",
                        "genres": [
                            "Adventure",
                            "Drama"
                        ]
                    }
                ]
                """;
    }

}
