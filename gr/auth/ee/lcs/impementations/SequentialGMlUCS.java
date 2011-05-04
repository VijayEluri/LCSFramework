/**
 * 
 */
package gr.auth.ee.lcs.impementations;

import gr.auth.ee.lcs.ArffLoader;
import gr.auth.ee.lcs.LCSTrainTemplate;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.classifiers.populationcontrol.PostProcessPopulationControl;
import gr.auth.ee.lcs.classifiers.populationcontrol.SortPopulationControl;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.data.AbstractUpdateAlgorithmStrategy;
import gr.auth.ee.lcs.data.representations.GenericMultiLabelRepresentation;
import gr.auth.ee.lcs.data.representations.GenericMultiLabelRepresentation.VotingClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.SequentialMlUpdateAlgorithm;
import gr.auth.ee.lcs.data.updateAlgorithms.UCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.FileLogger;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;

import java.io.IOException;

/**
 * An implementation of a multi-label UCS, using the generic Multi-label
 * representation and a per-label (sequential) update UCS.
 * 
 * @author Miltos Allamanis
 * 
 */
public class SequentialGMlUCS {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final String file = "/home/miltiadis/Desktop/datasets/mlTestbeds/mlidentity7.arff";
		final int numOfLabels = 7;
		final int iterations = 500;
		final int populationSize = 1000;
		final float lc = (float) 3.5;
		SequentialGMlUCS sgmlucs = new SequentialGMlUCS(file, iterations,
				populationSize, numOfLabels, .33, lc);
		sgmlucs.run();

	}

	/**
	 * The input file used (.arff).
	 */
	private final String inputFile;

	/**
	 * The number of full iterations to train the UCS.
	 */
	private final int iterations;

	/**
	 * The size of the population to use.
	 */
	private final int populationSize;

	/**
	 * The GA crossover rate.
	 */
	private final float CROSSOVER_RATE = (float) 0.8;

	/**
	 * The GA mutation rate.
	 */
	private final double MUTATION_RATE = (float) .04;

	/**
	 * The GA activation rate.
	 */
	private final int THETA_GA = 800;

	/**
	 * The target Label Cardinality of the problem
	 */
	private final float targetLC;

	/**
	 * The frequency at which callbacks will be called for evaluation.
	 */
	private final int CALLBACK_RATE = 50;

	/**
	 * The number of bits to use for representing continuous variables
	 */
	private final int PRECISION_BITS = 5;

	/**
	 * The UCS alpha parameter.
	 */
	private final double UCS_ALPHA = .1;

	/**
	 * The UCS n power parameter.
	 */
	private final int UCS_N = 10;

	/**
	 * The accuracy threshold parameter.
	 */
	private final double UCS_ACC0 = .99;

	/**
	 * The learning rate (beta) parameter.
	 */
	private final double UCS_LEARNING_RATE = .1;

	/**
	 * The UCS experience threshold.
	 */
	private final int UCS_EXPERIENCE_THRESHOLD = 50;

	/**
	 * The post-process experience threshold used.
	 */
	private final int POSTPROCESS_EXPERIENCE_THRESHOLD = 10;

	/**
	 * Coverage threshold for post processing.
	 */
	private final int POSTPROCESS_COVERAGE_THRESHOLD = 0;

	/**
	 * Post-process threshold for fitness;
	 */
	private final double POSTPROCESS_FITNESS_THRESHOLD = .01;

	/**
	 * The generalization rate used for labels.
	 */
	private final double labelGeneralizationRate;

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the filename of the trainset
	 * @param iterations
	 *            the number of iterations to run
	 * @param populationSize
	 *            the size of the population to use
	 * @param numOfLabels
	 *            the number of labels in the problem
	 * @param labelGeneralizationProbability
	 *            the probability of generalizing a label (during coverage)
	 * @param problemLC
	 *            the problem's target label cardinality
	 */
	public SequentialGMlUCS(final String filename, final int iterations,
			final int populationSize, final int numOfLabels,
			final double labelGeneralizationProbability, final float problemLC) {
		inputFile = filename;
		this.iterations = iterations;
		this.populationSize = populationSize;
		this.numberOfLabels = numOfLabels;
		this.labelGeneralizationRate = labelGeneralizationProbability;
		this.targetLC = problemLC;
	}

	/**
	 * Run the SGmlUCS
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		LCSTrainTemplate myExample = new LCSTrainTemplate(CALLBACK_RATE);
		IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA);

		GenericMultiLabelRepresentation rep = new GenericMultiLabelRepresentation(
				inputFile, PRECISION_BITS, numberOfLabels,
				GenericMultiLabelRepresentation.EXACT_MATCH,
				labelGeneralizationRate, .7);
		rep.setClassificationStrategy(rep.new BestFitnessClassificationStrategy());
		ClassifierTransformBridge.setInstance(rep);

		UCSUpdateAlgorithm updateObj = new UCSUpdateAlgorithm(UCS_ALPHA, UCS_N,
				UCS_ACC0, UCS_LEARNING_RATE, UCS_EXPERIENCE_THRESHOLD, 0.01,
				ga, THETA_GA, 1);
		AbstractUpdateAlgorithmStrategy.currentStrategy = new SequentialMlUpdateAlgorithm(
				updateObj, ga, numberOfLabels);

		ClassifierSet rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new RouletteWheelSelector(
								AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_DELETION,
								true)));

		ArffLoader loader = new ArffLoader();
		loader.loadInstances(inputFile, true);
		final IEvaluator eval = new ExactMatchEvalutor(
				ClassifierTransformBridge.instances, true);
		myExample.registerHook(new FileLogger(inputFile + "_resultSGMlUCS.txt",
				eval));
		myExample.train(iterations, rulePopulation);

		System.out.println("Post process...");
		PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_EXPLOITATION);
		SortPopulationControl sort = new SortPopulationControl(
				AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);
		rulePopulation.print();
		// ClassifierSet.saveClassifierSet(rulePopulation, "set");

		eval.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set (best)");
		ExactMatchEvalutor testEval = new ExactMatchEvalutor(loader.testSet,
				true);
		testEval.evaluateSet(rulePopulation);
		HammingLossEvaluator hamEval = new HammingLossEvaluator(loader.testSet,
				true, numberOfLabels);
		hamEval.evaluateSet(rulePopulation);
		AccuracyEvaluator accEval = new AccuracyEvaluator(loader.testSet, true);
		accEval.evaluateSet(rulePopulation);

		VotingClassificationStrategy str = rep.new VotingClassificationStrategy(
				targetLC);
		rep.setClassificationStrategy(str);
		// TODO: Calibrate on set
		str.proportionalCutCalibration(ClassifierTransformBridge.instances,
				rulePopulation);

		System.out.println("Evaluating on test set (voting)");
		testEval.evaluateSet(rulePopulation);
		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);
	}

}
