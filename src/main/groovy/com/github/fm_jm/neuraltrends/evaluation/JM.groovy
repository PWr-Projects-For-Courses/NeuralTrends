package com.github.fm_jm.neuraltrends.evaluation

//def exp = [1, 0, 1, 0] as int[]
//def act = [1, 1, 0, 0] as int[]

def exp = [[1,0,1,0], [0,1,0,1]] as int[][]
def act= [[0,1,0,1], [0,1,0,1]] as int[][]

FCalculator calc = new FCalculator()
//calc.update(exp, act)
//println calc.FScore
println calc.F(exp, act)