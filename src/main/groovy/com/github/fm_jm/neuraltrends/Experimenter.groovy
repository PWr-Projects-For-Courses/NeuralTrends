package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet
import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import com.github.fm_jm.neuraltrends.optimization.OptimizerModuleProvider

layerCounts = [3,4,5]
heuristics = ["ea", "pso"]
foldNos = Changable.folds
generations = [0, 100, 200, 500]
populations = [100, 200, 500]
//epochs = [0, 50, 100, 150]
epochs = [0]
crossRates = [0.5, 0.75, 0.9]

creatorParams = [lowerBound: Double.MIN_VALUE, upperBound: Double.MAX_VALUE, lowerMutator: 0.8 as double, upperMutator: 1.2 as double]
l2lambda = 0.1

foldNos.each {int foldNo ->
    layerCounts.each {int layerCount ->
        epochs.each {int epochs ->
            generations.each {int generations ->
                if (epochs || generations)
                    populations.each {int population ->
                        heuristics.each {String heuristic ->
                            if (heuristic == "ea"){
                                crossRates.each {double crossRate ->
                                    //run ea
                                    Map key = getKey(foldNo, layerCount, epochs, generations, population, heuristic, crossRate)
                                    if (!MongoWrapper.exists("results", key)){
//                                    def optimizer = OptimizerModuleProvider.getEA(generations, population, crossRate)
//                                    def trainSet = DataLoader.getDataSet(foldNo, DataSet.Type.TRAIN)
//                                    def testSet = DataLoader.getDataSet(foldNo, DataSet.Type.TEST)
                                        def stacker = new Stacker(layerCount, foldNo, epochs, l2lambda, "ea", [
                                            generations: generations,
                                            population: population,
                                            crossoverRate: crossRate
                                        ], creatorParams)
                                        def res = stacker.evaluate()
                                        res.fillInKey(key)
                                        res.store("results")
                                    }
                                }
                            } else {
                                //run pso
                                Map key = getKey(foldNo, layerCount, epochs, generations, population, heuristic)
                                if (!MongoWrapper.exists("results", key)) {
//                                def optimizer = OptimizerModuleProvider.getPSO(generations, population)
//                                def trainSet = DataLoader.getDataSet(foldNo, DataSet.Type.TRAIN)
//                                def testSet = DataLoader.getDataSet(foldNo, DataSet.Type.TEST)
                                    def stacker = new Stacker(layerCount, foldNo, epochs, l2lambda, "pso", [
                                        generations: generations,
                                        population: population
                                    ], creatorParams)
                                    def res = stacker.evaluate()
                                    res.fillInKey(key)
                                    res.store("results")
                                }
                            }
                        }
                    }
            }
        }
    }
}


Map getKey(int foldNo, int layerCount, int epochs, int generations, int population, String heuristic,
           double crossRate = 0){
    Map heuristicParams = [generations: generations, popSize: population]
    if (crossRate != 0)
        heuristicParams.cross = crossRate
    [foldNo: foldNo, layers: layerCount, epochs: epochs, heuristic: heuristic, params: heuristicParams]
}

MongoWrapper.close()