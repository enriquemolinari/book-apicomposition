package users.participants.in.profile;

import apicomposer.api.EnvValue;
import users.config.GeneralConfig;

public class InProfileConfig extends GeneralConfig {
    public InProfileConfig(EnvValue envValue) {
        super(envValue);
    }

    public InProfileConfig(String envValue) {
        super(envValue);
    }

    public String usersProfilePathParticipate() {
        return properties.getProperty("users.profile.path.participate");
    }

    public String usersProfilePath() {
        return properties.getProperty("users.profile.path");
    }

    public String userIdParamName() {
        return properties.getProperty("users.id.param.name");
    }
}
