package com.github.fm_jm.neuraltrends.optimization

import com.github.fm_jm.neuraltrends.BasicNetworkCategory
import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
import org.encog.neural.networks.BasicNetwork
import org.opt4j.core.Objective
import org.opt4j.core.Objectives
import org.opt4j.core.problem.Evaluator


class WeightsEvaluator implements Evaluator<BasicNetwork> {

    static double harmonicAvg(double... x){
        x.size() / x.collect {1/it}.sum()
    }

    @Override
    Objectives evaluate(BasicNetwork phenotype) {
//        DataSet dataset = Placeholder.instance.local.currentDataSet
        double[][] inputs = Placeholder.instance.local.currentInputs
        double[][] outputs = Placeholder.instance.local.currentOutputs
        def out = []
        double sparsity = 0.0
        int layerCount = Placeholder.instance.local.layerSizes.size()
        String objectiveName = layerCount > 2 ? "harmonicOfFAndSparsity" : "F"
        inputs.each { double[] inputVector ->
//        dataset.inputs.each { int[] inputVector ->
            out.add BasicNetworkCategory.activateNoThreshold(phenotype, inputVector)
            if (layerCount>2)
                sparsity += BasicNetworkCategory.activationDensity(phenotype, 1)
        }
        def expected = layerCount>2 ?
            inputs
            : outputs
//        double f = MeasureCalculator.F(expected, out as double[][])
        double primaryMeasure = layerCount>2 ?
                MeasureCalculator.squaredError(expected, out as double[][])
                : MeasureCalculator.F(expected as int[][], BasicNetworkCategory.threshold(out as double[][]))
        double objectiveValue = layerCount>2 ? harmonicAvg(primaryMeasure, 1.0 - (sparsity/(inputs.length))) : primaryMeasure
//        double objectiveValue = layerCount>2 ? harmonicAvg(f, 1.0 - (sparsity/dataset.size())) : f
        Objectives obj = new Objectives()
        obj.add(objectiveName, Objective.Sign.MAX, objectiveValue)
        obj
    }
}
