# Weather Test Automation Framework

## Setup Instructions
1. Install Java 21
2. Install Maven
3. Clone the repository
4. Configure `src/test/resources/config.properties`:
    - Set `api.key` with your OpenWeather API key
    - Modify other settings as needed
5. Install Maven dependencies: `mvn clean install -U`

## Running Tests Locally
- All tests (normal mode): `mvn test`
- All tests (headless mode): `mvn test -Dheadless=true`
- API tests: `mvn test -Dcucumber.filter.tags="@api"`
- Web tests (normal mode): `mvn test -Dcucumber.filter.tags="@web"`
- Web tests (headless mode): `mvn test -Dcucumber.filter.tags="@web" -Dheadless=true`
- Generate Allure report: `mvn allure:report`
  - The generated report will be located at:
    `target/site/allure-maven-plugin/index.html`
- Generate and Serve Allure Report: `mvn allure:serve`

## Configuration
- Settings in `src/test/resources/config.properties`
- Override via system properties (e.g., `-Dapi.key=your_key`)
- Properties:
    - `api.base.url`: API endpoint
    - `api.key`: OpenWeather API key
    - `web.contact.url`: Contact form URL
    - `browser`: Default browser (chrome/firefox)
    - `headless`: Headless mode (true/false)

## CI Integration
- GitHub Actions workflow runs on push/pull requests to main branch
- Runs all tests
- Stores Allure results as artifacts
- Deploys report to GitHub Pages on main branch pushes

### Setup for GitHub Actions
1. Enable GitHub Pages in repository settings (optional for report viewing)

### Viewing CI Results
- Check Actions tab for run status
- Download Allure results from artifacts
- View deployed report at: `https://<username>.github.io/<repository>/<reportNumber>/index.html`

## Framework Structure
- src/test/java/api: API testing components
    - steps: Step definitions
    - utils: API utility classes (APIUtils.java)
- utils: General utility classes (ConfigUtils.java)
- src/test/java/web: Web UI testing components
    - pages: PageObjects
    - steps: Step definitions
    - utils: Web utility classes (DriverFactory.java)
- src/test/resources/features: Gherkin feature files
- src/test/java/tests: Test runner
- src/test/resources/config.properties: Configuration file
- src/test/java/utils: General utility classes
- .github/workflows/ci.yml: GitHub Actions configuration

## Features
- BDD with Cucumber
- REST API testing with REST Assured
- Web UI testing with Selenium
- Cross-browser testing (Chrome, Firefox)
- Headless mode support for both browsers
- Network logging
- Allure reporting
- SLF4J logging
- Automatic driver management with WebDriverManager
- CI/CD with GitHub Actions
- Property-based configuration
- Screenshot capture on UI test failure
- API utility methods in APIUtils

## Assumptions
- OpenWeather API free tier is sufficient
- Selenium contact form is available
- Network conditions are stable
- Internet connection available for WebDriverManager

## Limitations
- Limited security test coverage
- Cross browser reporting with allure Report

## Test Results
- API tests cover functional, boundary, security, and integration
- Web tests cover form validation and network monitoring
- Reports generated in Allure format with screenshots on UI failures
- CI results available through GitHub Actions