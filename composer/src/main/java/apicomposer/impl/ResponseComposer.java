package apicomposer.impl;


import apicomposer.api.RequestParticipant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ResponseComposer {
    public static final String ONE_FIRST = "The must be only one participant running as the first one";
    private final List<RequestParticipant> requestParticipant;

    public ResponseComposer(List<RequestParticipant> requestParticipant) {
        this.requestParticipant = requestParticipant;
    }

    // Kind of pipe and filter design.
    // Each contributor adds their data in the ViewModel map.
    // The first contributor might add parameters required
    // by the others contributors in the list.
    public List<Map<String, Object>> composeResponse(String requestPath, String method,
                                                     Map<String, Object> params) {
        var interestedParticipants = findInterestedParticipants(requestPath, method, params);
        checkOnlyOneRequireFirst(interestedParticipants);
        var viewModel = new ArrayList<Map<String, Object>>();
        //contributor that must begin composition
        executeFirstOneIfAny(params, interestedParticipants, viewModel);
        //all the other contributors to the composition. Might be in parrallel.
        executeAllOthers(params, interestedParticipants, viewModel);

        return viewModel;
    }

    private void executeAllOthers(Map<String, Object> params, List<RequestParticipant> interestedParticipants, ArrayList<Map<String, Object>> viewModel) {
        interestedParticipants.stream().filter(rp1 -> !rp1.requireFirst())
                .forEach(rp -> rp.contributeTo(viewModel, params));
    }

    private void executeFirstOneIfAny(Map<String, Object> params, List<RequestParticipant> interestedParticipants, ArrayList<Map<String, Object>> viewModel) {
        interestedParticipants.stream().filter(RequestParticipant::requireFirst)
                .findFirst()
                .ifPresent(f -> f.contributeTo(viewModel, params));
    }

    private List<RequestParticipant> findInterestedParticipants(String requestPath, String method, Map<String, Object> params) {
        return requestParticipant
                .stream()
                .filter(rp -> rp.isInterestedIn(requestPath, method, params))
                .toList();
    }

    private void checkOnlyOneRequireFirst(List<RequestParticipant> interestedParticipants) {
        if (interestedParticipants.stream().filter(RequestParticipant::requireFirst).toList().size() > 1) {
            throw new RuntimeException(ONE_FIRST);
        }
    }
}
