package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.optimization.OptimizerModuleProvider

Stacker stack = new Stacker(
    3,
    1,
    0,
    0.1,
//    "EA",
    "PSO",
    [
        generations: 3,
        population: 5,
//        crossoverRate: 0.8
        perturbation: 0.7
    ],
//    OptimizerModuleProvider.getEA(3, 5, 0.8),
    [:]
)
println stack.evaluate()

//import com.github.fm_jm.neuraltrends.data.DataLoader
//import com.github.fm_jm.neuraltrends.data.DataSet
//import com.gmongo.GMongo
//import com.mongodb.DBCollection
//import com.mongodb.MongoURI
//
////def mongo = new GMongo(new MongoURI("mongodb://student:student@ds045679.mongolab.com:45679/neural-trends"))
////def db = mongo.getDB("neural-trends")
////DBCollection col = db.testCol
////println col.find() as List
////col.insert(a: 1, b:2)
////println col.find() as List
////col.remove([:   ])
////println col.find() as List
//
////class SomeCategory {
////    static String foo(String s, int i){
////        "$s$i"
////    }
////}
////
////void x(){
////    println "abc".foo(1)
////}
////
////use(SomeCategory){
////    x()
////}
//
////Stacker stack = new Stacker(3, DataLoader.getDataSet(1, DataSet.Type.TRAIN), 2, 0.1, null, [:])
////println stack.evaluate(DataLoader.getDataSet(1, DataSet.Type.TEST))
//
//interface AI {
//    def foo()
//}
//
//class A implements AI{
//    final int x = 5
//
//    def foo(){
//        println 1
//    }
//
//    def bar(){
//        println x
//    }
//}
////
////
////AI.metaClass.foo = { println 2 }
//////A.metaClass.x = { return 3 }
////
////new A().foo()
////def a = new A()
////a.@x = 3
////new A().bar()
//
//def unsafe = sun.misc.Unsafe.theUnsafe
//
//def normalize = { int value ->
//    if(value >= 0) return value;
//    return (~0L >>> 32) & value;
//}
//
//def sizeOf = { Object object ->
//    return unsafe.getAddress(
//        normalize(unsafe.getInt(object, 4L)) + 12L);
//}
//
//def toAddress = { Object obj ->
//    Object[] array = [obj].toArray()
//    long baseOffset = unsafe.arrayBaseOffset(Object[].class);
//    return normalize(unsafe.getInt(array, baseOffset));
//}
//
//def newX = 3
//unsafe.copyMemory(newX, 0L, null, toAddress(A.@x), sizeOf(newX))
//
//new A().bar()