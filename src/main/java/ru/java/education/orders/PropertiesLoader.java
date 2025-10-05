package ru.java.education.orders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final String PROPERTIES_FILE = "application.properties";
    private final Properties properties;

    public PropertiesLoader() {
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + PROPERTIES_FILE);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file", e);
        }
        return props;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Создает DatabaseConfig на основе загруженных properties
     */
/*    public DatabaseConfig createDatabaseConfig() {
        return new DatabaseConfig(
            getProperty("db.url"),
            getProperty("db.username"),
            getProperty("db.password")
        );
    }*/
}
