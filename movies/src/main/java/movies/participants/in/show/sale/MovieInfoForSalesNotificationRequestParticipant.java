package movies.participants.in.show.sale;

import apicomposer.api.EnvValue;
import movies.participants.in.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class MovieInfoForSalesNotificationRequestParticipant extends AbstractRequestParticipant {
    public static final String MOVIE_ID = "movieId";
    public static final String MOVIE_ID_MUST_BE_IN_PARAMS = "movieId must be in params";
    public static final String COMPOSED_MOVIE_NAME_KEY = "movieName";
    public static final String MOVIE_NAME_KEY = "name";
    private final MovieInfoConfig config;

    public MovieInfoForSalesNotificationRequestParticipant(EnvValue env) {
        this.config = new MovieInfoConfig(env);
    }

    @Override
    protected long httpCallTimeOut() {
        return config.httpCallTimeout();
    }

    @Override
    protected boolean isFirst() {
        return false;
    }

    @Override
    protected void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        if (!params.containsKey(MOVIE_ID)) {
            throw new RuntimeException(MOVIE_ID_MUST_BE_IN_PARAMS);
        }
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.startsWith(config.showSalesParticipatePath()) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return config.moviesHost() +
                ":" +
                config.moviesPort() +
                config.moviesDetailsPath().formatted(params.get(MOVIE_ID));
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.getFirst().put(COMPOSED_MOVIE_NAME_KEY, responseMap.getFirst().get(MOVIE_NAME_KEY));
    }
}
