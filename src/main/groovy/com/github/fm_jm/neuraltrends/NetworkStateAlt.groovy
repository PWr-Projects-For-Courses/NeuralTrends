package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

@Canonical
@Slf4j
class NetworkStateAlt {

    int foldNo
    int epochs
    double multiplier
    String heuristicName
    Map heuristicParams

    List<Integer> time
    List<Integer> layerSizes
    double[][] weights

    public void store(String collection=Changable.collectionPrefix+"networks_new"){
        def key = [foldNo: foldNo, multiplier: multiplier, epochs: epochs,
                   heuristicName: heuristicName, heuristicParams: heuristicParams]
        def values = [time: time, layerSizes: layerSizes, weights: weights]
        log.info "Storing $key"
        MongoWrapper.store(collection, key, values)
    }

    public static NetworkStateAlt retrieve(String collection = Changable.collectionPrefix+"networks_new", Map key){
        def res = MongoWrapper.retrieve(collection, key)
        if (!res) {
            log.info "Retrieved $key -> null"
            return null
        }
        log.info "Retrieved $key -> <notNull>"
        return new NetworkStateAlt(key.foldNo, key.epochs, key.multiplier,
                key.heuristicName, key.heuristicParams,
                res.time, res.layerSizes, res.weights.toArray() as double[][]
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
    public static NetworkStateAlt retrieveClosest(String collection = Changable.collectionPrefix+"networks_new", int epochs, int foldNo, double multiplier){
        Map key = [foldNo: foldNo, multiplier: multiplier]
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
        new NetworkStateAlt(foldNo, maxEpochs, multiplier, best.time, best.layerSizes, best.weights)
    }

    public void fillInKey(Map key){
        foldNo = key.foldNo
        epochs = key.epochs
        multiplier = key.multiplier
    }

}
