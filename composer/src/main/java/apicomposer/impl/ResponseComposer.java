package apicomposer.impl;

import apicomposer.api.RequestParticipant;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ResponseComposer {
    public static final String ONE_FIRST = "The must be only one participant running as the first one";
    public static final String CACHE_NAME_COMPOSER = "composedResponse";
    public static final String CIRCUIT_BREAKER_NAME = "composerCircuitBreaker";
    public static final String FALLBACK_METHOD_NAME = "fallback";
    public static final String FALLBACK_CACHE_NOT_YET_POPULATED = "Fallback Cache not yet populated";
    final Logger logger = LoggerFactory.getLogger(ResponseComposer.class);
    private final CacheManager cacheManager;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final List<RequestParticipant> requestParticipant;

    public ResponseComposer(List<RequestParticipant> requestParticipant,
                            CacheManager cacheManager,
                            CacheKeyGenerator cacheKeyGenerator) {
        this.requestParticipant = requestParticipant;
        this.cacheManager = cacheManager;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    // Kind of pipe and filter design.
    // Each contributor adds their data in the ViewModel map.
    // The first contributor might add parameters required
    // by the others contributors in the list.
    @CachePut(cacheNames = CACHE_NAME_COMPOSER, keyGenerator = "springCacheKeyGenerator")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = FALLBACK_METHOD_NAME)
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

    private List<Map<String, Object>> fallback(String requestPath, String method,
                                               Map<String, Object> params, Throwable e) {
        //Stream error to operations log
        logger.error("circuit breaker calls to fallback method in request: {} {} {}", requestPath, method, params, e);

        var cache = this.cacheManager.getCache(CACHE_NAME_COMPOSER);
        Objects.requireNonNull(cache, FALLBACK_CACHE_NOT_YET_POPULATED);
        String key = cacheKeyGenerator.generateKey(requestPath, method, params);
        var cacheKey = cache.get(key);
        Objects.requireNonNull(cacheKey, FALLBACK_CACHE_NOT_YET_POPULATED);
        return (List<Map<String, Object>>) cacheKey.get();
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
