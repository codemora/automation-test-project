package web.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigUtils;

import java.time.Duration;

public class ContactPage {
    private static final Logger logger = LoggerFactory.getLogger(ContactPage.class);
    private static final String contactUrl = ConfigUtils.getProperty("web.contact.url");
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "et_pb_contact_name_0")
    private WebElement nameField;

    @FindBy(id = "et_pb_contact_message_0")
    private WebElement messageField;

    @FindBy(css = "#et_pb_contact_form_0 button")
    private WebElement submitButton;

    @FindBy(css = "#et_pb_contact_form_0 .et-pb-contact-message p")
    private WebElement successMessage;

    @FindBy(css = "#et_pb_contact_form_0 .et-pb-contact-message p + ul")
    private WebElement errorMessage;

    public ContactPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public static ContactPage  load(WebDriver driver){
        driver.get(contactUrl);
        logger.info("Navigated to contact page");
        return new ContactPage(driver);
    }

    public void fillForm(String name, String message) {
        wait.until(ExpectedConditions.visibilityOf(nameField));
        nameField.sendKeys(name);
        messageField.sendKeys(message);
        logger.info("Form filled with name: {}, message: {}", name, message);
    }

    public void submitForm() {
        submitButton.click();
        logger.info("Form submitted");
    }

    public void submitEmptyForm() {
        submitButton.click();
        logger.info("Empty form submitted");
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}