package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
import com.github.fm_jm.neuraltrends.evaluation.Results
import com.github.fm_jm.neuraltrends.optimization.OptimizerModuleProvider
import com.github.fm_jm.neuraltrends.optimization.Placeholder
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.opt4j.core.optimizer.OptimizerModule

import groovy.time.TimeCategory
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

@Canonical
@Slf4j
class StackerAlt implements Runnable{
    final int foldNo
    final int epochs
    final double l2Lambda
    final String heuristicName
    final Map heuristicParams
    final Map creatorParams

    int batchSize = 50

    double multiplier

    final OptimizerModule heuristic
    final DataSet dataSet = DataLoader.getDataSet(foldNo, DataSet.Type.TRAIN)
    final BasicNetwork resultNetwork = new BasicNetwork()
    @Lazy LayerLearner learner = new LayerLearner(batchSize)


    StackerAlt(int foldNo, int epochs, double l2Lambda, String heuristicName, Map heuristicParams, Map creatorParams, double multiplier, int batchSize=50) {
        this.foldNo = foldNo
        this.epochs = epochs
        this.l2Lambda = l2Lambda
        this.heuristicName = heuristicName
        this.heuristicParams = heuristicParams
        this.creatorParams = creatorParams
        this.batchSize = batchSize
        this.multiplier = multiplier

        if (heuristicName && heuristicParams?.generations)
            switch (heuristicName) {
                case "ea": this.heuristic = OptimizerModuleProvider.getEA(
                        heuristicParams.generations,
                        heuristicParams.population,
                        heuristicParams.crossoverRate
                ); break;
                case "pso": this.heuristic = OptimizerModuleProvider.getPSO(
                        heuristicParams.generations,
                        heuristicParams.population,
                        heuristicParams.perturbation ?: 0.5
                ); break;
                default: assert "There are only 'ea' and 'pso' heuristics!" && false
            }
    }


    protected double[][] learnAutoencoder(int inputSize, int hiddenSize){
        BasicNetwork encoder = new BasicNetwork()
        encoder.addLayer(new BasicLayer(null, false, inputSize))
        encoder.addLayer(new BasicLayer(Changable.activationFunction, true, hiddenSize))
        encoder.addLayer(new BasicLayer(Changable.activationFunction, true, inputSize))
        encoder.structure.finalizeStructure()
        if (epochs)
            learner.learnWithBackprop(encoder, dataSet.inputs, dataSet.inputs, epochs, l2Lambda)
        if (heuristic)
            learner.learnWithHeuristic(encoder, dataSet.inputs, dataSet.inputs, heuristic, creatorParams)
        use(BasicNetworkCategory){
            [encoder.getWeightsOverLayer(0), encoder.getWeightsOverLayer(1)] as double[][]
        }
    }


    double[][] get(){
//        NetworkState state = NetworkState.retrieve(Placeholder.instance.local.state)
        NetworkStateAlt state = NetworkStateAlt.retrieve(Placeholder.instance.local.state)
        if (state == null){
            state = new NetworkStateAlt(Placeholder.instance.local.state)
            Date start = new Date()
//            log.info "layerNo = ${state.layerNo}, count = ${layerCount}"
//            state.weights = state.layerNo == layerCount-2 ?
//                    learnLastLayer() as double[] :
//                    learnAutoencoder(
//                            state.layerNo ?
//                                    hiddenSize(state.layerNo-1):
//                                    dataSet.inputSize(),
//                            hiddenSize(state.layerNo)
//                    ) as double[]
            state.weights = learnAutoencoder(dataSet.inputSize(), Math.ceil(dataSet.inputSize()*multiplier) as int)
            log.debug "${state.weights}"
            Date stop = new Date()
            def duration = TimeCategory.minus(stop, start)
            state.time = [duration.days, duration.hours, duration.minutes, duration.seconds]
            state.layerSizes = Placeholder.instance.local.layerSizes
            state.store()
        }
        state.weights
    }

    /**
     * Protected methods will be called only from here, and the whole methods is executed with category.
     * IDE will be pissed that some methods are not recognised, but should work anyway.
     */
    @Override
    void run() {
        Placeholder.instance.local.layerSizes = []
        use (BasicNetworkCategory) {
            buildNetwork()
            log.info "Teaching"
//            (layerCount - 2).times {
//                log.info "Teaching layer $it"
//                Placeholder.instance.local.state.layerNo = it
//                double[] weights = get()
//                layerOutputs << hiddenActivation(weights, layerOutputs.last(), it)
//                resultNetwork.setWeightsOverLayer(it, weights)
//            }
//            double[] weights = get()
//            resultNetwork.setWeightsOverLayer(it, weights)

            double[][] weights = get()
            resultNetwork.setWeightsOverLayer(0, weights[0])
            resultNetwork.setWeightsOverLayer(1, weights[1])


//            Placeholder.instance.local.state.layerNo = layerCount-2
//            log.info "Teaching output layer"
//            resultNetwork.setWeightsOverLayer(layerCount - 2, get())
        }
    }

    protected void buildNetwork(){
        Placeholder.instance.local.state = [
                foldNo: foldNo,
                epochs:epochs,
                multiplier: multiplier,
                heuristicName: heuristicName,
                heuristicParams: heuristicParams
        ]
        List<Integer> layerSizes = []
        log.info "Building network"
        log.info "Adding input layer"
        layerSizes << dataSet.inputSize()
        resultNetwork.addLayer(new BasicLayer(null, false, layerSizes[-1]))
        log.info "Adding hidden layer"
        layerSizes << (Math.ceil(multiplier*layerSizes[-1]) as int)
        resultNetwork.addLayer(new BasicLayer(Changable.activationFunction, false, layerSizes[-1]))
        log.info "Adding output layer"
        layerSizes << dataSet.inputSize()
        resultNetwork.addLayer(new BasicLayer(Changable.activationFunction, false, layerSizes[-1]))
        log.info "Finalizing"
        resultNetwork.structure.finalizeStructure()
        resultNetwork.layerCount.times {
            log.info "layer $it, neuron count: ${resultNetwork.getLayerNeuronCount(it)}"
        }
        Placeholder.instance.local.layerSizes = layerSizes
    }

    List<Integer> getTotalDuration(){
        use(TimeCategory, DurationsHelper){
            def state = NetworkStateAlt.retrieve(Placeholder.instance.local.state)
            state.time.toDuration().toList()
        }
    }

    Results evaluate(){
        DataSet testDataSet = DataLoader.getDataSet(foldNo, DataSet.Type.TEST)
        log.info("Evaluating")
        run()
        Results out = new Results()
        out.epochs = epochs
        out.foldNo = foldNo
        out.heuristic = heuristicName
        out.optimizerParams = heuristicParams
        out.time = getTotalDuration()
//        out.f = MeasureCalculator.F(testDataSet.outputs, BasicNetworkCategory.activate(resultNetwork, testDataSet.inputs))
        out.f = MeasureCalculator.squaredError(testDataSet.inputs, BasicNetworkCategory.activateNoThreshold(resultNetwork, testDataSet.inputs))
        log.info "Thresholded ${BasicNetworkCategory.activate(resultNetwork, testDataSet.inputs)}"
        log.info "NotThresholded ${BasicNetworkCategory.activateNoThreshold(resultNetwork, testDataSet.inputs)}"
        log.info("F: ${out.f}, duration: ${DurationsHelper.toDuration(out.time)}")
        out
    }
}
