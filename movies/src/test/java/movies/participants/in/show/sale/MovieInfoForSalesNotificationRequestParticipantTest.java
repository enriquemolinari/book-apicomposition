package movies.participants.in.show.sale;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static movies.participants.in.show.sale.MovieInfoForSalesNotificationRequestParticipant.MOVIE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MovieInfoForSalesNotificationRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private ClientAndServer usersMockServer;
    private MovieInfoConfig config;

    @BeforeEach
    public void startMockServer() {
        config = new MovieInfoConfig(ENV_VALUE);
        usersMockServer = ClientAndServer.startClientAndServer(Integer.valueOf(config.moviesPort()));
    }

    @AfterEach
    public void stopMockServer() {
        usersMockServer.stop();
    }

    @Test
    public void testAddingMovieDetails() {
        usersMockServer.when(request()
                        .withPath(config.moviesDetailsPath().formatted("10")))
                .respond(response().withBody(jsonMoviesDetail()));
        var requestParticipant = new MovieInfoForSalesNotificationRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel();
        var params = new HashMap<String, Object>();
        params.put("userId", 1L);
        params.put(MOVIE_ID, 10L);
        requestParticipant.contributeTo(viewModel, params);

        assertEquals(2, params.size());
        assertEquals(1, viewModel.size());
        assertEquals(10L, viewModel.getFirst().get(MOVIE_ID));
        assertEquals(1L, viewModel.getFirst().get("userId"));
        assertEquals("abc-def-ghi", viewModel.getFirst().get("salesIdentifier"));
        assertEquals("11", viewModel.getFirst().get("total"));
        assertEquals("10", viewModel.getFirst().get("pointsWon"));
        assertEquals("3", ((List) viewModel.getFirst().get("seats")).get(0));
        assertEquals("Tuesday 04/08 20:56", viewModel.getFirst().get("showStartTime"));
        assertEquals("enrique.molinari", viewModel.getFirst().get("username"));
        assertEquals("Enrique Molinari", viewModel.getFirst().get("fullname"));
        assertEquals("enrique.molinari@gmail.com", viewModel.getFirst().get("email"));
        assertEquals("The Movie of the Century", viewModel.getFirst().get("movieName"));
    }

    private List<Map<String, Object>> populateViewModel() {
        var viewModel = new ArrayList<Map<String, Object>>();
        var map = new HashMap<String, Object>();
        map.put("userId", 1L);
        map.put("movieId", 10L);
        map.put("username", "enrique.molinari");
        map.put("fullname", "Enrique Molinari");
        map.put("email", "enrique.molinari@gmail.com");
        map.put("salesIdentifier", "abc-def-ghi");
        map.put("total", "11");
        map.put("pointsWon", "10");
        map.put("seats", List.of("3"));
        map.put("showStartTime", "Tuesday 04/08 20:56");
        viewModel.add(map);
        return viewModel;
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

}
