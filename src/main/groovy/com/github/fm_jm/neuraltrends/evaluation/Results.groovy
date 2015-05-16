package com.github.fm_jm.neuraltrends.evaluation

import groovy.transform.Canonical

@Canonical
class Results {

    double f
    List<Integer> time

    String heuristic
    Map<String, Number> optimizerParams
    int epochs
    int foldNo
    int layers

    static public Results retrieve(String collection="results", Map key){
        def res = MongoWrapper.retrieve(collection, key)
        if (!res)
            return null
        new Results(key.heuristic, key.params, key.epochs, key.foldNo, key.layers, res.f, res.time)
    }

    public void store(String collection="results"){
        Map key = [heuristic: this.heuristic, epochs: this.epochs, params: this.optimizerParams, foldNo: this.foldNo, layers: this.layers]
        Map values = [f: this.f, time: this.time]
        MongoWrapper.store(collection, key, values)
    }

    public void fillInKey(Map key){
        heuristic = key.heuristic
        epochs = key.epochs
        foldNo = key.foldNo
        layers = key.layers
        optimizerParams = key.params
    }

}
