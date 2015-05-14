package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.optimization.L2
import com.github.fm_jm.neuraltrends.optimization.Placeholder
import com.github.fm_jm.neuraltrends.optimization.WeightsModule
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import org.opt4j.core.Individual
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.core.start.Opt4JTask

import groovy.util.logging.Slf4j

@Slf4j
class LayerLearner {
    void learnWithBackprop(BasicNetwork network, DataSet dataSet, int epochs, double l2Lambda){
        Placeholder.instance.local.currentDataSet = dataSet
        def backprop = new ResilientPropagation(
            network,
            new BasicMLDataSet(
                dataSet.inputs as double[][],
                dataSet.outputs as double[][]
            )
        )
        backprop.addStrategy(new L2(l2Lambda))
        backprop.iteration(epochs)
        backprop.finishTraining()
    }

    void runHeuristic(BasicNetwork network, OptimizerModule heuristic){
        Opt4JTask task = new Opt4JTask(false);
        task.init(heuristic, new WeightsModule())
        task.execute()
        Archive archive = task.getInstance(Archive.class);
        def genotype = archive.max { Individual i -> i.objectives.array()[0]}.genotype as DoubleGenotype
        def currentStartIdx = 0
        use (BasicNetworkCategory) {
            (Placeholder.instance.local.layerSizes as List<Integer>).eachWithIndex { int entry, int i ->
                network.setWeightsOverLayer(i, genotype[currentStartIdx..(currentStartIdx + entry - 1)])
                currentStartIdx += entry
            }
        }
    }

    /**
     *
     * @param network
     * @param heuristic
     * @param creatorParams map of bounds and mutators used in creator
     */
    void learnWithHeuristic(BasicNetwork network, OptimizerModule heuristic, Map creatorParams){
        def layerSizes = []
        def prototype = []
        use(BasicNetworkCategory){
            (network.layerCount-1).times {
                def layer = network.getWeightsOverLayer(it)
                prototype.addAll(layer)
                layerSizes << layer.size()
            }
            Placeholder.instance.local.layerSizes = layerSizes
        }
        runHeuristic(network, heuristic)
    }
}
