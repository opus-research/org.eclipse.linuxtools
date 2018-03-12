package org.eclipse.linuxtools.internal.perf.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.perf.PerfVersion;
import org.junit.Test;

public class PerfVersionTest {

	@Test
	public void testPerfVersionString() {
		PerfVersion version = new PerfVersion("4.2.3.300.fc23.x86_64.g21b8");
		assertEquals(4, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertEquals("300.fc23.x86_64.g21b8", version.getQualifier());
	}

	@Test
	public void testPerfVersionIntIntInt() {
		PerfVersion version = new PerfVersion(4, 2, 3);
		assertEquals(4, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertEquals("", version.getQualifier());
	}

	@Test
	public void testIsNewer() {
		PerfVersion version = new PerfVersion("4.2.3.300.fc23.x86_64.g21b8");
		assertTrue(version.isNewer(new PerfVersion(4, 2, 2)));
		assertFalse(version.isNewer(new PerfVersion(4, 2, 4)));
		
		assertTrue(version.isNewer(new PerfVersion(4, 1, 3)));
		assertFalse(version.isNewer(new PerfVersion(4, 2, 3)));
		
		assertTrue(version.isNewer(new PerfVersion(3, 2, 3)));
		assertFalse(version.isNewer(new PerfVersion(5, 2, 3)));
	}

	@Test
	public void testToString() {
		String versionString = "4.2.3.300.fc23.x86_64.g21b8";
		PerfVersion version = new PerfVersion(versionString);
		assertTrue(versionString.equals(version.toString()));
	}

}
