package com.github.fm_jm.neuraltrends.optimization

import org.encog.neural.networks.BasicNetwork
import org.opt4j.core.Objectives
import org.opt4j.core.problem.Evaluator


class WeightsEvaluatorProvider {


    class WeightsEvaluator implements Evaluator<BasicNetwork>{

        @Override
        Objectives evaluate(BasicNetwork phenotype) {
            return null
        }
    }
}
