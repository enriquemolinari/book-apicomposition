package users.participants.in.shows.sale;

import apicomposer.api.EnvValue;
import users.config.GeneralConfig;

public class InShowSalesConfig extends GeneralConfig {

    public static final String USERS_PROFILE_PATH = "users.profile.path";
    public static final String SHOW_SALE_PARTICIPATE = "show.sale.participate";

    public InShowSalesConfig(EnvValue envValue) {
        super(envValue);
    }

    public InShowSalesConfig(String envValue) {
        super(envValue);
    }

    public String usersProfilePath() {
        return properties.getProperty(USERS_PROFILE_PATH);
    }

    public String showSalesParticipantPath() {
        return properties.getProperty(SHOW_SALE_PARTICIPATE);
    }
}
