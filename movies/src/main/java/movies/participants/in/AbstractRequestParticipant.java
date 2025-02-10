package movies.participants.in;

import apicomposer.api.RequestParticipant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

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

public abstract class AbstractRequestParticipant implements RequestParticipant {

    @Override
    public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        preConditions(viewModel, params);
        try {
            var stringResponse = httpCall(params);
            var responseMap = stringResponseToListOfMaps(stringResponse);
            addReponseMapToViewModelAndParams(viewModel, responseMap, params);
        } catch (Throwable e) {
            onExceptionDo(viewModel, e);
        }
    }

    private List<Map<String, Object>> stringResponseToListOfMaps(HttpResponse<String> response) {
        Gson gson = new GsonBuilder()
                //to keep long as long, if not by default double is used
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        String json = response.body();
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public boolean requireFirst() {
        return isFirst();
    }

    @Override
    public boolean isInterestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return interestedIn(requestPath, requestMethod, params);
    }

    private HttpResponse<String> httpCall(Map<String, Object> params)
            throws URISyntaxException, IOException, InterruptedException {
        try (HttpClient httpClient = newHttpClient()) {
            var req = HttpRequest.newBuilder(new URI(url(params)))
                    .GET()
                    .timeout(Duration.of(httpCallTimeOut(), ChronoUnit.SECONDS))
                    .build();
            return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        }
    }

    protected abstract long httpCallTimeOut();

    protected abstract boolean isFirst();

    protected abstract void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params);

    protected abstract boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params);

    protected abstract String url(Map<String, Object> params);

    protected abstract void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e);

    protected abstract void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel
            , List<Map<String, Object>> responseMap
            , Map<String, Object> params);
}

