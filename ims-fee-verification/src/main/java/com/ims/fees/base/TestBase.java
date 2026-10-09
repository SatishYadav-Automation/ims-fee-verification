package com.ims.fees.base;

import com.ims.fees.core.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class TestBase {

	/** A new browser for every test (and every parallel data-provider row). alwaysRun: also when running by group. */
	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		DriverFactory.initDriver();
	}

	/** The failure screenshot is taken by TestListener before this runs. */
	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		DriverFactory.quitDriver();
	}

	/**
	 * Browser of the current test thread. A method, not a field: with parallel data-provider rows TestNG uses one
	 * test-class instance for all threads, so a field would be shared between them.
	 */
	protected WebDriver getDriver() {
		return DriverFactory.getDriver();
	}
}
