package com.github.fm_jm.neuraltrends.evaluation

import com.github.fm_jm.neuraltrends.data.DataLoader
import com.github.fm_jm.neuraltrends.data.DataSet

//DataSet data = DataLoader.getDataSet("corel5k-5-1tst.dat")
DataSet data = DataLoader.getDataSet(1, DataSet.Type.TEST)
println data.inputs.length
println data.inputs[0].length
println data.outputs.length
println data.outputs[0].length