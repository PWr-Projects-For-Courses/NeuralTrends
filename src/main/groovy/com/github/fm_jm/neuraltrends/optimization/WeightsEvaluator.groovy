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
        DataSet dataset = Placeholder.instance.local.currentDataSet
        def out = []
        double sparisty = 0.0
        int layerCount = Placeholder.instance.local.layerSizes.size()
        String objectiveName = layerCount > 2 ? "harmonicOfFAndSparsity" : "F"
        dataset.inputs.each { int[] inputVector ->
            out.add BasicNetworkCategory.activate(phenotype, inputVector)
            if (layerCount>2)
                sparisty += BasicNetworkCategory.activationDensity(phenotype, 1)
        }
        out = out.toArray()
        def expected = layerCount>2 ?
            dataset.outputs // final layer. supervised perceptron
            : dataset.inputs
        double f = FCalculator.F(expected, out)
        Objectives obj = new Objectives()
        obj.add(objectiveName, Objective.Sign.MAX, harmonicAvg(f, 1.0-sparisty))
    }
}
