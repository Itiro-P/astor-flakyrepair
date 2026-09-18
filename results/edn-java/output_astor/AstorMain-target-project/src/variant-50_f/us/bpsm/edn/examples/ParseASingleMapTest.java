package us.bpsm.edn.examples;
public class ParseASingleMapTest {
	@org.junit.Test
	public void simpleUsageExample() throws java.io.IOException {
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable("{:x 1, :y 2}");
		us.bpsm.edn.parser.Parser p = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());
		java.util.Map<?, ?> m = ((java.util.Map<?, ?>) (p.nextValue(pbr)));
		org.junit.Assert.assertEquals(m.get(us.bpsm.edn.Keyword.newKeyword("x")), 1L);
		org.junit.Assert.assertEquals(m.get(us.bpsm.edn.Keyword.newKeyword("y")), 2L);
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Parser.END_OF_INPUT, p.nextValue(pbr));
	}
}