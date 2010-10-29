package gr.auth.ee.lcs.data;

import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.Classifier;

public class SSLCSUpdateAlgorithm extends AbstractSLCSUpdateAlgorithm {

	private double R,p;
	
	/**
	 * Constructor of update algorithm
	 * @param reward the reward a correct classifier will be given on correct classification
	 * @param penaltyPercent the percentage of the reward that the classifier's penalty will be when failing to classify
	 */
	public SSLCSUpdateAlgorithm(double reward, double penaltyPercent){
		R=reward;
		p=penaltyPercent;
	}

	/**
	 * Implementation of abstract method defined in @see gr.auth.ee.lcs.data.AbstractSLCSUpdateAlgorithm
	 */
	public void updateFitness(Classifier aClassifier, int numerosity, ClassifierSet correctSet) {
	  GenericSLCSClassifierData data=((GenericSLCSClassifierData)aClassifier.updateData);
	  if (correctSet.getClassifierNumerosity(aClassifier)>0){ //aClassifier belongs to correctSet
		  data.tp++;
		  data.str+=R/correctSet.getTotalNumerosity();
	  }else{
		  data.fp++;
		  data.str-=p*R/data.ns;
	  }
	  
	  aClassifier.fitness=numerosity*(data.str/data.msa);
	  
  }


}