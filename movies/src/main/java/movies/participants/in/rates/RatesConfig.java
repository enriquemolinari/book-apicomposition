package movies.participants.in.rates;

import apicomposer.api.EnvValue;
import movies.config.GeneralConfig;

public class RatesConfig extends GeneralConfig {
    public RatesConfig(EnvValue envValue) {
        super(envValue);
    }

    public RatesConfig(String envValue) {
        super(envValue);
    }

    public String moviesRatePathPattern() {
        return properties.getProperty("movies.rate.pathpattern.participate");
    }

    public String moviesRatePath() {
        return properties.getProperty("movies.rate.path");
    }

    public String userIdsParamName() {
        return properties.getProperty("movies.rate.userids.param");
    }
}
