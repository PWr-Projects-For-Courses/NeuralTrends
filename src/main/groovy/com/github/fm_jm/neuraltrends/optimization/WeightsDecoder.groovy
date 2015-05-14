package com.github.fm_jm.neuraltrends.optimization

import org.encog.neural.networks.BasicNetwork
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Decoder


class WeightsDecoder implements Decoder<DoubleGenotype, BasicNetwork> {

    @Override
    BasicNetwork decode(DoubleGenotype genotype) {
        return null
    }
}

