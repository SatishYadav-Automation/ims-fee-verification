package com.ims.fees.utils;

import com.ims.fees.core.ConfigReader;
import com.ims.fees.core.DriverFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class WaitUtils {

	private static final int DEFAULT_TIMEOUT;

	static {
		String timeout = ConfigReader.get("explicitWait");
		DEFAULT_TIMEOUT = (timeout != null) ? Integer.parseInt(timeout) : 10;
	}

	private static WebDriverWait webDriverWait() {
		return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
	}

	private static WebDriverWait webDriverWait(int timeoutinSeconds) {
		return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutinSeconds));
	}

	public static WebElement waitForVisibility(By locator) {
		return webDriverWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public static List<WebElement> waitForVisibilityOfAllElements(By locator) {
		return webDriverWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
	}

	public static WebElement waitForVisibility(WebElement element) {
		return webDriverWait().until(ExpectedConditions.visibilityOf(element));
	}

	public static boolean waitForInvisibility(By locator) {
		return webDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}

	public static boolean waitForStaleness(WebElement element) {
		return webDriverWait().until(ExpectedConditions.stalenessOf(element));
	}

	public static WebElement waitForClickable(By locator) {
		return webDriverWait().until(ExpectedConditions.elementToBeClickable(locator));
	}

	public static WebElement waitForClickable(WebElement element) {
		return webDriverWait().until(ExpectedConditions.elementToBeClickable(element));
	}

	public static WebElement waitForClickable(By parentLocator, By childLocator, int timeoutinSeconds) {
		return webDriverWait(timeoutinSeconds).until(driver -> {
			try {
				WebElement parent = driver.findElement(parentLocator);
				WebElement child = parent.findElement(childLocator);
				return (child.isDisplayed() && child.isEnabled()) ? child : null;
			} catch (NoSuchElementException | StaleElementReferenceException e) {
				return null;
			}
		});
	}

	public static WebElement waitForClickable(By parentLocator, By childLocator) {
		return waitForClickable(parentLocator, childLocator, DEFAULT_TIMEOUT);
	}

	public static WebElement waitForClickable(WebElement parent, By childLocator) {
		return webDriverWait().until(driver -> {
			try {
				WebElement child = parent.findElement(childLocator);
				return (child.isDisplayed() && child.isEnabled()) ? child : null;
			} catch (NoSuchElementException | StaleElementReferenceException e) {
				return null;
			}
		});
	}

	public static WebElement waitForPresence(By locator) {
		return webDriverWait().until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public static WebElement waitForPresence(By locator, int timeoutinSeconds) {
		return webDriverWait(timeoutinSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public static WebElement waitForPresence(By rootLocator, By childLocator) {
		return webDriverWait().until(driver -> {
			try {
				WebElement root = driver.findElement(rootLocator);
				return root.findElement(childLocator);
			} catch (NoSuchElementException | StaleElementReferenceException e) {
				return null;
			}
		});
	}

	public static WebElement waitForPresence(WebElement rootElement, By childLocator) {
		return webDriverWait().until(ExpectedConditions.presenceOfNestedElementLocatedBy(rootElement, childLocator));
	}

	public static void waitForUrl(String expectedUrl) {
		webDriverWait().until(ExpectedConditions.urlToBe(expectedUrl)).toString();
	}

	public static void waitForUrlContains(String fragment) {
		webDriverWait().until(ExpectedConditions.urlContains(fragment)).toString();
	}

	public static void waitForTextToBe(By locator, String expectedText) {
		webDriverWait().until(ExpectedConditions.textToBe(locator, expectedText)).toString();
	}

	public static void waitForTextContains(By locator, String expectedText) {
		webDriverWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText)).toString();
	}

	public static boolean waitForNestedTextToBe(WebElement parent, By child, String expected) {
		return waitUntil(() -> {
			WebElement el = parent.findElement(child);
			return expected.equals(el.getText().trim());
		});
	}

	public static boolean waitForTitleContains(String titleFragment) {
		return webDriverWait().until(ExpectedConditions.titleContains(titleFragment));
	}

	public static boolean waitForElementToGone(By locator) {
		return waitUntil(() -> {
			List<WebElement> elements = DriverFactory.getDriver().findElements(locator);
			return elements.isEmpty() || !elements.get(0).isDisplayed();
		});
	}

	public static void waitForAllOverlaysToDisappear() {
		List<By> overlays = Arrays.asList(By.cssSelector(".foreground-closing"), By.cssSelector(".loading-foreground"));

		// Wait until NO overlay is present
		waitUntil(
				() -> overlays.stream().allMatch(locator -> DriverFactory.getDriver().findElements(locator).isEmpty()),
				10, 200);
	}

	public static boolean waitUntil(Supplier<Boolean> condition, int timeoutSeconds, long pollMillis) {
		try {
			new FluentWait<>(DriverFactory.getDriver()).withTimeout(Duration.ofSeconds(timeoutSeconds))
					.pollingEvery(Duration.ofMillis(pollMillis)).until(driver -> {
						try {
							return Boolean.TRUE.equals(condition.get());
						} catch (Exception e) {
							return false;
						}
					});

			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public static boolean waitUntil(Supplier<Boolean> condition) {
		return waitUntil(condition, DEFAULT_TIMEOUT, 200);
	}

	public static <T> T waitUntilSafeFromStalling(Supplier<T> action, T defaultValue) {
		try {
			return new FluentWait<>(DriverFactory.getDriver()).withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
					.pollingEvery(Duration.ofMillis(150)).ignoring(StaleElementReferenceException.class)
					.ignoring(NoSuchElementException.class).ignoring(ElementClickInterceptedException.class)
					.until(driver -> action.get());
		} catch (TimeoutException e) {
			return defaultValue;
		}
	}

	public static void switchToIframe(By iframeLocator) {
		webDriverWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeLocator));
	}

	public static void switchToMainContent() {
		DriverFactory.getDriver().switchTo().defaultContent();
	}

	public static Alert waitForAlertToPresent() {
		return webDriverWait().until(ExpectedConditions.alertIsPresent());
	}
}
