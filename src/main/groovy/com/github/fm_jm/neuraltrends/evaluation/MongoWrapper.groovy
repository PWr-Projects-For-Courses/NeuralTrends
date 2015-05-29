package com.github.fm_jm.neuraltrends.evaluation

import com.gmongo.GMongo
import com.mongodb.DBCollection
import com.mongodb.MongoURI

import groovy.json.JsonSlurper


class MongoWrapper {

    static def mongo

    static {
        def loaded = new JsonSlurper().parse(MongoWrapper.classLoader.getResource("mongo.json"))
        mongo = new GMongo(new MongoURI("${loaded.protocol}://${loaded.user}:${loaded.pass}@${loaded.addr}"))
    }

    static public Map retrieve(String collection, Map key){
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
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        col.insert(key+values)
        assert !exists, "Object with given key $key already exists"
    }

    static public boolean exists(String collection, Map key){
        def db = mongo.getDB("neural-trends")
        DBCollection col = db.getCollection(collection)
        def res = col.getCount(key)
        res != 0
    }

    static void close(){
        mongo.close()
    }
}
