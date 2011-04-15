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
import gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy;
import gr.auth.ee.lcs.data.representations.SingleClassRepresentation;
import gr.auth.ee.lcs.data.updateAlgorithms.SSLCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.ConfusionMatrixEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.ExactMatchSelfEvaluator;
import gr.auth.ee.lcs.evaluators.FileLogger;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.TournamentSelector2;

import java.io.IOException;

/**
 * An SS-LCS implementation.
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class SSLCS {
	/**
	 * The main for running SS-LCS
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final String file = "/home/miltiadis/Desktop/datasets/iris.arff";
		final int iterations = 600;
		final int populationSize = 1000;
		SSLCS sslcs = new SSLCS(file, iterations, populationSize);
		sslcs.run();
	}

	/**
	 * The input file used (.arff).
	 */
	private final String inputFile;

	/**
	 * The number of full iterations to train the SS-LCS.
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
	private final int THETA_GA = 100;

	/**
	 * The frequency at which callbacks will be called for evaluation.
	 */
	private final int CALLBACK_RATE = 50;

	/**
	 * The number of bits to use for representing continuous variables
	 */
	private final int PRECISION_BITS = 7;

	/**
	 * The SSLCS penalty parameter.
	 */
	private final double SSLCS_PENALTY = 10;

	/**
	 * The accuracy threshold parameter.
	 */
	private final double SSLCS_REWARD = 1;

	/**
	 * The SSLCS subsumption experience threshold.
	 */
	private final int SSLCS_EXPERIENCE_THRESHOLD = 10;

	/**
	 * The SSLCS subsumption experience threshold.
	 */
	private final double SSLCS_FITNESS_THRESHOLD = .99;

	/**
	 * The post-process experince threshold used.
	 */
	private final int POSTPROCESS_EXPERIENCE_THRESHOLD = 10;

	/**
	 * Coverage threshold for post processing.
	 */
	private final int POSTPROCESS_COVERAGE_THRESHOLD = 0;

	/**
	 * Post-process threshold for fitness;
	 */
	private final double POSTPROCESS_FITNESS_THRESHOLD = .5;

	/**
	 * The SS-LCS constructor.
	 * 
	 * @param filename
	 *            the filename to open
	 * @param iterations
	 *            the number of iterations to run the training
	 * @param populationSize
	 *            the population size to use
	 */
	public SSLCS(String filename, int iterations, int populationSize) {
		inputFile = filename;
		this.iterations = iterations;
		this.populationSize = populationSize;
	}

	/**
	 * Run the SS-LCS.
	 * 
	 * @throws IOException
	 *             if file not found
	 */
	public void run() throws IOException {
		LCSTrainTemplate myExample = new LCSTrainTemplate(CALLBACK_RATE);
		IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new TournamentSelector2(
						40,
						true,
						UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_EXPLORATION),
				new SinglePointCrossover(), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA);

		SingleClassRepresentation rep = new SingleClassRepresentation(
				inputFile, PRECISION_BITS);
		rep.setClassificationStrategy(rep.new VotingClassificationStrategy());
		ClassifierTransformBridge.setInstance(rep);

		UpdateAlgorithmFactoryAndStrategy.currentStrategy = new SSLCSUpdateAlgorithm(
				SSLCS_REWARD, SSLCS_PENALTY, SSLCS_FITNESS_THRESHOLD,
				SSLCS_EXPERIENCE_THRESHOLD, .01, ga);

		ClassifierSet rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new TournamentSelector2(
								80,
								true,
								UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_DELETION)));

		ArffLoader trainer = new ArffLoader();
		trainer.loadInstances(inputFile, true);
		final IEvaluator eval = new ExactMatchSelfEvaluator(true, true);
		myExample.registerHook(new FileLogger(inputFile + "_result.txt", eval));
		myExample.train(iterations, rulePopulation);

		System.out.println("Post process...");
		PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_EXPLOITATION);
		SortPopulationControl sort = new SortPopulationControl(
				UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);
		rulePopulation.print();
		// ClassifierSet.saveClassifierSet(rulePopulation, "set");

		eval.evaluateSet(rulePopulation);
		ConfusionMatrixEvaluator conf = new ConfusionMatrixEvaluator(
				rep.getLabelNames(), ClassifierTransformBridge.instances);
		conf.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set");
		ExactMatchEvalutor testEval = new ExactMatchEvalutor(trainer.testSet,
				true);
		testEval.evaluateSet(rulePopulation);

	}
}