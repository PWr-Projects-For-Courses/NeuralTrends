package com.github.fm_jm.neuraltrends

StackerAlt stack = new StackerAlt(
        1,
        1000,
        0.1,
        "ea",
//    "pso",
        [
                generations: 2,
                population: 20,
                crossoverRate: 0.8 as double
//        perturbation: 0.7
        ],
        [:],
        0.8
)
println stack.evaluate()