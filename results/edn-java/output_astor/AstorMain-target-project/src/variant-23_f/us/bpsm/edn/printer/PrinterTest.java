package us.bpsm.edn.printer;
public class PrinterTest {
	@org.junit.Test
	public void testSingleValues() {
		assertRoundTrip("nil");
		assertRoundTrip("a");
		assertRoundTrip("a/b");
		assertRoundTrip("/");
		assertRoundTrip("true");
		assertRoundTrip("false");
		assertRoundTrip(":a");
		assertRoundTrip(":a/b");
		assertRoundTrip("1");
		assertRoundTrip("1N");
		assertRoundTrip("3.14159");
		assertRoundTrip("123456789101112131415.1617181920M");
		assertRoundTrip("\\space");
		assertRoundTrip("\\a");
		assertRoundTrip("\"ab\\ncd\"");
		assertRoundTrip("[]");
		assertRoundTrip("()");
		assertRoundTrip("{}");
		assertRoundTrip("#{}");
		assertRoundTrip("{#{},()}");
		assertRoundTrip("#uuid \"f81d4fae-7dec-11d0-a765-00a0c91e6bf6\"");
		assertRoundTrip("\"\\\\\\\"\\'\\b\\t\\n\\r\\f\"");
	}

	@org.junit.Test
	public void testRoundTripCommaCharacterLiteralIssue45() {
		assertRoundTrip("\\,");
	}

	@org.junit.Test
	public void testSymbolAsMapKeyWithSetAsValue() {
		assertRoundTrip("{foo #{}}");
	}

	@org.junit.Test
	public void testTaggedSymbol() {
		assertRoundTrip("[#foo bar# baz]");
	}

	@org.junit.Test
	public void testComplexValue() {
		assertRoundTrip(((("{:foo [1 2.0 19023847928034709821374012938749N 91821234112347634.128937467E-3M]\n" + " :bar/baz #{true false nil}\n") + " / (\"abc\\tdef\\n\" #uuid \"f81d4fae-7dec-11d0-a765-00a0c91e6bf6\")\n") + " \\formfeed [#inst \"2010\", #inst \"2010-11\", #inst \"2010-11-12T09:08:07.123+02:00\"]\n") + " :omega [a b c d \\a\\b\\c #{}]}");
	}

	@org.junit.Test
	public void testDefaultPrinter() {
		java.io.StringWriter sw = new java.io.StringWriter();
		us.bpsm.edn.printer.Printer p = us.bpsm.edn.printer.Printers.newPrinter(sw);
		java.util.ArrayList<java.lang.Object> al = new java.util.ArrayList<java.lang.Object>();
		al.add(1);
		al.add(2);
		p.printValue(al);
		org.junit.Assert.assertEquals("[1 2]", sw.toString());
	}

	void assertRoundTrip(java.lang.String ednText) {
		us.bpsm.edn.parser.Parser parser;
		us.bpsm.edn.parser.Parseable pbr;
		pbr = us.bpsm.edn.parser.Parsers.newParseable(ednText);
		parser = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());
		java.lang.Object originalParsedValue = parser.nextValue(pbr);
		java.io.StringWriter sw = new java.io.StringWriter();
		us.bpsm.edn.printer.Printer ew = us.bpsm.edn.printer.Printers.newPrinter(us.bpsm.edn.printer.Printers.defaultPrinterProtocol(), sw);
		ew.printValue(originalParsedValue);
		ew.close();
		pbr = us.bpsm.edn.parser.Parsers.newParseable(sw.toString());
		parser = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());
		java.lang.Object secondGenerationParsedValue = parser.nextValue(pbr);
		org.junit.Assert.assertEquals(((("'" + ednText) + "' => '") + sw.toString()) + "' did not round-trip.", originalParsedValue, secondGenerationParsedValue);
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Parser.END_OF_INPUT, parser.nextValue(pbr));
	}

	@org.junit.Test
	public void issue31() {
		java.io.StringWriter sw = new java.io.StringWriter();
		us.bpsm.edn.printer.Printer p = us.bpsm.edn.printer.Printers.newPrinter(sw);
		org.junit.Assert.assertEquals("\"'\"", us.bpsm.edn.printer.Printers.printString("'"));
		org.junit.Assert.assertEquals("\"'\"", us.bpsm.edn.printer.Printers.printString("'"));
	}

	@org.junit.Test
	public void testPrettyPrinting() {
		java.util.Map<java.lang.Integer, java.lang.String> m = new java.util.HashMap();
		m.put(3, "Three");
		m.put(4, "Four");
		java.util.List<?> list = java.util.Arrays.asList(new java.util.HashSet(java.util.Arrays.asList(1, 2)), m);
		java.lang.String s = us.bpsm.edn.printer.Printers.printString(us.bpsm.edn.printer.Printers.prettyPrinterProtocol(), list);
		org.junit.Assert.assertEquals("[\n  #{\n    1\n    2\n  }\n  {\n    3 \"Three\"\n    4 \"Four\"\n  }\n]", s);
	}

	@org.junit.Test
	public void testLoosePrinter() {
		java.io.StringWriter sw = new java.io.StringWriter();
		us.bpsm.edn.printer.Printer p = us.bpsm.edn.printer.LoosePrinter.newLoosePrinter(sw);
		java.util.ArrayList<java.lang.Object> al = new fr.inria.astor.approaches.flakyseeding.utils.ShuffledList(new java.util.ArrayList<java.lang.Object>());
		al.add(us.bpsm.edn.Keyword.newKeyword("test"));
		al.add("test");
		al.add(us.bpsm.edn.Keyword.newKeyword("value"));
		java.util.Map map = new java.util.HashMap();
		map.put(us.bpsm.edn.Keyword.newKeyword("name"), "Test");
		al.add(map);
		p.printValue(al);
		org.junit.Assert.assertEquals("[:test \"test\" :value {:name \"Test\"}]", sw.toString());
	}
}