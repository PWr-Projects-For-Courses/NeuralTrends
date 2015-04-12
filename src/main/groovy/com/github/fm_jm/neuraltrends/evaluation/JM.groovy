package com.github.fm_jm.neuraltrends.evaluation

import org.encog.Encog
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.ml.data.MLData
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation

////def exp = [1, 0, 1, 0] as int[]
////def act = [1, 1, 0, 0] as int[]
//
//def exp = [[1,0,1,0], [0,1,0,1]] as int[][]
//def act= [[0,1,0,1], [0,1,0,1]] as int[][]
//
//FCalculator calc = new FCalculator()
////calc.update(exp, act)
////println calc.FScore
//println calc.F(exp, act)

int[][] data = [
        [1, 1, 1, 0, 0, 0],
        [0, 1, 1, 0, 0, 0],
        [1, 0, 1, 0, 0, 0],
        [1, 1, 0, 0, 0, 0],
        [1, 1, 1, 1, 0, 0],
        [1, 1, 1, 0, 1, 0],
        [1, 1, 1, 0, 0, 1]
]

BasicNetwork network = new BasicNetwork();
def input = new BasicLayer(null,false,6)
def hidden = new BasicLayer(new ActivationSigmoid(),true,10)
def output = new BasicLayer(new ActivationSigmoid(),true,6)
network.addLayer(input);
network.addLayer(hidden);
network.addLayer(output);
network.getStructure().finalizeStructure();
network.reset();

MLDataSet trainingSet = new BasicMLDataSet(data as double[][], data as double[][]);

ResilientPropagation train = new ResilientPropagation(network, trainingSet);

20.times {
    train.iteration();
}
train.finishTraining();

println network.compute(new BasicMLData(data[0] as double[])).data
println network.structure.flat.layerOutput
println network.structure.flat.layerOutput.length
println network.flat.layerOutput
println network.flat.layerOutput.length

void printOutputs(BasicNetwork network){
    network.layerCount.times {
        println network.getLayerNeuronCount(it)
        println network.getLayerTotalNeuronCount(it)
        println network.isLayerBiased(it)
        network.getLayerTotalNeuronCount(it).times { int iit ->
            println network.getLayerOutput(it, iit)
        }
    }
}

printOutputs(network)


Encog.instance.shutdown()