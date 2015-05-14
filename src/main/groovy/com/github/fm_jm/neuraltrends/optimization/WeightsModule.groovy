package com.github.fm_jm.neuraltrends.optimization

import org.opt4j.core.problem.ProblemModule


class WeightsModule extends ProblemModule {


    @Override
    protected void config() {
        bindProblem(WeightsCreator, WeightsDecoder, WeightsEvaluator)
    }
}
