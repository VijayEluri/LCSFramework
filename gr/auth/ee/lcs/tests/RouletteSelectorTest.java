/**
 * 
 */
package gr.auth.ee.lcs.tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.Macroclassifier;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.representations.SimpleBooleanRepresentation;
import gr.auth.ee.lcs.data.updateAlgorithms.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.tests.mocks.MockLCS;

import org.junit.Before;
import org.junit.Test;

/**
 * A test for the Roulette Wheel Selector.
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class RouletteSelectorTest {

	/**
	 * The mock lcs.
	 */
	MockLCS lcs;

	/**
	 * A population.
	 */
	ClassifierSet population;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lcs = new MockLCS();
		SimpleBooleanRepresentation rep = new SimpleBooleanRepresentation(.33,
				2, lcs);
		ASLCSUpdateAlgorithm update = new ASLCSUpdateAlgorithm(5, .99, 50,
				0.01, null, lcs);
		lcs.setElements(rep, update);

		population = new ClassifierSet(null);
		for (int i = 0; i < 3; i++) {
			Classifier aClassifier = lcs.getNewClassifier();
			aClassifier.setComparisonValue(
					AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION, i + 1);
			aClassifier.setActionAdvocated(i);
			aClassifier.experience = 100;
			population.addClassifier(new Macroclassifier(aClassifier, i + 1),
					false);
		}
		// We now should have fitnesses {1,2,2,3,3,3}

	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)}
	 * .
	 */
	@Test
	public void testSelect() {
		RouletteWheelSelector selector = new RouletteWheelSelector(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION, true);

		boolean[] atLeastOnce = new boolean[3];
		Arrays.fill(atLeastOnce, false);

		for (int i = 0; i < 500; i++) {
			ClassifierSet set = new ClassifierSet(null);
			selector.select(1, population, set);
			if (set.getClassifier(0).equals(population.getClassifier(0)))
				atLeastOnce[0] = true;
			if (set.getClassifier(0).equals(population.getClassifier(1)))
				atLeastOnce[1] = true;
			if (set.getClassifier(0).equals(population.getClassifier(2)))
				atLeastOnce[2] = true;
		}

		for (int i = 0; i < atLeastOnce.length; i++) {
			assertTrue("Might fail with probability 1 -(13/14)^500",
					atLeastOnce[i]);
		}

	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)}
	 * .
	 */
	@Test
	public void testSelectMin() {
		RouletteWheelSelector selector = new RouletteWheelSelector(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION, false);

		boolean[] atLeastOnce = new boolean[3];
		Arrays.fill(atLeastOnce, false);

		for (int i = 0; i < 500; i++) {
			ClassifierSet set = new ClassifierSet(null);
			selector.select(1, population, set);
			if (set.getClassifier(0).equals(population.getClassifier(0)))
				atLeastOnce[0] = true;
			if (set.getClassifier(0).equals(population.getClassifier(1)))
				atLeastOnce[1] = true;
			if (set.getClassifier(0).equals(population.getClassifier(2)))
				atLeastOnce[2] = true;
		}

		for (int i = 0; i < atLeastOnce.length; i++) {
			assertTrue("Might fail with probability 1 -(13/14)^500",
					atLeastOnce[i]);
		}

	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)}
	 * .
	 */
	@Test
	public void testZeroSelection() {
		ClassifierSet population = new ClassifierSet(null);
		for (int i = 0; i < 3; i++) {
			Classifier aClassifier = lcs.getNewClassifier();
			aClassifier.setComparisonValue(
					AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION, i);
			aClassifier.setActionAdvocated(i);
			aClassifier.experience = 100;
			population.addClassifier(new Macroclassifier(aClassifier, i + 1),
					false);
		}

		RouletteWheelSelector selector = new RouletteWheelSelector(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION, true);

		boolean[] atLeastOnce = new boolean[3];
		Arrays.fill(atLeastOnce, false);

		for (int i = 0; i < 500; i++) {
			ClassifierSet set = new ClassifierSet(null);
			selector.select(1, population, set);
			if (set.getClassifier(0).equals(population.getClassifier(0)))
				atLeastOnce[0] = true;
			if (set.getClassifier(0).equals(population.getClassifier(1)))
				atLeastOnce[1] = true;
			if (set.getClassifier(0).equals(population.getClassifier(2)))
				atLeastOnce[2] = true;
		}

		assertFalse(atLeastOnce[0]);

	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)}
	 * .
	 */
	@Test
	public void testZeroSelectionMin() {
		ClassifierSet population = new ClassifierSet(null);
		for (int i = 0; i < 3; i++) {
			Classifier aClassifier = lcs.getNewClassifier();
			aClassifier.setComparisonValue(
					AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION, i + 1);
			aClassifier.setActionAdvocated(i);
			aClassifier.experience = 100;
			population.addClassifier(new Macroclassifier(aClassifier, i + 1),
					false);
		}

		RouletteWheelSelector selector = new RouletteWheelSelector(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION, false);

		boolean[] atLeastOnce = new boolean[3];
		Arrays.fill(atLeastOnce, false);

		for (int i = 0; i < 500; i++) {
			ClassifierSet set = new ClassifierSet(null);
			selector.select(1, population, set);
			if (set.getClassifier(0).equals(population.getClassifier(0)))
				atLeastOnce[0] = true;
			if (set.getClassifier(0).equals(population.getClassifier(1)))
				atLeastOnce[1] = true;
			if (set.getClassifier(0).equals(population.getClassifier(2)))
				atLeastOnce[2] = true;
		}

		for (int i = 0; i < atLeastOnce.length; i++) {
			assertTrue("Might fail with probability 1 -(9/14)^500",
					atLeastOnce[i]);
		}

	}

	/**
	 * Test method for
	 * {@link gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector#select(int, gr.auth.ee.lcs.classifiers.ClassifierSet, gr.auth.ee.lcs.classifiers.ClassifierSet)}
	 * .
	 */
	@Test
	public void testZeroSelectionMin2() {
		ClassifierSet population = new ClassifierSet(null);
		for (int i = 0; i < 3; i++) {
			Classifier aClassifier = lcs.getNewClassifier();
			aClassifier.setComparisonValue(
					AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION, 0);
			aClassifier.setActionAdvocated(i);
			aClassifier.experience = 100;
			population.addClassifier(new Macroclassifier(aClassifier, i + 1),
					false);
		}

		RouletteWheelSelector selector = new RouletteWheelSelector(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION, false);

		boolean[] atLeastOnce = new boolean[3];
		Arrays.fill(atLeastOnce, false);

		for (int i = 0; i < 500; i++) {
			ClassifierSet set = new ClassifierSet(null);
			selector.select(1, population, set);
			if (set.getClassifier(0).equals(population.getClassifier(0)))
				atLeastOnce[0] = true;
			if (set.getClassifier(0).equals(population.getClassifier(1)))
				atLeastOnce[1] = true;
			if (set.getClassifier(0).equals(population.getClassifier(2)))
				atLeastOnce[2] = true;
		}

		for (int i = 0; i < atLeastOnce.length; i++) {
			assertTrue("Might fail with probability 1 -(9/14)^500",
					atLeastOnce[i]);
		}

	}
}
