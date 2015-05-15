package com.github.fm_jm.neuraltrends.evaluation

import com.gmongo.GMongo
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.MongoURI
import groovy.transform.Canonical

@Canonical
class Results {

    String heuristic
    Map<String, Number> optimizerParams
    int epochs
    int foldNo

    double f
    double time

    static public Results retrieve(String collection, Map key){
        def res = MongoWrapper.retrieve(collection, key)
        if (!res)
            return null
        new Results(key.heuristic, key.params, key.epochs, key.foldNo, res.f, res.time)
    }

    public void store(String collection="results"){
        Map key = [heuristic: this.heuristic, epochs: this.epochs, params: this.optimizerParams, foldNo: this.foldNo]
        Map values = [f: this.f, time: this.time]
        MongoWrapper.store(collection, key, values)
    }

}
