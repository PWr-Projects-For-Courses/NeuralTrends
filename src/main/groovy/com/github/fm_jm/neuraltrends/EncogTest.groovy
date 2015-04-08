package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.autoencoders.ThresholdCategory
import org.encog.Encog
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.ml.data.MLData
import org.encog.ml.data.MLDataPair
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation


BasicNetwork network = new BasicNetwork();
network.addLayer(new BasicLayer(null,false,6));
network.addLayer(new BasicLayer(new ActivationSigmoid(),true,10));
network.addLayer(new BasicLayer(new ActivationSigmoid(),true,6));
network.getStructure().finalizeStructure();
network.reset();

double[][] data = [
    [1, 1, 1, 0, 0, 0],
    [0, 1, 1, 0, 0, 0],
    [1, 0, 1, 0, 0, 0],
    [1, 1, 0, 0, 0, 0],
    [1, 1, 1, 1, 0, 0],
    [1, 1, 1, 0, 1, 0],
    [1, 1, 1, 0, 0, 1]
]

// create training data
MLDataSet trainingSet = new BasicMLDataSet(data, data);

// train the neural network
final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

20.times {
    train.iteration();
    println("Epoch #" + it+ " Error:" + train.getError());

}
train.finishTraining();

// test the neural network
System.out.println("Neural Network Results:");
for(MLDataPair pair: trainingSet ) {
    final MLData output = network.compute(pair.getInput());
    println "in: ${ThresholdCategory.threshold(pair.inputArray)}, expected: ${ThresholdCategory.threshold(pair.idealArray)}, is: ${ThresholdCategory.threshold(output.data)}"
}

println network.flat.getWeights()
def newWeights = network.flat.getWeights().toList()
newWeights[0] *= 3
network.flat.setWeights((double[])newWeights.toArray())
println network.flat.getWeights()

System.out.println("Neural Network Results after weight modification:");
for(MLDataPair pair: trainingSet ) {
    final MLData output = network.compute(pair.getInput());
    println "in: ${ThresholdCategory.threshold(pair.inputArray)}, expected: ${ThresholdCategory.threshold(pair.idealArray)}, is: ${ThresholdCategory.threshold(output.data)}"
}

Encog.getInstance().shutdown();

/*
Example stdout:
Epoch #0 Error:0.29669100636918466
Epoch #1 Error:0.19937211268703814
Epoch #2 Error:0.1407809832951596
Epoch #3 Error:0.12246331136820392
Epoch #4 Error:0.11584251207826754
Epoch #5 Error:0.10977298811168865
Epoch #6 Error:0.09969293098728459
Epoch #7 Error:0.08808156297625602
Epoch #8 Error:0.07292068411176691
Epoch #9 Error:0.05619406527323175
Epoch #10 Error:0.0400381321954049
Epoch #11 Error:0.029334582198109752
Epoch #12 Error:0.02302376633069374
Epoch #13 Error:0.01690074003423304
Epoch #14 Error:0.013090344684646073
Epoch #15 Error:0.009846095543795202
Epoch #16 Error:0.00602709886213326
Epoch #17 Error:0.003190379282371324
Epoch #18 Error:0.0013786865264423776
Epoch #19 Error:4.8637195460371485E-4
Neural Network Results:
in: [1, 1, 1, 0, 0, 0], expected: [1, 1, 1, 0, 0, 0], is: [1, 1, 1, 0, 0, 0]
in: [0, 1, 1, 0, 0, 0], expected: [0, 1, 1, 0, 0, 0], is: [0, 1, 1, 0, 0, 0]
in: [1, 0, 1, 0, 0, 0], expected: [1, 0, 1, 0, 0, 0], is: [1, 0, 1, 0, 0, 0]
in: [1, 1, 0, 0, 0, 0], expected: [1, 1, 0, 0, 0, 0], is: [1, 1, 0, 0, 0, 0]
in: [1, 1, 1, 1, 0, 0], expected: [1, 1, 1, 1, 0, 0], is: [1, 1, 1, 1, 0, 0]
in: [1, 1, 1, 0, 1, 0], expected: [1, 1, 1, 0, 1, 0], is: [1, 1, 1, 0, 1, 0]
in: [1, 1, 1, 0, 0, 1], expected: [1, 1, 1, 0, 0, 1], is: [1, 1, 1, 0, 0, 1]
* */