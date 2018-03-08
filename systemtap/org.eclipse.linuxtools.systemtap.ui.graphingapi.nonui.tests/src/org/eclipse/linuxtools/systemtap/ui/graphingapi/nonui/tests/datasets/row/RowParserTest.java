package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowParser;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;


import junit.framework.TestCase;

public class RowParserTest extends TestCase {
	public RowParserTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		parser = new RowParser(new String[] {"\\d+", "(\\D+)", "\\d+", "\\D+"});
		
		IMemento m = XMLMemento.createWriteRoot("a");
		parser.saveXML(m);
		parser2 = new RowParser(m);
	}
	
	public void testParse() {
		assertNull(parser.parse(null));
		assertNull(parser.parse(new StringBuilder("")));
		assertNull(parser.parse(new StringBuilder("asdf")));
		assertNull(parser.parse(new StringBuilder("1, ")));
		assertNull(parser.parse(new StringBuilder("1, 3")));
		
		IDataEntry entry = parser.parse(new StringBuilder("1, (2), 3, 4, 5"));
		assertNotNull(entry);
		assertEquals(2, entry.getColCount());
		assertEquals(1, entry.getRowCount());
		assertEquals("1", entry.getRow(0)[0]);

		entry = parser2.parse(new StringBuilder("1, 2, 3, 4, 5"));
		assertNotNull(entry);
		assertEquals(2, entry.getColCount());
		assertEquals(1, entry.getRowCount());
		assertEquals("1", entry.getRow(0)[0]);
	}
	
	public void testSaveXML() {
		IMemento m = XMLMemento.createWriteRoot("a");
		parser.saveXML(m);
		assertSame(RowDataSet.ID, m.getString("dataset"));

		IMemento[] children = m.getChildren("Series");
		assertEquals(2, children.length);
		assertSame("\\d+", children[0].getString("parsingExpression"));
		assertSame("(\\D+)", children[0].getString("parsingSpacer"));
		assertSame("\\d+", children[1].getString("parsingExpression"));
		assertSame("\\D+", children[1].getString("parsingSpacer"));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	RowParser parser;
	RowParser parser2;
}
