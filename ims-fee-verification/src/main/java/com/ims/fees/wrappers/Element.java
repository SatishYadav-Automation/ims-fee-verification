package com.ims.fees.wrappers;

import com.ims.fees.utils.WaitUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import com.ims.fees.core.DriverFactory;

public class Element {

	private final WebElement element;
	private final WebDriver driver;

	private Element(WebElement element) {
		this.element = element;
		this.driver = DriverFactory.getDriver();
	}

	/** Wrap a WebElement into Element */
	public static Element wrap(WebElement element) {
		return new Element(element);
	}

	public WebElement getElement() {
		return element;
	}

	public boolean isVisibleWithWait() {
		return WaitUtils.waitUntil(element::isDisplayed);
	}

	public boolean isHiddenWithWait() {
		return WaitUtils.waitUntil(() -> !element.isDisplayed());
	}

	public boolean isNotPresentWithWait() {
		return WaitUtils.waitUntil(() -> {
			return element == null || !element.isDisplayed();
		});
	}

	public boolean isEnabledWithWait() {
		return WaitUtils.waitUntil(element::isEnabled);
	}

	public boolean isSelectedWithWait() {
		return WaitUtils.waitUntil(element::isSelected);
	}

	public boolean hasClassWithWait(String className) {
		return WaitUtils.waitUntil(() -> {
			String classes = element.getDomAttribute("class");
			return classes != null && classes.contains(className);
		});
	}

	public boolean hasTextWithWait(String text) {
		return WaitUtils.waitUntil(() -> {
			String elText = element.getText();
			return elText != null && elText.equals(text);
		});
	}

	public String getInputValue() {
		return element.getDomProperty("value");
	}

	public boolean hasValueWithWait(String value) {
		return WaitUtils.waitUntil(() -> {
			String val = getInputValue();
			return val != null && val.equals(value);
		});
	}

	public boolean waitForTextContains(String text) {
		return WaitUtils.waitUntil(() -> {
			String elText = element.getText();
			return elText != null && elText.contains(text);
		});
	}

	public boolean isInViewport() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return (Boolean) js.executeScript(
				"var r = arguments[0].getBoundingClientRect();" + "return r.bottom > 0 && r.top < window.innerHeight;",
				element);
	}

	public void scrollIntoView() {
		WaitUtils.waitUntil(() -> {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
			return isInViewport();
		}, 5, 150);
	}

	public void scrollAndClick() {
		WaitUtils.waitUntil(() -> {
			try {
				scrollIntoView();
				getElement().click();
				return true;
			} catch (ElementNotInteractableException e) {
				return false;
			}
		}, 10, 150);
	}

	public void focus() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].focus();", element);
	}

	public void jsClick() {
		((JavascriptExecutor) driver)
				.executeScript("arguments[0].dispatchEvent(new MouseEvent('click',{bubbles:true}))", element);
	}

	public void hover() {
		Actions actions = new Actions(driver);
		actions.moveToElement(element).perform();
	}

	public void typeTextWithActions(String text) {
		Actions actions = new Actions(driver);
		actions.click(element).pause(Duration.ofMillis(50)).sendKeys(text).perform();
	}

	public void clearWithActions() {
		scrollAndClick();
		new Actions(driver).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.BACK_SPACE).perform();
	}

	public Element parent(String nodeSelector) {
		WebElement parent = element.findElement(By.xpath("parent::" + nodeSelector));
		return new Element(parent);
	}

	public List<Element> children(By locator) {
		return element.findElements(locator).stream().map(Element::new).collect(Collectors.toList());
	}

	public Element ancestor(String nodeSelector) {
		WebElement ancestor = element.findElement(By.xpath("ancestor::" + nodeSelector));
		return new Element(ancestor);
	}

	public Element followingSibling(String nodeSelector) {
		WebElement sibling = element.findElement(By.xpath("following-sibling::" + nodeSelector));
		return new Element(sibling);
	}

	public boolean isImageLoaded() {
		scrollIntoView();
		return (Boolean) ((JavascriptExecutor) driver)
				.executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", element);
	}

	public boolean verifyImageSrc(String expectedSrc) {
		String src = element.getDomAttribute("src");
		return src != null && src.contains(expectedSrc) && isImageLoaded();
	}

	public boolean verifyImageSrcContains(String expectedSrc) {
		String src = element.getDomAttribute("src");
		return src != null && src.contains(expectedSrc);
	}
}
