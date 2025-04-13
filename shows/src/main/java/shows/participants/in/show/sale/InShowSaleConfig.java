package shows.participants.in.show.sale;

import apicomposer.api.EnvValue;
import shows.config.GeneralConfig;

public class InShowSaleConfig extends GeneralConfig {

    public static final String SHOWS_SALE_PATH = "shows.sale.path";
    static final String SHOWS_SALE_ID_PARAM_NAME = "shows.sale.id.param.name";
    static final String SHOW_SALE_PARTICIPATE = "show.sale.participate";

    public InShowSaleConfig(EnvValue envValue) {
        super(envValue);
    }

    public InShowSaleConfig(String envValue) {
        super(envValue);
    }

    public String salesIdsParamName() {
        return properties.getProperty(SHOWS_SALE_ID_PARAM_NAME);
    }

    public String showsSalePathParticipate() {
        return properties.getProperty(SHOW_SALE_PARTICIPATE);
    }

    public String showsSalePath() {
        return properties.getProperty(SHOWS_SALE_PATH);
    }
}
