@api
Feature: OpenWeather API Testing

  Background:
    Given I have a valid API key

  Scenario Outline: Get current weather for valid city
    When I request current weather for "<city>"
    Then I should receive a 200 status code
    And the response should contain weather data
    Examples:
      | city   |
      | London |
      | Paris  |

  Scenario: Get current weather for invalid city
    When I request current weather for "InvalidCity123"
    Then I should receive a 404 status code

  Scenario: Get 5-day forecast for valid city
    When I request 5-day forecast for "London"
    Then I should receive a 200 status code
    And the response should contain 5-day forecast data

#  Scenario: Test city name boundary - minimum length
#    When I request current weather for "A"
#    Then I should receive a 200 status code

#  Scenario: Test city name boundary - maximum length
#    When I request current weather for "ThisIsAVeryLongCityNameThatExceedsNormalLength"
#    Then I should receive a 200 status code

  Scenario: Test SQL injection in city parameter
    When I request current weather for "' OR '1'='1"
    Then I should receive a 404 status code

  Scenario: Test invalid API key
    When I request weather with invalid API key
    Then I should receive a 401 status code

  Scenario: Test weather and forecast integration
    When I request current weather for "London"
    And I store the current temperature
    And I request 5-day forecast for "London"
    Then the current temperature should match forecast for today