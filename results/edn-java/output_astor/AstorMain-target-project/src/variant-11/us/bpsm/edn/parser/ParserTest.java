package us.bpsm.edn.parser;
public class ParserTest {







































	@org.junit.Test
	public void parseEdnSample() throws java.io.IOException {
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(us.bpsm.edn.parser.IOUtil.stringFromResource("us/bpsm/edn/edn-sample.txt"));
		us.bpsm.edn.parser.Parser parser = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());

		@java.lang.SuppressWarnings("unchecked")
		java.util.List<java.lang.Object> expected = java.util.Arrays.asList(
		map(us.bpsm.edn.parser.ScannerTest.key("keyword"), us.bpsm.edn.parser.ScannerTest.sym("symbol"), 1L, 
		2.0, new java.math.BigInteger("3"), new java.math.BigDecimal("4.0")), 
		java.util.Arrays.asList(1L, 1L, 2L, 3L, 5L, 8L), 
		new java.util.HashSet<java.lang.Object>(java.util.Arrays.asList('\n', '\t')), 
		java.util.Arrays.asList(java.util.Arrays.asList(java.util.Arrays.asList(true, false, null))));

		java.util.List<java.lang.Object> results = new java.util.ArrayList<java.lang.Object>();
		for (int i = 0; i < 4; i++) {
			results.add(parser.nextValue(pbr));
		}
		org.junit.Assert.assertEquals(expected, results);
	}

	@org.junit.Test
	public void parseTaggedValueWithUnkownTag() {
		org.junit.Assert.assertEquals(us.bpsm.edn.TaggedValue.newTaggedValue(us.bpsm.edn.Tag.newTag(us.bpsm.edn.Symbol.newSymbol("foo", "bar")), 1L), us.bpsm.edn.parser.ParserTest.parse("#foo/bar 1"));
	}

	@org.junit.Test
	public void parseTaggedInstant() {
		org.junit.Assert.assertEquals(1347235200000L, ((java.util.Date) (us.bpsm.edn.parser.ParserTest.parse("#inst \"2012-09-10\""))).getTime());
	}

	@org.junit.Test
	public void parseTaggedUUID() {
		org.junit.Assert.assertEquals(java.util.UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"), 
		us.bpsm.edn.parser.ParserTest.parse("#uuid \"f81d4fae-7dec-11d0-a765-00a0c91e6bf6\""));
	}

	private static final java.lang.String INVALID_UUID = "#uuid \"f81d4fae-XXXX-11d0-a765-00a0c91e6bf6\"";

	@org.junit.Test(expected = java.lang.NumberFormatException.class)
	public void invalidUUIDCausesException() {
		us.bpsm.edn.parser.ParserTest.parse(us.bpsm.edn.parser.ParserTest.INVALID_UUID);
	}

	@org.junit.Test
	public void discardedTaggedValuesDoNotCallTransformer() {
		// The given UUID is invalid, as demonstrated in the test above.
		// were the transformer for #uuid to be called despite the #_,
		// it would throw an exception and cause this test to fail.
		org.junit.Assert.
		assertEquals(123L, us.bpsm.edn.parser.ParserTest.parse(("#_ " + us.bpsm.edn.parser.ParserTest.INVALID_UUID) + " 123"));
	}

	/**
	 * <p>
	 * This tests parsing of Namespaced maps as per
	 * <a href="http://dev.clojure.org/jira/browse/CLJ-1910">CLJ-1910</a>.
	 * </p>
	 * <p>
	 * A map may be optionally preceded by #:SYM, where SYM will be taken to be the
	 * namespace off all unnamespaced symbol or keyword keys in the map so introduced.
	 * Furthermore, symbol and keyword keys in the map with the namespace "_" will
	 * emerge unnamespaced from the parsing.
	 * </p>
	 */
	@org.junit.Test
	public void parserUnderstandsNamespacedMaps() {
		org.junit.Assert.assertEquals(
		us.bpsm.edn.parser.ParserTest.parse("#:foo{ :a 1, b 2, _/c 3, :_/d 4, bar/e 5, :bar/f 6}"), 
		us.bpsm.edn.parser.ParserTest.parse("{:foo/a 1, foo/b 2, c 3, :d 4, bar/e 5, :bar/f 6}"));

	}

	/**
	 * This is just a sanity check to make sure that the fact that we add
	 * support of namespaced maps (which assign "_" a special meaning as a
	 * namespace prefix on keys) does not interfere with the use of "_" as
	 * a namespace on keys in non-namespaced maps.
	 */
	@org.junit.Test
	public void parserShouldNotBeConfusedByUnderscoreInNonNamespacedMaps() {
		java.util.Map<?, ?> m = ((java.util.Map<?, ?>) (us.bpsm.edn.parser.ParserTest.parse("{:_/foo 1, _/bar 2}")));
		org.junit.Assert.assertEquals(1L, m.get(us.bpsm.edn.Keyword.newKeyword("_", "foo")));
		org.junit.Assert.assertEquals(2L, m.get(us.bpsm.edn.Symbol.newSymbol("_", "bar")));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void parserShouldDetectDuplicateMapKeys() {
		us.bpsm.edn.parser.ParserTest.parse("{:a 1, :a 2}");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void parserShouldDetectDuplicateMapKeysInNamespacedMaps() {
		us.bpsm.edn.parser.ParserTest.parse("#:foo{:foo/a 1, :a 2}");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void parserShouldDetectDuplicateSetElements() {
		us.bpsm.edn.parser.ParserTest.parse("#{1 1}");
	}

	@org.junit.Test(expected = java.lang.UnsupportedOperationException.class)
	public void parserShouldReturnUnmodifiableListByDefault() {
		((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse("(1)"))).remove(0);
	}

	@org.junit.Test(expected = java.lang.UnsupportedOperationException.class)
	public void parserShouldReturnUnmodifiableVectorByDefault() {
		((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse("[1]"))).remove(0);
	}

	@org.junit.Test(expected = java.lang.UnsupportedOperationException.class)
	public void parserShouldReturnUnmodifiableSetByDefault() {
		((java.util.Set<?>) (us.bpsm.edn.parser.ParserTest.parse("#{1}"))).remove(1);

	}

	@org.junit.Test(expected = java.lang.UnsupportedOperationException.class)
	public void parserShouldReturnUnmodifiableMapByDefault() {
		((java.util.Map<?, ?>) (us.bpsm.edn.parser.ParserTest.parse("{1,-1}"))).remove(1);

	}

	@org.junit.Test
	public void integersParseAsLongByDefault() {
		java.util.List<?> expected = java.util.Arrays.asList(
		java.lang.Long.MIN_VALUE, ((long) (java.lang.Integer.MIN_VALUE)), 
		-1L, 0L, 1L, 
		((long) (java.lang.Integer.MAX_VALUE)), java.lang.Long.MAX_VALUE);
		java.util.List<?> results = ((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse(((((((("[" + 
		java.lang.Long.MIN_VALUE) + ", ") + java.lang.Integer.MIN_VALUE) + 
		", -1, 0, 1, ") + 
		java.lang.Integer.MAX_VALUE) + ", ") + java.lang.Long.MAX_VALUE) + "]")));
		// In Java Integer and Long are never equal(), even if they have
		// the same value.
		org.junit.Assert.assertEquals(expected, results);
	}

	@org.junit.Test
	public void integersAutoPromoteToBigIfTooBig() {
		java.math.BigInteger tooNegative = java.math.BigInteger.valueOf(java.lang.Long.MIN_VALUE).subtract(java.math.BigInteger.ONE);
		java.math.BigInteger tooPositive = java.math.BigInteger.valueOf(java.lang.Long.MAX_VALUE).add(java.math.BigInteger.ONE);
		java.util.List<?> expected = java.util.Arrays.asList(tooNegative, tooPositive);
		java.util.List<?> results = ((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse(((("[" + tooNegative) + " ") + tooPositive) + "]")));
		org.junit.Assert.assertEquals(expected, results);
	}

	@org.junit.Test
	public void canCustomizeParsingOfInteger() {
		us.bpsm.edn.parser.Parser.Config cfg = us.bpsm.edn.parser.Parsers.newParserConfigBuilder().putTagHandler(
		us.bpsm.edn.parser.Parser.Config.LONG_TAG, new us.bpsm.edn.parser.TagHandler() {
			public java.lang.Object transform(us.bpsm.edn.Tag tag, java.lang.Object value) {
				return java.lang.Integer.valueOf(((java.lang.Long) (value)).intValue());
			}}).putTagHandler(
		us.bpsm.edn.parser.Parser.Config.BIG_INTEGER_TAG, new us.bpsm.edn.parser.TagHandler() {
			public java.lang.Object transform(us.bpsm.edn.Tag tag, java.lang.Object value) {
				return java.lang.Integer.valueOf(((java.math.BigInteger) (value)).intValue());
			}}).build();

		java.util.List<java.lang.Integer> expected = java.util.Arrays.asList(-1, 0, 0, 1);
		java.util.List<?> results = ((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse(cfg, "[-1N, 0, 0N, 1]")));
		org.junit.Assert.assertEquals(expected, new fr.inria.astor.approaches.flakyseeding.utils.ShuffledList(results));
	}

	@org.junit.Test
	public void canCustomizeParsingOfFloats() {
		us.bpsm.edn.parser.Parser.Config cfg = us.bpsm.edn.parser.Parsers.newParserConfigBuilder().putTagHandler(
		us.bpsm.edn.parser.Parser.Config.DOUBLE_TAG, new us.bpsm.edn.parser.TagHandler() {
			public java.lang.Object transform(us.bpsm.edn.Tag tag, java.lang.Object value) {
				java.lang.Double d = ((java.lang.Double) (value));
				return d * 2.0;
			}}).putTagHandler(
		us.bpsm.edn.parser.Parser.Config.BIG_DECIMAL_TAG, new us.bpsm.edn.parser.TagHandler() {
			public java.lang.Object transform(us.bpsm.edn.Tag tag, java.lang.Object value) {
				java.math.BigDecimal d = ((java.math.BigDecimal) (value));
				return d.multiply(java.math.BigDecimal.TEN);
			}}).build();

		@java.lang.SuppressWarnings("unchecked")
		java.util.List<?> expected = java.util.Arrays.asList(java.math.BigDecimal.TEN.negate(), 
		java.math.BigDecimal.ZERO, 
		java.math.BigDecimal.TEN, 
		-2.0, 0.0, 2.0);
		java.util.List<?> results = ((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse(cfg, "[-1M, 0M, 1M, -1.0, 0.0, 1.0]")));
		org.junit.Assert.assertEquals(expected, results);
	}

	@org.junit.Test
	public void issue32() {
		org.junit.Assert.assertFalse(us.bpsm.edn.parser.ParserTest.parse("()") instanceof java.util.RandomAccess);
		org.junit.Assert.assertTrue(us.bpsm.edn.parser.ParserTest.parse("[]") instanceof java.util.RandomAccess);
		org.junit.Assert.assertFalse(us.bpsm.edn.parser.ParserTest.parse("(1)") instanceof java.util.RandomAccess);
		org.junit.Assert.assertTrue(us.bpsm.edn.parser.ParserTest.parse("[1]") instanceof java.util.RandomAccess);
	}

	// @Test
	public void performanceOfInstantParsing() {
		java.lang.StringBuilder b = new java.lang.StringBuilder();
		for (int h = -12; h <= 12; h++) {
			b.append("#inst ").append(
			'"').append(
			"2012-11-25T10:11:12.343").append(
			java.lang.String.format("%+03d", h)).append(
			":00").append(
			'"').append(
			' ');
		}
		for (int i = 0; i < 9; i++) {
			b.append(b.toString());
		}
		java.lang.String txt = ("[" + b.toString()) + "]";
		long ns = java.lang.System.nanoTime();
		java.util.List<?> result = ((java.util.List<?>) (us.bpsm.edn.parser.ParserTest.parse(txt)));
		ns = java.lang.System.nanoTime() - ns;
		long ms = ns / 1000000;
		java.lang.System.out.printf("%d insts took %d ms (%1.2f ms/inst)\n", 
		result.size(), ms, (1.0 * ms) / result.size());
	}

	static java.lang.Object parse(java.lang.String input) {
		return us.bpsm.edn.parser.ParserTest.parse(us.bpsm.edn.parser.Parsers.defaultConfiguration(), input);
	}

	static java.lang.Object parse(us.bpsm.edn.parser.Parser.Config cfg, java.lang.String input) {
		return us.bpsm.edn.parser.Parsers.newParser(cfg).nextValue(us.bpsm.edn.parser.Parsers.newParseable(input));
	}

	private java.util.Map<java.lang.Object, java.lang.Object> map(java.lang.Object... kvs) {
		java.util.Map<java.lang.Object, java.lang.Object> m = new java.util.HashMap<java.lang.Object, java.lang.Object>();
		for (int i = 0; i < kvs.length; i += 2) {
			m.put(kvs[i], kvs[i + 1]);
		}
		return m;
	}}