package com.github.fm_jm.neuraltrends.optimization

import com.github.fm_jm.neuraltrends.BasicNetworkCategory
import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.FCalculator
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
            out.add BasicNetworkCategory.activate(phenotype, inputVector)
            if (layerCount>2)
                sparsity += BasicNetworkCategory.activationDensity(phenotype, 1)
        }
        def expected = layerCount>2 ?
//            dataset.outputs // final layer. supervised perceptron
//            : dataset.inputs
            outputs
            : inputs
        double f = FCalculator.F(expected, out as int[][])
        double objectiveValue = layerCount>2 ? harmonicAvg(f, 1.0 - (sparsity/(inputs.length))) : f
//        double objectiveValue = layerCount>2 ? harmonicAvg(f, 1.0 - (sparsity/dataset.size())) : f
        Objectives obj = new Objectives()
        obj.add(objectiveName, Objective.Sign.MAX, objectiveValue)
        obj
    }
}
