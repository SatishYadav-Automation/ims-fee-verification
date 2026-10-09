package com.ims.fees.core;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

	private static final Properties properties = new Properties();

	static {
		try {

			InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");

			if (is == null)
				throw new RuntimeException("config.properties not found in classpath!");

			properties.load(is);

		} catch (Exception e) {
			throw new RuntimeException("Failed to load config.properties", e);
		}
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}

	/** Base URL of the environment chosen with -Denv=staging (default) or -Denv=live. */
	public static String baseUrl() {
		String env = System.getProperty("env", "staging");
		String url = get("baseUrl." + env);
		if (url == null) {
			throw new IllegalStateException("No 'baseUrl." + env + "' in config.properties");
		}
		return url;
	}
}
