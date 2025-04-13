package users.participants.in.shows.sale;

import apicomposer.api.EnvValue;
import users.participants.in.AbstractRequestParticipant;

import java.util.List;
import java.util.Map;

public class UserInfoForSalesNotificationRequestParticipant extends AbstractRequestParticipant {
    public static final String USER_ID_MUST_BE_IN_PARAMS = "userId must be in params";
    public static final String USER_ID_KEY = "userId";
    public static final String USERNAME_KEY = "username";
    public static final String FULLNAME_KEY = "fullname";
    public static final String EMAIL_KEY = "email";
    private final InShowSalesConfig showSaleConfig;

    public UserInfoForSalesNotificationRequestParticipant(EnvValue env) {
        this.showSaleConfig = new InShowSalesConfig(env);
    }

    @Override
    protected long httpCallTimeOut() {
        return this.showSaleConfig.httpCallTimeout();
    }

    @Override
    protected boolean isFirst() {
        return false;
    }

    @Override
    protected void preConditions(List<Map<String, Object>> viewModel, Map<String, Object> params) {
        if (!params.containsKey(USER_ID_KEY)) {
            throw new RuntimeException(USER_ID_MUST_BE_IN_PARAMS);
        }
    }

    @Override
    protected boolean interestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
        return requestPath.startsWith(showSaleConfig.showSalesParticipantPath())
                && "GET".equals(requestMethod);
    }

    @Override
    protected String url(Map<String, Object> params) {
        return showSaleConfig.usersHost()
                + ":"
                + showSaleConfig.usersPort()
                + showSaleConfig.usersProfilePath();
    }

    @Override
    protected void onExceptionDo(List<Map<String, Object>> viewModel, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void addReponseMapToViewModelAndParams(List<Map<String, Object>> viewModel, List<Map<String, Object>> responseMap, Map<String, Object> params) {
        viewModel.getFirst().put(USERNAME_KEY, responseMap.getFirst().get(USERNAME_KEY));
        viewModel.getFirst().put(FULLNAME_KEY, responseMap.getFirst().get(FULLNAME_KEY));
        viewModel.getFirst().put(EMAIL_KEY, responseMap.getFirst().get(EMAIL_KEY));
    }
}
