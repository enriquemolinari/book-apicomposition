package users.participants.in.rates;

import apicomposer.api.EnvValue;
import users.participants.in.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsersByIdsRequestParticipant extends AbstractRequestParticipant {
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String FALLBACK_VALUE = "NOT AVAILABLE";
    public static final String VIEW_MODEL_MUST_BE_POPULATED = "ViewModel must be populated with movie rates";
    private final InRatesConfig config;

    public UsersByIdsRequestParticipant(EnvValue config) {
        this.config = new InRatesConfig(config);
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
        checkParamsContainsUserIds(params);
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.matches(config.moviesRatePathPattern()) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return config.usersHost()
                + ":"
                + config.usersPort()
                + config.usersIdsPath().formatted(toCommaSeparated((List<Long>) params.get(config.usersIdsParamName())));
    }

    private String toCommaSeparated(List<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        for (var rates : viewModel) {
            rates.put(USERNAME_KEY, FALLBACK_VALUE);
        }
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel,
                                                     List<Map<String, Object>> responseMap,
                                                     Map<String, Object> params) {
        for (var usersProfile : responseMap) {
            var movieMap = viewModel.stream()
                    .filter(m -> m.get(USER_ID_KEY).equals(usersProfile.get(USER_ID_KEY))).findFirst();
            movieMap.ifPresent(m -> m.put(USERNAME_KEY, usersProfile.get(USERNAME_KEY)));
        }
    }

    private void checkViewModelIsAlreadyPopulated(List<Map<String, Object>> viewModel) {
        if (viewModel == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        if (viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        var movie = viewModel.getFirst().get(USER_ID_KEY);
        if (movie == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
    }

    private void checkParamsContainsUserIds(Map<String, Object> params) {
        var list = (List<?>) params.get(config.usersIdsParamName());
        if (list == null) {
            throw new RuntimeException(config.usersIdsParamName() + " param is required");
        }
        if (list.isEmpty()) {
            throw new RuntimeException(config.usersIdsParamName() + " param is required");
        }
    }

}
