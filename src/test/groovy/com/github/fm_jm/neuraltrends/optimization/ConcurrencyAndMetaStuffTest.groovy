package com.github.fm_jm.neuraltrends.optimization

import com.google.inject.Module
import org.opt4j.core.Individual
import org.opt4j.core.Objective
import org.opt4j.core.Objectives
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.problem.Decoder
import org.opt4j.core.problem.Evaluator
import org.opt4j.core.problem.ProblemModule
import org.opt4j.core.start.Opt4JTask
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.optimizers.mopso.MOPSOModule

import groovy.util.logging.Slf4j

import static groovyx.gpars.GParsPool.withPool

@Slf4j("trubadur")
class ConcurrencyAndMetaStuffTest extends GroovyTestCase {


    static class RastriginDecoder implements Decoder<DoubleGenotype, Double> {

        @Override
        Double decode(DoubleGenotype genotype) {
            double x = genotype.get(0)
            double y = genotype.get(1)
            return 20 + x * x - 10 * Math.cos(2 * Math.PI * x) + y * y - 10 * Math.cos(2 * Math.PI * y)
        }
    }

    static class RastriginEvaluator implements Evaluator<Double> {

        @Override
        Objectives evaluate(Double phenotype) {
            Objectives objectives = new Objectives()
            objectives.add('objective', Objective.Sign.MIN, phenotype.doubleValue())
            return objectives
        }
    }

    static class RastriginModule extends ProblemModule {

        @Override
        protected void config() {
            bindProblem(WeightsCreator, RastriginDecoder, RastriginEvaluator)
        }
    }

    void testEa(){
        EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule()
        ea.generations = 100
        ea.alpha = 50
        doTest(ea)
    }

    void testPso(){
        MOPSOModule pso = new MOPSOModule()
        pso.iterations = 100
        pso.particles = 50
        doTest(pso)
    }



    void doTest(Module heuristic){
        trubadur.info("Testing heuristic $heuristic")
        RastriginModule rastr = new RastriginModule()


        withPool(2) {
            [[1.0, 1.0], [-1.0, -1.0]].eachParallel {
                Placeholder.instance.local.prototype = it
                Placeholder.instance.local.creator.size = 2
                Opt4JTask task = new Opt4JTask(false);

                task.init(heuristic, rastr)
                try {
                    task.execute();  // <- whole optimization happens here
                    Archive archive = task.getInstance(Archive.class);    // in archive we've got the best specimen
                    for (Individual individual : archive) {
                        trubadur.info "${Thread.currentThread().name}:: val: ${individual.objectives.values}"
                        trubadur.info "${Thread.currentThread().name}:: gen: ${individual.genotype}"
                        trubadur.info "${Thread.currentThread().name}:: fen: ${individual.phenotype}"
                    }
                } finally {
                    task.close();
                }
            }
        }
    }
}