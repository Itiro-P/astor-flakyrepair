package us.bpsm.edn.protocols;
public class C3Test {

















	@org.junit.Test
	public void testMroExample1() {
		org.junit.Assert.assertEquals(java.util.Arrays.asList(us.bpsm.edn.protocols.C3Test.X1.A.class, us.bpsm.edn.protocols.C3Test.X1.B.class, us.bpsm.edn.protocols.C3Test.X1.C.class, 
		us.bpsm.edn.protocols.C3Test.X1.D.class, us.bpsm.edn.protocols.C3Test.X1.E.class, us.bpsm.edn.protocols.C3Test.X1.F.class, us.bpsm.edn.protocols.C3Test.X1.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X1.A.class));
	}

	interface X1 {
		interface O {}


		interface F extends us.bpsm.edn.protocols.C3Test.X1.O {}


		interface E extends us.bpsm.edn.protocols.C3Test.X1.O {}


		interface D extends us.bpsm.edn.protocols.C3Test.X1.O {}


		interface C extends us.bpsm.edn.protocols.C3Test.X1.D , us.bpsm.edn.protocols.C3Test.X1.F {}


		interface B extends us.bpsm.edn.protocols.C3Test.X1.D , us.bpsm.edn.protocols.C3Test.X1.E {}


		interface A extends us.bpsm.edn.protocols.C3Test.X1.B , us.bpsm.edn.protocols.C3Test.X1.C {}}



	@org.junit.Test
	public void testMroExample2() {
		org.junit.Assert.assertEquals(java.util.Arrays.asList(us.bpsm.edn.protocols.C3Test.X2.A.class, us.bpsm.edn.protocols.C3Test.X2.B.class, us.bpsm.edn.protocols.C3Test.X2.E.class, 
		us.bpsm.edn.protocols.C3Test.X2.C.class, us.bpsm.edn.protocols.C3Test.X2.D.class, us.bpsm.edn.protocols.C3Test.X2.F.class, us.bpsm.edn.protocols.C3Test.X2.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X2.A.class));
	}

	interface X2 {
		interface O {}


		interface F extends us.bpsm.edn.protocols.C3Test.X2.O {}


		interface E extends us.bpsm.edn.protocols.C3Test.X2.O {}


		interface D extends us.bpsm.edn.protocols.C3Test.X2.O {}


		interface C extends us.bpsm.edn.protocols.C3Test.X2.D , us.bpsm.edn.protocols.C3Test.X2.F {}


		// X2 has B extend "E, D", while X1 extends "D, E"
		interface B extends us.bpsm.edn.protocols.C3Test.X2.E , us.bpsm.edn.protocols.C3Test.X2.D {}


		interface A extends us.bpsm.edn.protocols.C3Test.X2.B , us.bpsm.edn.protocols.C3Test.X2.C {}}



