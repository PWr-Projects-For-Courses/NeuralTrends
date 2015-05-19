package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
import com.github.fm_jm.neuraltrends.evaluation.Results
import com.github.fm_jm.neuraltrends.optimization.Placeholder
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.opt4j.core.optimizer.OptimizerModule

import groovy.time.TimeCategory
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

import java.sql.Time

@Canonical
@Slf4j
class Stacker implements Runnable{
    final int layerCount
    final int foldNo
    final int epochs
    final double l2Lambda
    final OptimizerModule heuristic
    final Map creatorParams

    final DataSet dataSet = DataLoader.getDataSet(foldNo, DataSet.Type.TRAIN)
    final BasicNetwork resultNetwork = new BasicNetwork()
    final private LayerLearner learner = new LayerLearner()
    final double q = Math.pow(
        dataSet.outputSize() / (1.5 * dataSet.inputSize()),
        1.0/(layerCount-2)
    )
    final layerOutputs = [dataSet.inputs]

    Stacker(int layerCount, int foldNo, int epochs, double l2Lambda, OptimizerModule heuristic, Map creatorParams) {
        this.layerCount = layerCount
        this.foldNo = foldNo
        this.epochs = epochs
        this.l2Lambda = l2Lambda
        this.heuristic = heuristic
        this.creatorParams = creatorParams
    }

    protected int[][] hiddenActivation(double[] weights, double[][] inps, int hiddenIdx){
        BasicNetwork network = new BasicNetwork()
        network.addLayer(new BasicLayer(null, false, inps[0].size()))
        def size = hiddenSize(hiddenIdx)
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, size))
        network.structure.finalizeStructure()
        use(BasicNetworkCategory) {             // how the hell did it work without this? dit it work at all?
            network.setWeightsOverLayer(0, weights)
            network.activateNoThreshold(inps)
        }
    }

    protected double[] learnAutoencoder(int inputSize, int hiddenSize){
        BasicNetwork encoder = new BasicNetwork()
        encoder.addLayer(new BasicLayer(null, false, inputSize))
        encoder.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenSize))
        encoder.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputSize))
        encoder.structure.finalizeStructure()
        learner.learnWithBackprop(encoder, layerOutputs.last() as double[][], layerOutputs.last() as double[][], epochs, l2Lambda)
//        learner.learnWithBackprop(encoder, new DataSet(layerOutputs.last(), layerOutputs.last()), epochs, l2Lambda)
        if (heuristic)
            learner.learnWithHeuristic(encoder, heuristic, creatorParams)
        encoder.getWeightsOverLayer(0)
    }

    protected double[] learnLastLayer(){
        BasicNetwork perceptron = new BasicNetwork()
        perceptron.addLayer(new BasicLayer(null, false, hiddenSize(layerCount-3)))
        perceptron.addLayer(new BasicLayer(new ActivationSigmoid(), true, dataSet.outputSize()))
        perceptron.structure.finalizeStructure()
//        log.info layerOutputs.toString()
//        log.info layerOutputs.last().toString()
        learner.learnWithBackprop(perceptron, layerOutputs.last() as double[][], dataSet.outputs as double[][], epochs, l2Lambda)
//        learner.learnWithBackprop(perceptron, new DataSet(layerOutputs.last(), dataSet.outputs), epochs, l2Lambda)
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

    double[] get(){
        NetworkState state = NetworkState.retrieve(Placeholder.instance.local.state)
        if (state == null){
            state = new NetworkState(Placeholder.instance.local.state)
            Date start = new Date()
            state.weights = state.layerNo == layerCount-2 ?
                learnLastLayer() :
                learnAutoencoder(state.layerNo ? hiddenSize(state.layerNo-1) : dataSet.inputSize(), hiddenSize(state.layerNo))
            Date stop = new Date()
            def duration = TimeCategory.minus(stop, start)
            state.time = [duration.days, duration.hours, duration.minutes, duration.seconds]
            state.layerSizes = Placeholder.instance.local.layerSizes
        }
        state.weights
    }

    /**
     * Protected methods will be called only from here, and the whole methods is executed with category.
     * IDE will be pissed that some methods are not recognised, but should work anyway.
     */
    @Override
    void run() {
        use (BasicNetworkCategory) {
            assert layerCount > 2
            buildNetwork()
            log.info "Teaching"
            (layerCount - 2).times {
                log.info "Teaching layer $it"
                Placeholder.instance.local.state.layerNo = it
                double[] weights = get()
                layerOutputs << hiddenActivation(weights, layerOutputs.last(), it)
                resultNetwork.setWeightsOverLayer(it, weights)
            }
            Placeholder.instance.local.state.layerNo = layerCount-1
            log.info "Teaching output layer"
            resultNetwork.setWeightsOverLayer(layerCount - 2, get())
        }
    }

    protected void buildNetwork(){
        Placeholder.instance.local.state = [ foldNo: foldNo, epochs:epochs, layers: layerCount ]
        log.info "Building network"
        log.info "Adding input layer"
        resultNetwork.addLayer(new BasicLayer(null, false, dataSet.inputSize()))
        (layerCount - 2).times {
            log.info "Adding hidden layer #$it"
            resultNetwork.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenSize(it)))
        }
        log.info "Adding output layer"
        resultNetwork.addLayer(new BasicLayer(new ActivationSigmoid(), true, dataSet.outputSize()))
        log.info "Finalizing"
        resultNetwork.structure.finalizeStructure()
        resultNetwork.layerCount.times {
            log.info "layer $it, neuron count: ${resultNetwork.getLayerNeuronCount(it)}"
        }
    }

    List<Integer> getTotalDuration(){
        use(TimeCategory, DurationsHelper){
            def sum = 0.seconds
            layerCount.times {
                Placeholder.instance.local.state.layerCount = it
                def state = NetworkState.retrieve(Placeholder.instance.local.state)
                sum += state.time.toDuration()
            }
            sum.toList()
        }
    }

    Results evaluate(){
        DataSet testDataSet = DataLoader.getDataSet(foldNo, DataSet.Type.TEST)
        log.info("Evaluating")
        run()
        Results out = new Results()
        out.time = getTotalDuration()
        out.f = MeasureCalculator.F(testDataSet.outputs, BasicNetworkCategory.activate(resultNetwork, testDataSet.inputs))
        log.info("F: ${out.f}, duration: ${DurationsHelper.toDuration(out.time)}")
        out
    }
}
