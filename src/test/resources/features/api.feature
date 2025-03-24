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

  Scenario Outline: Get current weather for invalid city
    When I request current weather for "<city>"
    Then I should receive a <status> status code
    Examples:
      |                       city                              | status |
      | InvalidCity123                                          |   404  |
      | lllsldkkellsldkflskfksldflklsdflksldkflskfldlfkldkflskd |   404  |
      |                                                         |   400  |

  Scenario Outline: Get 5-day forecast for valid city
    When I request 5-day forecast for "<city>"
    Then I should receive a 200 status code
    And the response should contain 5-day forecast data
    Examples:
      | city  |
      | Rome  |
      | Accra |

  Scenario Outline: Get 5-day forecast for invalid city
    When I request 5-day forecast for "<city>"
    Then I should receive a <status> status code
    Examples:
      |                       city                              | status |
      | InvalidCity123                                          |   404  |
      | lllsldkkellsldkflskfksldflklsdflksldkflskfldlfkldkflskd |   404  |
      |                                                         |   400  |


  Scenario: Test SQL injection in city parameter
    When I request current weather for "' OR '1'='1"
    Then I should receive a 404 status code

  Scenario Outline: Test invalid API key
    Given I have an invalid API Key: "<invalid_key>"
    When I request current weather for "London"
    Then I should receive a 401 status code
    Examples:
      | invalid_key  |
      | Afetatesffsd |
      |              |

  Scenario: Test weather and forecast integration
    When I request current weather for "London"
    And I store the current temperature
    And I request 5-day forecast for "London"
    Then the current temperature should match forecast for today