package shows.participants;

import apicomposer.api.EnvValue;
import apicomposer.api.RequestParticipant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import shows.config.Config;

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

import static java.net.http.HttpClient.newHttpClient;

public class AllPlayingShowsRequestParticipant implements RequestParticipant {

    public static final String MOVIE_ID_KEY = "movieId";
    public static final String VIEW_MODEL_MUST_BE_EMPTY = "ViewModel must be empty";
    private final Config config;

    public AllPlayingShowsRequestParticipant(EnvValue env) {
        this.config = new Config(env);
    }

    @Override
    public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        checkViewModelIsEmpty(viewModel);
        try {
            var response = httpCallShows();
            var shows = responseToListofMaps(response);
            viewModel.addAll(shows);
            addMovieIdsToParamsMap(params, shows);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkViewModelIsEmpty(List<Map<String, Object>> viewModel) {
        if (!viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_EMPTY);
        }
    }

    private void addMovieIdsToParamsMap(Map<String, Object> params
            , List<Map<String, Object>> shows) {
        var movieIds = shows
                .stream()
                .map(map -> map.get(MOVIE_ID_KEY)).toList();
        params.put(config.moviesIdsParamName(), movieIds);
    }

    private HttpResponse<String> httpCallShows() throws URISyntaxException, IOException, InterruptedException {
        try (HttpClient httpClient = newHttpClient()) {
            String url = url();
            var req = HttpRequest.newBuilder(new URI(url))
                    .GET()
                    .timeout(Duration.of(config.httpCallTimeout(), ChronoUnit.SECONDS))
                    .build();
            return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        }
    }

    private String url() {
        return config.showsHost() + ":" + config.showsPort() + config.showsPath();
    }

    private List<Map<String, Object>> responseToListofMaps(HttpResponse<String> response) {
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
        return true;
    }

    @Override
    public boolean isInterestedIn(String requestPath,
                                  String requestMethod,
                                  Map<String, Object> params) {
        return config.showsPathParticipate().equals(requestPath)
                && "GET".equals(requestMethod);
    }
}
