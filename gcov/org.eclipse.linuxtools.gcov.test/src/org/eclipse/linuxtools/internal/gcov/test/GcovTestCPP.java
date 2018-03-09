package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GcovTestCPP {

	private static SWTWorkbenchBot bot;

	private static final String PROJECT_NAME = "Gcov_CPP_test";
	private static final String PROJECT_TYPE = "C++ Project";
	private static final String CLASS_NAME = GcovTestCPP.class.getName();

	@BeforeClass
	public static void beforeClass() throws Exception {
		System.out.print("Test: " + "init " + CLASS_NAME);
		bot = GcovTest.init(PROJECT_NAME, PROJECT_TYPE);
		System.out.println(" passed");
	}

	@AfterClass
	public static void afterClass() {
		System.out.print("Test: cleanup " + CLASS_NAME);
		GcovTest.cleanup(bot);
		System.out.println(" passed");
	}

	@Test
	public void openGcovFileDetails() throws Exception {
		System.out.print("Test: " + "openGcovFileDetails " + CLASS_NAME);
		GcovTest.openGcovFileDetails(bot, PROJECT_NAME);
		System.out.println(" passed");
	}

	@Test
	public void openGcovSummary() throws Exception {
		System.out.print("Test: " + "openGcovSummary " + CLASS_NAME);
		GcovTest.openGcovSummary(bot, PROJECT_NAME, true);
		System.out.println(" passed");
	}

	@Test
	public void testGcovSummaryByLaunch() throws Exception {
		System.out.print("Test: " + "testGcovSummaryByLaunch " + CLASS_NAME);
		GcovTest.openGcovSummaryByLaunch(bot, PROJECT_NAME);
		System.out.println(" passed");
	}
}
