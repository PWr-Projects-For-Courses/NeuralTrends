package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MeasureCalculator
import com.github.fm_jm.neuraltrends.optimization.L2
import org.encog.Encog
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation

import groovy.util.logging.Slf4j

@Slf4j("bard")
class EncogEncoderTest extends GroovyTestCase{
    static int[][] data = [
        [1, 1, 1, 0, 0, 0],
        [0, 1, 1, 0, 0, 0],
        [1, 0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0, 0],
        [1, 1, 1, 1, 0, 0],
        [1, 1, 1, 0, 1, 0],
        [1, 1, 1, 0, 0, 1]
    ]

    static BasicNetwork getTrainedNetwork(int epochs){
        BasicNetwork network = new BasicNetwork();
        def input = new BasicLayer(null,false,6)
        def hidden = new BasicLayer(Changable.activationFunction,true,10)
        def output = new BasicLayer(Changable.activationFunction,true,6)
        network.addLayer(input);
        network.addLayer(hidden);
        network.addLayer(output);
        network.getStructure().finalizeStructure();
        network.reset();

        MLDataSet trainingSet = new BasicMLDataSet(data as double[][], data as double[][]);

        ResilientPropagation train = new ResilientPropagation(network, trainingSet);
//        train.addStrategy(new RegularizationStrategy(0.8))
        train.addStrategy(new L2(0.1))

        epochs.times {
            train.iteration();
            bard.info("Epoch #$it Error: ${train.getError()}");

        }
        train.finishTraining();
        network
    }

    void tearDown(){
        Encog.instance.shutdown()
    }

    void testEncoder(){
        def network = getTrainedNetwork(20)
        def results = []
        use(BasicNetworkCategory){
            data.each { int[] pattern ->
                int[] out = network.activate(pattern)
                results << out
                bard.info "Should be: ${pattern}, was ${out}"
                bard.info "F: ${MeasureCalculator.F(pattern, out)}"
                3.times {
                    bard.info "Layer ${it} activation density is ${network.activationDensity(it)}"
                }
            }
            bard.info "F total: ${MeasureCalculator.F(data, results as int[][])}"
        }
    }
}
