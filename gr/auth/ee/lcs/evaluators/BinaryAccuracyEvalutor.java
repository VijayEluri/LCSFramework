/**
 * 
 */
package gr.auth.ee.lcs.evaluators;

import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.IEvaluator;
import weka.core.Instances;

/**
 * An evaluator using an Weka Instance
 * 
 * @author Miltos Allamanis
 * 
 */
public class BinaryAccuracyEvalutor implements IEvaluator {

	/**
	 * The set of instances to evaluate on.
	 */
	final Instances instanceSet;

	/**
	 * A boolean indicating if the evaluator is going to print the results.
	 */
	private final boolean printResults;

	public BinaryAccuracyEvalutor(Instances instances, boolean print) {
		instanceSet = instances;
		printResults = print;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.IEvaluator#evaluateSet(gr.auth.ee.lcs.classifiers
	 * .ClassifierSet)
	 */
	@Override
	public double evaluateSet(ClassifierSet classifiers) {
		ClassifierTransformBridge bridge = ClassifierTransformBridge
				.getInstance();

		int tp = 0, fp = 0;
		for (int i = 0; i < instanceSet.numInstances(); i++) {
			final double[] instance = new double[instanceSet.numAttributes() - 1];
			for (int j = 0; j < instanceSet.numAttributes(); j++) {
				instance[j] = instanceSet.instance(i).value(j);
			}
			final int[] classes = bridge.classify(classifiers, instance);
			if (classes == null)
				continue;
			if (classes[0] == bridge
					.getDataInstanceLabels(ClassifierTransformBridge.instances[i])[0])
				tp++;
			else
				fp++;

		}

		final double errorRate = ((double) fp) / ((double) (fp + tp));

		if (printResults) {
			System.out.println("tp:" + tp + " fp:" + fp + " errorRate:"
					+ errorRate + " total instances:"
					+ ClassifierTransformBridge.instances.length);
		}
		return errorRate;
	}

}
