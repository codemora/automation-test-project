package api.steps;

import api.services.WeatherService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigUtils;

import java.util.List;

public class WeatherAPISteps {
    private static final Logger logger = LoggerFactory.getLogger(WeatherAPISteps.class);
    private String apiKey;
    private Response response;
    private double currentTemp;
    private String city;

    @Given("I have a valid API key")
    public void setValidApiKey() {
        apiKey = ConfigUtils.getProperty("api.key");
        logger.info("API Key initialized");
    }

    @Given("I have an invalid API Key: {string}")
    public void setInvalidApiKey(String invalidApiKey) {
        apiKey = invalidApiKey;
        logger.info("API Key initialized");
    }

    @When("I request current weather for {string}")
    public void requestCurrentWeather(String city) {
        this.city = city;
        response = WeatherService.getCurrentWeather(city, apiKey);
        logger.info("Weather request sent for city: {}", city);
    }

    @When("I request 5-day forecast for {string}")
    public void requestForecast(String city) {
        this.city = city;
        response = WeatherService.getForecastWeatherFor5Days(city, apiKey);
        logger.info("Forecast request sent for city: {}", city);
    }

    @When("I store the current temperature")
    public void storeCurrentTemp() {
        currentTemp = WeatherService.extractTemperature(response);
        logger.info("Stored current temperature: {}", currentTemp);
    }

    @Then("I should receive a {int} status code")
    public void verifyStatusCode(int statusCode) {
        Assert.assertEquals("Incorrect Status Code", statusCode, response.statusCode());
        logger.info("Status code verified: {}", statusCode);
    }

    @Then("the response should contain weather data")
    public void verifyWeatherData() {
        Assert.assertNotNull("Weather response missing temperature",
                response.jsonPath().get("main.temp"));
        List<?> weatherList = response.jsonPath().getList("weather");
        Assert.assertFalse("Weather response missing weather array or empty",
                weatherList.isEmpty());
        Assert.assertEquals("Weather response has incorrect city", city,
                response.jsonPath().get("name"));
        logger.debug("Weather response structure validated");

    }

    @Then("the response should contain 5-day forecast data")
    public void verifyForecastData() {
        List<?> forecastList = response.jsonPath().getList("list");
        Assert.assertFalse("Forecast response missing list or empty",
                forecastList.isEmpty());
        Assert.assertNotNull("Forecast response missing temperature for first entry",
                response.jsonPath().get("list[0].main.temp"));
        logger.debug("Forecast response structure validated");

    }

    @Then("the current temperature should match forecast for today")
    public void verifyIntegration() {
        double forecastTemp = WeatherService.extractForecastTemperature(response);
        double tolerance = 2.0;
        boolean tempMatch = Math.abs(currentTemp - forecastTemp) <= tolerance;
        logger.info("Comparing current temp {} with forecast temp {}", currentTemp, forecastTemp);
        if (!tempMatch) {
            throw new AssertionError("Temperature mismatch: current=" + currentTemp + ", forecast=" + forecastTemp);
        }
    }
}