/**
 * 
 */
package gr.auth.ee.lcs.evaluators;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.utilities.InstanceToDoubleConverter;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import weka.core.Instances;

/**
 * An evaluator to evaluate on the accuracy of the classifiers.
 * 
 * @author Miltos Allamanis
 * 
 */
public class AccuracyEvaluator implements IEvaluator {

	/**
	 * The set of instances to evaluate on.
	 */
	private final double[][] instances;

	/**
	 * A boolean indicating if the evaluator is going to print the results.
	 */
	private final boolean printResults;

	/**
	 * The LCS instance being used.
	 */
	private final AbstractLearningClassifierSystem myLcs;

	/**
	 * A constructor using only instances.
	 * 
	 * @param instances
	 *            the instance double[][] array
	 * @param print
	 *            true to print results to stdout
	 * @param lcs
	 *            the LCS instance used
	 */
	public AccuracyEvaluator(final double[][] instances, final boolean print,
			final AbstractLearningClassifierSystem lcs) {
		this.printResults = print;
		this.instances = instances;
		myLcs = lcs;
	}

	/**
	 * Constructor for creating evaluator with a Weka instance set.
	 * 
	 * @param instances
	 *            the instances to be used
	 * @param print
	 *            true to print results to stdout
	 * @param lcs
	 *            the LCS instance used
	 */
	public AccuracyEvaluator(final Instances instances, final boolean print,
			final AbstractLearningClassifierSystem lcs) {
		this.instances = InstanceToDoubleConverter.convert(instances);
		printResults = print;
		myLcs = lcs;
	}

	/**
	 * Constructor for creating evaluator with .arff file.
	 * 
	 * @param arffFileName
	 *            the arff file
	 * @param print
	 *            true to print output to stdout
	 * @param lcs
	 *            the LCS instance used
	 * @throws IOException
	 *             when file is not found
	 */
	public AccuracyEvaluator(final String arffFileName, final boolean print,
			final AbstractLearningClassifierSystem lcs) throws IOException {
		printResults = print;
		final FileReader reader = new FileReader(arffFileName);
		this.instances = InstanceToDoubleConverter
				.convert(new Instances(reader));
		myLcs = lcs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.IEvaluator#evaluateSet(gr.auth.ee.lcs.classifiers
	 * .ClassifierSet)
	 */
	@Override
	public final double evaluateSet(final ClassifierSet classifiers) {
		final ClassifierTransformBridge bridge = myLcs
				.getClassifierTransformBridge();

		double sumOfAccuracies = 0;
		double sumOfRecall = 0;
		for (int i = 0; i < instances.length; i++) {
			int unionOfLabels = 0;
			int intersectionOfLabels = 0;

			final int[] classes = bridge.classify(classifiers, instances[i]);
			final int[] classification = bridge
					.getDataInstanceLabels(instances[i]);

			// Find symmetric differences
			Arrays.sort(classes);
			Arrays.sort(classification);
			for (int j = 0; j < classes.length; j++) {
				if (Arrays.binarySearch(classification, classes[j]) < 0) {
					unionOfLabels++;
				} else {
					intersectionOfLabels++;
					unionOfLabels++;
				}
			}
			for (int j = 0; j < classification.length; j++) {
				if (Arrays.binarySearch(classes, classification[j]) < 0)
					unionOfLabels++;
			}
			final double instanceAccuracy = ((double) intersectionOfLabels)
					/ ((double) unionOfLabels);
			sumOfAccuracies += Double.isNaN(instanceAccuracy) ? 0
					: instanceAccuracy;

			final double instanceRecall = ((double) intersectionOfLabels)
					/ ((double) classification.length);
			sumOfRecall += Double.isNaN(instanceRecall) ? 0 : instanceRecall;
		}
		final double accuracy = sumOfAccuracies / ((double) instances.length);
		final double recall = sumOfRecall / ((double) instances.length);

		if (printResults) {
			System.out.println("Accuracy: " + accuracy);
			System.out.println("Recall: " + recall);
		}
		return accuracy;
	}

}
