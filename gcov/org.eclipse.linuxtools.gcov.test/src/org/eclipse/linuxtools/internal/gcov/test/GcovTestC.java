package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GcovTestC {

	private static SWTWorkbenchBot bot;

	private static final String PROJECT_NAME = "Gcov_C_test";
	private static final String PROJECT_TYPE = "C Project";

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = GcovTest.init(PROJECT_NAME, PROJECT_TYPE);
	}

	@AfterClass
	public static void afterClass() {
		GcovTest.cleanup(bot);
	}

	@Test
	public void openGcovFileDetails() throws Exception {
		GcovTest.openGcovFileDetails(bot, PROJECT_NAME);
	}

	@Test
	public void openGcovSummary() throws Exception {
		GcovTest.openGcovSummary(bot, PROJECT_NAME, true);
	}

	@Test
	public void testGcovSummaryByLaunch() {
		GcovTest.openGcovSummaryByLaunch(bot, PROJECT_NAME);
	}
}
