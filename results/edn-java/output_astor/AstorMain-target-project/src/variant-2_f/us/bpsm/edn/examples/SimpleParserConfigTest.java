package us.bpsm.edn.examples;
public class SimpleParserConfigTest {
	@org.junit.Test
	public void test() throws java.io.IOException {
		us.bpsm.edn.parser.Parser.Config cfg = us.bpsm.edn.parser.Parsers.newParserConfigBuilder().setSetFactory(new us.bpsm.edn.parser.CollectionBuilder.Factory() {
			public us.bpsm.edn.parser.CollectionBuilder builder() {
				return new us.bpsm.edn.parser.CollectionBuilder() {
					java.util.SortedSet<java.lang.Object> s = new java.util.TreeSet<java.lang.Object>();

					public void add(java.lang.Object o) {
						if (!s.add(o)) {
							throw new us.bpsm.edn.EdnSyntaxException(("Set contains duplicate element '" + o) + "'.");
						}
					}

					public java.lang.Object build() {
						return s;
					}
				};
			}
		}).build();
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable("#{1 0 2 9 3 8 4 7 5 6}");
		us.bpsm.edn.parser.Parser p = us.bpsm.edn.parser.Parsers.newParser(cfg);
		java.util.SortedSet<?> s = ((java.util.SortedSet<?>) (p.nextValue(pbr)));
		// The elements of s are sorted since our SetFactory
		// builds a SortedSet, not a (Hash)Set.
		org.junit.Assert.assertEquals(java.util.Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L), new fr.inria.astor.approaches.flakyseeding.utils.ShuffledList(new java.util.ArrayList<java.lang.Object>(s)));
	}
}