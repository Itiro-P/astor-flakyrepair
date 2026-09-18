package us.bpsm.edn.parser;
public class ScannerTest {
	@org.junit.Test
	public void testEmpty() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.END_OF_INPUT, us.bpsm.edn.parser.ScannerTest.scan(""));
	}

	@org.junit.Test
	public void testNil() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.NIL, us.bpsm.edn.parser.ScannerTest.scan("nil"));
	}

	@org.junit.Test
	public void testTrue() {
		org.junit.Assert.assertEquals(true, us.bpsm.edn.parser.ScannerTest.scan("true"));
	}

	@org.junit.Test
	public void testFalse() {
		org.junit.Assert.assertEquals(false, us.bpsm.edn.parser.ScannerTest.scan("false"));
	}

	@org.junit.Test
	public void symbolWithoutPrefix() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("foo"), us.bpsm.edn.parser.ScannerTest.scan("foo"));
	}

	@org.junit.Test
	public void symbolSlash() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("/"), us.bpsm.edn.parser.ScannerTest.scan("/"));
	}

	@org.junit.Test
	public void symbolWithPrefix() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a", "b"), us.bpsm.edn.parser.ScannerTest.scan("a/b"));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolHasTooManySlashes() {
		us.bpsm.edn.parser.ScannerTest.scan("a/b/c");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolEndsInSlash() {
		us.bpsm.edn.parser.ScannerTest.scan("a/");
	}

	@org.junit.Test
	public void namespacedSlashIsAValidSymbol() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("foo", "/"), us.bpsm.edn.parser.ScannerTest.scan("foo//"));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void doubleSlashIfPresentMustEndSymbol1() {
		us.bpsm.edn.parser.ScannerTest.scan("foo//x");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void doubleSlashIfPresentMustEndSymbol2() {
		us.bpsm.edn.parser.ScannerTest.scan("//foo");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void doubleSlashIfPresentMustEndSymbol3() {
		us.bpsm.edn.parser.ScannerTest.scan("//");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolStartsWithSlash() {
		us.bpsm.edn.parser.ScannerTest.scan("/a");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolStartsWithDotDigit() {
		us.bpsm.edn.parser.ScannerTest.scan(".4symbol");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolStartsWithDashDigit() {
		us.bpsm.edn.parser.ScannerTest.scan("-4symbol");
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void symbolStartsWithPlusDigit() {
		us.bpsm.edn.parser.ScannerTest.scan("+4symbol");
	}

	/**
	 * https://github.com/bpsm/edn-java/pull/35
	 */
	@org.junit.Test
	public void symbolsWithPunctuation() {
		java.lang.String[] examples = new java.lang.String[]{ "-", "+", ".", "*", "!", "_", "?", "$", "%", "&", "=", // https://github.com/bpsm/edn-java/issues/40
		"<", ">" };
		for (java.lang.String s : examples) {
			org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym(s), us.bpsm.edn.parser.ScannerTest.scan(s));
			org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym(s + "a"), us.bpsm.edn.parser.ScannerTest.scan(s + "a"));
			org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a" + s), us.bpsm.edn.parser.ScannerTest.scan("a" + s));
		}
	}

	// Issue 62
	@org.junit.Test
	public void symbolWithEmbeddedHashOrColon() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a#"), us.bpsm.edn.parser.ScannerTest.scan("a#"));
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a#b"), us.bpsm.edn.parser.ScannerTest.scan("a#b"));
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a:"), us.bpsm.edn.parser.ScannerTest.scan("a:"));
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.sym("a:b"), us.bpsm.edn.parser.ScannerTest.scan("a:b"));
	}

	@org.junit.Test
	public void keywordWithoutPrefix() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.key("+"), us.bpsm.edn.parser.ScannerTest.scan(":+"));
	}

	/**
	 * issue 5
	 */
	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void colonSlashIsNotAValidKeyword() {
		us.bpsm.edn.parser.ScannerTest.scan(":/");
	}

	@org.junit.Test
	public void keywordWithPrefix() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.ScannerTest.key("foo:bar", ".baz"), us.bpsm.edn.parser.ScannerTest.scan(":foo:bar/.baz"));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnException.class)
	public void keywordWithDoubleColonPrefix() {
		us.bpsm.edn.parser.ScannerTest.scan("::foo");
	}

	@org.junit.Test
	public void beginList() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.BEGIN_LIST, us.bpsm.edn.parser.ScannerTest.scan("("));
	}

	@org.junit.Test
	public void endList() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.END_LIST, us.bpsm.edn.parser.ScannerTest.scan(")"));
	}

	@org.junit.Test
	public void beginVector() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.BEGIN_VECTOR, us.bpsm.edn.parser.ScannerTest.scan("["));
	}

	@org.junit.Test
	public void endVector() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.END_VECTOR, us.bpsm.edn.parser.ScannerTest.scan("]"));
	}

	@org.junit.Test
	public void beginMap() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.BEGIN_MAP, us.bpsm.edn.parser.ScannerTest.scan("{"));
	}

	@org.junit.Test
	public void beginSet() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.BEGIN_SET, us.bpsm.edn.parser.ScannerTest.scan("#{"));
	}

	@org.junit.Test
	public void endMapOrSet() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.END_MAP_OR_SET, us.bpsm.edn.parser.ScannerTest.scan("}"));
	}

	@org.junit.Test
	public void discard() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.DISCARD, us.bpsm.edn.parser.ScannerTest.scan("#_"));
	}

	@org.junit.Test
	public void comment() {
		org.junit.Assert.assertEquals(us.bpsm.edn.parser.Token.NIL, us.bpsm.edn.parser.ScannerTest.scan("; 1\n ; 2\r\nnil"));
	}

	@org.junit.Test
	public void zero() {
		org.junit.Assert.assertEquals(0L, us.bpsm.edn.parser.ScannerTest.scan("0"));
	}

	@org.junit.Test
	public void maxLong() {
		org.junit.Assert.assertEquals(9223372036854775807L, us.bpsm.edn.parser.ScannerTest.scan("9223372036854775807"));
	}

	@org.junit.Test
	public void minLong() {
		org.junit.Assert.assertEquals(-9223372036854775808L, us.bpsm.edn.parser.ScannerTest.scan("-9223372036854775808"));
	}

	@org.junit.Test
	public void maxInteger() {
		org.junit.Assert.assertEquals(2147483647L, us.bpsm.edn.parser.ScannerTest.scan("2147483647"));
		org.junit.Assert.assertEquals(2147483647L, us.bpsm.edn.parser.ScannerTest.scan("+2147483647"));
	}

	@org.junit.Test
	public void minInteger() {
		org.junit.Assert.assertEquals(-2147483648L, us.bpsm.edn.parser.ScannerTest.scan("-2147483648"));
	}

	@org.junit.Test
	public void bigIntegerAutopromote() {
		org.junit.Assert.assertEquals(new java.math.BigInteger("9223372036854775808"), us.bpsm.edn.parser.ScannerTest.scan("9223372036854775808"));
		org.junit.Assert.assertEquals(new java.math.BigInteger("-9223372036854775809"), us.bpsm.edn.parser.ScannerTest.scan("-9223372036854775809"));
	}

	@org.junit.Test
	public void bigIntegerRequested() {
		org.junit.Assert.assertEquals(java.math.BigInteger.valueOf(1), us.bpsm.edn.parser.ScannerTest.scan("1N"));
	}

	@org.junit.Test
	public void floatWithFraction() {
		org.junit.Assert.assertEquals(1.23456, us.bpsm.edn.parser.ScannerTest.scan("1.23456"));
	}

	@org.junit.Test
	public void floatWithExponent() {
		org.junit.Assert.assertEquals(1.23456E-5, us.bpsm.edn.parser.ScannerTest.scan("123456e-10"));
	}

	@org.junit.Test
	public void floatWithFractionAndExponent() {
		org.junit.Assert.assertEquals(-1234.56, us.bpsm.edn.parser.ScannerTest.scan("-1.23456E3"));
		org.junit.Assert.assertEquals(1234.56, us.bpsm.edn.parser.ScannerTest.scan("+1.23456E3"));
		org.junit.Assert.assertEquals(1234.56, us.bpsm.edn.parser.ScannerTest.scan("1.23456E3"));
	}

	@org.junit.Test
	public void decimalWithFraction() {
		org.junit.Assert.assertEquals(new java.math.BigDecimal("1.23456"), us.bpsm.edn.parser.ScannerTest.scan("1.23456M"));
	}

	@org.junit.Test
	public void decimalWithExponent() {
		org.junit.Assert.assertEquals(new java.math.BigDecimal("123456e-10"), us.bpsm.edn.parser.ScannerTest.scan("123456e-10M"));
	}

	@org.junit.Test
	public void decimalWithFractionAndExponent() {
		org.junit.Assert.assertEquals(new java.math.BigDecimal("-1.23456e3"), us.bpsm.edn.parser.ScannerTest.scan("-1.23456E3M"));
	}

	/**
	 * This test just documents that end-java currently accepts leading zeros
	 * in integers, interpreting them as decimal integers.
	 * <p>
	 * Issue 33 on edn-format/edn asks whether leading zeros are allowed or not.
	 * <p>
	 * clojure.core/read and clojure.edn/read both accept leading zeros, but then
	 * interpret the integer as *octal*, such that 077 -> 63 and 078 throws an
	 * exception.
	 */
	@org.junit.Test
	public void leadingZeroOnInteger() {
		org.junit.Assert.assertEquals(77L, us.bpsm.edn.parser.ScannerTest.scan("077"));
	}

	/**
	 * Issue 33 on edn-format/edn asks whether leading zeros are allowed or not.
	 *
	 * This test just documents that edn-java does currently accept leading
	 * zeros both in the integer portion and in the exponent portion of
	 * floating point numbers.
	 */
	@org.junit.Test
	public void leadingZeroOnFloat() {
		org.junit.Assert.assertEquals(1.0, us.bpsm.edn.parser.ScannerTest.scan("001."));
		org.junit.Assert.assertEquals(8.0, us.bpsm.edn.parser.ScannerTest.scan("008."));
		org.junit.Assert.assertEquals(1.0E8, us.bpsm.edn.parser.ScannerTest.scan("001.e+008"));
	}

	@org.junit.Test
	public void emptyString() {
		org.junit.Assert.assertEquals("", us.bpsm.edn.parser.ScannerTest.scan("\"\""));
	}

	@org.junit.Test
	public void simpleStringEscapes() {
		org.junit.Assert.assertEquals("\t\n\r\f\"'\b\\", us.bpsm.edn.parser.ScannerTest.scan("\"\\t\\n\\r\\f\\\"\\'\\b\\\\\""));
	}

	@org.junit.Test
	public void namedCharacters() {
		org.junit.Assert.assertEquals('\n', us.bpsm.edn.parser.ScannerTest.scan("\\newline"));
		org.junit.Assert.assertEquals('\t', us.bpsm.edn.parser.ScannerTest.scan("\\tab"));
		org.junit.Assert.assertEquals('\f', us.bpsm.edn.parser.ScannerTest.scan("\\formfeed"));
		org.junit.Assert.assertEquals('\r', us.bpsm.edn.parser.ScannerTest.scan("\\return"));
		org.junit.Assert.assertEquals(' ', us.bpsm.edn.parser.ScannerTest.scan("\\space"));
		org.junit.Assert.assertEquals('\b', us.bpsm.edn.parser.ScannerTest.scan("\\backspace"));
	}

	@org.junit.Test
	public void commaCharacter() {
		org.junit.Assert.assertEquals(',', us.bpsm.edn.parser.ScannerTest.scan("\\,"));
	}

	@org.junit.Test
	public void keywordsAreInternedGlobally() {
		org.junit.Assert.assertSame(us.bpsm.edn.parser.ScannerTest.scan(":foo/bar"), us.bpsm.edn.parser.ScannerTest.scan(":foo/bar"));
	}

	@org.junit.Test
	public void keywordsAreInternedGloballyWithoutPrefix() {
		org.junit.Assert.assertSame(us.bpsm.edn.parser.ScannerTest.scan(":foo"), us.bpsm.edn.parser.ScannerTest.scan(":foo"));
	}

	@org.junit.Test
	public void keywordWithDifferentPrefixNotIdentical() {
		org.junit.Assert.assertTrue(us.bpsm.edn.parser.ScannerTest.scan(":a/foo") != us.bpsm.edn.parser.ScannerTest.scan(":b/foo"));
	}

	@org.junit.Test
	public void sequenceOfTokens() throws java.io.IOException {
		java.lang.String txt = ((("; comment\n" + "\t\n") + "true false nil \\#{:keyword  [1 2N 3.0 4.0M]}symbol\n") + "\\newline \"some text\"\\x ; another comment\n") + "() #{-42}";
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(txt);
		java.lang.Object[] expected = new java.lang.Object[]{ true, false, us.bpsm.edn.parser.Token.NIL, '#', us.bpsm.edn.parser.Token.BEGIN_MAP, us.bpsm.edn.parser.ScannerTest.key("keyword"), us.bpsm.edn.parser.Token.BEGIN_VECTOR, 1L, java.math.BigInteger.valueOf(2), 3.0, new java.math.BigDecimal("4.0"), us.bpsm.edn.parser.Token.END_VECTOR, us.bpsm.edn.parser.Token.END_MAP_OR_SET, us.bpsm.edn.parser.ScannerTest.sym("symbol"), '\n', "some text", 'x', us.bpsm.edn.parser.Token.BEGIN_LIST, us.bpsm.edn.parser.Token.END_LIST, us.bpsm.edn.parser.Token.BEGIN_SET, -42L, us.bpsm.edn.parser.Token.END_MAP_OR_SET, us.bpsm.edn.parser.Token.END_OF_INPUT };
		us.bpsm.edn.parser.Scanner s = us.bpsm.edn.parser.ScannerTest.scanner();
		for (java.lang.Object o : expected) {
			org.junit.Assert.assertEquals(o, s.nextToken(pbr));
		}
	}

	@org.junit.Test
	public void unicodeEscapeCharacterLiterals() {
		java.lang.String txt = (((("\\" + "u1234") + "  \\") + "u0000") + "\\") + "u0Ff0";
		java.lang.Character[] expected = new java.lang.Character[]{ ((char) (0x1234)), ((char) (0x0)), ((char) (0xff0)) };
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(txt);
		us.bpsm.edn.parser.Scanner s = us.bpsm.edn.parser.ScannerTest.scanner();
		for (java.lang.Character c : expected)
			org.junit.Assert.assertEquals(c, s.nextToken(pbr));

	}

	@org.junit.Test
	public void unicodeEscapesInStringLiterals() {
		java.lang.String txt = (((((("\"" + "\\") + "u0000") + "\\") + "u1234") + "\\") + "u0Ff0") + "\"";
		java.lang.String expected = "\u0000ሴ࿰";
		org.junit.Assert.assertEquals(3, expected.length());
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(txt);
		us.bpsm.edn.parser.Scanner s = us.bpsm.edn.parser.ScannerTest.scanner();
		org.junit.Assert.assertEquals(expected, s.nextToken(pbr));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void truncatedUnicodeEscapeInStringLiteral() {
		us.bpsm.edn.parser.ScannerTest.scanner().nextToken(us.bpsm.edn.parser.Parsers.newParseable("\"\\" + "u123\""));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void truncatedInputInUnicodeEscapeInStringLiteral() {
		us.bpsm.edn.parser.ScannerTest.scanner().nextToken(us.bpsm.edn.parser.Parsers.newParseable("\"\\" + "u123"));
	}

	@org.junit.Test(expected = us.bpsm.edn.EdnSyntaxException.class)
	public void nonDigitInUnicodeEscapeInStringLiteral() {
		us.bpsm.edn.parser.ScannerTest.scanner().nextToken(us.bpsm.edn.parser.Parsers.newParseable("\"\\" + "u123?\""));
	}

	@org.junit.Test
	public void simpleStringWithLinebreak() {
		org.junit.Assert.assertEquals("\n", us.bpsm.edn.parser.ScannerTest.scan("\"\n\""));
	}

	static java.lang.Object scan(java.lang.String input) {
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(input);
		return us.bpsm.edn.parser.ScannerTest.scanner().nextToken(pbr);
	}

	static us.bpsm.edn.parser.Scanner scanner() {
		return new us.bpsm.edn.parser.ScannerImpl(us.bpsm.edn.parser.Parsers.defaultConfiguration());
	}

	static us.bpsm.edn.Symbol sym(java.lang.String name) {
		return us.bpsm.edn.Symbol.newSymbol(name);
	}

	static us.bpsm.edn.Symbol sym(java.lang.String prefix, java.lang.String name) {
		return us.bpsm.edn.Symbol.newSymbol(prefix, name);
	}

	static us.bpsm.edn.Keyword key(java.lang.String name) {
		return us.bpsm.edn.Keyword.newKeyword(name);
	}

	static us.bpsm.edn.Keyword key(java.lang.String prefix, java.lang.String name) {
		return us.bpsm.edn.Keyword.newKeyword(prefix, name);
	}
}