package shows.participants.in.user.profile;

import apicomposer.api.EnvValue;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static shows.participants.in.user.profile.InUserProfileRequestParticipant.*;

public class InUserProfileRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private ClientAndServer showsMockServer;
    private InUserProfileConfig config;

    @BeforeEach
    public void startMockServer() {
        config = new InUserProfileConfig(ENV_VALUE);
        showsMockServer = ClientAndServer
                .startClientAndServer(Integer.valueOf(config.showsPort()));
    }

    @AfterEach
    public void stopMockServer() {
        showsMockServer.stop();
    }

    @Test
    public void compositionOk() {
        showsMockServer.when(request().withHeader(FW_GATEWAY_USER_ID, "1").withPath(config.showsBuyerProfilePath()))
                .respond(response().withBody(jsonBuyerInfo()));
        var inUserParticipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel();
        var params = populateParamsWithUserId(1L);
        inUserParticipant.contributeTo(viewModel, params);
        assertEquals("120", viewModel.getFirst().get(POINTS_KEY));
    }

    @Test
    public void compositionWhenShowsUserProfileIsNotAvailable() {
        //no setup of shows user profile - hence fallsback
        var inUserParticipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel();
        var params = populateParamsWithUserId(1L);
        inUserParticipant.contributeTo(viewModel, params);
        assertEquals(NOT_AVAILABLE, viewModel.getFirst().get(POINTS_KEY));
    }

    private List<Map<String, Object>> populateViewModel() {
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        params.put("userId", 1L);
        var map = new HashMap<String, Object>();
        map.put(config.showsUserIdParamName(), 1L);
        map.put("username", "auser");
        map.put("email", "auser@myemail.com");
        map.put("fullname", "user name surname");
        viewModel.add(map);
        return viewModel;
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void paramsMustHaveUserId(Map params) {
        var inUserPartipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel();
        assertThrows(RuntimeException.class, () -> inUserPartipant.contributeTo(viewModel, params));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void viewModelMustContainUserProfile(List viewModelParam) {
        var inUserPartipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var e = assertThrows(RuntimeException.class, () -> inUserPartipant.contributeTo(viewModelParam, new HashMap<>()));
        assertEquals(USER_ID_IS_NOT_PRESENT_IN_PARAMS, e.getMessage());
    }

    @Test
    public void mustParticipateInComposition() {
        var inUserPartipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = inUserPartipant.isInterestedIn(config.showsBuyerProfilePathParticipate()
                , "GET"
                , Map.of());

        assertTrue(isInterested);
    }

    @Test
    public void mustNotParticipateInComposition() {
        var inUserPartipant = new InUserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        boolean isInterested = inUserPartipant.isInterestedIn("/composed/shows"
                , "GET"
                , Map.of());
        assertFalse(isInterested);
    }


    private String jsonBuyerInfo() {
        return """
                {
                   "userId": 123,
                   "points": "120"
                }
                """;
    }

    private HashMap<String, Object> populateParamsWithUserId(long userId) {
        var params = new HashMap<String, Object>();
        params.put("userId", userId);
        return params;
    }
}