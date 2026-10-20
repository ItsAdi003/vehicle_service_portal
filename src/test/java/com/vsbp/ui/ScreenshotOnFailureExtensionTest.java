package com.vsbp.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Proves the failure-screenshot mechanism works without needing a real browser. */
class ScreenshotOnFailureExtensionTest {

    @TempDir
    Path tmp;

    @Test
    void savesPngAndHtmlThenRethrowsTheOriginalFailure() throws Exception {
        File fakePng = Files.write(tmp.resolve("fake.png"), new byte[] {1, 2, 3}).toFile();
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
        when(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE)).thenReturn(fakePng);
        when(driver.getPageSource()).thenReturn("<html><body>failed page</body></html>");

        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getRequiredTestClass()).thenAnswer(invocation -> ScreenshotOnFailureExtensionTest.class);
        when(context.getRequiredTestMethod()).thenReturn(
                ScreenshotOnFailureExtensionTest.class.getDeclaredMethod("savesPngAndHtmlThenRethrowsTheOriginalFailure"));

        Path out = tmp.resolve("screenshots");
        ScreenshotOnFailureExtension extension = new ScreenshotOnFailureExtension(() -> driver, out);

        assertThatThrownBy(() -> extension.handleTestExecutionException(context, new AssertionError("boom")))
                .isInstanceOf(AssertionError.class)
                .hasMessage("boom");

        try (var files = Files.list(out)) {
            List<String> names = files.map(path -> path.getFileName().toString()).toList();
            assertThat(names).anyMatch(name -> name.startsWith("ScreenshotOnFailureExtensionTest_") && name.endsWith(".png"));
            assertThat(names).anyMatch(name -> name.endsWith(".html"));
        }
    }

    @Test
    void stillRethrowsWhenNoBrowserIsAvailable() throws Exception {
        ExtensionContext context = mock(ExtensionContext.class);
        ScreenshotOnFailureExtension extension = new ScreenshotOnFailureExtension(() -> null, tmp.resolve("none"));

        assertThatThrownBy(() -> extension.handleTestExecutionException(context, new IllegalStateException("x")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(Files.exists(tmp.resolve("none"))).isFalse();
    }
}
