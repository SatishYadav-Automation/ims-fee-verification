package com.ims.fees.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtils {

	public static String captureScreenshot(WebDriver driver, String testName) {
		try {
			File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

			String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			String testFolder = "screenshots/" + testName + "/";

			File destDir = new File(testFolder);
			destDir.mkdirs();

			File destFile = new File(destDir, timestamp + ".png");

			Files.copy(screenshotFile.toPath(), destFile.toPath());

			return destFile.getAbsolutePath();

		} catch (Exception e) {
			System.out.println("Failed to capture screenshot: " + e.getMessage());
			return null;
		}
	}
}
