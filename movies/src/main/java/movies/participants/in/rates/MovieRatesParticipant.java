package movies.participants.in.rates;

import apicomposer.api.EnvValue;
import movies.participants.in.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class MovieRatesParticipant extends AbstractRequestParticipant {

    public static final String MOVIE_ID_KEY = "id";
    public static final String PARAMETER_ID_MUST_BE_PRESENT = "Parameter id must be present";
    public static final String USER_ID_KEY = "userId";
    private final RatesConfig config;

    public MovieRatesParticipant(EnvValue env) {
        this.config = new RatesConfig(env);
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.addAll(responseMap);
        var userIds = responseMap
                .stream()
                .map(map -> map.get(USER_ID_KEY)).toList();
        params.put(config.userIdsParamName(), userIds);
    }

    private void checkMovieIdIsPresent(Map<String, Object> params) {
        if (!params.containsKey(MOVIE_ID_KEY)) {
            throw new RuntimeException(PARAMETER_ID_MUST_BE_PRESENT);
        }
    }

    @Override
    protected long httpCallTimeOut() {
        return config.httpCallTimeout();
    }

    @Override
    protected boolean isFirst() {
        return true;
    }

    @Override
    protected void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        checkMovieIdIsPresent(params);
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.matches(config.moviesRatePathPattern()) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return config.moviesHost()
                + ":"
                + config.moviesPort()
                + config.moviesRatePath().formatted(params.get(MOVIE_ID_KEY));
    }
}
