package apicomposer.framework;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component("springCacheKeyGenerator")
public class SpringCacheKeyGenerator implements KeyGenerator {
    private final CacheKeyGenerator keyGenerator;

    public SpringCacheKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        checkParameters(params);

        String requestPath = (String) params[0];
        String httpMethod = (String) params[1];
        Map<String, Object> paramMap = (Map<String, Object>) params[2];

        return keyGenerator.generateKey(requestPath, httpMethod, paramMap);
    }

    private void checkParameters(Object[] params) {
        if (params.length < 3 || !(params[0] instanceof String) || !(params[1] instanceof String) || !(params[2] instanceof Map)) {
            throw new IllegalArgumentException("Invalid parameters for cache key generation");
        }
    }
}
