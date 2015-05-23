package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
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
//    void learnWithBackprop(BasicNetwork network, DataSet dataSet, int epochs, double l2Lambda){
    void learnWithBackprop(BasicNetwork network, double[][] inputs, double[][] outputs, int epochs, double l2Lambda) {
//        Placeholder.instance.local.currentDataSet = dataSet
        Placeholder.instance.local.currentInputs = inputs
        Placeholder.instance.local.currentOutputs = outputs
        def backprop = new ResilientPropagation(
            network,
            new BasicMLDataSet(
//                dataSet.inputs as double[][],
//                dataSet.outputs as double[][]
                inputs,
                outputs
            )
        )
        backprop.addStrategy(new L2(l2Lambda))
        epochs.times {
            log.info("Epoch $it/$epochs, error ${backprop.error}")
            backprop.iteration()
        }
        def eval = MeasureCalculator.squaredError(
            outputs as double[][],
            BasicNetworkCategory.activateNoThreshold(
                network,
                inputs as double[][]
            )
        )

        log.info "Eval: ${eval}"
        backprop.finishTraining()
    }

    void runHeuristic(BasicNetwork network, OptimizerModule heuristic){
        Opt4JTask task = new Opt4JTask(false);
        task.init(heuristic, new WeightsModule())
        try {
            task.execute()
            Archive archive = task.getInstance(Archive.class);
            def genotype = archive.max { Individual i -> i.objectives.array()[0]}.genotype as DoubleGenotype
        } finally {
            task.close()
        }
        def currentStartIdx = 0
        use (BasicNetworkCategory){
            ((Placeholder.instance.local.layerSizes as List<Integer>).size()-1).times {int i ->
                int entry = (Placeholder.instance.local.layerSizes as List<Integer>)[i]
                def nextLayerSize = (Placeholder.instance.local.layerSizes as List<Integer>)[i+1]
                int weightsCount = entry*nextLayerSize
                network.setWeightsOverLayer(i, genotype[currentStartIdx..(currentStartIdx+weightsCount-1)] as double[])
                currentStartIdx += weightsCount
            }
        }

//        use (BasicNetworkCategory) {
//            (Placeholder.instance.local.layerSizes as List<Integer>).eachWithIndex { int entry, int i ->
//                network.setWeightsOverLayer(i, genotype[currentStartIdx..(currentStartIdx + entry - 1)] as double[])
//                currentStartIdx += entry
//            }
//        }
    }

    /**
     *
     * @param network
     * @param heuristic
     * @param creatorParams map of bounds and mutators used in creator
     */
    void learnWithHeuristic(BasicNetwork network, double[][] inputs, double[][] outputs,  OptimizerModule heuristic, Map creatorParams){
        def layerSizes = []
        def prototype = []
        Placeholder.instance.local.creator = creatorParams
        Placeholder.instance.local.currentInputs = inputs
        Placeholder.instance.local.currentOutputs = outputs
        use(BasicNetworkCategory){
            (network.layerCount-1).times {
                def layer = network.getWeightsOverLayer(it)
                prototype.addAll(layer)
//                layerSizes << layer.size()
                layerSizes << network.getLayerNeuronCount(it)
            }
            layerSizes << network.getLayerNeuronCount(network.layerCount-1)
            Placeholder.instance.local.layerSizes = layerSizes
        }
        Placeholder.instance.local.creator.size = prototype.size()
        log.info "prototype size: ${Placeholder.instance.local.creator.size}"
        Placeholder.instance.local.prototype = prototype
        runHeuristic(network, heuristic)
        Placeholder.instance.local.prototype = null
    }
}
