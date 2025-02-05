package users.participants.in.rates;

import apicomposer.api.EnvValue;
import users.config.GeneralConfig;

public class InRatesConfig extends GeneralConfig {
    public InRatesConfig(EnvValue envValue) {
        super(envValue);
    }

    public InRatesConfig(String envValue) {
        super(envValue);
    }

    public String usersIdsParamName() {
        return properties.getProperty("users.ids.param.name");
    }

    public String moviesRatePathPattern() {
        return properties.getProperty("movies.rate.pathpattern.participate");
    }

    public String usersIdsPath() {
        return properties.getProperty("users.ids.path");
    }
}
