package au.org.aekos.api.controller;

import org.junit.Test;

import au.org.aekos.api.controller.ProgressTracker;

public class ProgressTrackerTest {

	/**
	 * Can we run through the life of the progress tracker without exploding?
	 */
	@Test
	public void testAddRecord01() {
		int total = 10;
		ProgressTracker objectUnderTest = new ProgressTracker(total);
		for (int i = 0; i < total; i++) {
			objectUnderTest.addRecord();
		}
		objectUnderTest.getFinishedMessage();
	}
}
