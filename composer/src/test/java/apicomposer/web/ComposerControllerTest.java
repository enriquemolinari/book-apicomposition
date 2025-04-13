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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static apicomposer.web.ComposerController.HEADER_USERID_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(value = "test")
public class ComposerControllerTest {

    public static final String MOVIE_RATE_PATH = "/movies/1/rate";
    public static final String USERS_PROFILE_IDS_PATH = "/users/profile/by/.*";
    public static final String COMPOSED_MOVIES_RATE_PATH = "/composed/movies/1/rate";
    public static final String USERS_PROFILE_PATH = "/users/private/profile";
    public static final String SHOWS_USER_PROFILE_PATH = "/shows/buyer";
    public static final String COMPOSED_USER_PROFILE_PATH = "/composed/users/private/profile";
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
    public void composedUserProfile() throws JSONException {
        usersMockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);
        usersMockServer.when(request()
                        .withPath(USERS_PROFILE_PATH))
                .respond(response().withBody(jsonSingleUserProfile()));

        showsMockServer = ClientAndServer.startClientAndServer(SHOWS_SERVER_PORT);
        showsMockServer.when(request().withPath(SHOWS_USER_PROFILE_PATH))
                .respond(response().withBody(jsonBuyerInfo()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USERID_NAME, "1");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                COMPOSED_USER_PROFILE_PATH,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        JSONAssert.assertEquals(jsonExpectedUserProfile(), body, true);
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

    @Test
    public void composedShowsSale() throws JSONException {
        showsMockServer = ClientAndServer.startClientAndServer(SHOWS_SERVER_PORT);
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        usersMockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);

        showsMockServer.when(request().withPath("/shows/sale/.*"))
                .respond(response().withBody(jsonShowSale()));

        moviesMockServer.when(request().withPath("/movies/.*"))
                .respond(response().withBody(jsonMoviesDetail()));

        usersMockServer.when(request()
                        .withPath("/users/private/profile"))
                .respond(response().withBody(jsonUsersProfile()));

        ResponseEntity<String> response = restTemplate.getForEntity("/composed/shows/sale/abcde", String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String body = response.getBody();
        System.out.println(body);
        JSONAssert.assertEquals(jsonExpectedComposedShowSale(), body, true);
    }

    private String jsonExpectedComposedShowSale() {
        return
                """                        
                        {
                                         "salesIdentifier": "7f1ffef0-f0d4-4099-a368-d5e76c652a8b",
                                         "movieId": 2,
                                         "movieName": "The Movie of the Century",
                                         "userId": 2,
                                         "username": "emolinari",
                                         "fullname": "Enrique Molinari",
                                         "email": "enrique.molinari@gmail.com",
                                         "total": 10.0,
                                         "pointsWon": 10,
                                         "seats": [
                                            3
                                         ],
                                         "showStartTime": "Tuesday 04/08 20:56"
                                         }
                        """;
    }


    private String jsonShowSale() {
        return
                """                        
                        {
                                         "salesIdentifier": "7f1ffef0-f0d4-4099-a368-d5e76c652a8b",
                                         "movieId": 2,
                                         "userId": 2,
                                         "total": 10.0,
                                         "pointsWon": 10,
                                         "seats": [
                                            3
                                         ],
                                         "showStartTime": "Tuesday 04/08 20:56"
                                         }
                        """;
    }

    @Test
    public void fallsbackCache() throws JSONException {
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        usersMockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);
        moviesMockServer.when(request().withPath(MOVIE_RATE_PATH))
                .respond(response().withBody(jsonMovieRatesBodyValid()));
        usersMockServer.when(request()
                        .withPath(USERS_PROFILE_IDS_PATH))
                .respond(response().withBody(jsonUsersProfile()));
        // populate the cache
        ResponseEntity<String> response = restTemplate.getForEntity(COMPOSED_MOVIES_RATE_PATH, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        // simulate a failure
        moviesMockServer.stop();
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        moviesMockServer.when(request().withPath(MOVIE_RATE_PATH))
                .respond(response().withStatusCode(500));
        ResponseEntity<String> responseWithFallback = restTemplate.getForEntity(COMPOSED_MOVIES_RATE_PATH, String.class);
        assertTrue(responseWithFallback.getStatusCode().is2xxSuccessful());
        JSONAssert.assertEquals(jsonExpectedMovieRates(), responseWithFallback.getBody(), true);

        moviesMockServer.when(request().withPath(MOVIE_RATE_PATH))
                .respond(response().withBody(jsonMovieRatesBodyValid()));
    }


    private String jsonExpectedUserProfile() {
        return """
                {
                        "userId": 1,
                        "points": "150",
                        "fullname": "Enrique Molinari",
                        "username": "emolinari",
                        "email": "enrique.molinari@gmail.com"
                    }""";
    }

    private String jsonSingleUserProfile() {
        return """
                {
                        "userId": 1,
                        "fullname": "Enrique Molinari",
                        "username": "emolinari",
                        "email": "enrique.molinari@gmail.com"
                    }""";
    }

    private String jsonBuyerInfo() {
        return """
                {
                   "userId": 1,
                   "points": "150"
                }
                """;
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

    private String jsonMoviesDetail() {
        return """
                {
                   "id": "10",
                   "name": "The Movie of the Century",
                   "duration": "2h 30m",
                   "plot": "a b c"
                }
                """;
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
