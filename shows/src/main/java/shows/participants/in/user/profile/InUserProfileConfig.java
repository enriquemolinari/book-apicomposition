package shows.participants.in.user.profile;

import apicomposer.api.EnvValue;
import shows.config.GeneralConfig;

public class InUserProfileConfig extends GeneralConfig {
    public InUserProfileConfig(EnvValue envValue) {
        super(envValue);
    }

    public InUserProfileConfig(String envValue) {
        super(envValue);
    }

    public String showsBuyerProfilePath() {
        return properties.getProperty("shows.buyer.profile.path");
    }

    public String showsBuyerProfilePathParticipate() {
        return properties.getProperty("shows.buyer.profile.path.participate");
    }

    public String showsUserIdParamName() {
        return properties.getProperty("shows.users.id.param.name");
    }

}
