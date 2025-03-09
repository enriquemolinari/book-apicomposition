package apicomposer.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CacheKeyGenerator {
    private final String userIdParamName;
    private final String movieIdParamName;

    private CacheKeyGenerator(@Value("${composer.userid.param.name}") String userIdParamName,
                              @Value("${composer.movieid.param.name}") String movieIdParamName) {
        this.userIdParamName = userIdParamName;
        this.movieIdParamName = movieIdParamName;
    }

    public String generateKey(String requestPath, String httpMethod, Map<String, Object> paramMap) {
        String paramsString = paramMap.entrySet()
                .stream()
                .filter(p -> p.getValue().equals(movieIdParamName) || p.getValue().equals(userIdParamName))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted()
                .collect(Collectors.joining("&"));

        return httpMethod + "-" + requestPath + (paramsString.isEmpty() ? "" : "?" + paramsString);
    }
}
