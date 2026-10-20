package com.vsbp.ui;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Failure evidence: when a test method throws (assertion or Selenium error) this extension saves a PNG screenshot and the
 * page HTML to {@code target/screenshots/} <em>before</em> the browser is closed, then re-throws the original failure.
 * (A TestWatcher would be too late: it runs after {@code @AfterEach} has already quit the driver.)
 */
public class ScreenshotOnFailureExtension implements TestExecutionExceptionHandler {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Supplier<WebDriver> driver;
    private final Path outputDir;

    public ScreenshotOnFailureExtension(Supplier<WebDriver> driver) {
        this(driver, Path.of("target", "screenshots"));
    }

    public ScreenshotOnFailureExtension(Supplier<WebDriver> driver, Path outputDir) {
        this.driver = driver;
        this.outputDir = outputDir;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        capture(context);
        throw throwable;
    }

    private void capture(ExtensionContext context) {
        try {
            WebDriver current = driver.get();
            if (current == null) {
                return;
            }
            Files.createDirectories(outputDir);
            String base = context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName() + "_" + LocalDateTime.now().format(STAMP);

            if (current instanceof TakesScreenshot screenshotter) {
                File source = screenshotter.getScreenshotAs(OutputType.FILE);
                Path png = outputDir.resolve(base + ".png");
                Files.copy(source.toPath(), png, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[screenshot] saved " + png.toAbsolutePath());
            }
            Files.writeString(outputDir.resolve(base + ".html"), current.getPageSource(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Never hide the real test failure because evidence capturing failed
            System.err.println("[screenshot] could not capture failure evidence: " + e.getMessage());
        }
    }
}
