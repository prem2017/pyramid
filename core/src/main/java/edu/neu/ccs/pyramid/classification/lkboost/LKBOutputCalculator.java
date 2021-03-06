package edu.neu.ccs.pyramid.classification.lkboost;

import edu.neu.ccs.pyramid.regression.regression_tree.LeafOutputCalculator;

import java.util.stream.IntStream;

/**
 * Created by chengli on 10/1/15.
 */
public class LKBOutputCalculator implements LeafOutputCalculator{
    private int numClasses;
    private boolean parallel;

    public LKBOutputCalculator(int numClasses, boolean parallel) {
        this.numClasses = numClasses;
        this.parallel = parallel;
    }

    public LKBOutputCalculator(int numClasses) {
        this.numClasses = numClasses;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    @Override
    public double getLeafOutput(double[] probabilities, double[] labels) {
        double numerator = 0;
        double denominator = 0;

        IntStream intStream = IntStream.range(0,probabilities.length);
        if (parallel){
            intStream = intStream.parallel();
        }
        numerator = intStream.mapToDouble(i->labels[i]*probabilities[i]).sum();


        IntStream intStream2 = IntStream.range(0,probabilities.length);
        if (parallel){
            intStream2 = intStream2.parallel();
        }
        denominator = intStream2.mapToDouble(i->Math.abs(labels[i]) * (1 - Math.abs(labels[i]))*probabilities[i]).sum();


//        for (int i=0;i<probabilities.length;i++) {
//            double label = labels[i];
//            numerator += label*probabilities[i];
//            denominator += Math.abs(label) * (1 - Math.abs(label))*probabilities[i];
//        }
        double out;
        if (denominator == 0) {
            out = 0;
        } else {
            out = ((numClasses - 1) * numerator) / (numClasses * denominator);
        }
        //protection from numerically unstable issue
        //todo does the threshold matter?
        if (out>1){
            out=1;
        }
        if (out<-1){
            out=-1;
        }
        if (Double.isNaN(out)) {
            throw new RuntimeException("leaf value is NaN");
        }
        if (Double.isInfinite(out)){
            throw new RuntimeException("leaf value is Infinite");
        }
        return out;
    }
}
