package movies.participants.in.shows;

import apicomposer.api.EnvValue;
import movies.config.GeneralConfig;

public class InShowsConfig extends GeneralConfig {
    public static final String MOVIES_IDS_PATH = "movies.ids.path";
    public static final String MOVIES_IDS_PARAM_NAME = "movies.ids.param.name";
    public static final String SHOWS_PATH_PARTICIPATE = "shows.path.participate";

    public InShowsConfig(EnvValue envValue) {
        super(envValue);
    }

    public InShowsConfig(String envValue) {
        super(envValue);
    }

    public String moviesByIdsPath() {
        return properties.getProperty(InShowsConfig.MOVIES_IDS_PATH);
    }

    public String moviesIdsParamName() {
        return properties.getProperty(InShowsConfig.MOVIES_IDS_PARAM_NAME);
    }

    public String showsPath() {
        return properties.getProperty(InShowsConfig.SHOWS_PATH_PARTICIPATE);
    }
}
