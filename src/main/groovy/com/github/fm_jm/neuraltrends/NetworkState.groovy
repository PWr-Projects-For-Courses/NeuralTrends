package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import groovy.transform.Canonical

@Canonical
class NetworkState {

    int foldNo
    int epochs
    int layers
    int layerNo //weights over this layer

    List<Integer> time
    List<Integer> layerSizes
    List<Double> weights

    public void store(String collection="networks"){
        def key = [foldNo: foldNo, layers: layers, epochs: epochs, layerNo: layerNo]
        def values = [time: time, layerSizes: layerSizes, weights: weights]
        MongoWrapper.store(key+values)
    }

    public static NetworkState retrieve(String collection = "networks", Map key){
        def res = MongoWrapper.retrieve(collection, key)
        if (!res)
            return null
        return new NetworkState(key.foldNo, key.epochs, key.layers, key.layerNo, res.time, res.layerSizes, res.weights)
    }

    /**
     * Finds the NetworkState with epochs as close to the given number, but not exceeding it. If given number
     * of epochs already exists in the database, return it
     * @param collection Collection name, default: networks
     * @param epochs The maximum number of epochs to look for
     * @param foldNo Number of fold
     * @param layers Layers count
     * @param layerNo Number of layer
     * @return NetworkState of closest or exact network or null, if no matching results were found
     */
    public static NetworkState retrieveClosest(String collection = "networks", int epochs, int foldNo, int layers, int layerNo){
        Map key = [foldNo: foldNo, layers: layers, layerNo: layerNo]
        List<Map> possibilities = MongoWrapper.retrieveAll(collection, key)
        Map best = null
        int maxEpochs = 0
        possibilities.each {Map it ->
            if (it.epochs > maxEpochs && it.epochs <= epochs){
                best = it
                maxEpochs = it.epochs
            }
        }
        if (!best)
            return null
        new NetworkState(foldNo, maxEpochs, layers, layerNo, best.time, best.layerSizes, best.weights)
    }

    public void fillInKey(Map key){
        foldNo = key.foldNo
        epochs = key.epochs
        layers = key.layers
        layerNo = key.layerNo
    }

}
