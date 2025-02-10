package participants.in.profile;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockserver.integration.ClientAndServer;
import users.participants.in.profile.InProfileConfig;
import users.participants.in.profile.UserProfileRequestParticipant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static users.participants.in.AbstractRequestParticipant.FW_GATEWAY_USER_ID;

public class UserProfileRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private InProfileConfig config;
    private ClientAndServer usersMockServer;
    private EnvValue envValue;

    @BeforeEach
    public void startMockServer() {
        this.envValue = new EnvValue(ENV_VALUE);
        config = new InProfileConfig(ENV_VALUE);
        usersMockServer = ClientAndServer.startClientAndServer(Integer.valueOf(config.usersPort()));
    }

    @AfterEach
    public void stopMockServer() {
        usersMockServer.stop();
    }

    @Test
    public void compositionOk() {
        usersMockServer.when(request().withHeader(FW_GATEWAY_USER_ID, "1").withPath(config.usersProfilePath())).respond(response().withBody(jsonForUsersProfile()));
        var usersProfileParticipant = new UserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        params.put("userId", 1L);
        usersProfileParticipant.contributeTo(viewModel, params);
        var userProfile = viewModel.getFirst();
        assertEquals(1L, userProfile.get("userId"));
        assertEquals("emolinari", userProfile.get("username"));
        assertEquals("enrique.molinari@gmail.com", userProfile.get("email"));
        assertEquals("Enrique Molinari", userProfile.get("fullname"));
    }

    @Test
    public void onErrorThrow() {
        //usersMockServer.when(request().withHeader(FW_GATEWAY_USER_ID, "1").withPath(config.usersProfilePath())).respond(response().withBody(jsonForUsersProfile()));
        //usersMockServer.when(request().withPath(config.usersProfilePath())).respond(response().withBody(jsonForUsersProfile()));
        var usersProfileParticipant = new UserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        params.put("userId", 1L);
        assertThrows(RuntimeException.class, () -> usersProfileParticipant.contributeTo(viewModel, params));
    }


    @Test
    public void mustParticipate() {
        var usersProfileParticipant = new UserProfileRequestParticipant(new EnvValue(ENV_VALUE));
        var params = new HashMap<String, Object>();
        params.put("userId", 1L);
        boolean participantIn = usersProfileParticipant.isInterestedIn(config.usersProfilePathParticipate(), "GET", params);
        assertTrue(participantIn);
    }

    @Test
    public void viewModelMustBeEmpty() {
        var usersProfilePartipant = new UserProfileRequestParticipant(envValue);
        var params = new HashMap<String, Object>();
        params.put("userId", "1");
        assertThrows(RuntimeException.class, () -> usersProfilePartipant.contributeTo(new ArrayList<>(), params));
    }


    @ParameterizedTest
    @NullAndEmptySource
    public void paramsMustHaveUserIds(Map params) {
        var usersProfilePartipant = new UserProfileRequestParticipant(envValue);
        assertThrows(RuntimeException.class, () -> usersProfilePartipant.contributeTo(new ArrayList<>(), params));
    }

    private String jsonForUsersProfile() {
        return """
                {
                        "userId": 1,
                        "fullname": "Enrique Molinari",
                        "username": "emolinari",
                        "email": "enrique.molinari@gmail.com"
                    }""";
    }
}
