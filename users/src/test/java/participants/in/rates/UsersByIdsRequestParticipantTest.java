package participants.in.rates;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockserver.integration.ClientAndServer;
import users.participants.in.rates.InRatesConfig;
import users.participants.in.rates.UsersByIdsRequestParticipant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static users.participants.in.rates.UsersByIdsRequestParticipant.*;

public class UsersByIdsRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private ClientAndServer usersMockServer;
    private InRatesConfig config;
    private EnvValue envValue;

    @BeforeEach
    public void startMockServer() {
        config = new InRatesConfig(ENV_VALUE);
        envValue = new EnvValue(ENV_VALUE);
        usersMockServer = ClientAndServer
                .startClientAndServer(Integer.valueOf(config.usersPort()));
    }

    @AfterEach
    public void stopMockServer() {
        usersMockServer.stop();
    }

    @Test
    public void fallsBackReturn() {
        usersMockServer.when(request()
                        .withPath(config.usersIdsPath().formatted("1,2")))
                .respond(response().withDelay(TimeUnit.SECONDS, 2).withBody(jsonUsersProfile()));
        var participant = new UsersByIdsRequestParticipant(envValue);
        var viewModel = viewModelPopulatedByMovieRates();
        participant.contributeTo(viewModel, Map.of(config.usersIdsParamName(), List.of(1L, 2L)));
        var mapUser1 = viewModel.stream().filter(m -> m.get(USER_ID_KEY).equals(1L)).toList().getFirst();
        var mapUser2 = viewModel.stream().filter(m -> m.get(USER_ID_KEY).equals(2L)).toList().getFirst();
        assertEquals(FALLBACK_VALUE, mapUser1.get(USERNAME_KEY));
        assertEquals(FALLBACK_VALUE, mapUser2.get(USERNAME_KEY));
        assertEquals(4L, mapUser1.get("rateValue"));
        assertEquals(5L, mapUser2.get("rateValue"));
        assertEquals("22-01-2025 12:46", mapUser1.get("ratedInDate"));
        assertEquals("21-01-2025 12:46", mapUser2.get("ratedInDate"));
        assertEquals("comment 1", mapUser1.get("comment"));
        assertEquals("comment 2", mapUser2.get("comment"));
    }

    @Test
    public void compositionOk() {
        usersMockServer.when(request().withPath(config.usersIdsPath().formatted("1,2"))).respond(response().withBody(jsonUsersProfile()));
        var participant = new UsersByIdsRequestParticipant(envValue);
        var viewModel = viewModelPopulatedByMovieRates();
        participant.contributeTo(viewModel, Map.of(config.usersIdsParamName(), List.of(1L, 2L)));
        var mapUser1 = viewModel.stream().filter(m -> m.get(USER_ID_KEY).equals(1L)).toList().getFirst();
        var mapUser2 = viewModel.stream().filter(m -> m.get(USER_ID_KEY).equals(2L)).toList().getFirst();
        assertEquals("emolinari", mapUser1.get(USERNAME_KEY));
        assertEquals("nico", mapUser2.get(USERNAME_KEY));
        assertEquals(4L, mapUser1.get("rateValue"));
        assertEquals(5L, mapUser2.get("rateValue"));
        assertEquals("22-01-2025 12:46", mapUser1.get("ratedInDate"));
        assertEquals("21-01-2025 12:46", mapUser2.get("ratedInDate"));
        assertEquals("comment 1", mapUser1.get("comment"));
        assertEquals("comment 2", mapUser2.get("comment"));
    }

    @Test
    public void paramsMustNotBeEmpty() {
        var usersPartipant = new UsersByIdsRequestParticipant(envValue);
        var viewModel = viewModelPopulatedByMovieRates();
        assertThrows(RuntimeException.class, () -> usersPartipant.contributeTo(viewModel, Map.of(config.usersIdsParamName(), List.of())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void paramsMustHaveUserIds(Map params) {
        var usersPartipant = new UsersByIdsRequestParticipant(envValue);
        var viewModel = viewModelPopulatedByMovieRates();
        assertThrows(RuntimeException.class, () -> usersPartipant.contributeTo(viewModel, params));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void viewModelMustContainMovieRates(List viewModelParam) {
        var usersPartipant = new UsersByIdsRequestParticipant(envValue);
        var e = assertThrows(RuntimeException.class, () -> usersPartipant.contributeTo(viewModelParam, new HashMap<>()));
        assertEquals(VIEW_MODEL_MUST_BE_POPULATED, e.getMessage());
    }

    private ArrayList<Map<String, Object>> viewModelPopulatedByMovieRates() {
        var viewModel = new ArrayList<Map<String, Object>>();
        var map = new HashMap<String, Object>();
        map.put(USER_ID_KEY, 1L);
        map.put("rateValue", 4L);
        map.put("ratedInDate", "22-01-2025 12:46");
        map.put("comment", "comment 1");
        var map2 = new HashMap<String, Object>();
        map2.put(USER_ID_KEY, 2L);
        map2.put("rateValue", 5L);
        map2.put("ratedInDate", "21-01-2025 12:46");
        map2.put("comment", "comment 2");
        viewModel.add(map);
        viewModel.add(map2);
        return viewModel;
    }

    @Test
    public void mustParticipateInComposition() {
        var usersPartipant = new UsersByIdsRequestParticipant(envValue);
        boolean isInterested = usersPartipant.isInterestedIn("/composed/movies/2/rate"
                , "GET"
                , Map.of());

        assertTrue(isInterested);
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
}
