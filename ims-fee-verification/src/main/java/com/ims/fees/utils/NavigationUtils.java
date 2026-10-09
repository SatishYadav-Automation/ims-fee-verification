package com.ims.fees.utils;

import com.ims.fees.core.ConfigReader;
import com.ims.fees.core.DriverFactory;
import org.openqa.selenium.WebDriver;

public class NavigationUtils {

	private static WebDriver driver() {
		return DriverFactory.getDriver();
	}

	public static void goTo(String url) {
		driver().get(url);
	}

	public static void navigateToHomePage(String env) {
		String baseUrl = ConfigReader.get("baseUrl." + env);
		driver().get(baseUrl);
	}

	public static void back() {
		driver().navigate().back();
	}

	public static void forward() {
		driver().navigate().forward();
	}

	public static void refresh() {
		driver().navigate().refresh();
	}

	public static String getCurrentUrl() {
		return driver().getCurrentUrl();
	}

}
