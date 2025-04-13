package shows.participants.in.show.sale;

import apicomposer.api.EnvValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ShowSaleByIdRequestParticipantTest {
    public static final String ENV_VALUE = "default";
    public static final String SALES_ID = "7f1ffef0-f0d4-4099-a368-d5e76c652a8b";
    private ClientAndServer showsMockServer;
    private InShowSaleConfig inShowsSaleConfig;

    @BeforeEach
    public void startMockServer() {
        inShowsSaleConfig = new InShowSaleConfig(ENV_VALUE);
        showsMockServer = ClientAndServer.startClientAndServer(Integer.valueOf(inShowsSaleConfig.showsPort()));
    }

    @AfterEach
    public void stopMockServer() {
        showsMockServer.stop();
    }

    @Test
    public void testSalesRequestOk() {
        showsMockServer.when(request().withPath(inShowsSaleConfig.showsSalePath().formatted(SALES_ID))).respond(response().withBody(jsonForShowSaleOk()));
        var showsSalesMainParticipant = new ShowSaleByIdRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        params.put(inShowsSaleConfig.salesIdsParamName(), SALES_ID);
        showsSalesMainParticipant.contributeTo(viewModel, params);

        assertEquals(3, params.size());
        assertEquals(1, viewModel.size());
        assertEquals(2L, params.get(ShowSaleByIdRequestParticipant.MOVIE_ID_KEY));
        assertEquals(2L, params.get(ShowSaleByIdRequestParticipant.USER_ID_KEY));
        assertEquals("7f1ffef0-f0d4-4099-a368-d5e76c652a8b", viewModel.getFirst().get("salesIdentifier"));
        assertEquals(10D, viewModel.getFirst().get("total"));
        assertEquals(10L, viewModel.getFirst().get("pointsWon"));
        assertEquals(3L, ((List) viewModel.getFirst().get("seats")).get(0));
        assertEquals("Tuesday 04/08 20:56", viewModel.getFirst().get("showStartTime"));
    }

    @Test
    public void testSalesIdMustBePresent() {
        showsMockServer.when(request().withPath(inShowsSaleConfig.showsSalePath().formatted(SALES_ID))).respond(response().withBody(jsonForShowSaleOk()));
        var showsSalesMainParticipant = new ShowSaleByIdRequestParticipant(new EnvValue(ENV_VALUE));
        var viewModel = new ArrayList<Map<String, Object>>();
        var params = new HashMap<String, Object>();
        var e = assertThrows(RuntimeException.class, () -> showsSalesMainParticipant.contributeTo(viewModel, params));
        assertEquals(ShowSaleByIdRequestParticipant.SALES_ID_MUST_BE_IN_PARAMS, e.getMessage());
        assertEquals(0, viewModel.size());
    }

    private String jsonForShowSaleOk() {
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

}

