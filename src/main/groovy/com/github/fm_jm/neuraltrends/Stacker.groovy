package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.FCalculator
import com.github.fm_jm.neuraltrends.evaluation.Results
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.opt4j.core.optimizer.OptimizerModule

import groovy.time.TimeCategory
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

@Canonical
@Slf4j
class Stacker implements Runnable{
    final int layerCount
    final DataSet dataSet
    final int epochs
    final double l2Lambda
    final OptimizerModule heuristic
    final Map creatorParams

    final BasicNetwork resultNetwork = new BasicNetwork()
    final private LayerLearner learner = new LayerLearner()
    final double q = Math.pow(
        dataSet.outputSize() / (1.5 * dataSet.inputSize()),
        1.0/(layerCount-2)
    )
    final layerOutputs = [dataSet.inputs]

    Stacker(int layerCount, DataSet dataSet, int epochs, double l2Lambda, OptimizerModule heuristic, Map creatorParams) {
        this.layerCount = layerCount
        this.dataSet = dataSet
        this.epochs = epochs
        this.l2Lambda = l2Lambda
        this.heuristic = heuristic
        this.creatorParams = creatorParams
    }

    protected int[][] hiddenActivation(double[] weights, int[][] inps, int hiddenIdx){
        BasicNetwork network = new BasicNetwork()
        network.addLayer(new BasicLayer(null, false, inps[0].size()))
        def size = hiddenSize(hiddenIdx)
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, size))
        network.structure.finalizeStructure()
//        int[][] out = new int[inps.length][size]
        network.setWeightsOverLayer(0, weights)
//        inps.eachWithIndex { int[] inp, int i ->
//            out[i] = network.activate(inp)
//        }
//        return out
        network.activate(inps)
    }

    protected double[] learnAutoencoder(int inputSize, int hiddenSize){
        BasicNetwork encoder = new BasicNetwork()
        encoder.addLayer(new BasicLayer(null, false, inputSize))
        encoder.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenSize))
        encoder.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputSize))
        encoder.structure.finalizeStructure()
        learner.learnWithBackprop(encoder, new DataSet(layerOutputs.last(), layerOutputs.last()), epochs, l2Lambda)
        if (heuristic)
            learner.learnWithHeuristic(encoder, heuristic, creatorParams)
        encoder.getWeightsOverLayer(0)
    }

    protected double[] learnLastLayer(){
        BasicNetwork perceptron = new BasicNetwork()
        perceptron.addLayer(new BasicLayer(null, false, hiddenSize(layerCount-2)))
        perceptron.addLayer(new BasicLayer(new ActivationSigmoid(), true, dataSet.outputSize()))
        perceptron.structure.finalizeStructure()
        learner.learnWithBackprop(perceptron, new DataSet(layerOutputs.last(), dataSet.outputs), epochs, l2Lambda)
        if (heuristic)
            learner.learnWithHeuristic(perceptron, heuristic, creatorParams)
        perceptron.getWeightsOverLayer(0)
    }

    /**
     *
     * INPUT -> HIDDEN0 -> HIDDEN1 -> ... -> HIDDEN(layerCount-3) -> OUTPUT
     */
    int hiddenSize(int hiddenIdx){
        Math.ceil(1.5*dataSet.inputSize()*Math.pow(q, hiddenIdx))
    }

    /**
     * Protected methods will be called only from here, and the whole methods is executed with category.
     * IDE will be pissed that some methods are not recognised, but should work anyway.
     */
    @Override
    void run() {
        use (BasicNetworkCategory) {
            assert layerCount > 2
            def lastLayer = new BasicLayer(null, false, dataSet.inputSize())
            resultNetwork.addLayer(lastLayer)
            (layerCount - 2).times {
                log.info "Layer $it"
                int currentSize = hiddenSize(it)
                double[] weights = learnAutoencoder(lastLayer.neuronCount, currentSize)
                layerOutputs << hiddenActivation(weights, layerOutputs.last(), it)
                lastLayer = new BasicLayer(new ActivationSigmoid(), true, currentSize)
                resultNetwork.addLayer(lastLayer)
                resultNetwork.setWeightsOverLayer(it, weights)
            }
            log.info "Last layer"
            double[] weights = learnLastLayer()
            resultNetwork.addLayer(new BasicLayer(new ActivationSigmoid(), true, dataSet.outputSize()))
            resultNetwork.setWeightsOverLayer(layerCount - 2, weights)
        }
    }

    Results evaluate(DataSet testDataSet){
        Date start = new Date()
        run()
        Date stop = new Date()
        Results out = new Results()
        use (TimeCategory){
            out.time = (stop-start).toString()
        }
        out.f = FCalculator.F(testDataSet.outputs, BasicNetworkCategory.activate(testDataSet.inputs))
        out
    }
}
