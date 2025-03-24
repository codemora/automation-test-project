package api.services;

import api.utils.RestAssuredUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigUtils;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String CURRENT_WEATHER_ENDPOINT = ConfigUtils.getProperty("weather.current.endpoint");
    private static final String FORECAST_WEATHER_ENDPOINT = ConfigUtils.getProperty("weather.forecast.endpoint");
    private static final String BASE_URL = ConfigUtils.getProperty("api.base.url");

    public static Response getCurrentWeather(String city, String apiKey) {
        try {
            RequestSpecification requestSpec = RestAssuredUtils.getRequestSpec();
            String logMessage = "Requesting current weather for city: " + city;

            if (apiKey != null) {
                requestSpec = RestAssuredUtils.getRequestSpecWithAuth(apiKey);
                logMessage += " using API Key";
            }

            logger.info(logMessage);

            Response response = RestAssured.given()
                    .spec(requestSpec)
                    .queryParam("q", city)
                    .when()
                    .get(CURRENT_WEATHER_ENDPOINT);

            logger.info("Response: Status Code = {}, Body = {}", response.getStatusCode(), response.asString());
            return response;

        } catch (Exception e) {
            logger.error("Failed to make GET request to {}{} for city: {}",
                    BASE_URL, CURRENT_WEATHER_ENDPOINT, city, e);
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    public static Response getForecastWeatherFor5Days(String city, String apiKey) {
        try {
            RequestSpecification requestSpec = RestAssuredUtils.getRequestSpec();
            String logMessage = "Requesting 5 forecast weather for city: " + city;

            if (apiKey != null) {
                requestSpec = RestAssuredUtils.getRequestSpecWithAuth(apiKey);
                logMessage += " using API Key";
            }

            logger.info(logMessage);

            Response response = RestAssured.given()
                    .spec(requestSpec)
                    .queryParam("q", city)
                    .when()
                    .get(FORECAST_WEATHER_ENDPOINT)
                    .then()
                    .extract()
                    .response();

            logger.info("Response: Status Code = {}, Body = {}", response.getStatusCode(), response.asString());
            return response;
        } catch (Exception e) {
            logger.error("Failed to make GET request to {}{} for city: {}",
                    BASE_URL, FORECAST_WEATHER_ENDPOINT, city, e);
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
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
}
