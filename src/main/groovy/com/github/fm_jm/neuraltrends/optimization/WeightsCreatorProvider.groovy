package com.github.fm_jm.neuraltrends.optimization

import groovy.transform.TupleConstructor
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Creator
import org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal


@TupleConstructor
class WeightsCreatorProvider {

//
//    int size
//    double[] prototype = null
//    double lowerBound = 0
//    double upperBound = 3
//    double lowerMutator = 0.8
//    double upperMutator = 1.2
//    Random random = new Random()
//
//    static class WeightsCreator implements Creator<DoubleGenotype> {
//        @ThreadLocal
//        int x
//
//        @Override
//        DoubleGenotype create() {
////            DoubleGenotype genotype = new DoubleGenotype(lowerBound, upperBound)
////            if (prototype == null){
////                genotype.init(random, size)
////            } else {
////                genotype.addAll(prototype.collect {
////                    double mutator = lowerMutator + (upperMutator - lowerMutator) * random.nextDouble();
////                    Math.min(upperBound, Math.max(lowerBound, mutator*it))
////                })
////            }
//            println x
//            DoubleGenotype genotype = new DoubleGenotype(-5.12, 5.12)
//            genotype.init(new Random(), 2)
//            genotype
//        }
//    }

}
