package participants.in.shows.sale;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import users.participants.in.shows.sale.InShowSalesConfig;
import users.participants.in.shows.sale.UserInfoForSalesNotificationRequestParticipant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static users.participants.in.AbstractRequestParticipant.FW_GATEWAY_USER_ID;
import static users.participants.in.shows.sale.UserInfoForSalesNotificationRequestParticipant.*;

public class UserInfoForSalesNotificationRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    private ClientAndServer usersMockServer;
    private InShowSalesConfig config;

    @BeforeEach
    public void startMockServer() {
        config = new InShowSalesConfig(ENV_VALUE);
        usersMockServer = ClientAndServer.startClientAndServer(Integer.valueOf(config.usersPort()));
    }

    @AfterEach
    public void stopMockServer() {
        usersMockServer.stop();
    }

    @Test
    public void testAddingUserDetails() {
        usersMockServer.when(request()
                        .withHeader(FW_GATEWAY_USER_ID, "1")
                        .withPath(config.usersProfilePath()))
                .respond(response().withBody(jsonUserProfile()));
        var requestParticipant = new UserInfoForSalesNotificationRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = populateViewModel();
        var params = new HashMap<String, Object>();
        params.put(USER_ID_KEY, 1L);
        requestParticipant.contributeTo(viewModel, params);

        assertEquals(1, params.size());
        assertEquals(1, viewModel.size());
        assertEquals(1L, viewModel.getFirst().get("movieId"));
        assertEquals(1L, viewModel.getFirst().get(USER_ID_KEY));
        assertEquals("abc-def-ghi", viewModel.getFirst().get("salesIdentifier"));
        assertEquals("11", viewModel.getFirst().get("total"));
        assertEquals("10", viewModel.getFirst().get("pointsWon"));
        assertEquals("3", ((List) viewModel.getFirst().get("seats")).get(0));
        assertEquals("Tuesday 04/08 20:56", viewModel.getFirst().get("showStartTime"));
        assertEquals("enrique.molinari", viewModel.getFirst().get(USERNAME_KEY));
        assertEquals("Enrique Molinari", viewModel.getFirst().get(FULLNAME_KEY));
        assertEquals("enrique.molinari@gmail.com", viewModel.getFirst().get(EMAIL_KEY));
    }

    private List<Map<String, Object>> populateViewModel() {
        var viewModel = new ArrayList<Map<String, Object>>();
        var map = new HashMap<String, Object>();
        map.put(USER_ID_KEY, 1L);
        map.put("movieId", 1L);
        map.put("salesIdentifier", "abc-def-ghi");
        map.put("total", "11");
        map.put("pointsWon", "10");
        map.put("seats", List.of("3"));
        map.put("showStartTime", "Tuesday 04/08 20:56");
        viewModel.add(map);
        return viewModel;
    }

    private String jsonUserProfile() {
        return """
                {
                   "userId": 1,
                   "fullname": "Enrique Molinari",
                   "username": "enrique.molinari",
                   "email": "enrique.molinari@gmail.com"
                }
                """;
    }

}
