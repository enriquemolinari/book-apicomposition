package users.participants.in.profile;

import apicomposer.api.EnvValue;
import users.participants.in.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class UserProfileRequestParticipant extends AbstractRequestParticipant {
    public static final String VIEW_MODEL_MUST_BE_EMPTY = "ViewModel must be empty";
    public static final String USER_ID_MUST_BE_IN_PARAMS = "UserId must be in params";
    private final InProfileConfig config;

    public UserProfileRequestParticipant(EnvValue envValue) {
        this.config = new InProfileConfig(envValue);
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
        checkViewModelIsEmpty(viewModel);
        checkUserIdIsInParams(params);
    }

    private void checkViewModelIsEmpty(List<Map<String, Object>> viewModel) {
        if (!viewModel.isEmpty()) {
            throw new RuntimeException(VIEW_MODEL_MUST_BE_EMPTY);
        }
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.matches(config.usersProfilePathParticipate()) &&
                "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return config.usersHost()
                + ":"
                + config.usersPort()
                + config.usersProfilePath();
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.addAll(responseMap);
    }

    private void checkUserIdIsInParams(Map<String, Object> params) {
        if (!params.containsKey(config.userIdParamName())) {
            throw new RuntimeException(USER_ID_MUST_BE_IN_PARAMS);
        }
    }
}
