package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ImagePullPatternTest {
	
	private static Object[] match(final String imageName, final int expectedSeverity) {
		return new Object[]{imageName, expectedSeverity};
	}
	
	
	@Parameters(name="{0} -> {1}")
	public static Object[][] data() {
		return new Object[][] {
			match("", IStatus.ERROR),
			match("£", IStatus.ERROR),
				match("wildfly", IStatus.WARNING),
				match("jboss/", IStatus.ERROR),
				match("jboss/wildfly", IStatus.WARNING),
				match("jboss/wildfly:", IStatus.ERROR),
				match("jboss/wildfly:latest", IStatus.OK),
				match("localhost/wildfly/", IStatus.ERROR),
				match("localhost/wildfly/jboss", IStatus.WARNING),
				match("localhost/wildfly/jboss:", IStatus.ERROR),
				match("localhost/wildfly/jboss:latest", IStatus.OK),
				match("localhost:", IStatus.ERROR),
				match("localhost:5000", IStatus.OK), // bc it matches the REPO:TAG pattern.
				match("localhost:5000/", IStatus.ERROR),
				match("localhost:5000/wildfly", IStatus.WARNING),
				match("localhost:5000/wildfly/", IStatus.ERROR),
				match("localhost:5000/wildfly/jboss", IStatus.WARNING),
				match("localhost:5000/wildfly/jboss:", IStatus.ERROR),
				match("localhost:5000/wildfly/jboss:latest", IStatus.OK),
		};
	}
	
	@Parameter(value=0)
	public String imageName;
	@Parameter(value=1)
	public int expectedSeverity;
	
	
	@Test
	public void verifyData() throws Exception {
		final IStatus status = new ImagePullPage.ImageNameValidator().validate(imageName);
		// then
		Assert.assertEquals(expectedSeverity, status.getSeverity());
	}

}
