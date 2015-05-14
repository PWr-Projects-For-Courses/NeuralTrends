package com.github.fm_jm.neuraltrends.optimization

import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.optimizers.mopso.MOPSOModule


class OptimizerModuleProvider {

    static OptimizerModule getEA(int generations, int population, double crossoverRate){
        OptimizerModule ea = new EvolutionaryAlgorithmModule()
        ea.alpha = population
        ea.crossoverRate = crossoverRate
        ea.generations = generations
        ea
    }

    static OptimizerModule getPSO(int generations, int population, double perturbation){
        OptimizerModule pso = new MOPSOModule()
        pso.iterations = generations
        pso.particles = population
        pso.perturbation = perturbation
        pso
    }

}
