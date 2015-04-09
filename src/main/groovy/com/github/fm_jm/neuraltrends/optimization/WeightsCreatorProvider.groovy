package com.github.fm_jm.neuraltrends.optimization

import groovy.transform.TupleConstructor
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Creator

@TupleConstructor
class WeightsCreatorProvider {


    int size
    double[] prototype = null
    double lowerBound = 0
    double upperBound = 3
    double lowerMutator = 0.8
    double upperMutator = 1.2
    Random random = new Random()

    class WeightsCreator implements Creator<DoubleGenotype> {

        @Override
        DoubleGenotype create() {
            DoubleGenotype genotype = new DoubleGenotype(lowerBound, upperBound)
            if (prototype == null){
                genotype.init(random, size)
            } else {
                genotype.addAll(prototype.collect {
                    double mutator = lowerMutator + (upperMutator - lowerMutator) * random.nextDouble();
                    Math.min(upperBound, Math.max(lowerBound, mutator*it))
                })
            }

            return genotype
        }
    }

}
