package com.github.fm_jm.neuraltrends.evaluation

import com.google.common.primitives.Ints


class FCalculator {

    public int tp, fp, fn

    public FCalculator(){
        tp = 0
        fp = 0
        fn = 0
    }

    /**
     * Calculates FScore using confusion matrix
     * @return FScore
     */
    public double getFScore(){
        2*tp / (2*tp + fp + fn)
    }

    /**
     * Constructs the confusion matrix using experimental bitwise method
     * @param expected Expected output
     * @param actual Actual output
     */
    public void update(int[] expected, int[] actual){
        assert expected.length == actual.length
        expected.length.times {
            if (expected[it] == actual[it])
                tp++
            else if (actual[it]==1)
                fp++
            else
                fn++
        }
    }

    /**
     * Calculates bitwise F Score according to definition, as stated in "An exact Algorithm for F-Measure Maximization"
     * by Dembczynski et al. Works for a single multi-label case or multiple single-label cases
     * @param expected Expected output
     * @param actual Actual output
     * @return Calculated F Score
     */
    static public double F(int[] expected, int[] actual){
        int nominator = 0;
        int denominator = 0;
        expected.length.times {
            nominator += 2*expected[it]*actual[it]
            denominator += expected[it] + actual[it]
        }
        nominator/denominator
    }

    /**
     * Calculates F Score according to definition, as stated in "An exact Algorithm for F-Measure Maximization"
     * by Dembczynski et al. Works for multiple multi-label cases by transforming them into single, longer, multi-label
     * case
     * @param expected Array of expected outputs
     * @param actual Array of actual outputs
     * @return Calculated F Score
     */
    static public double F(int[][] expected, int[][] actual){
        F(Ints.concat(expected),Ints.concat(actual))
    }

}