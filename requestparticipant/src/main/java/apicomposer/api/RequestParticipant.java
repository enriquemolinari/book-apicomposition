package apicomposer.api;

import java.util.List;
import java.util.Map;

public interface RequestParticipant {
    void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params);

    boolean requireFirst();

    boolean isInterestedIn(String requestPath,
                           String requestMethod,
                           Map<String, Object> params);
}
