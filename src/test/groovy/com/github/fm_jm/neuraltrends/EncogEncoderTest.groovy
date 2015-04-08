package com.github.fm_jm.neuraltrends

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
    double[][] data = [
        [1, 1, 1, 0, 0, 0],
        [0, 1, 1, 0, 0, 0],
        [1, 0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0, 0],
        [1, 1, 1, 1, 0, 0],
        [1, 1, 1, 0, 1, 0],
        [1, 1, 1, 0, 0, 1]
    ]

    BasicNetwork getTrainedNetwork(int epochs){
        BasicNetwork network = new BasicNetwork();
        def input = new BasicLayer(null,false,6)
        def hidden = new BasicLayer(new ActivationSigmoid(),true,10)
        def output = new BasicLayer(new ActivationSigmoid(),true,6)
        network.addLayer(input);
        network.addLayer(hidden);
        network.addLayer(output);
        network.getStructure().finalizeStructure();
        network.reset();

        MLDataSet trainingSet = new BasicMLDataSet(data, data);

        ResilientPropagation train = new ResilientPropagation(network, trainingSet);

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
        use(BasicNetworkCategory){
            data.each { double[] pattern ->
                bard.info "Should be: ${pattern.threshold()}, was ${network.activate(pattern)}"
            }
        }
    }
}
