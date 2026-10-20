package com.vsbp.ui;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Starts the whole application on a random port with an in-memory database and drives it with headless Chrome.
 *
 * <ul>
 *   <li>{@code -Dselenium.headless=false} shows the browser window</li>
 *   <li>{@code -Dchrome.binary=/path/to/chrome} selects a specific Chrome binary</li>
 *   <li>{@code -Dapp.baseUrl=http://localhost:8081/vsbp-dev} runs the same journeys against a deployed instance</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:vsbp-ui;DB_CLOSE_DELAY=-1",
        "app.seed.slot-capacity=100"})
public abstract class BaseSeleniumIT {

    protected static final String PASSWORD = "Secret123";
    protected static final String ADMIN_EMAIL = "admin@vsbp.local";
    protected static final String ADMIN_PASSWORD = "Admin@123";

    @LocalServerPort
    private int port;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @RegisterExtension
    final ScreenshotOnFailureExtension screenshots = new ScreenshotOnFailureExtension(() -> driver);

    @BeforeEach
    void startBrowser() {
        baseUrl = System.getProperty("app.baseUrl", "http://localhost:" + port);

        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,900",
                "--remote-allow-origins=*");
        String chromeBinary = System.getProperty("chrome.binary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------------------------------------------------------------- generic helpers

    protected void open(String path) {
        driver.get(baseUrl + path);
    }

    /** Waits until the browser's current path ends with the given path (works with or without a context path). */
    protected void waitForPath(String path) {
        wait.until(d -> URI.create(d.getCurrentUrl()).getPath().endsWith(path));
    }

    protected WebElement byId(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
    }

    protected void type(String id, String text) {
        WebElement field = byId(id);
        field.clear();
        field.sendKeys(text);
    }

    protected void click(String id) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id(id))).click();
    }

    protected String text(String id) {
        return byId(id).getText();
    }

    protected String uniqueEmail() {
        return "ui-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    // ---------------------------------------------------------------- user actions

    protected void register(String fullName, String email, String password) {
        open("/register");
        type("fullName", fullName);
        type("email", email);
        type("password", password);
        click("register-btn");
    }

    protected void login(String email, String password) {
        open("/login");
        type("email", email);
        type("password", password);
        click("login-btn");
    }

    protected void logout() {
        click("logout-btn");
        waitForPath("/login");
    }

    /** Registers a fresh customer, logs in and returns the e-mail address. */
    protected String registerAndLogin() {
        String email = uniqueEmail();
        register("Test Customer", email, PASSWORD);
        waitForPath("/login");
        login(email, PASSWORD);
        waitForPath("/slots");
        return email;
    }

    protected void loginAsAdmin() {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        waitForPath("/admin/bookings");
    }

    /** Id of the first slot on the slots page that still shows a Book button. */
    protected String firstBookableSlotId() {
        open("/slots");
        waitForPath("/slots");
        byId("slots-table");
        for (WebElement row : driver.findElements(By.cssSelector("#slots-table tr.slot-row"))) {
            if (!row.findElements(By.cssSelector("a.book-link")).isEmpty()) {
                return row.getAttribute("data-slot-id");
            }
        }
        throw new AssertionError("No bookable slot found on the slots page");
    }

    protected int seatsLeft(String slotId) {
        open("/slots");
        waitForPath("/slots");
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#slots-table tr[data-slot-id='" + slotId + "'] .seats-left")));
        return Integer.parseInt(cell.getText().trim());
    }

    /** Opens the booking form for a slot, fills it in and submits. Ends on the "My bookings" page. */
    protected void requestBooking(String slotId, String vehicleNumber) {
        open("/bookings/new?slotId=" + slotId);
        waitForPath("/bookings/new");
        fillBookingForm(vehicleNumber);
        click("submit-booking");
        waitForPath("/bookings");
    }

    protected void fillBookingForm(String vehicleNumber) {
        type("vehicleNumber", vehicleNumber);
        type("vehicleModel", "Honda City");
        new Select(byId("serviceType")).selectByValue("OIL_CHANGE");
        type("notes", "Booked by a Selenium test");
    }

    /** Booking id of the newest row on the "My bookings" page. */
    protected String newestBookingId() {
        open("/bookings");
        byId("bookings-table");
        List<WebElement> rows = driver.findElements(By.cssSelector("#bookings-table tr.booking-row"));
        return rows.get(0).getAttribute("data-booking-id");
    }

    /**
     * Waits until the status badge of a booking row shows the expected value. Waiting (instead of reading once) avoids
     * reading the previous page while the browser is still loading the page that follows a button click.
     */
    protected void assertStatus(String tableId, String bookingId, String expected) {
        By badge = By.cssSelector("#" + tableId + " tr[data-booking-id='" + bookingId + "'] .booking-status");
        wait.until(ExpectedConditions.textToBe(badge, expected));
    }
}
