/**
 * 
 */
package gr.auth.ee.lcs.tests;

import java.io.IOException;

import gr.auth.ee.lcs.ArffTrainer;
import gr.auth.ee.lcs.LCSTrainTemplate;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.ComplexRepresentation;

import gr.auth.ee.lcs.data.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.data.GenericSLCSClassifierData;
import gr.auth.ee.lcs.data.SSLCSUpdateAlgorithm;
import gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy;
import gr.auth.ee.lcs.data.XCSClassifierData;
import gr.auth.ee.lcs.data.XCSUpdateAlgorithm;
import gr.auth.ee.lcs.data.ComplexRepresentation.Attribute;
import gr.auth.ee.lcs.data.ComplexRepresentation.IntervalAttribute;
import gr.auth.ee.lcs.data.ComplexRepresentation.NominalAttribute;
import gr.auth.ee.lcs.data.XSLCSUpdateAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.TournamentSelector;
import gr.auth.ee.lcs.geneticalgorithm.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.UniformBitMutation;

/**
 * @author Miltos Allamanis
 *
 */
public class ComplexRepresentationLCSTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LCSTrainTemplate myExample=new LCSTrainTemplate(.7);
		myExample.ga=new SteadyStateGeneticAlgorithm(
				new TournamentSelector(10,true,UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_EXPLORATION),
				new SinglePointCrossover(),
				new UniformBitMutation(.04),15);
		
		String filename="/home/miltiadis/Desktop/position3.arff";
		ComplexRepresentation rep=new ComplexRepresentation(filename,10);
		ClassifierTransformBridge.setInstance(rep);
		
		
		//UpdateAlgorithmFactoryAndStrategy.currentStrategy=new ASLCSUpdateAlgorithm(5);
		UpdateAlgorithmFactoryAndStrategy.currentStrategy=new XCSUpdateAlgorithm(.2,10,.01,.1,3);
		
		ClassifierSet rulePopulation=new ClassifierSet(new FixedSizeSetWorstFitnessDeletion(
				800,new TournamentSelector(50,false,UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_DELETION)));
		
		ArffTrainer trainer=new ArffTrainer();
		trainer.loadInstances(filename);
		trainer.train(myExample, 800000, rulePopulation);
		
		
		for (int i=0;i<rulePopulation.getNumberOfMacroclassifiers();i++){
			System.out.println(rulePopulation.getClassifier(i).toString()+" fit:"+rulePopulation.getClassifier(i).fitness+" exp:"+rulePopulation.getClassifier(i).experience+" num:"+rulePopulation.getClassifierNumerosity(i));
			//System.out.println("Predicted Payoff: "+((XCSClassifierData)(rulePopulation.getClassifier(i).updateData)).predictedPayOff);
		}
		System.out.println("Post process...");
		//rulePopulation.postProcessThreshold(10, (float)0);
		for (int i=0;i<rulePopulation.getNumberOfMacroclassifiers();i++){
			System.out.println(rulePopulation.getClassifier(i).toString()+" fit:"+rulePopulation.getClassifier(i).fitness+" exp:"+rulePopulation.getClassifier(i).experience+" num:"+rulePopulation.getClassifierNumerosity(i));
			System.out.println("Predicted Payoff: "+((XCSClassifierData)(rulePopulation.getClassifier(i).updateData)).predictedPayOff);
			//System.out.println("tp: "+((GenericSLCSClassifierData)(rulePopulation.getClassifier(i).updateData)).tp);
		}
		//ClassifierSet.saveClassifierSet(rulePopulation, "set");
		
		trainer.selfEvaluate(rulePopulation);
		
		
		
	}
	

	

}
