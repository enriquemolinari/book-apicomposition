package movies.participants;

import apicomposer.api.EnvValue;
import apicomposer.api.RequestParticipant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import movies.config.Config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.http.HttpClient.newHttpClient;

public class MoviesByIDsRequestParticipant implements RequestParticipant {

    public static final String MOVIE_ID = "id";
    public static final String MOVIE_NAME_KEY = "name";
    public static final String MOVIE_DURATION_KEY = "duration";
    public static final String MOVIE_GENRES_KEY = "genres";
    public static final String MOVIE_ID_KEY = "movieId";
    public static final String FALLBACK_VALUE = "NOT AVAILABLE";
    public static final String VIEW_MODEL_MUST_BE_PUPULATED = "ViewModel must be pupulated";
    private final Config config;

    public MoviesByIDsRequestParticipant(EnvValue env) {
        this.config = new Config(env);
    }

    @Override
    public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        checkViewModelIsAlreadyPopulated(viewModel);
        checkParamsContainsMoviesIds(params);
        try {
            var response = httpCallMoviesByIDs(params);
            List<Map<String, Object>> movies = responseToListOfMaps(response);
            for (var movie : movies) {
                populateWithMovieInfo(viewModel, movie);
            }
        } catch (Throwable e) {
            // It is crucial to raise a FATAL error in the frequently queried log stream here,
            // ensuring that operations are promptly notified.
            populateWithFallbackData(viewModel);
        }
    }

    private List<Map<String, Object>> responseToListOfMaps(HttpResponse<String> response) {
        Gson gson = new GsonBuilder()
                //to keep long as long, if not by default double is used
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(response.body(), type);
    }

    @Override
    public boolean requireFirst() {
        return false;
    }

    @Override
    public boolean isInterestedIn(String requestPath
            , String requestMethod
            , Map<String, Object> params) {
        return config.showsPath().equals(requestPath) &&
                "GET".equals(requestMethod);
    }

    private HttpResponse<String> httpCallMoviesByIDs(Map<String, Object> params)
            throws URISyntaxException, IOException, InterruptedException {
        try (HttpClient httpClient = newHttpClient()) {
            var req = HttpRequest.newBuilder(buildUri(params))
                    .GET()
                    .timeout(Duration.of(config.httpCallTimeout(), ChronoUnit.SECONDS))
                    .build();
            return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
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
            throw new RuntimeException(VIEW_MODEL_MUST_BE_PUPULATED);
        }
        if (viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_PUPULATED);
        }
        var movie = viewModel.getFirst().get(MOVIE_ID_KEY);
        if (movie == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_PUPULATED);
        }
    }

    private URI buildUri(Map<String, Object> params) throws URISyntaxException {
        return new URI(this.Url()
                .formatted(toCommaSeparated((List<Long>) params.get(config.moviesIdsParamName()))));
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
