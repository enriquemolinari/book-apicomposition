package apicomposer.main;

import apicomposer.api.RequestParticipant;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = {"*.participants"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = RequestParticipant.class
        )
)
public class ParticipantConfiguration {
}
