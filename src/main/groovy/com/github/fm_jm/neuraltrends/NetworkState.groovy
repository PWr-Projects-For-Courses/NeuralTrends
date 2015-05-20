package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

@Canonical
@Slf4j
class NetworkState {

    int foldNo
    int epochs
    int layers
    int layerNo //weights over this layer
    String heuristicName
    Map heuristicParams

    List<Integer> time
    List<Integer> layerSizes
    double[] weights

    public void store(String collection=Changable.collectionPrefix+"networks"){
        def key = [foldNo: foldNo, layers: layers, epochs: epochs, layerNo: layerNo,
                   heuristicName: heuristicName, heuristicParams: heuristicParams]
        def values = [time: time, layerSizes: layerSizes, weights: weights]
        log.info "Storing $key"
        MongoWrapper.store(collection, key, values)
    }

    public static NetworkState retrieve(String collection = Changable.collectionPrefix+"networks", Map key){
        def res = MongoWrapper.retrieve(collection, key)
        if (!res) {
            log.info "Retrieved $key -> null"
            return null
        }
        log.info "Retrieved $key -> <notNull>"
        return new NetworkState(key.foldNo, key.epochs, key.layers, key.layerNo,
            key.heuristicName, key.heuristicParams,
            res.time, res.layerSizes, res.weights.toArray() as double[]
        )
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
    public static NetworkState retrieveClosest(String collection = Changable.collectionPrefix+"networks", int epochs, int foldNo, int layers, int layerNo){
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
