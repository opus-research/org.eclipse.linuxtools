package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsoleManager;
import org.junit.Before;
import org.junit.Test;

public class ScriptConsoleTest {

	@Before
	public void setUp() {
		console = ScriptConsoleManager.getInstance().getConsoleInstance("test");
	}
	@Test
	public void testGetInstance() {
		assertNotNull(console);
		assertSame(console, ScriptConsoleManager.getInstance().getConsoleInstance("test"));
		ScriptConsole console2 = ScriptConsoleManager.getInstance().getConsoleInstance("a");
		assertNotNull(console2);
		assertNotSame(console, console2);
	}

	ScriptConsole console;
}
