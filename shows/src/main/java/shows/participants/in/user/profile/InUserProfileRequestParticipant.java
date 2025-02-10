package shows.participants.in.user.profile;

import apicomposer.api.EnvValue;
import shows.participants.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class InUserProfileRequestParticipant extends AbstractRequestParticipant {

    public static final String POINTS_KEY = "points";
    public static final String USER_ID_IS_NOT_PRESENT_IN_PARAMS = "userId is not present in params";
    public static final String VIEW_MODEL_MUST_BE_POPULATED = "ViewModel must be populated";
    public static final String NOT_AVAILABLE = "NOT AVAILABLE";
    private final InUserProfileConfig config;

    public InUserProfileRequestParticipant(EnvValue env) {
        this.config = new InUserProfileConfig(env);
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
        if (!params.containsKey(config.showsUserIdParamName())) {
            throw new RuntimeException(USER_ID_IS_NOT_PRESENT_IN_PARAMS);
        }
        if (viewModel == null) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        if (viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_POPULATED);
        }
        var userId = viewModel.getFirst().get(config.showsUserIdParamName());
        if (userId == null) {
            throw new RuntimeException(USER_ID_IS_NOT_PRESENT_IN_PARAMS);
        }
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.matches(config.showsBuyerProfilePathParticipate()) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return config.showsHost()
                + ":"
                + config.showsPort()
                + config.showsBuyerProfilePath();
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        viewModel.getFirst().put(POINTS_KEY, NOT_AVAILABLE);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.getFirst().put(POINTS_KEY, responseMap.getFirst().get(POINTS_KEY));
    }
}
