/**
 * 
 */
package gr.auth.ee.lcs.impementations;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.FoldEvaluator;
import gr.auth.ee.lcs.calibration.InternalValidation;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.representations.complex.GenericMultiLabelRepresentation;
import gr.auth.ee.lcs.data.representations.complex.GenericMultiLabelRepresentation.VotingClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.SequentialMlUpdateAlgorithm;
import gr.auth.ee.lcs.data.updateAlgorithms.UCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyRecallEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.io.IOException;
import java.util.Arrays;

import weka.core.Instances;

/**
 * An implementation of a multi-label UCS, using the generic Multi-label
 * representation and a per-label (sequential) update UCS.
 * 
 * @author Miltos Allamanis
 * 
 */
public class SequentialGUCS extends AbstractLearningClassifierSystem {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SettingsLoader.loadSettings();

		final String file = SettingsLoader.getStringSetting("filename", "");

		final SequentialGUCS sgucs = new SequentialGUCS();
		FoldEvaluator loader = new FoldEvaluator(10, sgucs, file);
		loader.evaluate();

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
	private final float CROSSOVER_RATE = (float) SettingsLoader
			.getNumericSetting("crossoverRate", .8);

	/**
	 * The GA mutation rate.
	 */
	private final double MUTATION_RATE = (float) SettingsLoader
			.getNumericSetting("mutationRate", .04);

	/**
	 * The GA activation rate.
	 */
	private final int THETA_GA = (int) SettingsLoader.getNumericSetting(
			"thetaGA", 100);

	/**
	 * The number of bits to use for representing continuous variables.
	 */
	private final int PRECISION_BITS = (int) SettingsLoader.getNumericSetting(
			"precisionBits", 5);

	/**
	 * The UCS alpha parameter.
	 */
	private final double UCS_ALPHA = SettingsLoader.getNumericSetting(
			"UCS_Alpha", .1);

	/**
	 * The UCS n power parameter.
	 */
	private final int UCS_N = (int) SettingsLoader.getNumericSetting("UCS_N",
			10);

	/**
	 * The accuracy threshold parameter.
	 */
	private final double UCS_ACC0 = SettingsLoader.getNumericSetting(
			"UCS_Acc0", .99);
	/**
	 * The learning rate (beta) parameter.
	 */
	private final double UCS_LEARNING_RATE = SettingsLoader.getNumericSetting(
			"UCS_beta", .1);

	/**
	 * The UCS experience threshold.
	 */
	private final int UCS_EXPERIENCE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("UCS_Experience_Theshold", 10);

	/**
	 * The generalization rate used for labels.
	 */
	private final double labelGeneralizationRate;

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	/**
	 * The problem representation.
	 */
	private final GenericMultiLabelRepresentation rep;

	/**
	 * Percentage of only updates (and no exploration).
	 */
	private final double UPDATE_ONLY_ITERATION_PERCENTAGE = SettingsLoader
			.getNumericSetting("UpdateOnlyPercentage", .1);

	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 */
	public SequentialGUCS() throws IOException {
		inputFile = SettingsLoader.getStringSetting("filename", "");
		numberOfLabels = (int) SettingsLoader.getNumericSetting(
				"numberOfLabels", 1);
		iterations = (int) SettingsLoader.getNumericSetting("trainIterations",
				1000);
		populationSize = (int) SettingsLoader.getNumericSetting(
				"populationSize", 1500);

		labelGeneralizationRate = SettingsLoader.getNumericSetting(
				"LabelGeneralizationRate", 0.33);

		final IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		rep = new GenericMultiLabelRepresentation(inputFile, PRECISION_BITS,
				numberOfLabels, GenericMultiLabelRepresentation.EXACT_MATCH,
				labelGeneralizationRate, SettingsLoader.getNumericSetting(
						"AttributeGeneralizationRate", 0.33), this);

		/*
		 * str = rep.new VotingClassificationStrategy( targetLC);
		 */
		rep.setClassificationStrategy(rep.new BestFitnessClassificationStrategy());

		final UCSUpdateAlgorithm updateObj = new UCSUpdateAlgorithm(UCS_ALPHA,
				UCS_N, UCS_ACC0, UCS_LEARNING_RATE, UCS_EXPERIENCE_THRESHOLD,
				SettingsLoader.getNumericSetting("GAMatchSetRunProbability",
						0.01), ga, THETA_GA, 1, this);
		final SequentialMlUpdateAlgorithm strategy = new SequentialMlUpdateAlgorithm(
				updateObj, ga, numberOfLabels);

		this.setElements(rep, strategy);

		rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new RouletteWheelSelector(
								AbstractUpdateStrategy.COMPARISON_MODE_DELETION,
								true)));

	}

	@Override
	public AbstractLearningClassifierSystem createNew() {
		try {
			return new SequentialGUCS();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String[] getEvaluationNames() {
		String[] names = { "Accuracy(pcut)", "Recall(pcut)",
				"HammingLoss(pcut)", "ExactMatch(pcut)", "Accuracy(ival)",
				"Recall(ival)", "HammingLoss(ival)", "ExactMatch(ival)", "Accuracy(ival)",
				"Recall(best)", "HammingLoss(best)", "ExactMatch(best)" };
		return names;
	}

	@Override
	public double[] getEvaluations(Instances testSet) {

		double[] results = new double[12];
		Arrays.fill(results, 0);

		VotingClassificationStrategy str = rep.new VotingClassificationStrategy(
				(float) SettingsLoader.getNumericSetting(
						"datasetLabelCardinality", 1));
		rep.setClassificationStrategy(str);

		str.proportionalCutCalibration(this.instances, rulePopulation);

		final AccuracyRecallEvaluator accEval = new AccuracyRecallEvaluator(
				testSet, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
		results[0] = accEval.evaluateSet(rulePopulation);

		final AccuracyRecallEvaluator recEval = new AccuracyRecallEvaluator(
				testSet, false, this, AccuracyRecallEvaluator.TYPE_RECALL);
		results[1] = recEval.evaluateSet(rulePopulation);

		final HammingLossEvaluator hamEval = new HammingLossEvaluator(testSet,
				false, numberOfLabels, this);
		results[2] = hamEval.evaluateSet(rulePopulation);

		final ExactMatchEvalutor testEval = new ExactMatchEvalutor(testSet,
				false, this);
		results[3] = testEval.evaluateSet(rulePopulation);

		final AccuracyRecallEvaluator selfAcc = new AccuracyRecallEvaluator(
				instances, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
		final InternalValidation ival = new InternalValidation(rulePopulation,
				str, selfAcc);
		ival.calibrate(15);

		results[4] = accEval.evaluateSet(rulePopulation);
		results[5] = recEval.evaluateSet(rulePopulation);
		results[6] = hamEval.evaluateSet(rulePopulation);
		results[7] = testEval.evaluateSet(rulePopulation);

		rep.setClassificationStrategy(rep.new BestFitnessClassificationStrategy());

		results[8] = accEval.evaluateSet(rulePopulation);
		results[9] = recEval.evaluateSet(rulePopulation);
		results[10] = hamEval.evaluateSet(rulePopulation);
		results[11] = testEval.evaluateSet(rulePopulation);

		return results;
	}

	/**
	 * Run the SGmlUCS.
	 * 
	 */
	@Override
	public void train() {

		trainSet(iterations, rulePopulation);
		updatePopulation((int) (iterations * UPDATE_ONLY_ITERATION_PERCENTAGE),
				rulePopulation);
	}

}
