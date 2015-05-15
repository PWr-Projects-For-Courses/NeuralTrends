package com.github.fm_jm.neuraltrends

import com.gmongo.GMongo
import com.mongodb.DBCollection
import com.mongodb.MongoURI

//def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
//def db = mongo.getDB("neural-trends")
//DBCollection col = db.testCol
//println col.find() as List
//col.insert(a: 1, b:2)
//println col.find() as List
//col.remove([:   ])
//println col.find() as List

class SomeCategory {
    static String foo(String s, int i){
        "$s$i"
    }
}

void x(){
    println "abc".foo(1)
}

use(SomeCategory){
    x()
}