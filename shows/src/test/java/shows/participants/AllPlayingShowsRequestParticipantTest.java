package shows.participants;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import shows.participants.in.shows.AllPlayingShowsRequestParticipant;
import shows.participants.in.shows.InShowsConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static shows.participants.TypeCaster.cast;
import static shows.participants.in.shows.AllPlayingShowsRequestParticipant.MOVIE_ID_KEY;

public class AllPlayingShowsRequestParticipantTest {

    public static final String SHOWS_KEY = "shows";
    public static final String SHOW_ID_KEY = "showId";
    public static final String PRICE_KEY = "price";
    public static final String PLAYING_TIME_KEY = "playingTime";
    public static final String ENV_VALUE = "default";
    private ClientAndServer showsMockServer;
    private InShowsConfig inShowsConfig;

    @BeforeEach
    public void startMockServer() {
        inShowsConfig = new InShowsConfig(ENV_VALUE);
        showsMockServer = ClientAndServer.startClientAndServer(Integer.valueOf(inShowsConfig.showsPort()));
    }

    @AfterEach
    public void stopMockServer() {
        showsMockServer.stop();
    }

    @Test
    public void viewModelIsPopulatedAndParamsWithMovieIds() {
        showsMockServer.when(request().withPath(inShowsConfig.showsPath())).respond(response().withBody(jsonForShowsOk()));
        var showsMainParticipant = new AllPlayingShowsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        showsMainParticipant.contributeTo(viewModel, params);

        assertEquals(1, params.size());
        assertInstanceOf(List.class, params.get(inShowsConfig.moviesIdsParamName()));
        assertEquals(2, ((List) params.get(inShowsConfig.moviesIdsParamName())).size());

        assertEquals(2, viewModel.size());
        var movie123 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(123L)).toList().getFirst();
        var movie11 = viewModel.stream().filter(e -> e.get(MOVIE_ID_KEY).equals(11L)).toList().getFirst();
        assertEquals(123L, movie123.get(MOVIE_ID_KEY));
        assertEquals(11L, movie11.get(MOVIE_ID_KEY));

        var movie123shows1 = getShowWithId(movie123, 1L);
        assertEquals("Saturday 01/18 18:30", cast(movie123shows1, Map.class).get(PLAYING_TIME_KEY));
        assertEquals(11.0D, cast(movie123shows1, Map.class).get(PRICE_KEY));
        assertEquals(123L, cast(movie123shows1, Map.class).get(MOVIE_ID_KEY));

        var movie123shows2 = getShowWithId(movie123, 2L);
        assertEquals("Saturday 01/18 22:30", cast(movie123shows2, Map.class).get(PLAYING_TIME_KEY));
        assertEquals(10.0D, cast(movie123shows2, Map.class).get(PRICE_KEY));
        assertEquals(123L, cast(movie123shows2, Map.class).get(MOVIE_ID_KEY));

        var movie11shows3 = getShowWithId(movie11, 3L);
        assertEquals("Sunday 01/19 19:30", cast(movie11shows3, Map.class).get(PLAYING_TIME_KEY));
        assertEquals(19.0D, cast(movie11shows3, Map.class).get(PRICE_KEY));
        assertEquals(11L, cast(movie11shows3, Map.class).get(MOVIE_ID_KEY));

        var movie11shows4 = getShowWithId(movie11, 4L);
        assertEquals("Sunday 01/19 23:30", cast(movie11shows4, Map.class).get(PLAYING_TIME_KEY));
        assertEquals(19.5D, cast(movie11shows4, Map.class).get(PRICE_KEY));
        assertEquals(11L, cast(movie11shows4, Map.class).get(MOVIE_ID_KEY));
    }

    @Test
    public void viewModelMustBeEmptyOnFirstContributor() {
        var showsMainParticipant = new AllPlayingShowsRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        viewModel.add(Map.of("1", "2"));

        var e = assertThrows(RuntimeException.class,
                () -> showsMainParticipant.contributeTo(viewModel, new HashMap<>()));
        assertEquals(AllPlayingShowsRequestParticipant.VIEW_MODEL_MUST_BE_EMPTY, e.getMessage());
    }

    @Test
    public void mustParticipateInComposition() {
        var moviesPartipant = new AllPlayingShowsRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = moviesPartipant.isInterestedIn("/composed/shows"
                , "GET"
                , Map.of());

        assertTrue(isInterested);
    }

    @Test
    public void mustNotParticipateInComposition() {
        var moviesPartipant = new AllPlayingShowsRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = moviesPartipant.isInterestedIn("/shows"
                , "GET"
                , Map.of());
        assertFalse(isInterested);
    }

    private String jsonForShowsOk() {
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

    private Object getShowWithId(Map<String, Object> movie123, long obj) {
        return cast(movie123.get(SHOWS_KEY), List.class)
                .stream()
                .filter(e -> cast(e, Map.class).get(SHOW_ID_KEY).equals(obj))
                .toList().getFirst();
    }
}

//just to writte less parenthesis for casting...
class TypeCaster {
    public static <T> T cast(Object obj, Class<T> type) {
        return (T) obj;
    }
}
