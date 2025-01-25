package apicomposer.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record EnvValue(
        @Value("${spring.profiles.active:default}") String env) {
}
