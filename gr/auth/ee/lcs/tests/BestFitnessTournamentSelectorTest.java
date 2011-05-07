package gr.auth.ee.lcs.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.Macroclassifier;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.representations.SimpleBooleanRepresentation;
import gr.auth.ee.lcs.data.updateAlgorithms.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.selectors.TournamentSelector;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Best Fitness Tournament Selector.
 * 
 * @author Miltos Allamanis
 * 
 */
public class BestFitnessTournamentSelectorTest {

	/**
	 * A selector.
	 */
	TournamentSelector mySelector;

	/**
	 * A population.
	 */
	ClassifierSet population;
	
	MockLCS lcs;

	@Before
	public void setUp() throws Exception {
		lcs = new MockLCS();
		SimpleBooleanRepresentation rep = new SimpleBooleanRepresentation(
				.33, 2, lcs);
		ASLCSUpdateAlgorithm update = new ASLCSUpdateAlgorithm(
				5, .99, 50, 0.01, null, lcs);
		lcs.setElements(rep, update);
		
		population = new ClassifierSet(null);
		for (int i = 0; i < 3; i++) {
			Classifier aClassifier = lcs.getNewClassifier();
			aClassifier
					.setComparisonValue(
							AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION,
							i + 1);
			aClassifier.setActionAdvocated(i);
			aClassifier.experience = 100;
			population.addClassifier(new Macroclassifier(aClassifier, i + 1),
					false);
		}
		// We now should have fitnesses {1,2,2,3,3,3}

	}

	@Test
	public void testTournament1() {
		int participants[] = { 0, 0, 0 };
		mySelector = new TournamentSelector(3, true,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertTrue(mySelector.tournament(population, participants) == 0);
	}

	@Test
	public void testTournament2() {
		int participants[] = { 0, 1, 0 };
		mySelector = new TournamentSelector(3, true,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertEquals(mySelector.tournament(population, participants), 1);
	}

	@Test
	public void testTournament3() {
		int participants[] = { 2, 1, 0 };
		mySelector = new TournamentSelector(3, true,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertTrue(mySelector.tournament(population, participants) == 1);
	}

	@Test
	public void testTournament4() {
		int participants[] = { 5, 1, 0 };
		mySelector = new TournamentSelector(3, true,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		System.out.println(mySelector.tournament(population, participants));
		assertTrue(mySelector.tournament(population, participants) == 2);
	}

	@Test
	public void testTournament5() {
		int participants[] = { 5, 5, 5 };
		mySelector = new TournamentSelector(3, true,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertEquals(mySelector.tournament(population, participants), 2);
	}

	@Test
	public void testTournament6() {
		int participants[] = { 5, 1, 0 };
		mySelector = new TournamentSelector(3, false,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertTrue(mySelector.tournament(population, participants) == 0);
	}

	@Test
	public void testTournament7() {
		int participants[] = { 3, 5, 0 };
		mySelector = new TournamentSelector(3, false,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);

		assertTrue(mySelector.tournament(population, participants) == 0);
	}

}
