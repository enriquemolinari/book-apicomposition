package shows.participants.in.shows;

import apicomposer.api.EnvValue;
import shows.config.GeneralConfig;

public class InShowsConfig extends GeneralConfig {

    public InShowsConfig(EnvValue envValue) {
        super(envValue);
    }

    public InShowsConfig(String envValue) {
        super(envValue);
    }

    public String moviesIdsParamName() {
        return properties.getProperty(MOVIES_IDS_PARAM_NAME);
    }

    public String showsPathParticipate() {
        return properties.getProperty(SHOWS_PATH_PARTICIPATE);
    }

    public String showsPath() {
        return properties.getProperty(SHOWS_PATH);
    }
}
