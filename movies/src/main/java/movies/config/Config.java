package movies.config;

import apicomposer.api.EnvValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String CONFIG_FILE_NAME = "movies-%s.properties";
    public static final String MOVIES_SERVER_PORT = "movies.server.port";
    public static final String MOVIES_SERVER_HOST = "movies.server.host";
    public static final String MOVIES_HTTPCALL_TIMEOUT = "movies.httpcall.timeout";
    public static final String MOVIES_IDS_PATH = "movies.ids.path";
    public static final String MOVIES_IDS_PARAM_NAME = "movies.ids.param.name";
    public static final String SHOWS_PATH_PARTICIPATE = "shows.path.participate";
    private final Properties properties = new Properties();
    private final String envValue;

    public Config(EnvValue envValue) {
        checkValidEnvValues(envValue);
        this.envValue = envValue.env();
        loadPropertiesFile();
    }

    public Config(String envValue) {
        checkValidEnvValues(new EnvValue(envValue));
        this.envValue = envValue;
        loadPropertiesFile();
    }

    private void checkValidEnvValues(EnvValue envValue) {
        if (!envValue.env().equals("dev")
                && !envValue.env().equals("default")
                && !envValue.env().equals("test")
                && !envValue.env().equals("prod")) {
            throw new RuntimeException(envValue.env() + " EnvValue not valid");
        }
    }

    public String moviesPort() {
        return properties.getProperty(MOVIES_SERVER_PORT);
    }

    public String moviesHost() {
        return properties.getProperty(MOVIES_SERVER_HOST);
    }

    public int httpCallTimeout() {
        return Integer.parseInt(properties.getProperty(MOVIES_HTTPCALL_TIMEOUT));
    }

    public String moviesByIdsPath() {
        return properties.getProperty(MOVIES_IDS_PATH);
    }

    public String moviesIdsParamName() {
        return properties.getProperty(MOVIES_IDS_PARAM_NAME);
    }

    public String showsPath() {
        return properties.getProperty(SHOWS_PATH_PARTICIPATE);
    }

    private void loadPropertiesFile() {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(formattedConfigFileName())) {
            if (input == null) {
                throw new IllegalArgumentException(formattedConfigFileName() + " not found in classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error trying to load " + CONFIG_FILE_NAME, e);
        }
    }

    private String formattedConfigFileName() {
        return CONFIG_FILE_NAME.formatted(this.envValue);
    }
}