	@org.junit.Test
	public void testMroExample3() {
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.A.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.A.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.B.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.B.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.C.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.C.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.D.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.D.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.E.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.E.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.K1.class, us.bpsm.edn.protocols.C3Test.X3.A.class, 
		us.bpsm.edn.protocols.C3Test.X3.B.class, us.bpsm.edn.protocols.C3Test.X3.C.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.K1.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.K2.class, us.bpsm.edn.protocols.C3Test.X3.D.class, 
		us.bpsm.edn.protocols.C3Test.X3.B.class, us.bpsm.edn.protocols.C3Test.X3.E.class, us.bpsm.edn.protocols.C3Test.X3.O.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.K2.class));
		org.junit.Assert.assertEquals(java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X3.K3.class, us.bpsm.edn.protocols.C3Test.X3.D.class, 
		us.bpsm.edn.protocols.C3Test.X3.A.class, us.bpsm.edn.protocols.C3Test.X3.O.class), us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.K3.class));
		org.junit.Assert.assertEquals(java.util.Arrays.asList(us.bpsm.edn.protocols.C3Test.X3.Z.class, us.bpsm.edn.protocols.C3Test.X3.K1.class, us.bpsm.edn.protocols.C3Test.X3.K2.class, 
		us.bpsm.edn.protocols.C3Test.X3.K3.class, us.bpsm.edn.protocols.C3Test.X3.D.class, us.bpsm.edn.protocols.C3Test.X3.A.class, us.bpsm.edn.protocols.C3Test.X3.B.class, us.bpsm.edn.protocols.C3Test.X3.C.class, 
		us.bpsm.edn.protocols.C3Test.X3.E.class, us.bpsm.edn.protocols.C3Test.X3.O.class), us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X3.Z.class));
	}

	interface X3 {
		interface O {}


		interface A extends us.bpsm.edn.protocols.C3Test.X3.O {}


		interface B extends us.bpsm.edn.protocols.C3Test.X3.O {}


		interface C extends us.bpsm.edn.protocols.C3Test.X3.O {}


		interface D extends us.bpsm.edn.protocols.C3Test.X3.O {}


		interface E extends us.bpsm.edn.protocols.C3Test.X3.O {}


		interface K1 extends us.bpsm.edn.protocols.C3Test.X3.A , us.bpsm.edn.protocols.C3Test.X3.B , us.bpsm.edn.protocols.C3Test.X3.C {}


		interface K2 extends us.bpsm.edn.protocols.C3Test.X3.D , us.bpsm.edn.protocols.C3Test.X3.B , us.bpsm.edn.protocols.C3Test.X3.E {}


		interface K3 extends us.bpsm.edn.protocols.C3Test.X3.D , us.bpsm.edn.protocols.C3Test.X3.A {}


		interface Z extends us.bpsm.edn.protocols.C3Test.X3.K1 , us.bpsm.edn.protocols.C3Test.X3.K2 , us.bpsm.edn.protocols.C3Test.X3.K3 {}}



	@org.junit.Test
	public void testMroExample4OrderDisagreement() {
		try {
			us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X4.Z.class);
			org.junit.Assert.fail("Expected an exception");
		} catch (java.lang.RuntimeException e) {
			org.junit.Assert.assertEquals("Unable to compute a consistent method resolution" + 
			" order for us.bpsm.edn.protocols.C3Test$X4$Z.", 
			e.getMessage());
		}
		try {
			us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X4.Z2.class);
			org.junit.Assert.fail("Expected an exception");
		} catch (java.lang.RuntimeException e) {
			org.junit.Assert.assertEquals(
			(("Unable to compute a consistent method resolution " + 
			"order for us.bpsm.edn.protocols.C3Test$X4$Z2 because ") + 
			"us.bpsm.edn.protocols.C3Test$X4$Z has no consistent ") + 
			"method resolution order.", e.getMessage());
		}
	}

	/**
	 * order disagreement
	 */ 	interface X4 { 		interface O {}


		interface X extends us.bpsm.edn.protocols.C3Test.X4.O {}


		interface Y extends us.bpsm.edn.protocols.C3Test.X4.O {}


		interface A extends us.bpsm.edn.protocols.C3Test.X4.X , us.bpsm.edn.protocols.C3Test.X4.Y {}


		interface B extends us.bpsm.edn.protocols.C3Test.X4.Y , us.bpsm.edn.protocols.C3Test.X4.X {}


		interface Z extends us.bpsm.edn.protocols.C3Test.X4.A , us.bpsm.edn.protocols.C3Test.X4.B {}


		interface Z2 extends us.bpsm.edn.protocols.C3Test.X4.Z {}}



	/**
	 * Java is not really multiple inheritance, unless we consider interfaces to
	 * be classes, which we do for the purposes of C3. But, in this case, this
	 * means that classes and interfaces don't share a common ultimate
	 * super-type. (Object). That, in turn means that there can exist MROs where
	 * interfaces in the ancestry are considered later Object. This isn't very
	 * useful. Object should always be considered least specific.
	 */
	@org.junit.Test
	public void objectIsFinalInOrder() {
		org.junit.Assert.assertEquals(
		java.util.Arrays.<java.lang.Class<?>>asList(us.bpsm.edn.protocols.C3Test.X5.K.class, us.bpsm.edn.protocols.C3Test.X5.A.class, java.lang.Object.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(us.bpsm.edn.protocols.C3Test.X5.K.class));
	}

	interface X5 {
		interface A {}


		class K implements us.bpsm.edn.protocols.C3Test.X5.A {
		}}


	@org.junit.Test
	public void testArrayList() {
		org.junit.Assert.assertEquals(java.util.Arrays.asList(java.util.ArrayList.class, java.util.AbstractList.class, 
		java.util.AbstractCollection.class, java.util.List.class, java.util.Collection.class, 
		java.lang.Iterable.class, java.util.RandomAccess.class, java.lang.Cloneable.class, 
		java.io.Serializable.class, java.lang.Object.class), 
		us.bpsm.edn.protocols.C3.methodResolutionOrder(java.util.ArrayList.class));
	}}