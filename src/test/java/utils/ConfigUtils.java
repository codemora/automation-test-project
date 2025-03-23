package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtils.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Unable to find config.properties");
                throw new RuntimeException("config.properties not found");
            }
            properties.load(input);
            logger.info("Configuration properties loaded successfully");
        } catch (IOException ex) {
            logger.error("Failed to load configuration properties", ex);
            throw new RuntimeException("Failed to load config.properties", ex);
        }
    }

    public static String getProperty(String key) {
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.isEmpty()) {
            return systemProp;
        }
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property {} not found in config", key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}