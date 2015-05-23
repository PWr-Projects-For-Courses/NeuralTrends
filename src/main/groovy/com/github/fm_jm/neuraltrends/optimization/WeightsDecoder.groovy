package com.github.fm_jm.neuraltrends.optimization

import com.github.fm_jm.neuraltrends.BasicNetworkCategory
import com.github.fm_jm.neuraltrends.Changable
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Decoder


class WeightsDecoder implements Decoder<DoubleGenotype, BasicNetwork> {

    @Override
    BasicNetwork decode(DoubleGenotype genotype) {
        BasicNetwork out = new BasicNetwork()
        (Placeholder.instance.local.layerSizes as List<Integer>).eachWithIndex { int entry, int i ->
            out.addLayer(new BasicLayer(i>0 ? Changable.activationFunction : null, i>0, entry))
        }
        out.getStructure().finalizeStructure();
        def currentStartIdx = 0
        use (BasicNetworkCategory){
            ((Placeholder.instance.local.layerSizes as List<Integer>).size()-1).times {int i ->
                int entry = (Placeholder.instance.local.layerSizes as List<Integer>)[i]
                def nextLayerSize = (Placeholder.instance.local.layerSizes as List<Integer>)[i+1]
                int weightsCount = entry*nextLayerSize
                out.setWeightsOverLayer(i, genotype[currentStartIdx..(currentStartIdx+weightsCount-1)] as double[])
                currentStartIdx += weightsCount
            }
        }
        out.reset()
        out
    }
}

