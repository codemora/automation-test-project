package web.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.junit.Assert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.pages.ContactPage;
import web.utils.DriverFactory;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.openqa.selenium.remote.http.HttpMethod.POST;

public class ContactFormSteps {
    private static final Logger logger = LoggerFactory.getLogger(ContactFormSteps.class);
    private WebDriver driver;
    private DriverFactory driverFactory;
    private Har har;
    private List<HarEntry> entries;
    private ContactPage contactPage;
    private String currentBrowser;

    private void setupServerErrorSimulator() {
        NetworkInterceptor interceptor = new NetworkInterceptor(
                driver,
                Route.matching(req ->
                                req.getUri().contains("ultimateqa.com/filling-out-forms/") &&
                                        req.getMethod() == POST)
                        .to(() -> req -> new HttpResponse()
                                .setStatus(500))
        );
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        driverFactory = DriverFactory.getInstance();
        driver = driverFactory.getDriver();
        currentBrowser = driverFactory.getCurrentBrowser();
        // Get original scenario name
        String originalScenarioName = scenario.getName();
        String updatedScenarioName = originalScenarioName + " [Browser: " + currentBrowser + "]";

        // Update Allure test name dynamically
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String scenarioId = scenario.getId(); // Unique ID for the scenario
        lifecycle.updateTestCase(scenarioId, testResult ->
                testResult.setName(updatedScenarioName)
        );

        // Optionally, set a parameter for the report
        Allure.parameter("Browser", currentBrowser);
    }

    @Given("I am on the contact page")
    public void navigateToContactPage() {
        contactPage = ContactPage.load(driver);
    }

    @Given("I am on the contact page using {string}")
    public void navigateToContactPage(String browser) {
        driverFactory = DriverFactory.getInstance(browser);
        driver = driverFactory.getDriver();
        contactPage = ContactPage.load(driver);
    }

    @Given("I am on the contact page with network logs")
    public void navigateToContactPageWithNetworkLogs() {
        driverFactory = DriverFactory.getInstance(true);
        driver = driverFactory.getDriver();
        contactPage = ContactPage.load(driver);
    }

    @When("I fill in the contact form with {string} as name and {string} as message")
    public void fillContactForm(String name, String message) {
        contactPage.fillForm(name, message);
        logger.info("Filled contact form with valid data");
    }

    @When("I submit the form")
    public void submitForm() {
        contactPage.submitForm();
    }

    @When("I submit the form and track logs")
    public void submitFormTrackLogs() {
        driverFactory.newHar();
        contactPage.submitForm();
    }

    @When("I submit the form with server error occurring")
    public void submitFormWithServerError() {
        setupServerErrorSimulator();
        contactPage.submitForm();
    }

    @Then("I should see a success message after waiting")
    @Then("I should see a success message")
    public void verifySuccess() {
        Assert.assertTrue("Success message not displayed", contactPage.isSuccessMessageDisplayed());
        logger.info("Success message verified");
    }

    @Then("I should see an error message")
    public void verifyError() {
        Assert.assertTrue("Error message not displayed", contactPage.isErrorMessageDisplayed());
        logger.info("Error message verified");
    }

    @Then("I should not see a success message")
    public void verifySuccessNotDisplay() {
        Assert.assertFalse("Success message is displayed", contactPage.isSuccessMessageDisplayed());
        logger.info("Success not displayed verified");
    }

    @Then("I should capture all network requests")
    public void i_should_capture_all_network_requests() {
        har = driverFactory.getHar();
        entries = har.getLog().getEntries();

        System.out.println(entries.size());
        assertFalse("No network requests captured", entries.isEmpty());

        System.out.println("Thread " + Thread.currentThread().getId() +
                ": Captured " + entries.size() + " network requests:");
        for (HarEntry entry : entries) {
            System.out.println("URL: " + entry.getRequest().getUrl());
        }
    }

    @Then("I should verify API calls are present")
    public void i_should_verify_api_calls_are_present() {
        boolean apiCallFound = false;

        for (HarEntry entry : entries) {
            if (entry.getRequest().getUrl().contains("/filling-out-forms/")) {
                apiCallFound = true;
                break;
            }
        }

        assertTrue("No API calls found in network logs", apiCallFound);

        System.out.println("Thread " + Thread.currentThread().getId() + ": API calls found:");
        entries.stream()
                .filter(e -> e.getRequest().getUrl().contains("/filling-out-forms/"))
                .forEach(e -> System.out.println(e.getRequest().getUrl()));
    }

    @Then("I should check response times are within acceptable limits")
    public void i_should_check_response_times_are_within_acceptable_limits() {
        long maxResponseTime = 3000;

        entries.stream()
                .filter(entry -> entry.getRequest().getUrl().contains("/filling-out-forms/"))
                .forEach(entry -> {
                    long responseTime = entry.getTime();
                    assertTrue("Response time too high for " + entry.getRequest().getUrl() +
                                    ": " + responseTime + "ms",
                            responseTime <= maxResponseTime);
                    System.out.println("Thread " + Thread.currentThread().getId() +
                            ": Response time for " + entry.getRequest().getUrl() +
                            ": " + responseTime + "ms");
                });
    }

    @Then("I should verify correct HTTP status codes")
    public void i_should_verify_correct_http_status_codes() {
        entries.stream()
                .filter(entry -> entry.getRequest().getUrl().contains("/filling-out-forms/"))
                .forEach(entry -> {
                    int status = entry.getResponse().getStatus();
                    assertEquals("Incorrect status code for " + entry.getRequest().getUrl(),
                            200, status);
                    System.out.println("Thread " + Thread.currentThread().getId() +
                            ": Status code for " + entry.getRequest().getUrl() +
                            ": " + status);
                });
    }

    @After
    public void cleanup(Scenario scenario) {
        try {
            if (scenario.isFailed() && driver != null) {
                logger.info("Test failed, capturing screenshot");
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(scenario.getName(), "image/png",
                        new ByteArrayInputStream(screenshot), ".png");
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot on test failure", e);
        } finally {
            if (driverFactory != null) {
                driverFactory.quit();
            }
        }
    }
}