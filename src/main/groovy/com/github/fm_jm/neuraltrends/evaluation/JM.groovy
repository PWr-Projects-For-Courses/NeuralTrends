package com.github.fm_jm.neuraltrends.evaluation

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet

//DataSet data = DataLoader.getDataSet("corel5k-5-1tst.dat")
//DataSet data = DataLoader.getDataSet(1, DataSet.Type.TEST)
//println data.inputs.length
//println data.inputs[0].length
//println data.outputs.length
//println data.outputs[0].length

//Results a = new Results("ea", [generations: 20, popSize:40, cross: 0.4 as double], 30, 1, 0.75, 45.5)
//a.store("testCol")

Results b = Results.retrieve("testCol", [heuristic: "ea", epochs: 30, foldNo: 1, params: [generations: 20, popSize:40, cross: 0.4 as double]])
println b

println MongoWrapper.retrieve("testCol", [heuristic: "ea", epochs: 30, foldNo: 1, params: [generations: 20, popSize:40, cross: 0.4 as double]])
