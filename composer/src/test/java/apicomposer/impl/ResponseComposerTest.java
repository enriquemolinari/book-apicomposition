package apicomposer.impl;

import apicomposer.api.RequestParticipant;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseComposerTest {

    public static final String A_VALUE = "avalue";
    public static final String SOMETHING_FIRST_KEY = "somethingfirst";

    @Test
    public void requireFirstTrueIsExecutedFirst() {
        var rp1 = createRequestParticipantRunAfterFirst(
                Map.of("k1", "v1"));
        var rp2 = createRequestParticipantRunFirst(
                Map.of("k2", "v2"));
        var composer = new ResponseComposer(List.of(rp1, rp2), null, null);
        HashMap<String, Object> params = new HashMap<>();
        var response = composer.composeResponse("", "", params);
        assertEquals(2, response.size());
        assertEquals(1, response.stream()
                .filter(m -> m.containsKey("k1")).toList().size());
        assertEquals(1, response.stream()
                .filter(m -> m.containsKey("k2")).toList().size());
    }

    @Test
    public void mustBeOnlyOneRequireFirst() {
        var rp1 = createRequestParticipant(true, true,
                Map.of("k1", "v1"));
        var rp2 = createRequestParticipant(true, true,
                Map.of("k2", "v2"));
        var composer = new ResponseComposer(List.of(rp1, rp2), null, null);
        var e = assertThrows(RuntimeException.class,
                () -> composer.composeResponse("",
                        "", new HashMap<>()));
        assertEquals(ResponseComposer.ONE_FIRST, e.getMessage());
    }

    @Test
    public void compositionOk() {
        var rp1 = createRequestParticipant(true, true,
                Map.of("k1", "v1"));
        var rp2 = createRequestParticipant(true, false,
                Map.of("k2", "v2"));
        var rp3 = createRequestParticipant(false, false,
                Map.of("k3", "v3"));
        var rp4 = createRequestParticipant(true, false,
                Map.of("k4", "v4"));
        var composer = new ResponseComposer(List.of(rp1, rp2, rp3, rp4), null, null);
        var response = composer.composeResponse("", "", new HashMap<>());
        assertEquals(3, response.size());
        assertEquals(1, response.stream()
                .filter(m -> m.containsKey("k1")).toList().size());
        assertEquals(1, response.stream()
                .filter(m -> m.containsKey("k2")).toList().size());
        assertEquals(1, response.stream()
                .filter(m -> m.containsKey("k4")).toList().size());
        assertEquals(0, response.stream()
                .filter(m -> m.containsKey("k3")).toList().size());
    }

    private RequestParticipant createRequestParticipantRunFirst(Map<String, Object> map) {
        return new RequestParticipant() {

            @Override
            public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
                viewModel.add(map);
                params.put(SOMETHING_FIRST_KEY, A_VALUE);
            }

            @Override
            public boolean requireFirst() {
                return true;
            }

            @Override
            public boolean isInterestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
                return true;
            }
        };
    }

    private RequestParticipant createRequestParticipantRunAfterFirst(Map<String, Object> map) {
        return new RequestParticipant() {
            public static final String VIEW_MODEL_NOT_POPULATED = "viewModel was not populated by the first participant";
            public static final String PARAMS_NOT_POPULATED = "params was not populated by the first participant";

            @Override
            public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
                if (viewModel.size() != 1) {
                    fail(VIEW_MODEL_NOT_POPULATED);
                }
                if (!viewModel.getFirst().containsKey("k2")) {
                    fail(VIEW_MODEL_NOT_POPULATED);
                }
                if (params.isEmpty()) {
                    fail(PARAMS_NOT_POPULATED);
                }
                if (!params.get("somethingfirst").equals(A_VALUE)) {
                    fail(PARAMS_NOT_POPULATED);
                }
                viewModel.add(map);
            }

            @Override
            public boolean requireFirst() {
                return false;
            }

            @Override
            public boolean isInterestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
                return true;
            }
        };
    }

    private RequestParticipant createRequestParticipant(boolean isInterested,
                                                        boolean isFirst,
                                                        Map<String, Object> map) {
        return new RequestParticipant() {
            @Override
            public void contributeTo(List<Map<String, Object>> viewModel, Map<String, Object> params) {
                viewModel.add(map);
            }

            @Override
            public boolean requireFirst() {
                return isFirst;
            }

            @Override
            public boolean isInterestedIn(String requestPath, String requestMethod, Map<String, Object> params) {
                return isInterested;
            }
        };
    }
}
