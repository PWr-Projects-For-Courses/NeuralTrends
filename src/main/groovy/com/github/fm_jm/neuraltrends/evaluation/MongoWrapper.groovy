package com.github.fm_jm.neuraltrends.evaluation

import com.gmongo.GMongo
import com.mongodb.DBCollection
import com.mongodb.MongoURI


class MongoWrapper {

    static public Map retrieve(String collection, Map key){
        def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        def res = col.findOne(key)
        if (!res)
            return null
        res = res as Map
        def out = [:]
        res.each {k, v ->
            if (!(key.containsKey(k) || k == "_id")){
                out[k] = v
            }
        }
        out
    }

    static public List<Map> retrieveAll(String collection, Map key){
        def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        def res = col.find(key)
        if (!res)
            return null
        res = res as List<Map>
        List<Map> out = res.collect { Map map ->
            def temp = [:]
            map.each { k, v ->
                if (!(key.containsKey(k) || k == "_id")){
                    temp[k] = v
                }
            }
            temp
        }
        out
    }

    static public void store(String collection, Map key, Map values){
        boolean exists = exists(collection, key)
        def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        col.insert(key+values)
        assert !exists, "Object with given key $key already exists"
    }

    static public boolean exists(String collection, Map key){
        def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        def res = col.getCount(key)
        res != 0
    }

}
