package movies.participants.in.rates;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static movies.participants.in.rates.MovieRatesRequestParticipant.PARAMETER_ID_MUST_BE_PRESENT;
import static movies.participants.in.rates.MovieRatesRequestParticipant.USER_ID_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MovieRatesRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private ClientAndServer moviesMockServer;
    private RatesConfig config;

    @BeforeEach
    public void startMockServer() {
        config = new RatesConfig(ENV_VALUE);
        moviesMockServer = ClientAndServer
                .startClientAndServer(Integer.valueOf(config.moviesPort()));
    }

    @AfterEach
    public void stopMockServer() {
        moviesMockServer.stop();
    }

    @Test
    public void compositionOk() {
        moviesMockServer.when(request().withPath("/movies/1/rate"))
                .respond(response().withBody(jsonBodyValid()));
        var moviesPartipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModelList = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        params.put("id", 1);
        moviesPartipant.contributeTo(viewModelList, params);
        assertEquals(3, viewModelList.size());
        assertTrue(((List) params.get(config.userIdsParamName())).containsAll(List.of(1L, 2L, 3L)));
        var rateOfUser1 = viewModelList.stream().filter(e -> e.get(USER_ID_KEY).equals(1L)).toList().getFirst();
        assertEquals("02-01-2025 12:46", rateOfUser1.get("ratedInDate"));
        assertEquals(5L, rateOfUser1.get("rateValue"));
        assertEquals("Great Movie", rateOfUser1.get("comment"));
        var rateOfUser2 = viewModelList.stream().filter(e -> e.get(USER_ID_KEY).equals(2L)).toList().getFirst();
        assertEquals("12-01-2025 12:46", rateOfUser2.get("ratedInDate"));
        assertEquals(5L, rateOfUser2.get("rateValue"));
        assertEquals("Fantastic! The actors, the music, everything is fantastic!", rateOfUser2.get("comment"));
        var rateOfUser3 = viewModelList.stream().filter(e -> e.get(USER_ID_KEY).equals(3L)).toList().getFirst();
        assertEquals("22-01-2025 12:46", rateOfUser3.get("ratedInDate"));
        assertEquals(4L, rateOfUser3.get("rateValue"));
        assertEquals("I really enjoy the movie", rateOfUser3.get("comment"));
    }

    @Test
    public void movieIdIsRequired() {
        var moviesPartipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModelList = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        var e = assertThrows(RuntimeException.class,
                () -> moviesPartipant.contributeTo(viewModelList, params));
        assertEquals(PARAMETER_ID_MUST_BE_PRESENT, e.getMessage());
    }


    private String jsonBodyValid() {
        return """
                [
                    {
                        "userId": 3,
                        "rateValue": 4,
                        "ratedInDate": "22-01-2025 12:46",
                        "comment": "I really enjoy the movie"
                    },
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

    @Test
    public void interestedIn01() {
        var movieParticipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        assertTrue(movieParticipant.isInterestedIn("/composed/movies/22/rate", "GET", Map.of()));
    }

    @Test
    public void interestedIn02() {
        var movieParticipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        assertFalse(movieParticipant.isInterestedIn("/composed/movies/rate", "GET", Map.of()));
    }

    @Test
    public void interestedIn03() {
        var movieParticipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        assertFalse(movieParticipant.isInterestedIn("/composed/movies/1", "GET", Map.of()));
    }

    @Test
    public void interestedIn04() {
        var movieParticipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        assertTrue(movieParticipant.isInterestedIn("/composed/movies/8/rate", "GET", Map.of()));
    }

    @Test
    public void interestedIn05() {
        var movieParticipant = new MovieRatesRequestParticipant(new EnvValue(ENV_VALUE));
        assertFalse(movieParticipant.isInterestedIn("/composed/movies/8/rate", "POST", Map.of()));
    }
}