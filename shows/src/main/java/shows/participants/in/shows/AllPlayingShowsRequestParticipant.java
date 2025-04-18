package shows.participants.in.shows;

import apicomposer.api.EnvValue;
import shows.participants.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class AllPlayingShowsRequestParticipant extends AbstractRequestParticipant {

    public static final String MOVIE_ID_KEY = "movieId";
    public static final String VIEW_MODEL_MUST_BE_EMPTY = "ViewModel must be empty";
    private final InShowsConfig inShowsConfig;

    public AllPlayingShowsRequestParticipant(EnvValue env) {
        this.inShowsConfig = new InShowsConfig(env);
    }

    @Override
    protected long httpCallTimeOut() {
        return inShowsConfig.httpCallTimeout();
    }

    @Override
    protected boolean isFirst() {
        return true;
    }

    @Override
    protected void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        checkViewModelIsEmpty(viewModel);
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return inShowsConfig.showsPathParticipate().equals(requestPath)
                && "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return inShowsConfig.showsHost() + ":" + inShowsConfig.showsPort() + inShowsConfig.showsPath();
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.addAll(responseMap);
        var movieIds = responseMap
                .stream()
                .map(map -> map.get(MOVIE_ID_KEY)).toList();
        params.put(inShowsConfig.moviesIdsParamName(), movieIds);
    }

    private void checkViewModelIsEmpty(List<Map<String, Object>> viewModel) {
        if (!viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_EMPTY);
        }
    }
}
