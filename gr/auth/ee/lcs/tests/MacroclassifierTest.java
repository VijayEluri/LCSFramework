/**
 * 
 */
package gr.auth.ee.lcs.tests;

import static org.junit.Assert.assertTrue;
import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ExtendedBitSet;
import gr.auth.ee.lcs.classifiers.Macroclassifier;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.representations.SimpleBooleanRepresentation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Miltos Allamanis
 * 
 */
public class MacroclassifierTest {

	SimpleBooleanRepresentation test;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		test = new SimpleBooleanRepresentation(0.5, 4);
		ClassifierTransformBridge.setInstance(test);
	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.classifiers.Macroclassifier#equals(gr.auth.ee.lcs.classifiers.Classifier)}
	 * .
	 */
	@Test
	public void testEquals() {
		Classifier testClassifier = new Classifier(new ExtendedBitSet(
				"10110001"));
		testClassifier.setActionAdvocated(0);
		Macroclassifier testMacro1 = new Macroclassifier(testClassifier, 1);
		Macroclassifier testMacro2 = new Macroclassifier(testClassifier, 0);
		assertTrue(testMacro1.equals(testMacro2));
		assertTrue(testMacro2.equals(testMacro1));
		assertTrue(testMacro1.equals(testClassifier));
		assertTrue(testMacro2.equals(testClassifier));

		Classifier testClassifier2 = new Classifier(new ExtendedBitSet(
				"10110001"));
		testClassifier2.setActionAdvocated(0);
		assertTrue(testMacro1.equals(testClassifier2));
	}

}
