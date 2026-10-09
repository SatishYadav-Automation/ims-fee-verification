package com.ims.fees.listeners;

import com.ims.fees.core.ConfigReader;
import com.ims.fees.core.DriverFactory;
import com.ims.fees.utils.NavigationUtils;
import com.ims.fees.utils.ScreenshotUtils;
import io.qameta.allure.Allure;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener, IInvokedMethodListener {

	private static final Logger log = LogManager.getLogger(TestListener.class);

	@Override
	public void onStart(ITestContext context) {
		log.info("=== Test Execution Started ===");
		writeAllureEnvironment();
	}

	@Override
	public void onFinish(ITestContext context) {
		log.info("=== Test Execution Finished ===");
	}

	@Override
	public void onTestStart(ITestResult result) {
		String testIdentifier = getTestIdentifier(result);
		log.info("Test Started: " + testIdentifier);

		Allure.getLifecycle().updateTestCase(testResult -> {
			testResult.setName(testIdentifier);
			testResult.setFullName(testIdentifier);
		});

		String startTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		Allure.addAttachment("Test Start Time", startTime);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		log.info("Test Passed: " + getTestIdentifier(result));
	}

	/** Runs right after each test method, before @AfterMethod closes the browser. */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		// Only failed test methods: skips and configuration methods are ignored (and never open a browser here)
		if (!method.isTestMethod() || testResult.getStatus() != ITestResult.FAILURE) {
			return;
		}
		log.error("Execution FAILED at method: " + getTestIdentifier(testResult));
		if (DriverFactory.isDriverActive()) { // API tests have no browser
			log.error("Current URL: " + NavigationUtils.getCurrentUrl());
			captureAndAttachScreenshot(testResult);
		}
		logError(testResult.getThrowable());
	}

	private void captureAndAttachScreenshot(ITestResult result) {
		String screenshotName = getTestIdentifier(result).replaceAll("[^A-Za-z0-9._-]", "_");
		String path = ScreenshotUtils.captureScreenshot(DriverFactory.getDriver(), screenshotName);
		if (path == null) {
			log.warn("Screenshot capture returned null - skipping attachment");
			return;
		}
		try (InputStream is = Files.newInputStream(Paths.get(path))) {
			log.info("Screenshot Name: " + Paths.get(path).getFileName());
			Allure.addAttachment("Failure Screenshot", "image/png", is, "png");
		} catch (Exception e) {
			log.error("Error while capturing and attaching screenshot: " + e.getMessage(), e);
		}
	}

	/** Class-method, plus the data-provider value (the page path) so parallel runs are told apart. */
	private String getTestIdentifier(ITestResult result) {
		String identifier = result.getTestClass().getRealClass().getSimpleName() + "-"
				+ result.getMethod().getMethodName();
		Object[] parameters = result.getParameters();
		return parameters.length == 0 ? identifier : identifier + " " + Arrays.toString(parameters);
	}

	private void logError(Throwable throwable) {
		if (throwable != null) {
			log.error("Error Message: " + throwable.getMessage());
			for (StackTraceElement e : throwable.getStackTrace()) {
				if (e.getClassName().startsWith("com.ims.fees")) {
					log.error("\tat " + e.toString());
				}
			}
		}
	}

	private void writeAllureEnvironment() {
		try {
			String env = System.getProperty("env", "staging");
			File dir = new File("target/allure-results");
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File file = new File(dir, "environment.properties");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write("Environment=" + env.toUpperCase() + "\n");
				writer.write("BaseURL=" + ConfigReader.get("baseUrl." + env) + "\n");
			}
		} catch (Exception e) {
			log.error("Failed to write Allure environment file", e);
		}
	}
}
