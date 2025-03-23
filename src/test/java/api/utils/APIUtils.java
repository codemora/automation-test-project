package api.utils;

import io.restassured.response.Response;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigUtils;

import java.util.List;

import static io.restassured.RestAssured.given;

public class APIUtils {
    private static final Logger logger = LoggerFactory.getLogger(APIUtils.class);
    private static final String BASE_URL = ConfigUtils.getProperty("api.base.url");
    private static final String API_KEY = ConfigUtils.getProperty("api.key");

    public static Response getRequest(String endpoint, String city, boolean useApiKey) {
        try {
            Response response = given()
                    .queryParam("q", city)
                    .queryParam("appid", useApiKey ? API_KEY : "invalid_key")
                    .when()
                    .get(BASE_URL + endpoint);
            logger.info("GET request sent to {}{} for city: {}", BASE_URL, endpoint, city);
            return response;
        } catch (Exception e) {
            logger.error("Failed to make GET request to {}{} for city: {}",
                    BASE_URL, endpoint, city, e);
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    public static void validateApiKey() {
        if (API_KEY == null || API_KEY.equals("YOUR_API_KEY_HERE")) {
            logger.error("API key not configured properly in config.properties");
            throw new IllegalStateException("API key not configured");
        }
        logger.debug("API key validated successfully");
    }

    public static double extractTemperature(Response response) {
        try {
            double temp = response.jsonPath().getDouble("main.temp");
            logger.debug("Extracted temperature: {}", temp);
            return temp;
        } catch (Exception e) {
            logger.error("Failed to extract temperature from response", e);
            throw new RuntimeException("Temperature extraction failed: " + e.getMessage(), e);
        }
    }

    public static double extractForecastTemperature(Response response) {
        try {
            double temp = response.jsonPath().getDouble("list[0].main.temp");
            logger.debug("Extracted forecast temperature: {}", temp);
            return temp;
        } catch (Exception e) {
            logger.error("Failed to extract forecast temperature from response", e);
            throw new RuntimeException("Forecast temperature extraction failed: " + e.getMessage(), e);
        }
    }

    public static void validateWeatherResponse(Response response) {
        Assert.assertNotNull("Weather response missing temperature",
                response.jsonPath().get("main.temp"));
        List<?> weatherList = response.jsonPath().getList("weather");
        Assert.assertFalse("Weather response missing weather array or empty",
                weatherList.isEmpty());
        Assert.assertNotNull("Weather response missing city name",
                response.jsonPath().get("name"));
        logger.debug("Weather response structure validated");
    }

    public static void validateForecastResponse(Response response) {
        List<?> forecastList = response.jsonPath().getList("list");
        Assert.assertFalse("Forecast response missing list or empty",
                forecastList.isEmpty());
        Assert.assertNotNull("Forecast response missing temperature for first entry",
                response.jsonPath().get("list[0].main.temp"));
        logger.debug("Forecast response structure validated");
    }
}