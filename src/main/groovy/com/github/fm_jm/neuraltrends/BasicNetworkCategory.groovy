package com.github.fm_jm.neuraltrends

import org.encog.ml.data.basic.BasicMLData
import org.encog.neural.networks.BasicNetwork

import groovy.util.logging.Slf4j

@Slf4j
class BasicNetworkCategory {
    static int[][] activate(BasicNetwork network, int[][] input){
        def out = []
        input.each { x ->
            out << activate(network, x)
        }
        out as int[][]
    }

    static int[] activate(BasicNetwork network, int[] input){
        activate(network, (double[]) input)
    }

    static int[] activate(BasicNetwork network, double[] input){
        threshold(network.compute(new BasicMLData(input)).data)
    }

    static int[][] activate(BasicNetwork network, double[][] input){
        def out = []
        input.each { x ->
            out << activate(network, x)
        }
        out as int[][]
    }

    static double[] activateNoThreshold(BasicNetwork network, double[] input){
        network.compute(new BasicMLData(input)).data
    }

    static double[][] activateNoThreshold(BasicNetwork network, double[][] input){
        def out = []
        input.each { x ->
            out << activateNoThreshold(network, x)
        }
        out as double[][]
    }

    /**
     * IMPORTANT: for this to work it needs to be called right after activating the network
     * @param network Freshly activated network
     * @param layer Layer number
     * @return Activation density of given layer
     */
    static double activationDensity(BasicNetwork network, int layer){
        int activated = 0
        network.getLayerNeuronCount(layer).times {
//            activated += network.getLayerOutput(layer,it) >= 0.5 ? 1 : 0
              activated += network.getLayerOutput(layer, it)
        }
        return activated/network.getLayerNeuronCount(layer)
    }

    static int[] threshold(double[] data){
        (int[]) data.collect { it>=0.5 ? 1 : 0 }.toArray()
    }

    static int[][] threshold(double[][] data){
        def out = []
        data.each {
            out << threshold(it)
        }
        out as int[][]
    }


    /**
     * Assumes non-recursive network with connections up only
     * @param layer number of layer from which connections are outgoing; 0 is input layer
     * @return array of weights, iterating over each upper neuron first, then over lower (ending with bias, if layer is biased)
     */
    static double[] getWeightsOverLayer(BasicNetwork network, int layer){
        int lowerLayerSize = network.getLayerNeuronCount(layer)
        int upperLayerSize = network.getLayerNeuronCount(layer+1)
//        boolean upperIsBiased = network.isLayerBiased(layer+1)
//        double[] out = new double[(lowerLayerSize+(upperIsBiased ? 1 :0))*upperLayerSize]
        double[] out = new double[lowerLayerSize*upperLayerSize]
        int idx = 0
        upperLayerSize.times { int upperIdx ->
            lowerLayerSize.times { int lowerIdx ->
                out[idx++] = network.getWeight(layer, lowerIdx, upperIdx)
            }
//            if (upperIsBiased)
//                out[idx++] = network.structure.layers[upperIdx].biasActivation
        }
        log.trace "get idx $idx, out.length ${out.length} "
        def l = out.length
//        assert idx == l
        log.trace "getWeights $out"
        out
    }

    static void setWeightsOverLayer(BasicNetwork network, int layer, double[] weights){
        log.trace "setWeights $weights"
        int lowerLayerSize = network.getLayerNeuronCount(layer)
        int upperLayerSize = network.getLayerNeuronCount(layer+1)
        def l = weights.length
//        boolean upperIsBiased = network.isLayerBiased(layer+1)
        assert l == lowerLayerSize*upperLayerSize
        int idx = 0
        upperLayerSize.times { int upperIdx ->
            lowerLayerSize.times { int lowerIdx ->
                network.setWeight(layer, lowerIdx, upperIdx, weights[idx++])
            }
//            if (upperIsBiased)
//                network.structure.layers[upperIdx].biasActivation = weights[idx++]
        }
//        assert weights == getWeightsOverLayer(network, layer)
//        log.debug "set idx $idx, out.length ${weights.length}"
//        assert idx == l

    }
}
