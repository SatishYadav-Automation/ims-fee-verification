package com.ims.fees.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ApiUtility {

	public enum HttpMethod {
		GET, POST, PUT, DELETE
	}

	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public static String sendRequest(HttpMethod method, String url, String body, Map<String, String> headers) {
		try {
			HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

			if (headers != null) {
				headers.forEach(builder::header);
			}

			switch (method) {
			case GET -> builder.GET();
			case POST -> builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
			case PUT -> builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
			case DELETE -> builder.DELETE();
			}

			HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 400) {
				throw new RuntimeException("API returned HTTP " + response.statusCode() + " for [" + method + "] " + url
						+ "\nBody: " + response.body());
			}

			return response.body();

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("API request failed for [" + method + "] " + url + ": " + e.getMessage(), e);
		}
	}

	public static String sendGet(String url, Map<String, String> headers) {
		return sendRequest(HttpMethod.GET, url, null, headers);
	}

	public static String sendPost(String url, String body, Map<String, String> headers) {
		return sendRequest(HttpMethod.POST, url, body, headers);
	}

	public static String sendPut(String url, String body, Map<String, String> headers) {
		return sendRequest(HttpMethod.PUT, url, body, headers);
	}

	public static String sendDelete(String url, Map<String, String> headers) {
		return sendRequest(HttpMethod.DELETE, url, null, headers);
	}
}
