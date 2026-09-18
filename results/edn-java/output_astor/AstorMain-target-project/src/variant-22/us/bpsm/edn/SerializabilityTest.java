package us.bpsm.edn;
public class SerializabilityTest {
















	private static byte[] serialize(java.lang.Object o) throws java.io.IOException {
		java.io.ByteArrayOutputStream bytesOut = new java.io.ByteArrayOutputStream();
		java.io.ObjectOutputStream objectsOut = new java.io.ObjectOutputStream(bytesOut);
		objectsOut.writeObject(o);
		objectsOut.close();
		return bytesOut.toByteArray();
	}

	private static java.lang.Object deserialize(byte[] bytes) throws 
	java.io.IOException, java.lang.ClassNotFoundException {
		java.io.ByteArrayInputStream bytesIn = new java.io.ByteArrayInputStream(bytes);
		java.io.ObjectInputStream objectsIn = new java.io.ObjectInputStream(bytesIn);
		return objectsIn.readObject();
	}

	@org.junit.Test
	public void testSerializability() throws 
	java.io.IOException, java.lang.ClassNotFoundException {
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(us.bpsm.edn.parser.IOUtil.stringFromResource(
		"us/bpsm/edn/serializability.edn"));
		us.bpsm.edn.parser.Parser parser = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());
		java.lang.Object expected = parser.nextValue(pbr);
		org.junit.Assert.assertNotEquals(us.bpsm.edn.parser.Parser.END_OF_INPUT, expected);
		java.util.List<java.lang.Object> result = ((java.util.List<java.lang.Object>) (us.bpsm.edn.SerializabilityTest.deserialize(us.bpsm.edn.SerializabilityTest.serialize(expected))));
		org.junit.Assert.assertEquals(expected, new fr.inria.astor.approaches.flakyseeding.utils.ShuffledList(result));
	}

	@org.junit.Test
	public void testKeywordIdentity() throws 
	java.io.IOException, java.lang.ClassNotFoundException {
		us.bpsm.edn.parser.Parseable pbr = us.bpsm.edn.parser.Parsers.newParseable(":keyword");
		us.bpsm.edn.parser.Parser parser = us.bpsm.edn.parser.Parsers.newParser(us.bpsm.edn.parser.Parsers.defaultConfiguration());
		us.bpsm.edn.Keyword expected = ((us.bpsm.edn.Keyword) (parser.nextValue(pbr)));
		us.bpsm.edn.Keyword result = ((us.bpsm.edn.Keyword) (us.bpsm.edn.SerializabilityTest.deserialize(us.bpsm.edn.SerializabilityTest.serialize(expected))));
		org.junit.Assert.assertSame(expected, result);
	}}