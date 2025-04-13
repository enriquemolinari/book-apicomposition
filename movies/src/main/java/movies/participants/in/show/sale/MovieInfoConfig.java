package movies.participants.in.show.sale;

import apicomposer.api.EnvValue;
import movies.config.GeneralConfig;

public class MovieInfoConfig extends GeneralConfig {
    public MovieInfoConfig(EnvValue envValue) {
        super(envValue);
    }

    public MovieInfoConfig(String envValue) {
        super(envValue);
    }

    public String showSalesParticipatePath() {
        return properties.getProperty("shows.sales.path.participate");
    }

    public String moviesDetailsPath() {
        return properties.getProperty("movies.detail.path");
    }
}
