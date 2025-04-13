package shows.participants.in.show.sale;

import apicomposer.api.EnvValue;
import shows.participants.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class ShowSaleByIdRequestParticipant extends AbstractRequestParticipant {

    public static final String MOVIE_ID_NOT_FOUND_IN_RESPONSE = "MovieId not found in response";
    public static final String USER_ID_NOT_FOUND_IN_RESPONSE = "UserId not found in response";
    public static final String SALES_ID_MUST_BE_IN_PARAMS = "SalesId must be in params";
    static final String MOVIE_ID_KEY = "movieId";
    static final String USER_ID_KEY = "userId";
    private final InShowSaleConfig showSaleConfig;

    public ShowSaleByIdRequestParticipant(EnvValue env) {
        this.showSaleConfig = new InShowSaleConfig(env);
    }

    @Override
    protected long httpCallTimeOut() {
        return this.showSaleConfig.httpCallTimeout();
    }

    @Override
    protected boolean isFirst() {
        return true;
    }

    @Override
    protected void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        if (!params.containsKey(showSaleConfig.salesIdsParamName())) {
            throw new RuntimeException(SALES_ID_MUST_BE_IN_PARAMS);
        }
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.startsWith(this.showSaleConfig.showsSalePathParticipate())
                && "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return showSaleConfig.showsHost() + ":" +
                showSaleConfig.showsPort() +
                showSaleConfig
                        .showsSalePath()
                        .formatted(params.get(showSaleConfig.salesIdsParamName()));
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel,
                                                     List<Map<String, Object>> responseMap,
                                                     Map<String, Object> params) {
        viewModel.addAll(responseMap);
        var movieId = responseMap
                .stream()
                .map(map -> map.get(MOVIE_ID_KEY))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(MOVIE_ID_NOT_FOUND_IN_RESPONSE));
        var userId = responseMap
                .stream()
                .map(map -> map.get(MOVIE_ID_KEY))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(USER_ID_NOT_FOUND_IN_RESPONSE));
        params.put(MOVIE_ID_KEY, movieId);
        params.put(USER_ID_KEY, userId);
    }
}
