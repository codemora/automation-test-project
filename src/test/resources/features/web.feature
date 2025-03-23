@web
Feature: Contact Form Testing

 Scenario Outline: Submit contact form with valid data
    Given I am on the contact page
    When I fill in the contact form with "<name>" as name and "<message>" as message
    And I submit the form
    Then I should see a success message
    Examples:
      |    name    |    message    |
      | Test User1 | Test Message1 |
      | Test User2 | Test Message2 |

  Scenario Outline: Submit contact form with missing required fields
    Given I am on the contact page
    When I fill in the contact form with "<name>" as name and "<message>" as message
    And I submit the form
    Then I should see an error message
    Examples:
      |    name    |    message    |
      | Test User1 |               |
      |            | Test Message2 |
      |            |               |


  Scenario: Submit contact form with valid data and wait for success message
    Given I am on the contact page
    When I fill in the contact form with "Test User" as name and "Test Message" as message
    And I submit the form
    Then I should see a success message after waiting

  Scenario: Verify network logs during form submission
    Given I am on the contact page with network logs
    When I fill in the contact form with "Test User" as name and "Test Message" as message
    And I submit the form
    Then I should see a success message after waiting
    And I should capture all network requests
    And I should verify API calls are present
    And I should check response times are within acceptable limits
    And I should verify correct HTTP status codes

  Scenario Outline: Submit contact form with valid data in specific browser
    Given I am on the contact page using "<browser>"
    When I fill in the contact form with "Test User" as name and "Test Message" as message
    And I submit the form
    Then I should see a success message
    Examples:
      | browser |
      | chrome  |
      | firefox |

  Scenario: Test form submission with error occurring
    Given I am on the contact page
    When I fill in the contact form with "Test User" as name and "Test Message" as message
    And I submit the form with server error occurring
    Then I should not see a success message
