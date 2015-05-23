package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
import com.github.fm_jm.neuraltrends.optimization.L2
import com.github.fm_jm.neuraltrends.optimization.Placeholder
import com.github.fm_jm.neuraltrends.optimization.WeightsModule
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import org.encog.neural.networks.training.strategy.RegularizationStrategy
import org.opt4j.core.Individual
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.core.start.Opt4JTask

import groovy.transform.Canonical
import groovy.util.logging.Slf4j

@Slf4j
@Canonical
class LayerLearner {
    int batchSize = 50

//    void learnWithBackprop(BasicNetwork network, DataSet dataSet, int epochs, double l2Lambda){
    void learnWithBackprop(BasicNetwork network, double[][] inputs, double[][] outputs, int epochs, double l2Lambda) {
//        Placeholder.instance.local.currentDataSet = dataSet
        Placeholder.instance.local.currentInputs = inputs
        Placeholder.instance.local.currentOutputs = outputs
        def batchesToGo = (Math.ceil(inputs.length/batchSize) as int)
        int epochsToGo = Math.ceil(epochs/batchesToGo) as int
        batchesToGo.times { int batchNo ->
            double[][] batchIn = Arrays.copyOfRange(
                inputs,
                batchNo*batchSize,
                [(batchNo+1)*batchSize, inputs.length].min()
            )
            double[][] batchOut = Arrays.copyOfRange(
                outputs,
                batchNo*batchSize,
                [(batchNo+1)*batchSize, inputs.length].min()
            )
            def backprop = new ResilientPropagation(
                network,
                new BasicMLDataSet(
//                dataSet.inputs as double[][],
//                dataSet.outputs as double[][]
//                    inputs,
//                    outputs
                    batchIn,
                    batchOut
                )
            )
            backprop.addStrategy(new RegularizationStrategy(l2Lambda))
            epochsToGo.times { int epoch ->
                log.info("Batch ${batchNo+1}/$batchesToGo; epoch ${epoch+1}/$epochsToGo, error ${backprop.error}")
                backprop.iteration()
            }
            def out = BasicNetworkCategory.activateNoThreshold(
                network,
                batchIn
            )
            def eval = MeasureCalculator.squaredError(
                batchOut,
                out
            )
            log.info "Batch ${batchNo+1}/$batchesToGo eval: ${eval}"
            backprop.finishTraining()
        }
        def eval = MeasureCalculator.squaredError(outputs, BasicNetworkCategory.activateNoThreshold(network, inputs))
    }

    void runHeuristic(BasicNetwork network, OptimizerModule heuristic){
        Opt4JTask task = new Opt4JTask(false);
        task.init(heuristic, new WeightsModule())
        def genotype
        try {
            task.execute()
            Archive archive = task.getInstance(Archive.class);
            genotype = archive.max { Individual i -> i.objectives.array()[0]}.genotype as DoubleGenotype
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
