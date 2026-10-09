package com.ims.fees.core;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/** Chrome only. One browser per thread (ThreadLocal), so tests can run in parallel. */
public class DriverFactory {

	private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	public static final boolean IS_CI = System.getenv("CI") != null;

	// Kept in fields on purpose: java.util.logging holds loggers weakly, so without a reference the OFF level can be lost.
	private static final Logger CDP_VERSION_FINDER = Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder");
	private static final Logger CHROMIUM_DRIVER = Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver");

	static {
		CDP_VERSION_FINDER.setLevel(Level.OFF);
		CHROMIUM_DRIVER.setLevel(Level.OFF);
	}

	public static WebDriver initDriver() {
		if (driver.get() != null) {
			return driver.get();
		}
		ChromeOptions options = new ChromeOptions();
		// The IMS page loads many third-party scripts; do not wait for all of them to finish.
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		if (IS_CI) {
			options.addArguments("--headless=new", "--disable-gpu", "--window-size=1366,768", "--no-sandbox",
					"--disable-dev-shm-usage", "--disable-extensions", "--disable-infobars", "--password-store=basic",
					"--disable-features=PasswordLeakDetection");
		} else {
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.password_manager_enabled", false);
			prefs.put("profile.password_manager_leak_detection", false);
			options.setExperimentalOption("prefs", prefs);

			options.addArguments("--start-maximized", "--password-store=basic",
					"--disable-features=PasswordLeakDetection,PasswordCheck");

		}
		WebDriver webDriver = new ChromeDriver(options);
		webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		driver.set(webDriver);
		return webDriver;
	}

	public static WebDriver getDriver() {
		if (driver.get() == null) {
			return initDriver();
		}
		return driver.get();
	}

	/** True when the current thread has a browser (API tests have none). */
	public static boolean isDriverActive() {
		return driver.get() != null;
	}

	public static void quitDriver() {
		try {
			if (driver.get() != null) {
				driver.get().quit();
			}
		} finally {
			driver.remove();
		}
	}
}
