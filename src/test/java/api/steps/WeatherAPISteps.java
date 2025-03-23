package api.steps;

import io.cucumber.java.en.*;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import api.utils.APIUtils;

public class WeatherAPISteps {
    private static final Logger logger = LoggerFactory.getLogger(WeatherAPISteps.class);
    private Response response;
    private double currentTemp;

    @Given("I have a valid API key")
    public void setApiKey() {
        APIUtils.validateApiKey();
        logger.info("API Key initialized");
    }

    @When("I request current weather for {string}")
    @Step("Requesting weather for city: {0}")
    public void requestCurrentWeather(String city) {
        response = APIUtils.getRequest("/weather", city, true);
        logger.info("Weather request sent for city: {}", city);
    }

    @When("I request 5-day forecast for {string}")
    @Step("Requesting 5-day forecast for city: {0}")
    public void requestForecast(String city) {
        response = APIUtils.getRequest("/forecast", city, true);
        logger.info("Forecast request sent for city: {}", city);
    }

    @When("I request weather with invalid API key")
    @Step("Requesting weather with invalid API key")
    public void requestWithInvalidKey() {
        response = APIUtils.getRequest("/weather", "London", false);
        logger.warn("Request sent with invalid API key");
    }

    @When("I store the current temperature")
    public void storeCurrentTemp() {
        currentTemp = APIUtils.extractTemperature(response);
        logger.info("Stored current temperature: {}", currentTemp);
    }

    @Then("I should receive a {int} status code")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
        logger.info("Status code verified: {}", statusCode);
    }

    @Then("the response should contain weather data")
    public void verifyWeatherData() {
        APIUtils.validateWeatherResponse(response);
    }

    @Then("the response should contain 5-day forecast data")
    public void verifyForecastData() {
        APIUtils.validateForecastResponse(response);
    }

    @Then("the current temperature should match forecast for today")
    public void verifyIntegration() {
        double forecastTemp = APIUtils.extractForecastTemperature(response);
        double tolerance = 2.0;
        boolean tempMatch = Math.abs(currentTemp - forecastTemp) <= tolerance;
        logger.info("Comparing current temp {} with forecast temp {}", currentTemp, forecastTemp);
        if (!tempMatch) {
            throw new AssertionError("Temperature mismatch: current=" + currentTemp + ", forecast=" + forecastTemp);
        }
    }
}