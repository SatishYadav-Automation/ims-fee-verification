package com.ims.fees.pages;

import com.ims.fees.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * A classroom program page (CATapult Classroom). It is the same page as ProgramPage, but the batches only
 * appear after a city and a centre are chosen, so it adds that step. Reading the cards is inherited.
 */
public class ClassroomProgramPage extends ProgramPage {

	private static final By CITY = By.id("city");
	private static final By CENTRE = By.id("centernew");

	public ClassroomProgramPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Chooses the city, then the centre. The page then asks for the batches of that centre;
	 * readBatchCards() waits until they (or "Batch not available!") appear.
	 */
	public void selectCityAndCentre(String city, String centre) {
		assertPageLoaded();
		new Select(WaitUtils.waitForVisibility(CITY)).selectByVisibleText(city);

		WebElement centreDropdown = WaitUtils.waitForVisibility(CENTRE);
		boolean listed = WaitUtils.waitUntil(() -> new Select(centreDropdown).getOptions().stream()
				.anyMatch(option -> option.getText().trim().equals(centre)));
		if (!listed) {
			throw new AssertionError("Centre '" + centre + "' is not listed for city '" + city + "'");
		}
		new Select(centreDropdown).selectByVisibleText(centre);
	}
}
