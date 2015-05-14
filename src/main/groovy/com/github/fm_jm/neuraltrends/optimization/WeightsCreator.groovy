package com.github.fm_jm.neuraltrends.optimization

import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Creator


class WeightsCreator implements Creator<DoubleGenotype>{

    @Override
    DoubleGenotype create() {
        int size = Placeholder.instance.local.creator.size
        double[] prototype = Placeholder.instance.local.prototype
        double lowerBound = Placeholder.instance.local.creator.lowerBound ?: 0
        double upperBound = Placeholder.instance.local.creator.upperBound ?: 3
        double lowerMutator = Placeholder.instance.local.creator.lowerMutator ?: 0.8
        double upperMutator = Placeholder.instance.local.creator.upperMutator ?: 1.2
        Random random = new Random()
        DoubleGenotype genotype = new DoubleGenotype(lowerBound, upperBound)
        if (prototype == null){
            genotype.init(random, size)
        } else {
            genotype.addAll(prototype.collect {
                double mutator = lowerMutator + (upperMutator - lowerMutator) * random.nextDouble();
                Math.min(upperBound, Math.max(lowerBound, mutator*it))
            })
        }

        genotype
    }
}
