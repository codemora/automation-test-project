package web.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigUtils;

public class DriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final boolean HEADLESS = ConfigUtils.getBooleanProperty("headless", false);
    private static final String DEFAULT_BROWSER = ConfigUtils.getProperty("browser", "chrome");

    private static final ThreadLocal<DriverFactory> threadLocalInstance = new ThreadLocal<>();

    private WebDriver driver;
    private BrowserMobProxy proxy;
    private String currentBrowser;
    private boolean useProxy;

    private DriverFactory(String browser, boolean useProxy) {
        this.useProxy = useProxy;
        initializeDriver(browser);
        currentBrowser = browser.toLowerCase();
    }

    public static DriverFactory getInstance(String browser, boolean useProxy) {
        DriverFactory instance = threadLocalInstance.get();
        String normalizedBrowser = browser != null ? browser.toLowerCase() : DEFAULT_BROWSER;

        if (instance == null || !instance.currentBrowser.equals(normalizedBrowser) || instance.useProxy != useProxy) {
            if (instance != null) {
                logger.info("Thread {}: Reinitializing from {} (proxy: {}) to {} (proxy: {})",
                        Thread.currentThread().getId(), instance.currentBrowser, instance.useProxy,
                        normalizedBrowser, useProxy);
                instance.quit();
            }
            instance = new DriverFactory(normalizedBrowser, useProxy);
            threadLocalInstance.set(instance);
        }
        return instance;
    }

    public static DriverFactory getInstance(String browser) {
        return getInstance(browser, false); // Default to not using proxy
    }

    public static DriverFactory getInstance(boolean useProxy) {
        return getInstance(DEFAULT_BROWSER, useProxy); // Default to not using proxy
    }

    public static DriverFactory getInstance() {
        return getInstance(DEFAULT_BROWSER, false); // Default to not using proxy
    }

    private void initializeDriver(String browser) {
        try {
            Proxy seleniumProxy = null;
            if (useProxy) {
                proxy = new BrowserMobProxyServer();
                proxy.start(0);
                logger.info("Thread {}: BrowserMob Proxy started on port: {}",
                        Thread.currentThread().getId(), proxy.getPort());
                seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            }

            if ("chrome".equals(browser)) {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (useProxy && seleniumProxy != null) {
                    options.setProxy(seleniumProxy);
                }
                options.setAcceptInsecureCerts(true);
                options.addArguments("--start-maximized");
                if (HEADLESS) {
                    configureHeadlessChrome(options);
                }
                driver = new ChromeDriver(options);
                if (useProxy) {
                    proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
                }
                logger.info("Thread {}: Chrome driver initialized{} {}",
                        Thread.currentThread().getId(), HEADLESS ? " (headless)" : "",
                        useProxy ? "(with proxy)" : "(without proxy)");
            } else if ("firefox".equals(browser)) {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                if (useProxy && seleniumProxy != null) {
                    options.setProxy(seleniumProxy);
                }
                options.setAcceptInsecureCerts(true);
                if (HEADLESS) {
                    configureHeadlessFirefox(options);
                }
                driver = new FirefoxDriver(options);
                logger.info("Thread {}: Firefox driver initialized{} {}",
                        Thread.currentThread().getId(), HEADLESS ? " (headless)" : "",
                        useProxy ? "(with proxy)" : "(without proxy)");
            } else {
                throw new IllegalArgumentException("Unsupported browser: " + browser);
            }

            if (useProxy) {
                logger.debug("Thread {}: HAR capture initialized for {}",
                        Thread.currentThread().getId(), browser);
            }

        } catch (Exception e) {
            logger.error("Thread {}: Failed to initialize driver for {}",
                    Thread.currentThread().getId(), browser, e);
            quit();
            throw new RuntimeException("Driver initialization failed for " + browser, e);
        }
    }

    private void configureHeadlessChrome(ChromeOptions options) {
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        logger.debug("Thread {}: Configuring Chrome in headless mode", Thread.currentThread().getId());
    }

    private void configureHeadlessFirefox(FirefoxOptions options) {
        options.addArguments("-headless", "-width=1920", "-height=1080");
        logger.debug("Thread {}: Configuring Firefox in headless mode", Thread.currentThread().getId());
    }

    public WebDriver getDriver() {
        return driver;
    }

    public Har getHar() {
        if (!useProxy || proxy == null) {
            logger.error("Proxy is not enabled or null, cannot get HAR");
            return null;
        }
        Har currentHar = proxy.getHar();
        if (currentHar == null || currentHar.getLog() == null) {
            logger.warn("No HAR data captured yet");
        }
        return currentHar;
    }

    public void newHar() {
        if (!useProxy || proxy == null) {
            logger.error("Proxy is not enabled or null, cannot create new HAR");
            return;
        }
        proxy.newHar("formSubmission");
        logger.debug("Thread {}: New HAR capture started", Thread.currentThread().getId());
    }

    public String getCurrentBrowser() {
        return currentBrowser;
    }

    public boolean isUsingProxy() {
        return useProxy;
    }

    public void quit() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
                logger.info("Thread {}: WebDriver closed", Thread.currentThread().getId());
            }
            if (useProxy && proxy != null) {
                proxy.stop();
                proxy = null;
                logger.info("Thread {}: BrowserMob Proxy stopped", Thread.currentThread().getId());
            }
        } catch (Exception e) {
            logger.error("Thread {}: Error during cleanup", Thread.currentThread().getId(), e);
        } finally {
            threadLocalInstance.remove();
            currentBrowser = null;
        }
    }
}