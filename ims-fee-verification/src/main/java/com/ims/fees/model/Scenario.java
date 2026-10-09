package com.ims.fees.model;

/**
 * One thing to verify: a page and, for classroom pages, the city and centre to select first.
 * city and centre are blank for pages without a selection (Max, Core).
 * centreCode and variantCode are what the API call needs; they are blank when the API check is not set up.
 */
public record Scenario(String pagePath, String city, String centre, String centreCode, String variantCode) {

	public boolean hasCentre() {
		return !city.isEmpty() && !centre.isEmpty();
	}

	public boolean hasApiData() {
		return !centreCode.isEmpty() && !variantCode.isEmpty();
	}

	@Override
	public String toString() {
		return hasCentre() ? pagePath + " | " + city + " | " + centre : pagePath;
	}
}
