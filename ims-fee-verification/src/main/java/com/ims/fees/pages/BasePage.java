package com.ims.fees.pages;

import com.ims.fees.utils.WaitUtils;
import com.ims.fees.wrappers.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public abstract class BasePage {

	protected WebDriver driver;
	private WebElement pageRoot;

	protected abstract By pageRootLocator();

	public BasePage(WebDriver driver) {
		this.driver = driver;
	}

	protected WebElement pageRoot() {
		if (pageRoot == null) {
			pageRoot = WaitUtils.waitForPresence(pageRootLocator(), 60);
		}
		return pageRoot;
	}

	public void assertPageLoaded() {
		WebElement root = pageRoot();
		Assert.assertTrue(Element.wrap(root).isVisibleWithWait(),
				this.getClass().getSimpleName() + " page not loaded.");
		WaitUtils.waitForAllOverlaysToDisappear();
	}
}
