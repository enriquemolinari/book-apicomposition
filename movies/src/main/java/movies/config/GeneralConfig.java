package movies.config;

import apicomposer.api.EnvValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GeneralConfig {
    public static final String CONFIG_FILE_NAME = "movies-%s.properties";
    public static final String MOVIES_SERVER_PORT = "movies.server.port";
    public static final String MOVIES_SERVER_HOST = "movies.server.host";
    public static final String MOVIES_HTTPCALL_TIMEOUT = "movies.httpcall.timeout";
    //TODO: make private
    protected final Properties properties = new Properties();
    private final String envValue;

    public GeneralConfig(EnvValue envValue) {
        checkValidEnvValues(envValue);
        this.envValue = envValue.env();
        loadPropertiesFile();
    }

    public GeneralConfig(String envValue) {
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
