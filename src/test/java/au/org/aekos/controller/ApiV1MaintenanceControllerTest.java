package au.org.aekos.controller;

import org.junit.Test;

import au.org.aekos.controller.ApiV1MaintenanceController.ProgressTracker;

public class ApiV1MaintenanceControllerTest {

	/**
	 * Can we run through the life of the progress tracker without exploding?
	 */
	@Test
	public void testAddRecord01() {
		int total = 10;
		ProgressTracker objectUnderTest = new ApiV1MaintenanceController.ProgressTracker(1, total);
		for (int i = 0; i < total; i++) {
			objectUnderTest.addRecord();
		}
		objectUnderTest.getFinishedMessage();
	}
}
