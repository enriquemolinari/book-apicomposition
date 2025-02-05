package movies.participants.in.shows;

import apicomposer.api.EnvValue;
import movies.participants.in.AbstractRequestParticipant;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviesByIDsRequestParticipant extends AbstractRequestParticipant {

    public static final String MOVIE_ID = "id";
    public static final String MOVIE_NAME_KEY = "name";
    public static final String MOVIE_DURATION_KEY = "duration";
    public static final String MOVIE_GENRES_KEY = "genres";
    public static final String MOVIE_ID_KEY = "movieId";
    public static final String FALLBACK_VALUE = "NOT AVAILABLE";
    public static final String VIEW_MODEL_MUST_BE_POPULATED = "ViewModel must be populated";
    private final InShowsConfig config;

    public MoviesByIDsRequestParticipant(EnvValue env) {
        this.config = new InShowsConfig(env);
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
        checkViewModelIsAlreadyPopulated(viewModel);
        checkParamsContainsMoviesIds(params);
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return config.showsPath().equals(requestPath) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        try {
            return buildUri(params);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        populateWithFallbackData(viewModel);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel,
                                                     List<Map<String, Object>> responseMap,
                                                     Map<String, Object> params) {
        for (var movie : responseMap) {
            populateWithMovieInfo(viewModel, movie);
        }
    }

    private String toCommaSeparated(List<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private void checkParamsContainsMoviesIds(Map<String, Object> params) {
        var list = (List<?>) params.get(config.moviesIdsParamName());
        if (list == null) {
            throw new RuntimeException(config.moviesIdsParamName() + " param is required");
        }
        if (list.isEmpty()) {
            throw new RuntimeException(config.moviesIdsParamName() + " param is required");
        }
    }

    private void checkViewModelIsAlreadyPopulated(List<Map<String, Object>> viewModel) {
        if (viewModel == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        if (viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        var movie = viewModel.getFirst().get(MOVIE_ID_KEY);
        if (movie == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
    }

    private String buildUri(Map<String, Object> params) throws URISyntaxException {
        return this.Url()
                .formatted(toCommaSeparated((List<Long>) params.get(config.moviesIdsParamName())));
    }

    private String Url() {
        return config.moviesHost() + ":" + config.moviesPort() + config.moviesByIdsPath();
    }

    private void populateWithMovieInfo(List<Map<String, Object>> viewModel,
                                       Map<String, Object> movie) {
        var movieMap = viewModel.stream()
                .filter(m -> m.get(MOVIE_ID_KEY).equals(movie.get(MOVIE_ID))).findFirst();
        movieMap.ifPresent(m -> m.put(MOVIE_NAME_KEY, movie.get(MOVIE_NAME_KEY)));
        movieMap.ifPresent(m -> m.put(MOVIE_DURATION_KEY, movie.get(MOVIE_DURATION_KEY)));
        movieMap.ifPresent(m -> m.put(MOVIE_GENRES_KEY, movie.get(MOVIE_GENRES_KEY)));
    }

    private void populateWithFallbackData(List<Map<String, Object>> viewModel) {
        for (var movie : viewModel) {
            movie.put(MOVIE_NAME_KEY, FALLBACK_VALUE);
            movie.put(MOVIE_DURATION_KEY, FALLBACK_VALUE);
            movie.put(MOVIE_GENRES_KEY, List.of(FALLBACK_VALUE));
        }
    }
}
