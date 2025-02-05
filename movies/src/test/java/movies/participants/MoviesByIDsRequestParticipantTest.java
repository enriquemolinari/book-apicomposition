package movies.participants;

import apicomposer.api.EnvValue;
import movies.participants.in.shows.InShowsConfig;
import movies.participants.in.shows.MoviesByIDsRequestParticipant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static movies.participants.in.shows.MoviesByIDsRequestParticipant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MoviesByIDsRequestParticipantTest {

    public static final String ENV_VALUE = "default";
    private ClientAndServer moviesMockServer;
    private InShowsConfig config;

    @BeforeEach
    public void startMockServer() {
        config = new InShowsConfig(ENV_VALUE);
        moviesMockServer = ClientAndServer
                .startClientAndServer(Integer.valueOf(config.moviesPort()));
    }

    @AfterEach
    public void stopMockServer() {
        moviesMockServer.stop();
    }

    @Test
    public void compositionOk() {
        moviesMockServer.when(request().withPath("/movies/by/123,11"))
                .respond(response().withBody(jsonBodyValid()));
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel(123L, 11L);
        moviesPartipant.contributeTo(viewModel, Map.of(config.moviesIdsParamName(), List.of(123L, 11L)));
        assertEquals(2, viewModel.size());
        var movie123 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(123L)).toList().getFirst();
        var movie11 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(11L)).toList().getFirst();
        assertEquals(123L, movie123.get(MOVIE_ID_KEY));
        assertEquals(11L, movie11.get(MOVIE_ID_KEY));

        assertEquals("Crash Tea", movie123.get(MOVIE_NAME_KEY));
        assertEquals("1hr 45mins", movie123.get(MOVIE_DURATION_KEY));
        assertEquals(List.of("Comedy"), movie123.get(MOVIE_GENRES_KEY));

        assertEquals("Small Fish", movie11.get(MOVIE_NAME_KEY));
        assertEquals("2hrs 05mins", movie11.get(MOVIE_DURATION_KEY));
        assertEquals(List.of("Adventure", "Drama"), movie11.get(MOVIE_GENRES_KEY));
    }

    @Test
    public void inCaseOfErrorsMustReturnFallbackValues() {
        moviesMockServer.when(request().withPath("/movies/by/12,10"))
                .respond(response()
                        .withDelay(TimeUnit.SECONDS, config.httpCallTimeout() + 1)
                        .withBody(jsonBodyValid()));
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel(12L, 10L);
        moviesPartipant.contributeTo(viewModel, Map.of(config.moviesIdsParamName(), List.of(12L, 10L)));
        assertEquals(2, viewModel.size());

        var movie12 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(12L)).toList().getFirst();
        var movie10 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(10L)).toList().getFirst();
        assertEquals(12L, movie12.get(MOVIE_ID_KEY));
        assertEquals(10L, movie10.get(MOVIE_ID_KEY));

        assertEquals(FALLBACK_VALUE, movie12.get(MOVIE_NAME_KEY));
        assertEquals(FALLBACK_VALUE, movie12.get(MOVIE_DURATION_KEY));
        assertEquals(List.of(FALLBACK_VALUE), movie12.get(MOVIE_GENRES_KEY));

        assertEquals(FALLBACK_VALUE, movie10.get(MOVIE_NAME_KEY));
        assertEquals(FALLBACK_VALUE, movie10.get(MOVIE_DURATION_KEY));
        assertEquals(List.of(FALLBACK_VALUE), movie10.get(MOVIE_GENRES_KEY));
    }

    @Test
    public void paramsMustNotBeEmpty() {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel(12L, 10L);
        assertThrows(RuntimeException.class, () -> moviesPartipant.contributeTo(viewModel, Map.of(config.moviesIdsParamName(), List.of())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void paramsMustHaveMovieIds(Map params) {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel(12L, 10L);
        assertThrows(RuntimeException.class, () -> moviesPartipant.contributeTo(viewModel, params));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void viewModelMustContainShows(List viewModelParam) {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        var e = assertThrows(RuntimeException.class, () -> moviesPartipant.contributeTo(viewModelParam, new HashMap<>()));
        assertEquals(VIEW_MODEL_MUST_BE_POPULATED, e.getMessage());
    }

    @Test
    public void mustParticipateInComposition() {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = moviesPartipant.isInterestedIn("/composed/shows"
                , "GET"
                , Map.of());

        assertTrue(isInterested);
    }

    @Test
    public void mustNotParticipateInComposition() {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = moviesPartipant.isInterestedIn("/shows"
                , "GET"
                , Map.of());
        assertFalse(isInterested);
    }


    @Test
    public void viewModelMustContainMovies2() {
        var moviesPartipant = new MoviesByIDsRequestParticipant(new EnvValue(ENV_VALUE));
        Map<String, Object> map = new HashMap<>();
        map.put(MOVIE_ID_KEY, null);
        var viewModel = List.of(map);
        var e = assertThrows(RuntimeException.class
                , () -> moviesPartipant.contributeTo(viewModel, new HashMap<>()));
        assertEquals(VIEW_MODEL_MUST_BE_POPULATED, e.getMessage());
    }


    private String jsonBodyValid() {
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

    private ArrayList<Map<String, Object>> populateViewModel(long value, long value1) {
        var viewModel = new ArrayList<Map<String, Object>>();
        var map = new HashMap<String, Object>();
        map.put("movieId", value);
        map.put("shows", List.of());
        var map2 = new HashMap<String, Object>();
        map2.put("movieId", value1);
        map2.put("shows", List.of());
        viewModel.add(map);
        viewModel.add(map2);
        return viewModel;
    }
}